package travelsampleloadgen.loadgenerator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import travelsampleloadgen.service.CouchbaseCURDService;
import travelsampleloadgen.service.CouchbaseQueryService;
import travelsampleloadgen.util.Utils;

public class QueryLoadGenerator extends Thread {
	
	private Utils util = new Utils();
	private CouchbaseQueryService cbQueryService;
	private CouchbaseCURDService cbCURDService;
	private boolean stopThread = false;
	private ArrayList<String> documentTypes;
	
	
	/**
	 * @param stopThread the stopThread to set
	 */
	public void setStopThread(boolean stopThread) {
		this.stopThread = stopThread;
	}

	public QueryLoadGenerator() throws FileNotFoundException, IOException, ParseException {
		this.cbQueryService = new CouchbaseQueryService();
		this.cbCURDService = new CouchbaseCURDService();
		util.setSeed(System.currentTimeMillis());
		this.documentTypes = new ArrayList<String>();
		this.documentTypes.add("airline");
		this.documentTypes.add("airport");
		this.documentTypes.add("route");
	}
	
	public void run() {
		while(!stopThread) {
			try {
				this.getMinMaxId();
				this.getFaa();
				this.getAllAirline();
				this.getRandomRoute();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (ParseException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void getMinMaxId() throws ParseException {
		String docType = (String) this.util.getRandomArrayItem(this.documentTypes);
		this.cbQueryService.getMaxId(docType);
		this.cbQueryService.getMinId(docType);
	}
	
	private void getFaa() throws ParseException {
		String randomFaa = this.util.getRandomString(3, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789");
		this.cbQueryService.getAllAirport(randomFaa);
	}
	
	private JSONArray getAllAirline() throws ParseException {
		return this.cbQueryService.getExistingDocumentIdsFromBucket("airline");
	}
	
	private JSONArray getRandomRoute() throws ParseException, FileNotFoundException, IOException {
		
		long randomSourceAirportId = this.util.getRandomDocumentId("airport");
		long randomDestinationAirportId = this.util.getRandomDocumentId("airport");
		JSONObject sourceAiport = this.cbCURDService.getDocumentById("airport_" + randomSourceAirportId);
		JSONObject destinationAirport = this.cbCURDService.getDocumentById("airport_" + randomDestinationAirportId);
		String sourceAirportName = (String) sourceAiport.get("airportname");
		String destinationAirportName = (String) destinationAirport.get("airportname");
		Date randDate = util.getRandomDate(0, Math.abs(System.currentTimeMillis()));
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(randDate);
		return this.cbQueryService.findAllPath(sourceAirportName, destinationAirportName, calendar);
	}
}
