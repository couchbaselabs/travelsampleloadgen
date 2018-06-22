# travelsampleloadgen

## NOTE: 
   This branch is for testing the latest, unreleased version of the Couchbase Java SDK. You will be required to pull down repos and build artifacts locally as they are not in Maven Central.

## Requirements:
  1. Java JDK
  2. Maven 

## Building the loadgenerator:
    mkdir ./loadgen
    cd ./loadgen
    git clone https://github.com/couchbase/couchbase-jvm-core.git
    cd ./couchbase-jvm-core
    mvn clean
    mvn versions:set -DnewVersion=999.999.999-SNAPSHOT
    mvn install -Dmaven.test.skip
    cd ..
    git clone https://github.com/couchbase/java-couchbase-encryption
    cd java-couchbase-encryption
    mvn clean
    mvn versions:set -DnewVersion=999.999.999-SNAPSHOT
    mvn install -Dmaven.test.skip
    cd ..
    git clone https://github.com/couchbase/couchbase-java-client.git
    mvn clean
    mvn versions:set -DnewVersion=999.999.999-SNAPSHOT
    mvn install -Dmaven.test.skip -Dcore.version=999.999.999-SNAPSHOT 
    cd ..
    git clone https://github.com/bharath-gp/travelsampleloadgen
    cd travelsampleloadgen
    mvn package

## LoadgenProperties.json
  The ./target/LoadgenProperties.json contains information required for the loadgen to run. The following properties can be changed before running the tool and after building the project.
   1. "NumberOfOps": Number of operations to perform (int)
   2. "Creates" : Percentage of creates (int)
   3. "Updates" : Percentage of updates (int)
   4. "Deletes" : Percentage of deletes (int)
   5. "couchbase-host": IP address of the couchbase host(s). Ex: "10.111.170.101,10.111.170.102"
   6. "couchbase-admin-username": Couchbase administrator username,
   7. "couchbase-admin-password": Couchbase administrator password,
Note that the sum of creates, updates and deletes should equal 100, else the app will make some adjustments when possible else will error out.
You can also create a different copy of the loadgen properties file and pass them when running the application.
    
## Running the application
  1. Create an empty json file at /tmp/loadgenseeds.json
     echo "{}" > /tmp/loadgenseeds.json
  2. Change directory to the target folder after building the project
  3. Run the application using java
     java TravelSampleLoadGenerator.jar --loadgen-properties LoadgenProperties.json  --sample-data-file TravelSampleData.json

## Branch features
  In addition to creating load for the travel sample bucket, this branch has the following functionality:
  1. x509 is enabled before loadgen start.
  2. Field level encryption happens for some fields during inserts and updates.
  3. Log redaction is set to FULL.
  4. Compression will happen by default if server is 5.5+
  5. Traceability will happen by default if server is 5.5+
  
## TODO

  1. Add verification for encryption
  2. Add verification for log redaction
  3. Add verification for compression
  4. Add ability to trigger traceability
    
  