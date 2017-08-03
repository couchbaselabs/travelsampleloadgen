package travelsampleloadgen.loadgenerator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.simple.parser.ParseException;

import travelsampleloadgen.util.Constants;
import travelsampleloadgen.util.Utils;

public class LoadGeneratorMainThread extends Thread {

	long numberOfOps;
	private long counter;
	private long masterSeed;
	private int numberOfThreads;
	private boolean mobile;
	private SharedWorkerData sharedWorkerData;
	private Utils util = new Utils();

	public LoadGeneratorMainThread(boolean mobile) throws FileNotFoundException, IOException, ParseException {
		this(Constants.getInstance().getLoadgenPropertiesFile(), Constants.getInstance().getTravelSampleDataFilePath());
		this.mobile = mobile;
	}

	private LoadGeneratorMainThread(String propertiesFile, String inputDataFile)
			throws FileNotFoundException, IOException, ParseException {
		this.setName("LoadGeneratorMainThread");
		Constants.getInstance().initializeLoadgenConstants();
		this.numberOfOps = Constants.numberOfOps;
		this.numberOfThreads = ((Long) Constants.threads).intValue();
		this.masterSeed = System.currentTimeMillis();
		this.util.setSeed(masterSeed);
		this.sharedWorkerData = new SharedWorkerData();
	}

	public void run() {
		ExecutorService executor = Executors.newFixedThreadPool(this.numberOfThreads);
		this.counter = 0;
		while (this.counter < this.numberOfOps) {
			if (this.counter % (this.numberOfThreads * 5) == 0) {
				executor.shutdown();
				while (!executor.isTerminated()) {
				}
				this.sharedWorkerData.currentCreateIds.clear();
				executor = Executors.newFixedThreadPool(this.numberOfThreads);
			}
			Runnable worker;
			long threadSeed = this.masterSeed + this.counter;
			try {
				if (this.mobile) {

					worker = new MobileLoadGeneratorThread(threadSeed, this.sharedWorkerData);

				} else {
					worker = new SDKLoadGeneratorThread(threadSeed, this.sharedWorkerData);
				}
				executor.execute(worker);
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
			this.counter++;
		}
		executor.shutdown();
		while (!executor.isTerminated()) {
		}
		try {
			this.storeLoadgenStats();
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

	private void storeLoadgenStats() throws FileNotFoundException, IOException, ParseException {
		Map<String, List<LoadgenStats>> loadgenStatsToStore = new HashMap<String, List<LoadgenStats>>();
		List<LoadgenStats> list = new ArrayList<LoadgenStats>();
		LoadgenStats loadgenStats = new LoadgenStats();
		loadgenStats.masterSeed = this.masterSeed;
		loadgenStats.numberOfOps = this.counter;
		loadgenStats.creates = this.sharedWorkerData.numberOfCreates;
		loadgenStats.updates = this.sharedWorkerData.numberOfUpdates;
		loadgenStats.deletes = this.sharedWorkerData.numberOfDeletes;
		loadgenStats.mobile = this.mobile;
		list.add(loadgenStats);
		loadgenStatsToStore.put("LoadgenData", list);
		String loadGenStatsFilePath = Constants.loadgen_stats_file;
		util.updateLoadgenDataToFiles(loadGenStatsFilePath, loadgenStatsToStore);
	}

	class LoadgenStats {
		long masterSeed;
		long numberOfOps;
		long creates;
		long updates;
		long deletes;
		boolean mobile;
	}
}
