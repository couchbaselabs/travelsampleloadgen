package travelsampleloadgen.service;

import static com.couchbase.client.java.query.dsl.Expression.i;
import static com.couchbase.client.java.query.dsl.Expression.s;
import static com.couchbase.client.java.query.dsl.Expression.x;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.couchbase.client.java.query.N1qlQuery;
import com.couchbase.client.java.query.N1qlQueryResult;
import com.couchbase.client.java.query.N1qlQueryRow;
import com.couchbase.client.java.query.Select;
import com.couchbase.client.java.query.Statement;
import com.google.gson.Gson;

public class CouchbaseCURDService {
	private Bucket bucket;
	
	public CouchbaseCURDService() throws FileNotFoundException, IOException, ParseException {
		this.bucket = CouchbaseService.getInstance().getBucket();
	}
	
	public void closeCouchbase() throws FileNotFoundException, IOException, ParseException {
		CouchbaseService.closeCouchbaseConnections();
	}
	
	public boolean insertToBucket(JSONObject json, String document_name) {
		JsonObject jsonObject = JsonObject.fromJson(json.toJSONString());
		JsonDocument document = JsonDocument.create(document_name, jsonObject);
		try {
			this.bucket.insert(document);
			return true;
		} catch (Exception e) {
			System.out.println(document_name);
			System.out.println(e);
			return false;
		}
	}
	
	public boolean updateToBucket(JSONObject json, String document_name) {
		JsonDocument document = bucket.get(document_name);
        if (document == null) {
        		throw new DocumentDoesNotExistException("Document " + document_name + " does not exist in the bucket");
        }
		JsonObject jsonObject = JsonObject.fromJson(json.toJSONString());
		document = JsonDocument.create(document_name, jsonObject);
		try {
			this.bucket.upsert(document);
			return true;
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
	}
	
	public boolean deleteFromBucket(String document_name) {
		JsonDocument document = bucket.get(document_name);
		if (document == null) {
			throw new DocumentDoesNotExistException("Document " + document_name + " does not exist in the bucket");
		}
		try {
			this.bucket.remove(document);
			return true;
		} catch (Exception e) {
			System.out.println(e);
			return false;
		}
	}
	
	public boolean checkIfDocumentExists(String documentId) {
		return bucket.exists(documentId);
	}
	
	public JSONObject getDocumentById(String documentId) throws ParseException {
		JsonDocument document = bucket.get(documentId);
		if(document == null) {
			return null;
		}
		JSONParser parser = new JSONParser();
		JSONObject jsonObject = (JSONObject) parser.parse(document.content().toString());
		return jsonObject;
	}
	
	public JSONArray getExistingDocumentIdsFromBucket(String type) {
		JSONArray result = new JSONArray();
		Statement statement = Select.select("id").from(i(this.bucket.name())).where(x("type").eq(s(type)));
		N1qlQueryResult n1qlresult = this.bucket.query(N1qlQuery.simple(statement));
		for (N1qlQueryRow row : n1qlresult) {
			result.add(row.value());
		}
		return result;
	}
}
