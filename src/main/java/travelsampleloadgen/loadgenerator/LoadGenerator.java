package travelsampleloadgen.loadgenerator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.couchbase.client.java.error.DocumentDoesNotExistException;

import travelsampleloadgen.service.AirlineDocument;
import travelsampleloadgen.service.AirportDocument;
import travelsampleloadgen.service.CouchbaseQueryService;
import travelsampleloadgen.service.DocumentTemplate;
import travelsampleloadgen.service.RouteDocument;
import travelsampleloadgen.util.Constants;
import travelsampleloadgen.util.Utils;

public abstract class LoadGenerator {
	long numberOfOps;
	private long counter;
	private long creates;
	private long updates;
	private long deletes;
	private JSONParser parser;
	private JSONObject inputData;
	private CouchbaseQueryService queryHelper;
	private long numberOfCreates;
	private long numberOfUpdates;
	private long numberOfDeletes;
	private long createsSeed;
	private long updatesSeed;
	private long deletesSeed;
	private long masterSeed;
	private boolean onlyCreates = true;
	private Map<String, DocumentType> documentTypes;
	private Utils util = new Utils();

	public LoadGenerator() throws FileNotFoundException, IOException, ParseException {
		this(Constants.getInstance().getLoadgenPropertiesFile(),
				Constants.getInstance().getTravelSampleDataFilePath());
	}
	
	public LoadGenerator(String propertiesFile, String inputDataFile)
			throws FileNotFoundException, IOException, ParseException {
		Constants.getInstance().initializeLoadgenConstants();
		this.numberOfOps = Constants.numberOfOps;
		this.creates = Constants.creates;
		this.updates = Constants.updates;
		this.deletes = Constants.deletes;
		this.numberOfCreates = 0;
		this.numberOfDeletes = 0;
		this.numberOfUpdates = 0;
		this.masterSeed = System.currentTimeMillis();
		this.setSeeds();
		parser = new JSONParser();
		this.inputData = (JSONObject) parser.parse(new FileReader(inputDataFile));
		this.queryHelper = new CouchbaseQueryService();
		this.documentTypes = initiateDocumentTypes();
		this.getDocumentMinId();
		this.getInitialDocumentsMaxId();
		if (this.creates + this.updates + this.deletes != 100) {
			this.updates += 100 - (this.creates + this.updates + this.deletes);
		}
	}

	public void generate() throws ParseException, FileNotFoundException, IOException {
		this.counter = 0;
		while (this.counter < this.numberOfOps) {
			this.next();
			this.counter++;
		}
		this.storeLoadgenStats();
	}

	public void next() throws ParseException, FileNotFoundException, IOException {
		this.util.setSeed(this.masterSeed + this.counter);
		int randomInt = this.util.getRandomInt(0, 100);
		if (randomInt < this.creates || this.onlyCreates()) {
			this.create();
		} else if (randomInt < this.creates + this.updates) {
			this.update();
		} else {
			this.delete();
		}
	}

	public void create() throws ParseException, FileNotFoundException, IOException {
		ArrayList<String> types = new ArrayList<String>(
				Arrays.asList(this.documentTypes.keySet().toArray(new String[0])));
		// Increase the probability of creating routes, since the travel sample is mainly for routes.
		types.add("route");
		types.add("route");
		// Remove route from creating the document if not enough airline and airports are created yet.
		if (this.documentTypes.get("airline").lastDocument < this.documentTypes.get("airline").firstDocument + 2
				|| this.documentTypes.get("airport").lastDocument < this.documentTypes.get("airport").firstDocument
						+ 4) {
			types.removeIf(new Predicate<String>() {
				public boolean test(String p) {
					return p.equals("route");
				}
			});
			if(this.documentTypes.get("airline").lastDocument < this.documentTypes.get("airline").firstDocument + 2) {
				types.add("airline");
			}
			if (this.documentTypes.get("airport").lastDocument < this.documentTypes.get("airport").firstDocument + 4) {
				types.add("airport");
			}
		}
		this.util.setSeed(this.createsSeed + this.numberOfCreates);
		String randType = (String) this.util.getRandomArrayItem(types);
		DocumentType documentType = this.documentTypes.get(randType);
		this.createDocument(documentType);
		this.numberOfCreates++;
	}

