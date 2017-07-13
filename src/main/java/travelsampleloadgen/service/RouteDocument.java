package travelsampleloadgen.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.couchbase.client.java.document.json.JsonObject;
import com.google.gson.Gson;

import travelsampleloadgen.model.Airline;
import travelsampleloadgen.model.Airport;
import travelsampleloadgen.model.Route;
import travelsampleloadgen.model.Schedule;
import travelsampleloadgen.util.Utils;

public class RouteDocument extends DocumentTemplate {

	public long id;
	public String airline;
	public String airlineid;
	public String sourceairport;
	public String destinationairport;
	public int stops;
	public String equipment;
	public Schedule[] schedule;
	public float distance;
	private JSONObject routeData;
	public Route route;
	private Airline airLine;
	private Airport sourceAirport;
	private Airport destinationAirport;
	
	
	/**
	 * @param id the id to set
	 */
	public void setId() {
		this.id = seed;
	}


	/**
	 * @param airline the airline to set
	 */
	public void setAirline() {
		this.airline = airLine.iata;
	}


	/**
	 * @param airlineid the airlineid to set
	 */
	public void setAirlineid() {
		this.airlineid = "airline_" + airLine.id;
	}


	/**
	 * @param sourceairport the sourceairport to set
	 */
	public void setSourceairport() {
		this.sourceairport = sourceAirport.faa;
	}


	/**
	 * @param destinationairport the destinationairport to set
	 */
	public void setDestinationairport() {
		this.destinationairport = destinationAirport.faa;
	}


	/**
	 * @param stops the stops to set
	 */
	public void setStops() {
		this.stops = Utils.getRandomInt(0, 3);
	}


	/**
	 * @param equipment the equipment to set
	 */
	public void setEquipment() {
		int numberOfEquipment = Utils.getRandomInt(1, 3);
		this.equipment = "";
		for(int i = 0; i < numberOfEquipment; i++) {
			this.equipment += Utils.getRandomInt(0, 999) + " ";
		}
		this.equipment.trim();
	}


	/**
	 * @param schedule the schedule to set
	 */
	public void setSchedule() {
		int numberOfSchedules = Utils.getRandomInt(3, 20);
		List<Schedule> schedules = new ArrayList<Schedule>();
		for(int i = 0; i < numberOfSchedules; i++) {
			Schedule schedule = new Schedule();
			Date randDate = Utils.getRandomDate(0, Math.abs(System.currentTimeMillis()));
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(randDate);
			int day = calendar.get(Calendar.DAY_OF_WEEK);
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			String utc = sdf.format(randDate);
			String flight = this.airline + Utils.getRandomInt(100, 999);
			schedule.day = day;
			schedule.utc = utc;
			schedule.flight = flight;
			schedules.add(schedule);
		}
		this.schedule = schedules.toArray(new Schedule[0]);
	}


	/**
	 * @param distance the distance to set
	 */
	public void setDistance() {
		this.distance = Utils.getRandomFloat(100, 10000);
	}

	
	public RouteDocument(long seed, int revison, JSONObject routeData) throws FileNotFoundException, IOException, ParseException {
		this.routeData = routeData;
		this.setSeed(seed, revison);
		Utils.setSeed(this.revisionSeed);
		this.setId();
		CouchbaseCURDService cbHelper = new CouchbaseCURDService();
		JSONArray existingDocumentIds = cbHelper.getExistingDocumentIdsFromBucket("airline");
		int randomAirline = ((JsonObject)Utils.getRandomArrayItem(existingDocumentIds)).getInt("id");
		this.airLine = new AirlineDocument(randomAirline, 0, routeData).airline;
		existingDocumentIds = cbHelper.getExistingDocumentIdsFromBucket("airport");
		int sourceAirline = ((JsonObject)Utils.getRandomArrayItem(existingDocumentIds)).getInt("id");
		int destinationAirline = ((JsonObject)Utils.getRandomArrayItem(existingDocumentIds)).getInt("id");
		while (sourceAirline == destinationAirline) {
			destinationAirline = ((JsonObject)Utils.getRandomArrayItem(existingDocumentIds)).getInt("id");
		}
		this.sourceAirport = new AirportDocument(sourceAirline, 0, routeData).airport;
		this.destinationAirport = new AirportDocument(destinationAirline, 0, routeData).airport;
		this.setAirline();
		this.setAirlineid();
		this.setSourceairport();
		this.setDestinationairport();
		this.setStops();
		this.setEquipment();
		this.setSchedule();
		this.setDistance();
		this.route = new Route(this.id, this.airline, this.airlineid, this.sourceairport, this.destinationairport, this.stops, this.equipment, this.schedule, this.distance);
	}
	
	@Override
	public JSONObject getJsonObject() throws ParseException {
		Gson gson = new Gson();
		String jsonInString = gson.toJson(this.route);
		JSONParser parser = new JSONParser();
		try {
			return (JSONObject) parser.parse(jsonInString);
		} catch (ParseException e) {
			e.printStackTrace();
			throw e;
		}
	}

}
