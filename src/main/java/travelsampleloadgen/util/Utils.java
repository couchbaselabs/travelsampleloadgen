package travelsampleloadgen.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;

import travelsampleloadgen.service.CouchbaseQueryService;

public class Utils {
	private static Random random = new Random();

	public static void setSeed(long seed) {
		random.setSeed(seed);
	}

	public static Random getRandomGenerator() {
		return random;
	}

	public static int getRandomInt(int min, int max) {
		return random.nextInt(max - min + 1) + min;
	}
	
	public static long getRandomLong(long min, long max) {
		return (long)(random.nextDouble() * (max - min)) + min;
	}
	
	public static float getRandomFloat(int min, int max) {
		return random.nextFloat() * (max - min) + min;
	}

	public static boolean getRandomBoolean() {
		return random.nextBoolean();
	}

	public static Date getRandomDate(long min, long max) {
		long randomMilliseconds = (long) (random.nextDouble() * (max - min)) + min;
		return new Date(randomMilliseconds);
	}

	public static char getRandomChar() {
		String charSet = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
		return charSet.charAt(random.nextInt(charSet.length()));
	}
	
	public static char getRandomChar(String charSet) {
		return charSet.charAt(random.nextInt(charSet.length()));
	}
	
	public static String getRandomString(int length, boolean withNumbers) {
		String charSet = " abcdef ghijklm nopqrstuvwxyz ";
		if(withNumbers) {
			charSet += "1234 567890 ";
		}
		char[] randomString = new char[length];
		for(int i=0;i<length;i++) {
			randomString[i] = charSet.charAt(random.nextInt(charSet.length()));
		}
//		randomString[randomString.length - 1] = '\0';
		boolean found = false;
		for(int i = 0; i < randomString.length; i++) {
			if (!found && Character.isLetter(randomString[i])) {
				randomString[i] = Character.toUpperCase(randomString[i]);
				found = true;
			} 
			else if (Character.isWhitespace(randomString[i]) || Character.isDigit(randomString[i])) {
				found = false;
			}
		}
		return new String(randomString);
	}
	
	public static String getRandomString(int length, String charSet) {
		charSet = charSet.replaceAll("\\s+", "");
		char[] randomString = new char[length];
		for(int i=0;i<length;i++) {
			randomString[i] = charSet.charAt(random.nextInt(charSet.length()));
			
		}
//		randomString[randomString.length - 1] = '\0';
		return new String(randomString);
	}
	
	public static Object getRandomArrayItem(ArrayList<?> list) {
		if(list.size() == 1) {
			return list.get(0);
		}
		return list.get(random.nextInt(list.size()));
	}
	
	public static Object getRandomJsonArrayItem(JSONArray array) {
		if(array.size() == 1) {
			return array.get(0);
		}
		return array.get(random.nextInt(array.size()));
	}
	
	public static String getFilePathFromResources(String fileName) {
		ClassLoader classLoader = Utils.class.getClassLoader();
		return classLoader.getResource(fileName).getPath();
	}
	
	public static Object getLoadGenPropertyFromResource(String propertyName, String fileName) throws FileNotFoundException, IOException, ParseException {
		ClassLoader classLoader = Utils.class.getClassLoader();
		String filePath = classLoader.getResource(fileName).getPath();
		JSONParser parser = new JSONParser();
		JSONObject properties = (JSONObject) parser.parse(new FileReader(filePath));
		return properties.get(propertyName);
	}
	
	public static Object getLoadGenPropertyFromFilePath(String propertyName, String filePath) throws FileNotFoundException, IOException, ParseException {
		JSONParser parser = new JSONParser();
		JSONObject properties = (JSONObject) parser.parse(new FileReader(filePath));
		return properties.get(propertyName);
	}
	
	@SuppressWarnings("unchecked")
	public static JSONObject deepMergeJSONObjects(JSONObject source, JSONObject target) {
	    for (Object key: source.keySet()) {
	            Object value = source.get(key);
	            if (!target.containsKey(key)) {
	                // new value for "key":
	                target.put(key, value);
	            } else {
	                // existing value for "key" - recursively deep merge:
	                if (value instanceof JSONObject) {
	                    JSONObject valueJson = (JSONObject)value;
	                    deepMergeJSONObjects(valueJson, (JSONObject)target.get(key));
	                } else if (value instanceof JSONArray) {
	                		JSONArray sourceArray = (JSONArray) value;
	                		JSONArray targetArray = (JSONArray) target.get(key);
	                		for(Object v : sourceArray) {
	                			targetArray.add(v);
	                		}
	                } else {
	                    target.put(key, value);
	                }
	            }
	    }
	    return target;
	}
	
	public static void updateLoadgenDataToFiles(String filePath, Object objectToStore) throws ParseException, FileNotFoundException, IOException {
		JSONParser parser = new JSONParser();
		Gson gson = new Gson();
		FileReader reader;
		try {
		reader = new FileReader(filePath);
		} catch(FileNotFoundException e) {
			FileWriter newFile = new FileWriter(filePath);
			newFile.write("{}");
			newFile.flush();
			newFile.close();
			reader = new FileReader(filePath);
		}
		JSONObject originalJson = (JSONObject) parser.parse(reader);
		JSONObject appendJson = (JSONObject) parser.parse(gson.toJson(objectToStore));
		JSONObject jsonToStore = Utils.deepMergeJSONObjects(appendJson, originalJson);
		reader.close();
		FileWriter writer = new FileWriter(filePath);
		writer.write(jsonToStore.toJSONString());
		writer.flush();
		writer.close();
	}
	
	public static long getRandomDocumentId(String type) throws ParseException, FileNotFoundException, IOException {
		CouchbaseQueryService queryHelper = new CouchbaseQueryService();
		JSONArray documentIds = queryHelper.getExistingDocumentIdsFromBucket(type);
		JSONObject randomDocumentId = (JSONObject) Utils.getRandomJsonArrayItem(documentIds);
		long randomId = (Long) randomDocumentId.get("id");
		return randomId;
	}
}
