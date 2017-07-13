package travelsampleloadgen;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream.GetField;

import org.json.simple.JSONArray;
import org.json.simple.parser.ParseException;

import travelsampleloadgen.loadgenerator.LoadGenerator;
import travelsampleloadgen.service.CouchbaseCURDService;

public class TravelSampleLoadGenerator {
	
	public static void main(String[] args) {
		try {
			LoadGenerator loadGen = new LoadGenerator();
			loadGen.generate();
			loadGen.generateRoutes();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
