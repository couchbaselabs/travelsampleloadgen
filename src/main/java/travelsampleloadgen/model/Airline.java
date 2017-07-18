package travelsampleloadgen.model;

import org.json.simple.JSONObject;

public class Airline {
	public long id;
	public final String type = "airline";
	public String name;
	public String iata;
	public String icao;
	public String callsign;
	public String country;
	
	public Airline(long id, String name, String iata, String icao, String callsign, String country) {
		this.id = id;
		this.name = name;
		this.iata = iata;
		this.icao = icao;
		this.callsign = callsign;
		this.country = country;
	}
	
	public Airline(JSONObject airline) {
		try {
		this.id = (Long) airline.get("id");
		this.name = (String) airline.get("name");
		this.iata = (String) airline.get("iata");
		this.icao = (String) airline.get("icao");
		this.callsign = (String) airline.get("callsign");
		this.country = (String) airline.get("country");
		} catch (java.lang.NullPointerException e) {
			System.out.println(e);
		}
	}
}
