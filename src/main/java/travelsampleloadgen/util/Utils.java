package travelsampleloadgen.util;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

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
	
	public static Object getRandomArrayItem(JSONArray array) {
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
}
