package travelsampleloadgen.loadgenerator;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import travelsampleloadgen.service.CouchbaseCURDService;

public class SDKLoadGeneratorThread extends LoadGeneratorWorkers {
	
	private CouchbaseCURDService curdHelper;
	
	public SDKLoadGeneratorThread(long threadSeed, SharedWorkerData sharedWorkerData) throws FileNotFoundException, IOException, ParseException {
		super(threadSeed, sharedWorkerData);
		this.curdHelper = new CouchbaseCURDService();
	}

	@Override
	public boolean createDocumentToServer(JSONObject document, String documentId) {
		return this.curdHelper.insertToBucket(document, documentId);
	}

	@Override
	public boolean updateDocumentToServer(JSONObject document, String documentId) {
		return this.curdHelper.updateToBucket(document, documentId);
	}

	@Override
	public boolean deleteDocumentFromServer(String documentId) {
		return this.curdHelper.deleteFromBucket(documentId);
	}
}
