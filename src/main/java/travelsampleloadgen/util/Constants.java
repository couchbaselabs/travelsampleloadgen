package travelsampleloadgen.util;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.parser.ParseException;

public class Constants {
	
	private static Constants instance;
	private String LoadgenPropertiesFilePath;
	private String TravelSampleDataFilePath;
	public static long numberOfOps;
	public static long creates;
	public static long updates;
	public static long deletes;
	public static String couchbase_host;
	public static String couchbase_admin_username;
	public static String couchbase_admin_password;
	public static String bucket;
	public static String bucket_password;
	public static String loadgen_stats_file;
	public static String mobile_sync_host;
	public static String mobile_db;
	public static long threads;
	private static boolean constantsInitialized = false;
	
	
	private Constants() {
	}
	
	public static Constants getInstance() {
		if(instance == null) {
			instance = new Constants();
		}
		return instance;
	}
	
	public void setLoadgenPropertiesFile(String loadgenPropertiesFile) {
		this.LoadgenPropertiesFilePath = loadgenPropertiesFile;
	}
	
	public String getLoadgenPropertiesFile() {
		if (this.LoadgenPropertiesFilePath == null) {
			ClassLoader classLoader = getClass().getClassLoader();
			this.LoadgenPropertiesFilePath = classLoader.getResource("LoadgenProperties.json").getPath();
		}
		return this.LoadgenPropertiesFilePath;
	}
	
	public void setTravelSampleDataFilePath(String travelSampleDataFilePath) {
		this.TravelSampleDataFilePath = travelSampleDataFilePath;
	}
	
	public String getTravelSampleDataFilePath() {
		if (this.TravelSampleDataFilePath == null) {
			ClassLoader classLoader = getClass().getClassLoader();
			this.TravelSampleDataFilePath = classLoader.getResource("TravelSampleData.json").getPath();
		}
		return this.TravelSampleDataFilePath;
	}
	
	public void initializeLoadgenConstants() throws FileNotFoundException, IOException, ParseException {
		if(constantsInitialized) {
			return;
		}
		Utils util = new Utils();
		String propertiesFile = this.getLoadgenPropertiesFile();
		numberOfOps = (Long) util.getLoadGenPropertyFromFilePath("NumberOfOps", propertiesFile);
		creates = (Long ) util.getLoadGenPropertyFromFilePath("Creates", propertiesFile);
		updates = (Long ) util.getLoadGenPropertyFromFilePath("Updates", propertiesFile);
		deletes = (Long ) util.getLoadGenPropertyFromFilePath("Deletes", propertiesFile);
		couchbase_host = (String ) util.getLoadGenPropertyFromFilePath("couchbase-host", propertiesFile);
		couchbase_admin_username = (String)util.getLoadGenPropertyFromFilePath("couchbase-admin-username", propertiesFile);
		couchbase_admin_password = (String)util.getLoadGenPropertyFromFilePath("couchbase-admin-password", propertiesFile);
		bucket = (String ) util.getLoadGenPropertyFromFilePath("bucket", propertiesFile);
		bucket_password = (String ) util.getLoadGenPropertyFromFilePath("bucket-password", propertiesFile);
		loadgen_stats_file = (String ) util.getLoadGenPropertyFromFilePath("loadgen-stats", propertiesFile);
		mobile_sync_host = (String ) util.getLoadGenPropertyFromFilePath("mobile-host", propertiesFile);
		mobile_db = (String ) util.getLoadGenPropertyFromFilePath("mobile-db", propertiesFile);
		threads = (Long ) util.getLoadGenPropertyFromFilePath("threads", propertiesFile);
		constantsInitialized = true;
	}
}
