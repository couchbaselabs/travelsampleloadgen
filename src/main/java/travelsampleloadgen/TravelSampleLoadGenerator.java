package travelsampleloadgen;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.json.simple.parser.ParseException;

import travelsampleloadgen.loadgenerator.LoadGenerator;
import travelsampleloadgen.loadgenerator.MobileLoadGenerator;
import travelsampleloadgen.loadgenerator.SDKLoadGenerator;
import travelsampleloadgen.service.CouchbaseService;
import travelsampleloadgen.util.Constants;

public class TravelSampleLoadGenerator {
	private static boolean mobile;

	public static void main(String[] args) {
		try {
			TravelSampleLoadGenerator.getOptions(args);
		} catch (org.apache.commons.cli.ParseException e1) {
			e1.printStackTrace();
		}
		try {
			if (!mobile) {
				LoadGenerator loadGen = new SDKLoadGenerator();
				loadGen.generate();
			} else {
				LoadGenerator loadGen = new MobileLoadGenerator();
				loadGen.generate();
			}

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				CouchbaseService.closeCouchbaseConnections();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public static void getOptions(String[] args) throws org.apache.commons.cli.ParseException {
		Options options = new Options();
		Option loadGenPropertiesFilePath = Option.builder().desc("Loadgenerator Properties File path").hasArg()
				.longOpt("loadgen-properties").required(false).argName("file-path").build();
		Option sampleDataFilePath = Option.builder().desc("Loadgen sample data file path").hasArg()
				.longOpt("sample-data-file").required(true ).argName("file-path").build();
		options.addOption(loadGenPropertiesFilePath);
		options.addOption(sampleDataFilePath);
		options.addOption("m", "mobile", false, "Load generate through Mobile sync gateway");
		CommandLineParser parser = new DefaultParser();
		CommandLine commandLine = parser.parse(options, args);
		Constants constants = Constants.getInstance();
		if (commandLine.hasOption("loadgen-properties")) {
			String filePath = commandLine.getOptionValue("loadgen-properties");
			constants.setLoadgenPropertiesFile(filePath);
		}
		if (commandLine.hasOption("sample-data-file")) {
			constants.setTravelSampleDataFilePath(commandLine.getOptionValue("sample-data-file"));
		}
		if (commandLine.hasOption('m')) {
			mobile = true;
		} else {
			mobile = false;
		}
	}

}
