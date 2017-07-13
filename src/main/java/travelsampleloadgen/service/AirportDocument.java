package travelsampleloadgen.service;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.google.gson.Gson;

import travelsampleloadgen.model.Airport;
import travelsampleloadgen.util.Utils;

public class AirportDocument extends DocumentTemplate {
	public long id;
	public String airportname;
	public String city;
	public String country;
	public String faa;
	public String icao;
	public String tz;
	public Map<?, ?> geo;
	private JSONObject airportData;
	public Airport airport;
	
	/**
	 * @param id the id to set
	 */
	public void setId() {
		this.id = this.seed;
	}


	/**
	 * @param airportname the airportname to set
	 */
	public void setAirportname() {
		int length = Utils.getRandomInt(5, 10);
		String name = Utils.getRandomString(length, false);
		this.airportname = name;
	}


	/**
	 * @param city the city to set
	 */
	public void setCity() {
		JSONObject country = (JSONObject) this.airportData.get(this.country);
		JSONArray cities = (JSONArray) country.get("city");
		this.city = (String) Utils.getRandomArrayItem(cities);
	}


	/**
	 * @param country the country to set
	 */
	public void setCountry() {
		JSONArray countries = (JSONArray)this.airportData.get("country");
		this.country = (String) Utils.getRandomArrayItem(countries);
	}


	/**
	 * @param faa the faa to set
	 */
	public void setFaa() {
		String faa = Utils.getRandomString(3, this.airportname);
		this.faa = faa;
	}


	/**
	 * @param icao the icao to set
	 */
	public void setIcao() {
		char randChar = Utils.getRandomChar(this.airportname);
		String icao = this.faa + randChar;
		this.icao = icao;
	}


	/**
	 * @param tz the tz to set
	 */
	public void setTz() {
		JSONObject countryDetails = (JSONObject)this.airportData.get(this.country);
		JSONArray timeZones = (JSONArray)countryDetails.get("timeZone");
		this.tz = (String) Utils.getRandomArrayItem(timeZones);
	}


	/**
	 * @param geo the geo to set
	 */
	public void setGeo() {
		Map<String, Float> geo = new HashMap<String, Float>();
		geo.put("lat", Utils.getRandomFloat(-90, 90));
		geo.put("lon", Utils.getRandomFloat(-180, 180));
		geo.put("alt", Utils.getRandomFloat(0, 3000));
		this.geo = geo;
	}
	
	public AirportDocument(long seed, int revison, JSONObject airportData) {
		this.airportData = airportData;
		this.setSeed(seed, revison);
		Utils.setSeed(this.revisionSeed);
		this.setId();
		this.setAirportname();
		this.setFaa();
		this.setIcao();
		this.setCountry();
		this.setCity();
		this.setTz();
		this.setGeo();
		this.airport = new Airport(this.id, this.airportname, this.city, this.country, this.faa, this.icao, this.tz, this.geo);
	}

	@Override
	public JSONObject getJsonObject() throws ParseException {
		Gson gson = new Gson();
		String jsonInString = gson.toJson(this.airport);
		JSONParser parser = new JSONParser();
		try {
			return (JSONObject) parser.parse(jsonInString);
		} catch (ParseException e) {
			e.printStackTrace();
			throw e;
		}
	}

}