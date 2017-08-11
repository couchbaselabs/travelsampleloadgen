package travelsampleloadgen.service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;

import travelsampleloadgen.util.Constants;

public class CouchbaseService {
	private Bucket bucket;
	private CouchbaseCluster couchbaseCluster;
	
	/**
	 * @return the bucket
	 */
	public Bucket getBucket() {
		return bucket;
	}

	public static CouchbaseService instance;
	
	private CouchbaseService() throws FileNotFoundException, IOException, ParseException {
		Constants constants = Constants.getInstance();
		constants.initializeLoadgenConstants();
		String hostName = Constants.couchbase_host;
		List<String> hostNames = new ArrayList<String>(Arrays.asList(hostName.split(",")));
		String bucketName = Constants.bucket;
		String bucketPassword = Constants.bucket_password;
		this.couchbaseCluster = CouchbaseCluster.create(hostNames);
		this.bucket = couchbaseCluster.openBucket(bucketName, bucketPassword);
	}
	
	public static CouchbaseService getInstance() throws FileNotFoundException, IOException, ParseException {
		if(instance == null) {
			instance = new CouchbaseService();
		}
		return instance;
	}
	
	public static void closeCouchbaseConnections() throws FileNotFoundException, IOException, ParseException {
		if (instance == null) {
			instance = getInstance();
		}
		instance.bucket.close();
		instance.couchbaseCluster.disconnect();
	}
}