	public void createDocument(DocumentType document) throws ParseException, FileNotFoundException, IOException {
		String type = document.type;
		long lastIteration = document.lastIteration;
		long numCreated = document.numCreated;
		DocumentTemplate template = null;
		long seed = numCreated + lastIteration + 1;
		if (type.equals("airline")) {
			template = new AirlineDocument(seed, 0, this.inputData);
		} else if (type.equals("airport")) {
			template = new AirportDocument(seed, 0, this.inputData);
		} else if (type.equals("route")) {
			template = new RouteDocument(seed, 0, this.inputData);
		}
		JSONObject jsonDocument = template.getJsonObject();
		String documentId = type + "_" + jsonDocument.get("id").toString();
		boolean success = this.createDocumentToServer(jsonDocument, documentId);
		if (success) {
			document.lastDocument = seed;
			document.numCreated++;
			this.setRouteMaxIds();
			//System.out.println("Successfully inserted " + documentId);
		}

	}

	public void delete() throws ParseException, FileNotFoundException, IOException {
		ArrayList<String> types = new ArrayList<String>(
				Arrays.asList(this.documentTypes.keySet().toArray(new String[0])));
		for (DocumentType document : this.documentTypes.values()) {
			if (document.lastDocument == 0) {
				types.remove(document.type);
			}
		}
		this.util.setSeed(this.deletesSeed + this.numberOfDeletes);
		String randType = (String) this.util.getRandomArrayItem(types);
		DocumentType documentType = this.documentTypes.get(randType);
		this.deleteDocument(documentType);
		this.numberOfDeletes++;
	}

