package travelsampleloadgen.model;

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
}
