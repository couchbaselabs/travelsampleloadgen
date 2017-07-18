package travelsampleloadgen.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class CouchbaseMobileService {
	URL url;
	HttpURLConnection connection;
	String baseUrlString;
	String db;
	String hostName;
	
	public CouchbaseMobileService() throws FileNotFoundException, IOException, ParseException {
		ClassLoader classLoader = getClass().getClassLoader();
		String fileName = classLoader.getResource("LoadgenProperties.json").getPath();
		JSONParser parser = new JSONParser();
		JSONObject properties = (JSONObject) parser.parse(new FileReader(fileName));
		hostName = (String) properties.get("mobile-host");
		db = (String) properties.get("mobile-db");
		this.baseUrlString = "http://" + hostName + "/" + db + "/";
	}
	
	public JSONObject getDocument(String documentName) throws IOException, ParseException {
		JSONObject result = null;
		String urlString = this.baseUrlString + documentName;
		this.url = new URL(urlString);
		this.connection = (HttpURLConnection) url.openConnection();
		this.connection.setRequestMethod("GET");
		this.connection.setRequestProperty("Accept", "application/json");
		int response = this.connection.getResponseCode();
		if (response < 200 || response > 202) {
			return null;
		}
		BufferedReader inputStream = new BufferedReader(new InputStreamReader(this.connection.getInputStream()));
		String line;
		String output = "";
		while ((line = inputStream.readLine()) != null) {
			output += line;
		}
		if(output.isEmpty()) {
			return null;
		}
		JSONParser parser = new JSONParser();
		result = (JSONObject) parser.parse(output);
		inputStream.close();
		this.connection.disconnect();
		return result;
	}
	
	public boolean putDocument(String documentName, JSONObject document) throws IOException {
		String urlString = this.baseUrlString +  documentName;
		this.url = new URL(urlString);
		this.connection = (HttpURLConnection) url.openConnection();
		this.connection.setRequestMethod("PUT");
		this.connection.setRequestProperty("Content-Type",  "application/json");
		this.connection.setDoInput(true);
		this.connection.setDoOutput(true);
		DataOutputStream outputStream = new DataOutputStream(this.connection.getOutputStream());
		outputStream.write(document.toJSONString().getBytes());
		outputStream.flush();
		outputStream.close();
		int response = this.connection.getResponseCode();
		if (response < 200 || response > 202) {
			return false;
		}
		this.connection.disconnect();
		return true;
	}
	
	public boolean updateDocument(String documentName, JSONObject document) throws IOException, ParseException {
		JSONObject olddocument = this.getDocument(documentName);
		if (olddocument == null) {
			return false;
		}
		String rev_id = (String) olddocument.get("_rev");
		String urlString = this.baseUrlString + documentName;
		urlString += "?rev=" + rev_id;
		this.url = new URL(urlString);
		this.connection = (HttpURLConnection) url.openConnection();
		this.connection.setRequestMethod("PUT");
		this.connection.setRequestProperty("Content-Type",  "application/json");
		this.connection.setDoInput(true);
		this.connection.setDoOutput(true);
		DataOutputStream outputStream = new DataOutputStream(this.connection.getOutputStream());
		outputStream.write(document.toJSONString().getBytes());
		outputStream.flush();
		outputStream.close();
		int response = this.connection.getResponseCode();
		if (response < 200 || response > 202) {
			return false;
		}
		this.connection.disconnect();
		return true;
	}
	
	public boolean deleteDocument(String documentName) throws IOException, ParseException {
		JSONObject olddocument = this.getDocument(documentName);
		if (olddocument == null) {
			return false;
		}
		String rev_id = (String) olddocument.get("_rev");
		String urlString = this.baseUrlString + documentName + "?rev=" + rev_id;
		this.url = new URL(urlString);
		this.connection = (HttpURLConnection) url.openConnection();
		this.connection.setRequestMethod("DELETE");
		this.connection.setRequestProperty("Accept", "application/json");
		int response = this.connection.getResponseCode();
		if (response < 200 || response > 202) {
			return false;
		}
		this.connection.disconnect();
		return true;
	}
}
