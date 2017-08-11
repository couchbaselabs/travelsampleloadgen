package travelsampleloadgen.service;

import static com.couchbase.client.java.query.Select.select;
import static com.couchbase.client.java.query.dsl.Expression.i;
import static com.couchbase.client.java.query.dsl.Expression.s;
import static com.couchbase.client.java.query.dsl.Expression.x;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Calendar;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.Select;
import com.couchbase.client.java.query.Statement;
import com.couchbase.client.java.query.dsl.Sort;
import com.couchbase.client.java.query.dsl.path.AsPath;

public class CouchbaseQueryService {
	private Bucket bucket;

	public CouchbaseQueryService() throws FileNotFoundException, IOException, ParseException {
		this.bucket = CouchbaseService.getInstance().getBucket();
	}

	public JSONArray getExistingDocumentIdsFromBucket(String type) throws ParseException {
		Statement statement = Select.select("id").from(i(this.bucket.name())).where(x("type").eq(s(type)));
		N1qlQueryResult n1qlresult = this.bucket.query(N1qlQuery.simple(statement));
		return getJSONArrayFromResults(n1qlresult);
	}

	public JSONArray getMaxId(String type) throws ParseException {
		Statement statement = Select.select(x("max(id)").as("id")).from(i(this.bucket.name())).where(x("type").eq(s(type)));
		N1qlQueryResult n1qlresult = this.bucket.query(N1qlQuery.simple(statement));
		return getJSONArrayFromResults(n1qlresult);
	}

	public JSONArray getMinId(String type) throws ParseException {
		Statement statement = Select.select(x("min(id)").as("id")).from(i(this.bucket.name())).where(x("type").eq(s(type)));
		N1qlQueryResult n1qlresult = this.bucket.query(N1qlQuery.simple(statement));
		return getJSONArrayFromResults(n1qlresult);
	}
	
	public JSONArray getAllAirport(String faa) throws ParseException {
		Statement query;
		AsPath prefix = select("airportname").from(i(this.bucket.name()));
        if (faa.length() == 3) {
            query = prefix.where(x("faa").eq(s(faa.toUpperCase())));
        } else if (faa.length() == 4 && (faa.equals(faa.toUpperCase()) || faa.equals(faa.toLowerCase()))) {
            query = prefix.where(x("icao").eq(s(faa.toUpperCase())));
        } else {
            query = prefix.where(i("airportname").like(s(faa + "%")));
        }
        N1qlQueryResult result = this.bucket.query(N1qlQuery.simple(query));
        return getJSONArrayFromResults(result);
	}
	
	public JSONArray findAllPath(String from, String to, Calendar leave) throws ParseException{
		Statement query = select(x("faa").as("fromAirport"))
	            .from(i(this.bucket.name()))
	            .where(x("airportname").eq(s(from)))
	            .union()
	            .select(x("faa").as("toAirport"))
	            .from(i(this.bucket.name()))
	            .where(x("airportname").eq(s(to)));

	        N1qlQueryResult result = this.bucket.query(N1qlQuery.simple(query));

	        if (!result.finalSuccess()) {
	            return null;
	        }

	        String fromAirport = null;
	        String toAirport = null;
	        for (N1qlQueryRow row : result) {
	            if (row.value().containsKey("fromAirport")) {
	                fromAirport = row.value().getString("fromAirport");
	            }
	            if (row.value().containsKey("toAirport")) {
	                toAirport = row.value().getString("toAirport");
	            }
	        }

	        Statement joinQuery = select("a.name", "s.flight", "s.utc", "r.sourceairport", "r.destinationairport", "r.equipment")
	            .from(i(this.bucket.name()).as("r"))
	            .unnest("r.schedule AS s")
	            .join(i(this.bucket.name()).as("a") + " ON KEYS r.airlineid")
	            .where(x("r.sourceairport").eq(s(fromAirport)).and(x("r.destinationairport").eq(s(toAirport))).and(x("s.day").eq(leave.get(Calendar.DAY_OF_WEEK))))
	            .orderBy(Sort.asc("a.name"));

	        N1qlQueryResult otherResult = bucket.query(joinQuery);
	        return getJSONArrayFromResults(otherResult);
	}
	
	@SuppressWarnings("unchecked")
	private JSONArray getJSONArrayFromResults(N1qlQueryResult result) throws ParseException {
		JSONArray jsonArray = new JSONArray();
		JSONParser parser = new JSONParser();
		for (N1qlQueryRow row : result) {
			String jsonString = row.value().toString();
			jsonArray.add(parser.parse(jsonString));
		}
		return jsonArray;
	}
}
