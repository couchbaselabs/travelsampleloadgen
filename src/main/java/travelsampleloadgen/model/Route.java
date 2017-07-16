package travelsampleloadgen.model;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Route {
	public long id;
	public final String type = "route";
	public String airline;
	public String airlineid;
	public String sourceairport;
	public String destinationairport;
	public int stops;
	public String equipment;
	public Schedule[] schedule;
	public float distance;

	public Route(long id, String airline, String airlineid, String sourceairport, String destinationairport, int stops,
			String equipment, Schedule[] schedule, float distance) {
		this.id = id;
		this.airline = airline;
		this.airlineid = airlineid;
		this.sourceairport = sourceairport;
		this.destinationairport = destinationairport;
		this.stops = stops;
		this.equipment = equipment;
		this.schedule = schedule;
		this.distance = distance;
	}
	
	public Route(JSONObject route) {
		this.id = (Long) route.get("id");
		this.airline = (String) route.get("airline");
		this.airlineid = (String) route.get("airlineid");
		this.sourceairport = (String) route.get("sourceairport");
		this.destinationairport = (String) route.get("destinationairport");
		this.stops = (Integer) route.get("stops");
		this.equipment = (String) route.get("equipment");
		JSONArray schedules = (JSONArray) route.get("schedule");
		this.distance = (Float) route.get("distance");
		this.schedule = new Schedule[schedules.size()];
		for(int i = 0; i < schedules.size(); i++) {
			JSONObject schedule = (JSONObject) schedules.get(i);
			this.schedule[i] = new Schedule(schedule);
		}
	}
}
