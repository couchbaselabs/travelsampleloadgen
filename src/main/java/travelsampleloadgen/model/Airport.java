package travelsampleloadgen.model;

import java.util.Map;

import org.json.simple.JSONObject;

public class Airport {
	public long id;
	public final String type = "airport";
	public String airportname;
	public String city;
	public String country;
	public String faa;
	public String icao;
	public String tz;
	public Map<?, ?> geo;
	
	public Airport(long id, String airportname, String city, String country, String faa, String icao, String tz, Map<?,?> geo) {
		this.id = id;
		this.airportname = airportname;
		this.city = city;
		this.country = country;
		this.faa = faa;
		this.icao = icao;
		this.tz = tz;
		this.geo = geo;
	}
	
	public Airport(JSONObject airport) {
		this.id = (Long) airport.get("id");
		this.airportname = (String) airport.get("airportname");
		this.city = (String) airport.get("city");
		this.country = (String) airport.get("country");
		this.faa = (String) airport.get("faa");
		this.icao = (String) airport.get("icao");
		this.tz = (String) airport.get("tz");
		this.geo = (Map<?, ?>) airport.get("geo");
	}
}
