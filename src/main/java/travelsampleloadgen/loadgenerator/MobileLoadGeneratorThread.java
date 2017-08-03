package travelsampleloadgen.loadgenerator;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import travelsampleloadgen.service.CouchbaseMobileService;

public class MobileLoadGeneratorThread extends LoadGeneratorWorkers {

	private CouchbaseMobileService cbMobileHelper;
	
	public MobileLoadGeneratorThread(long threadSeed, SharedWorkerData sharedWorkerData) throws FileNotFoundException, IOException, ParseException {
		super(threadSeed, sharedWorkerData);
		this.cbMobileHelper = new CouchbaseMobileService();
	}

	@Override
	public boolean createDocumentToServer(JSONObject document, String documentId) {
		try {
			return this.cbMobileHelper.putDocument(documentId, document);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean updateDocumentToServer(JSONObject document, String documentId) {
		try {
			return this.cbMobileHelper.updateDocument(documentId, document);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean deleteDocumentFromServer(String documentId) {
		try {
			return this.cbMobileHelper.deleteDocument(documentId);
		}catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (ParseException e) {
			e.printStackTrace();
			return false;
		}
	}

}
