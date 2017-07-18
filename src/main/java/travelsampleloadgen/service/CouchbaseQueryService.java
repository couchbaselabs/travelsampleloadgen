package travelsampleloadgen.service;

import static com.couchbase.client.java.query.dsl.Expression.i;
import static com.couchbase.client.java.query.dsl.Expression.s;
import static com.couchbase.client.java.query.dsl.Expression.x;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.Select;
import com.couchbase.client.java.query.Statement;
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
