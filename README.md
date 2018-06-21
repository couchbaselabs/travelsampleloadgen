# travelsampleloadgen
## Requirements:
  1. Java jdk installed on the system
  2. Maven build system to build the tool

## Building the loadgenerator:
   Use Maven to build the project by running following command from source folder:
      mvn package

## Make changes to LoadgenProperties.json
  LoadgenProperties.json contains the information required for running the loadgen. Following properties can be changed before running the tool.Make the changes after building the project under target/ folder
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
     
## Running different versions of SDK
  To run different version of SDK, change the branch to corresponding version and build the project again.
  
## Adding new version of SDK
  To add new versions of SDK, create a new branch with the sdk version as the name. Change the couchbase client version in pom.xml to get the corresponding version of the SDK client. Build the project again.
