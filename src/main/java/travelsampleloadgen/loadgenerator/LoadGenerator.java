package travelsampleloadgen.loadgenerator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import travelsampleloadgen.service.AirlineDocument;
import travelsampleloadgen.service.AirportDocument;
import travelsampleloadgen.service.CouchbaseCURDService;
import travelsampleloadgen.service.DocumentTemplate;
import travelsampleloadgen.service.RouteDocument;
import travelsampleloadgen.util.Utils;

public class LoadGenerator {
	private long numberOfOps;
	private long counter;
	private double creates;
	private double updates;
	private double deletes;
	private JSONParser parser;
	private JSONObject inputData;
	private String inputDataFile;
	private CouchbaseCURDService cbHelper;
	
	public LoadGenerator() throws FileNotFoundException, IOException, ParseException {
		this((Long)Utils.getLoadGenPropertyFromResource("NumberOfOps", "LoadgenProperties.json"), Utils.getFilePathFromResources("TravelSampleData.json"));
	}
	
	public LoadGenerator(Long numberOfOps, String inputDataFile)
			throws FileNotFoundException, IOException, ParseException {
		this.numberOfOps = numberOfOps;
		this.counter = this.numberOfOps;
		this.inputDataFile = inputDataFile;
		parser = new JSONParser();
		this.inputData = (JSONObject) parser.parse(new FileReader(this.inputDataFile));
		this.cbHelper = new CouchbaseCURDService();
	}

	public void generate() throws ParseException {
		while (this.counter != 0) {
			this.next();
			this.counter--;
		}
	}

	public void generateRoutes() throws ParseException, FileNotFoundException, IOException {
		this.counter = this.numberOfOps;
		while (this.counter != 0) {
			this.createRoute();
			this.counter--;
		}
	}

	public void next() throws ParseException {
		this.create();
	}

	public void create() throws ParseException {
		this.createAirline();
		this.createAirport();
	}

	public void createAirline() throws ParseException {
		DocumentTemplate template = new AirlineDocument(this.counter + 10000, 0, this.inputData);
		JSONObject jsonDocument = template.getJsonObject();
		String documentId = jsonDocument.get("type").toString() + "_" + jsonDocument.get("id").toString();
		boolean success = this.cbHelper.insertToBucket(jsonDocument, documentId);
		if (success) {
			System.out.println("Successfully inserted " + documentId);
		}
	}

	public void createAirport() throws ParseException {
		DocumentTemplate template = new AirportDocument(this.counter + 20000, 0, this.inputData);
		JSONObject jsonDocument = template.getJsonObject();
		String documentId = jsonDocument.get("type").toString() + "_" + jsonDocument.get("id").toString();
		boolean success = this.cbHelper.insertToBucket(jsonDocument, documentId);
		if (success) {
			System.out.println("Successfully inserted " + documentId);
		}
	}

	public void createRoute() throws ParseException, FileNotFoundException, IOException {
		DocumentTemplate template = new RouteDocument(this.counter + 30000, 0, this.inputData);
		JSONObject jsonDocument = template.getJsonObject();
		String documentId = jsonDocument.get("type").toString() + "_" + jsonDocument.get("id").toString();
		boolean success = this.cbHelper.insertToBucket(jsonDocument, documentId);
		if (success) {
			System.out.println("Successfully inserted " + documentId);
		}
	}
}
