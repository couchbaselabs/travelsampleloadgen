package travelsampleloadgen.loadgenerator;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import travelsampleloadgen.service.CouchbaseCURDService;
import travelsampleloadgen.util.Utils;

public class SDKLoadGenerator extends LoadGenerator {
	
	private CouchbaseCURDService curdHelper;
	
	public SDKLoadGenerator() throws FileNotFoundException, IOException, ParseException {
		this(Utils.getFilePathFromResources("LoadgenProperties.json"),
				Utils.getFilePathFromResources("TravelSampleData.json"));
	}
	
	public SDKLoadGenerator(String propertiesFile, String inputDataFile)
			throws FileNotFoundException, IOException, ParseException {
		super(propertiesFile, inputDataFile);
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
