package travelsampleloadgen.model;

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
}