	public void deleteDocument(DocumentType document) throws ParseException, FileNotFoundException, IOException {
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
					this.getDocumentMinId();
					this.getDocumentsMaxId();
					firstDocument = document.firstDocument;
					lastDocument = document.lastDocument;
					tries++;
				}
			} catch (DocumentDoesNotExistException e) {
				deleted = false;
				this.getDocumentMinId();
				this.getDocumentsMaxId();
				firstDocument = document.firstDocument;
				lastDocument = document.lastDocument;
				tries++;
			}
		}
	}

	public void update() throws FileNotFoundException, IOException, ParseException {
		ArrayList<String> types = new ArrayList<String>(
				Arrays.asList(this.documentTypes.keySet().toArray(new String[0])));
		for (DocumentType document : this.documentTypes.values()) {
			if (document.lastDocument == 0) {
				types.remove(document.type);
			}
		}
		this.util.setSeed(this.updatesSeed + this.numberOfUpdates);
		String randType = (String) this.util.getRandomArrayItem(types);
		DocumentType documentType = this.documentTypes.get(randType);
		this.updateDocument(documentType);
		this.numberOfUpdates++;
	}

	public void updateDocument(DocumentType document) throws FileNotFoundException, IOException, ParseException {
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
				template = new AirlineDocument(randomId, this.numberOfUpdates + 1, this.inputData);
			} else if (type.equals("airport")) {
				template = new AirportDocument(randomId, this.numberOfUpdates + 1, this.inputData);
			} else if (type.equals("route")) {
				template = new RouteDocument(randomId, this.numberOfUpdates + 1, this.inputData);
			}
			String document_name = type + "_" + randomId;
			try {
				updated = this.updateDocumentToServer(template.getJsonObject(), document_name);
				if (updated) {
					//System.out.println("Successfully updated " + document_name);
				}
				else {
					updated = false;
					this.getDocumentMinId();
					this.getDocumentsMaxId();
					firstDocument = document.firstDocument;
					lastDocument = document.lastDocument;
					tries++;
				}
			} catch (DocumentDoesNotExistException e) {
				updated = false;
				this.getDocumentMinId();
				this.getDocumentsMaxId();
				firstDocument = document.firstDocument;
				lastDocument = document.lastDocument;
				tries++;
			}
		}
	}
	
	public abstract boolean createDocumentToServer(JSONObject document, String documentId);
	
	public abstract boolean updateDocumentToServer(JSONObject document, String documentId);
	
	public abstract boolean deleteDocumentFromServer(String documentId);

	private Map<String, DocumentType> initiateDocumentTypes() {
		Map<String, DocumentType> documentTypes = new HashMap<String, DocumentType>();
		DocumentType airline = new DocumentType("airline", 0);
		DocumentType airport = new DocumentType("airport", 1);
		DocumentType route = new DocumentType("route", 2);
		documentTypes.put("airline", airline);
		documentTypes.put("airport", airport);
		documentTypes.put("route", route);
		return documentTypes;
	}

	private void getDocumentsMaxId() throws ParseException {
		for (DocumentType document : this.documentTypes.values()) {
			JSONObject queryResult = (JSONObject) this.queryHelper.getMaxId(document.type).get(0);
			Object obj = queryResult.get("id");
			document.lastDocument = obj == null ? document.lastDocument : (Long) obj;
		}
		this.setRouteMaxIds();
	}
	
	private void getInitialDocumentsMaxId()throws ParseException {
		for (DocumentType document : this.documentTypes.values()) {
			JSONObject queryResult = (JSONObject) this.queryHelper.getMaxId(document.type).get(0);
			Object obj = queryResult.get("id");
			document.lastDocument = obj == null ? document.lastDocument : (Long) obj;
			document.lastIteration = obj == null ? document.lastIteration : (Long) obj;
		}
		this.setRouteMaxIds();
	}

	private void getDocumentMinId() throws ParseException {
		for (DocumentType document : this.documentTypes.values()) {
			JSONObject queryResult = (JSONObject) this.queryHelper.getMinId(document.type).get(0);
			Object obj = queryResult.get("id");
			document.firstDocument = obj == null ? document.firstDocument : (Long) obj;
		}
		this.setRouteMaxIds();
	}
	
	private void setRouteMaxIds() {
		DocumentType document = this.documentTypes.get("airline");
		RouteDocument.minAirlineId = document.firstDocument;
		RouteDocument.maxAirlineId = document.lastDocument;
		document = this.documentTypes.get("airport");
		RouteDocument.minAirportId = document.firstDocument;
		RouteDocument.maxAirportId = document.lastDocument;
	}
	
	private void setSeeds() {
		this.util.setSeed(this.masterSeed);
		this.createsSeed = this.util.getRandomLong(0, this.masterSeed);
		this.updatesSeed = this.util.getRandomLong(0, this.masterSeed);
		this.deletesSeed = this.util.getRandomLong(0, this.masterSeed);
	}

	@SuppressWarnings("unused")
	private boolean onlyCreates() {
		if (this.onlyCreates) {
			this.onlyCreates = false;
			for (DocumentType document : this.documentTypes.values()) {
				if (document.lastDocument == document.firstDocument) {
					this.onlyCreates = true || this.onlyCreates;
				}
			}
		}
		return this.onlyCreates;
	}
	
	private void storeLoadgenStats() throws FileNotFoundException, IOException, ParseException {
		Map<String, List<LoadgenStats>> loadgenStatsToStore = new HashMap<String, List<LoadgenStats>>();
		List<LoadgenStats> list = new ArrayList<LoadGenerator.LoadgenStats>();
		LoadgenStats loadgenStats = new LoadgenStats();
		loadgenStats.masterSeed = this.masterSeed;
		loadgenStats.numberOfOps = this.counter;
		loadgenStats.creates = this.numberOfCreates;
		loadgenStats.updates = this.numberOfUpdates;
		loadgenStats.deletes = this.numberOfDeletes;
		if(this instanceof MobileLoadGenerator) {
			loadgenStats.mobile = true;
		} else {
			loadgenStats.mobile = false;
		}
		list.add(loadgenStats);
		loadgenStatsToStore.put("LoadgenData", list);
		String loadGenStatsFilePath = Constants.loadgen_stats_file;
		this.util.updateLoadgenDataToFiles(loadGenStatsFilePath, loadgenStatsToStore);
	}
	
	class LoadgenStats {
		long masterSeed;
		long numberOfOps;
		long creates;
		long updates;
		long deletes;
		boolean mobile;
	}
}
