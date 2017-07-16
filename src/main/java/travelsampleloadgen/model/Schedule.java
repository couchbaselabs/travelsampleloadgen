package travelsampleloadgen.model;

import org.json.simple.JSONObject;


public class Schedule {
	public int day;
	public String utc;
	public String flight;
	public Schedule() {
	}
	
	public Schedule(JSONObject schedule) {
		this.day = (Integer) schedule.get("day");
		this.utc = (String) schedule.get("utc");
		this.flight = (String) schedule.get("flight");
	}
}
