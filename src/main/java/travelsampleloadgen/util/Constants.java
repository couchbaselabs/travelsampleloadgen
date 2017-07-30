package travelsampleloadgen.util;

public class Constants {
	
	private static Constants instance;
	private String LoadgenPropertiesFilePath;
	private String TravelSampleDataFilePath;
	
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
}
