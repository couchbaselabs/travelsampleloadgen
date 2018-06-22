package travelsampleloadgen.service;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;

import com.couchbase.client.core.logging.CouchbaseLoggerFactory;
import com.couchbase.client.core.logging.RedactionLevel;

import com.couchbase.client.crypto.JceksKeyStoreProvider;
import com.couchbase.client.crypto.RSACryptoProvider;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.*;

import com.couchbase.client.crypto.EncryptionConfig;
import com.couchbase.client.java.document.json.ValueEncryptionConfig;
import com.couchbase.client.java.encryption.EncryptionProvider;

import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;

import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.cluster.AuthDomain;
import com.couchbase.client.java.cluster.UserRole;
import com.couchbase.client.java.cluster.UserSettings;

import travelsampleloadgen.util.Constants;

public class CouchbaseService {
	private Bucket bucket;
	private CouchbaseCluster couchbaseCluster;
	private RSACryptoProvider rsaCryptoProvider;
	private ValueEncryptionConfig rsaConfig = new ValueEncryptionConfig(EncryptionProvider.RSA);
	
	/**
	 * @return the bucket
	 */
	public Bucket getBucket() {
		return bucket;
	}

	public ValueEncryptionConfig getValueCryptoConfig() {
		return rsaConfig;
	}

	public static CouchbaseService instance;
	
	private CouchbaseService() throws FileNotFoundException, IOException, ParseException, Exception {
		Constants constants = Constants.getInstance();
		constants.initializeLoadgenConstants();
		String hostName = Constants.couchbase_host;
		List<String> hostNames = new ArrayList<String>(Arrays.asList(hostName.split(",")));
		String bucketName = Constants.bucket;
		String bucketPassword = Constants.bucket_password;

		// encryption
		JceksKeyStoreProvider kp = new JceksKeyStoreProvider("secret");

		String rsaKeyName = "RSAtestkey";
		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(2048);
		KeyPair keyPair = keyPairGenerator.generateKeyPair();
		PublicKey pubKey = keyPair.getPublic();
		PrivateKey privateKey = keyPair.getPrivate();
		kp.storeKey(rsaKeyName, pubKey.getEncoded(), privateKey.getEncoded());

		rsaCryptoProvider = new RSACryptoProvider(rsaKeyName);
		rsaCryptoProvider.setKeyName(rsaKeyName);
		rsaCryptoProvider.setKeyStoreProvider(kp);

		EncryptionConfig encryptionConfig = new EncryptionConfig();
		encryptionConfig.addCryptoProvider(rsaCryptoProvider);

		// x509 cert
		String username = "sdkqecertuser";
		String host = hostNames.get(0);
		String clusterVersion = "5.5.0"; // add functionality to grab cluster version
		initCert(username, host, clusterVersion);
		createAdminUser(username, "admin", host);

		CouchbaseEnvironment environment = DefaultCouchbaseEnvironment
				.builder()
				.sslEnabled(true)
				.sslKeystoreFile("cert/keystore.jks")
				.sslKeystorePassword("123456")
				.connectTimeout(50000)
				.computationPoolSize(5)
				.bootstrapCarrierSslPort(11207)
				.certAuthEnabled(true)
				.bootstrapHttpDirectPort(8091)
				.bootstrapHttpSslPort(18091)
				.bootstrapCarrierDirectPort(11210)
				.encryptionConfig(encryptionConfig)
				.build();

		this.couchbaseCluster = CouchbaseCluster.create(environment, hostNames);

		this.bucket = couchbaseCluster.openBucket(bucketName, bucketPassword);

		// turn bucket compression to active

		//Thread.sleep(10000);

		// set log redaction to full
		CouchbaseLoggerFactory.setRedactionLevel(RedactionLevel.FULL);
	}
	
	public static CouchbaseService getInstance() throws FileNotFoundException, IOException, ParseException, Exception {
		if(instance == null) {
			instance = new CouchbaseService();
		}
		return instance;
	}
	
	public static void closeCouchbaseConnections() throws FileNotFoundException, IOException, ParseException, Exception {
		if (instance == null) {
			instance = getInstance();
		}
		instance.bucket.close();
		instance.couchbaseCluster.disconnect();
	}

	public static int initCert(String username, String host, String clusterVersion) {
		ProcessBuilder builder = new ProcessBuilder();
		builder.command("./gen_keystore.sh", host, username, clusterVersion);
		builder.directory(new File("cert"));
		int exitcode = 1;
		StringBuffer output = new StringBuffer();

		// generate cert and install on couchbase server
		try {
			Process p = builder.start();
			BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String line = "";
			while ((line = reader.readLine()) != null) {
				output.append(line + "\n");
			}
			exitcode = p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (output.length() > 0) {
			System.out.printf("%s\n", output);
		}
		return exitcode;
	}

	public static boolean createAdminUser(String username, String role, String host) {
		Cluster cluster = CouchbaseCluster.create(host);
		cluster.authenticate("Administrator", "password");
		try {
			cluster.clusterManager().upsertUser(AuthDomain.LOCAL, username, UserSettings.build().password("password").roles(
					Arrays.asList(new UserRole(role))
			));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			//TODO: find a way to close cluster gracefully without intefering the operation on the fly
		}
		return true;
	}

}
