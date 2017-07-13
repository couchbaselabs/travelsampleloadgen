package travelsampleloadgen.service;

import java.util.Random;

import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

public abstract class DocumentTemplate {
long seed, revisionSeed;
	
	public void setSeed(long seed, int revison) {
		this.seed = seed;
		Random rand = new Random(seed);
		this.revisionSeed = rand.nextLong();
		for(int i = 1;i < revison; i++) {
			this.revisionSeed = rand.nextLong();
		}
	}
	
	public abstract JSONObject getJsonObject() throws ParseException;
}
