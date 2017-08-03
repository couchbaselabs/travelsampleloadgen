package travelsampleloadgen.loadgenerator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import com.couchbase.client.java.error.DocumentDoesNotExistException;

import travelsampleloadgen.service.AirlineDocument;
import travelsampleloadgen.service.AirportDocument;
import travelsampleloadgen.service.DocumentTemplate;
import travelsampleloadgen.service.RouteDocument;
import travelsampleloadgen.util.Constants;
import travelsampleloadgen.util.Utils;

public abstract class LoadGeneratorWorkers implements Runnable {

	private long creates;
	private long updates;
	private long deletes;
	private long threadSeed;
	private SharedWorkerData sharedWorkerData;
	private Utils util = new Utils();

	public LoadGeneratorWorkers() {

	}

	public LoadGeneratorWorkers(long threadSeed, SharedWorkerData sharedWorkerData)
			throws FileNotFoundException, IOException, ParseException {
		Constants.getInstance().initializeLoadgenConstants();
		String propertiesFilePath = Constants.getInstance().getLoadgenPropertiesFile();
		this.creates = Constants.creates;
		this.updates = Constants.updates;
		this.deletes = Constants.deletes;
		this.threadSeed = threadSeed;
		this.sharedWorkerData = sharedWorkerData;
		this.util.setSeed(this.threadSeed);
		if (this.creates + this.updates + this.deletes != 100) {
			this.updates += 100 - (this.creates + this.updates + this.deletes);
		}
	}

