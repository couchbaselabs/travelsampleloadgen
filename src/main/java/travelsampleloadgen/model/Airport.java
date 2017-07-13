package travelsampleloadgen.model;

import java.util.Map;

public class Airport {
	public long id;
	public String type = "airport";
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
}
