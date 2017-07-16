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
import travelsampleloadgen.service.CouchbaseCURDService;
import travelsampleloadgen.service.CouchbaseQueryService;
import travelsampleloadgen.service.DocumentTemplate;
import travelsampleloadgen.service.RouteDocument;
import travelsampleloadgen.util.Utils;

public class LoadGenerator {
	private long numberOfOps;
	private long counter;
	private long creates;
	private long updates;
	private long deletes;
	private JSONParser parser;
	private JSONObject inputData;
	private CouchbaseCURDService curdHelper;
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

	public LoadGenerator() throws FileNotFoundException, IOException, ParseException {
		this(Utils.getFilePathFromResources("LoadgenProperties.json"),
				Utils.getFilePathFromResources("TravelSampleData.json"));
	}

	public LoadGenerator(String propertiesFile, String inputDataFile)
			throws FileNotFoundException, IOException, ParseException {
		this.numberOfOps = (Long) Utils.getLoadGenPropertyFromFilePath("NumberOfOps", propertiesFile);
		this.creates = (Long) Utils.getLoadGenPropertyFromFilePath("Creates", propertiesFile);
		this.updates = (Long) Utils.getLoadGenPropertyFromFilePath("Updates", propertiesFile);
		this.deletes = (Long) Utils.getLoadGenPropertyFromFilePath("Deletes", propertiesFile);
		this.numberOfCreates = 0;
		this.numberOfDeletes = 0;
		this.numberOfUpdates = 0;
		this.masterSeed = System.currentTimeMillis();
		this.setSeeds();
		parser = new JSONParser();
		this.inputData = (JSONObject) parser.parse(new FileReader(inputDataFile));
		this.curdHelper = new CouchbaseCURDService();
		this.queryHelper = new CouchbaseQueryService();
		this.documentTypes = initiateDocumentTypes();
		this.getDocumentMinId();
		this.getDocumentsMaxId();
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
		this.storeMasterSeed();
	}

	public void next() throws ParseException, FileNotFoundException, IOException {
		Utils.setSeed(this.masterSeed);
		int randomInt = 0;
		for (long i = 0; i <= this.counter; i++) {
			randomInt = Utils.getRandomInt(0, 100);
		}
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
		Utils.setSeed(this.createsSeed + this.numberOfCreates);
		String randType = (String) Utils.getRandomArrayItem(types);
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
		boolean success = this.curdHelper.insertToBucket(jsonDocument, documentId);
		if (success) {
			document.lastDocument = seed;
			document.numCreated++;
			System.out.println("Successfully inserted " + documentId);
		}

	}

	public void delete() {
		ArrayList<String> types = new ArrayList<String>(
				Arrays.asList(this.documentTypes.keySet().toArray(new String[0])));
		for (DocumentType document : this.documentTypes.values()) {
			if (document.lastDocument == 0) {
				types.remove(document.type);
			}
		}
		Utils.setSeed(this.deletesSeed + this.numberOfDeletes);
		String randType = (String) Utils.getRandomArrayItem(types);
		DocumentType documentType = this.documentTypes.get(randType);
		this.deleteDocument(documentType);
		this.numberOfDeletes++;
	}

	public void deleteDocument(DocumentType document) {
		boolean deleted = false;
		long firstDocument = document.firstDocument;
		long lastDocument = document.lastDocument;
		String type = document.type;
		while (!deleted) {
			long randomId = Utils.getRandomLong(firstDocument, lastDocument);
			String document_name = type + "_" + randomId;
			try {
				deleted = this.curdHelper.deleteFromBucket(document_name);
				if (deleted) {
					System.out.println("Successfully deleted " + document_name);
				}
			} catch (DocumentDoesNotExistException e) {
				deleted = false;
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
		Utils.setSeed(this.updatesSeed + this.numberOfUpdates);
		String randType = (String) Utils.getRandomArrayItem(types);
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
		while (!updated) {
			long randomId = Utils.getRandomLong(firstDocument, lastDocument);
			if (type.equals("airline")) {
				template = new AirlineDocument(randomId, this.numberOfUpdates + 1, this.inputData);
			} else if (type.equals("airport")) {
				template = new AirportDocument(randomId, this.numberOfUpdates + 1, this.inputData);
			} else if (type.equals("route")) {
				template = new RouteDocument(randomId, this.numberOfUpdates + 1, this.inputData);
			}
			String document_name = type + "_" + randomId;
			try {
				updated = this.curdHelper.updateToBucket(template.getJsonObject(), document_name);
				if (updated) {
					System.out.println("Successfully updated " + document_name);
				}
			} catch (DocumentDoesNotExistException e) {
				updated = false;
			}
		}

	}

	private Map<String, DocumentType> initiateDocumentTypes() {
		Map<String, DocumentType> documentTypes = new HashMap<String, LoadGenerator.DocumentType>();
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
			document.lastIteration = obj == null ? document.lastIteration : (Long) obj;
		}
	}

	private void getDocumentMinId() throws ParseException {
		for (DocumentType document : this.documentTypes.values()) {
			JSONObject queryResult = (JSONObject) this.queryHelper.getMinId(document.type).get(0);
			Object obj = queryResult.get("id");
			document.firstDocument = obj == null ? document.firstDocument : (Long) obj;
		}
	}

	private void setSeeds() {
		Utils.setSeed(this.masterSeed);
		this.createsSeed = Utils.getRandomLong(0, this.masterSeed);
		this.updatesSeed = Utils.getRandomLong(0, this.masterSeed);
		this.deletesSeed = Utils.getRandomLong(0, this.masterSeed);
	}

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
	
	private void storeMasterSeed() throws FileNotFoundException, ParseException, IOException {
		Map<String, List<Long>> map = new HashMap<String, List<Long>>();
		List<Long> seeds = new ArrayList<Long>();
		seeds.add(this.masterSeed);
		map.put("seeds", seeds);
		String loadgenSeedsFilePath = (String) Utils.getLoadGenPropertyFromResource("loadgen-seeds", "LoadgenProperties.json");
		Utils.updateLoadgenDataToFiles(loadgenSeedsFilePath, map);
	}
	
	class DocumentType {
		String type;
		int offSet;
		long firstDocument;
		long lastDocument;
		long lastIteration;
		long numCreated;

		DocumentType(String type, int offset) {
			this.type = type;
			this.offSet = offset;
			this.firstDocument = 10000 * offset;
			this.lastDocument = 10000 * offset;
			this.lastIteration = 10000 * offset;
			this.numCreated = 0;
		}
	}
}