	public void run() {
		int randomInt = this.util.getRandomInt(0, 100);
		try {
			if (randomInt < this.creates || this.sharedWorkerData.onlyCreates()) {

				this.create();

			} else if (randomInt < this.creates + this.updates) {
				this.update();
			} else {
				this.delete();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	public void create() throws FileNotFoundException, IOException, ParseException {
		ArrayList<String> types = new ArrayList<String>(
				Arrays.asList(this.sharedWorkerData.documentTypes.keySet().toArray(new String[0])));
		// Increase the probability of creating routes, since the travel sample is
		// mainly for routes.
		types.add("route");
		types.add("route");
		// Remove route from creating the document if not enough airline and airports
		// are created yet.
		if (this.sharedWorkerData.documentTypes
				.get("airline").lastDocument < this.sharedWorkerData.documentTypes.get("airline").firstDocument + 2
				|| this.sharedWorkerData.documentTypes
						.get("airport").lastDocument < this.sharedWorkerData.documentTypes.get("airport").firstDocument
								+ 4) {
			types.removeIf(new Predicate<String>() {
				public boolean test(String p) {
					return p.equals("route");
				}
			});
			if (this.sharedWorkerData.documentTypes.get(
					"airline").lastDocument < this.sharedWorkerData.documentTypes.get("airline").firstDocument + 2) {
				types.add("airline");
			}
			if (this.sharedWorkerData.documentTypes.get(
					"airport").lastDocument < this.sharedWorkerData.documentTypes.get("airport").firstDocument + 4) {
				types.add("airport");
			}
		}
		String randType = (String) this.util.getRandomArrayItem(types);
		DocumentType documentType = this.sharedWorkerData.documentTypes.get(randType);
		this.createDocument(documentType);
		this.sharedWorkerData.incrementNumberOfCreates();
	}

	private void createDocument(DocumentType document) throws FileNotFoundException, IOException, ParseException {
		String type = document.type;
		long lastIteration = document.lastIteration;
		long numCreated = document.numCreated;
		DocumentTemplate template = null;
		long seed = numCreated + lastIteration + 1;
		while (!this.sharedWorkerData.insertToCurrentCreatedIds(seed)) {
			seed++;
		}
		if (type.equals("airline")) {
			template = new AirlineDocument(seed, 0, this.sharedWorkerData.inputData);
		} else if (type.equals("airport")) {
			template = new AirportDocument(seed, 0, this.sharedWorkerData.inputData);
		} else if (type.equals("route")) {
			template = new RouteDocument(seed, 0, this.sharedWorkerData.inputData);
		}
		JSONObject jsonDocument = template.getJsonObject();
		String documentId = type + "_" + jsonDocument.get("id").toString();
		boolean success = this.createDocumentToServer(jsonDocument, documentId);
		if (success) {
			document.setLastDocument(seed);
			document.increaseNumCreated();
			this.sharedWorkerData.setRouteMaxIds();
			// System.out.println("Successfully inserted " + documentId);
		}
	}

	public void update() throws FileNotFoundException, ParseException, IOException {
		ArrayList<String> types = new ArrayList<String>(
				Arrays.asList(this.sharedWorkerData.documentTypes.keySet().toArray(new String[0])));
		for (DocumentType document : this.sharedWorkerData.documentTypes.values()) {
			if (document.lastDocument == 0) {
				types.remove(document.type);
			}
		}
		String randType = (String) this.util.getRandomArrayItem(types);
		DocumentType documentType = this.sharedWorkerData.documentTypes.get(randType);
		this.updateDocument(documentType);
		this.sharedWorkerData.incrementNumberOfUpdates();;

	}
	
	private void updateDocument(DocumentType document) throws ParseException, FileNotFoundException, IOException {
		boolean updated = false;
		long firstDocument = document.firstDocument;
		long lastDocument = document.lastDocument;
		String type = document.type;
		DocumentTemplate template = null;
		int tries = 0;
		while (!updated && tries < 11) {
			long randomId = 0;
			if (tries == 10){
				randomId = this.util.getRandomDocumentId(type);
			}else {
				randomId = this.util.getRandomLong(firstDocument, lastDocument);
			}
			if (type.equals("airline")) {
				template = new AirlineDocument(randomId, this.sharedWorkerData.numberOfUpdates + 1, this.sharedWorkerData.inputData);
			} else if (type.equals("airport")) {
				template = new AirportDocument(randomId, this.sharedWorkerData.numberOfUpdates + 1, this.sharedWorkerData.inputData);
			} else if (type.equals("route")) {
				template = new RouteDocument(randomId, this.sharedWorkerData.numberOfUpdates + 1, this.sharedWorkerData.inputData);
			}
			String document_name = type + "_" + randomId;
			try {
				updated = this.updateDocumentToServer(template.getJsonObject(), document_name);
				if (!updated) {
					updated = false;
					this.sharedWorkerData.getDocumentMinId();
					this.sharedWorkerData.getDocumentsMaxId();
					firstDocument = document.firstDocument;
					lastDocument = document.lastDocument;
					tries++;
				}
			} catch (DocumentDoesNotExistException e) {
				updated = false;
				this.sharedWorkerData.getDocumentMinId();
				this.sharedWorkerData.getDocumentsMaxId();
				firstDocument = document.firstDocument;
				lastDocument = document.lastDocument;
				tries++;
			}
		}
	}

	public void delete() throws FileNotFoundException, ParseException, IOException {
		ArrayList<String> types = new ArrayList<String>(
				Arrays.asList(this.sharedWorkerData.documentTypes.keySet().toArray(new String[0])));
		for (DocumentType document : this.sharedWorkerData.documentTypes.values()) {
			if (document.lastDocument == 0) {
				types.remove(document.type);
			}
		}
		String randType = (String) this.util.getRandomArrayItem(types);
		DocumentType documentType = this.sharedWorkerData.documentTypes.get(randType);
		this.deleteDocument(documentType);
		this.sharedWorkerData.incrementNumberOfCreates();
	}
	
	private void deleteDocument(DocumentType document) throws ParseException, FileNotFoundException, IOException {
		boolean deleted = false;
		long firstDocument = document.firstDocument;
		long lastDocument = document.lastDocument;
		String type = document.type;
		int tries = 0;
		while (!deleted && tries < 11) {
			long randomId = 0;
			if (tries == 10){
				randomId = this.util.getRandomDocumentId(type);
			}else {
				randomId = this.util.getRandomLong(firstDocument, lastDocument);
			}
			String document_name = type + "_" + randomId;
			try {
				deleted = this.deleteDocumentFromServer(document_name);
				if (deleted) {
					//System.out.println("Successfully deleted " + document_name);
				} else  {
					deleted = false;
					this.sharedWorkerData.getDocumentMinId();
					this.sharedWorkerData.getDocumentsMaxId();
					firstDocument = document.firstDocument;
					lastDocument = document.lastDocument;
					tries++;
				}
			} catch (DocumentDoesNotExistException e) {
				deleted = false;
				this.sharedWorkerData.getDocumentMinId();
				this.sharedWorkerData.getDocumentsMaxId();
				firstDocument = document.firstDocument;
				lastDocument = document.lastDocument;
				tries++;
			}
		}
	}

	public abstract boolean createDocumentToServer(JSONObject document, String documentId);

	public abstract boolean updateDocumentToServer(JSONObject document, String documentId);

	public abstract boolean deleteDocumentFromServer(String documentId);
}
