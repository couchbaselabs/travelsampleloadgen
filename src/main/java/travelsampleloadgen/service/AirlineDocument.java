package travelsampleloadgen.service;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;

import travelsampleloadgen.model.Airline;
import travelsampleloadgen.util.Utils;

public class AirlineDocument extends DocumentTemplate {

	public Airline airline;
	public long id;
	public String name;
	public String iata;
	public String icao;
	public String callsign;
	public String country;
	private JSONObject airlineData;
	/**
	 * @param id the id to set
	 */
	public void setId() {
		this.id = this.seed;
	}
	/**
	 * @param name the name to set
	 */
	public void setName() {
		int length = Utils.getRandomInt(4, 10);
		String name = Utils.getRandomString(length, true);
		this.name = name;
	}
	/**
	 * @param iata the iata to set
	 */
	public void setIata() {
		String iata = Utils.getRandomString(2, this.name);
		this.iata = iata.toUpperCase();
	}
	/**
	 * @param icao the icao to set
	 */
	public void setIcao() {
		char randChar = Utils.getRandomChar(this.name);
		String icao = this.iata + randChar;
		this.icao = icao.toUpperCase();
	}
	/**
	 * @param callsign the callsign to set
	 */
	public void setCallsign() {
		String[] callSigns = this.name.split(" ");
		this.callsign = callSigns[0];
	}
	/**
	 * @param country the country to set
	 */
	public void setCountry() {
		JSONArray countries = (JSONArray)this.airlineData.get("country");
		this.country = (String) Utils.getRandomArrayItem(countries);
	}
	
	public AirlineDocument(long seed, int revison, JSONObject airlineData) {
		this.airlineData = airlineData;
		this.setSeed(seed, revison);
		Utils.setSeed(this.revisionSeed);
		this.setId();
		this.setName();
		this.setIata();
		this.setIcao();
		this.setCallsign();
		this.setCountry();
		airline = new Airline(this.id, this.name, this.iata, this.icao, this.callsign, this.country);
	}
	
	@Override
	public JSONObject getJsonObject() throws ParseException {
		Gson gson = new Gson();
		String jsonInString = gson.toJson(this.airline);
		JSONParser parser = new JSONParser();
		try {
			return (JSONObject) parser.parse(jsonInString);
		} catch (ParseException e) {
			e.printStackTrace();
			throw e;
		}
	}

}
