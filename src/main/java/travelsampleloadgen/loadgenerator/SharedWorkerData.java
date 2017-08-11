package travelsampleloadgen.loadgenerator;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import travelsampleloadgen.service.CouchbaseQueryService;
import travelsampleloadgen.service.RouteDocument;
import travelsampleloadgen.util.Constants;

public class SharedWorkerData {
	
	public long numberOfCreates;
	public long numberOfUpdates;
	public long numberOfDeletes;
	public boolean onlyCreates = true;
	public Map<String, DocumentType> documentTypes;
	public Map<Long, Long> currentCreateIds;
	public JSONObject inputData;
	private CouchbaseQueryService queryHelper;
	
	public SharedWorkerData() throws FileNotFoundException, IOException, ParseException {
		this.numberOfCreates = 0;
		this.numberOfDeletes = 0;
		this.numberOfUpdates = 0;
		String sampleDataFilePath = Constants.getInstance().getTravelSampleDataFilePath();
		JSONParser parser = new JSONParser();
		this.inputData = (JSONObject) parser.parse(new FileReader(sampleDataFilePath));
		this.queryHelper = new CouchbaseQueryService();
		this.documentTypes = this.initiateDocumentTypes();
		this.getDocumentMinId();
		this.getInitialDocumentsMaxId();
		currentCreateIds = new ConcurrentHashMap<Long, Long>();
	}
	
	public synchronized void incrementNumberOfCreates() {
		this.numberOfCreates++;
	}
	/**
	 * @param numberOfUpdates the numberOfUpdates to set
	 */
	public synchronized void incrementNumberOfUpdates() {
		this.numberOfUpdates++;
	}
	/**
	 * @param numberOfDeletes the numberOfDeletes to set
	 */
	public synchronized void incrementNumberOfDeletes() {
		this.numberOfDeletes++;
	}
	/**
	 * @param onlyCreates the onlyCreates to set
	 */
	
	public synchronized boolean alreadyCreated(long id) {
		return this.currentCreateIds.containsKey(id);
	}
	
	public synchronized boolean insertToCurrentCreatedIds(long id) {
		if(this.currentCreateIds.containsKey(id)) {
			return false;
		}
		this.currentCreateIds.put(id, id);
		return true;
	}
	
	public synchronized void setOnlyCreates(boolean onlyCreates) {
		this.onlyCreates = onlyCreates;
	}
	
	public synchronized void getDocumentsMaxId() throws ParseException {
		for (DocumentType document : this.documentTypes.values()) {
			JSONObject queryResult = (JSONObject) this.queryHelper.getMaxId(document.type).get(0);
			Object obj = queryResult.get("id");
			document.setLastDocument((obj == null ? document.lastDocument : (Long) obj));
		}
		this.setRouteMaxIds();
	}

	public synchronized void getDocumentMinId() throws ParseException {
		for (DocumentType document : this.documentTypes.values()) {
			JSONObject queryResult = (JSONObject) this.queryHelper.getMinId(document.type).get(0);
			Object obj = queryResult.get("id");
			document.setFirstDocument((obj == null ? document.firstDocument : (Long) obj));
		}
		this.setRouteMaxIds();
	}
	
	@SuppressWarnings("unused")
	public boolean onlyCreates() {
		if (this.onlyCreates) {
			synchronized (this) {
				this.onlyCreates = false;
				for (DocumentType document : this.documentTypes.values()) {
					if (document.lastDocument == document.firstDocument) {
						this.onlyCreates = true || this.onlyCreates;
					}
				}
			}
		}
		return this.onlyCreates;
	}
	
	public synchronized void setRouteMaxIds() {
		DocumentType document = this.documentTypes.get("airline");
		RouteDocument.minAirlineId = document.firstDocument;
		RouteDocument.maxAirlineId = document.lastDocument;
		document = this.documentTypes.get("airport");
		RouteDocument.minAirportId = document.firstDocument;
		RouteDocument.maxAirportId = document.lastDocument;
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
	
}
