package travelsampleloadgen.loadgenerator;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import travelsampleloadgen.service.CouchbaseCURDService;
import travelsampleloadgen.service.CouchbaseMobileService;
import travelsampleloadgen.util.Constants;
import travelsampleloadgen.util.Utils;

public class MobileLoadGenerator extends LoadGenerator {
	
	private CouchbaseMobileService cbMobileHelper;
	
	public MobileLoadGenerator() throws FileNotFoundException, IOException, ParseException {
		this(Constants.getInstance().getLoadgenPropertiesFile(),
				Constants.getInstance().getTravelSampleDataFilePath());
	}
	
	public MobileLoadGenerator(String propertiesFile, String inputDataFile)
			throws FileNotFoundException, IOException, ParseException {
		super(propertiesFile, inputDataFile);
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
