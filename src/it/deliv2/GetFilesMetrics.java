package it.deliv2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map.Entry;

import it.deliv2.helpers.Filenames;
import it.deliv2.helpers.Git;
import it.deliv2.metrics.Authors;
import it.deliv2.metrics.ChgSet;
import it.deliv2.metrics.Loc;

public class GetFilesMetrics {

	private static Git gitManager;
	private static List<HashMap<String, Loc>> filesLocInfo;
	private static List<HashMap<String, Integer>> filesRevisions;
	private static List<HashMap<String, Authors>> filesAuthors;
	private static List<HashMap<String, ChgSet>> filesChg;
	private static List<String> versionDates;
	
	private static File createFile(String filename) throws IOException {
		File newCSV = new File(filename);
		if (!newCSV.exists() && !newCSV.createNewFile()) {
				//Log error
				Logger logger = Logger.getLogger("metrics");
				logger.log(Level.SEVERE, "Cannot save progress");
		}
		return newCSV;
	}
	
	//Save the file information to a csv
	private static void saveToCSV(String fileName) throws IOException {
		
		//Create file if it does not exist
		File newCSV = createFile(fileName);
		
		
		try (FileWriter fw = new FileWriter(newCSV)) {
			
			//First line
			fw.write("Version, File, size, LOC_touched, LOC_added, MAX_LOC_Added, AVG_LOC_Added, Churn, MAX_Churn, AVG_Churn, ChgSetSize, MAX_ChgSet, AVG_CHGSET, NR, NAuth\n");
			
			//Iterate trought versions
			for (int i = 0;  i < filesLocInfo.size(); i++) {
				
				//Iterate trought single files
				for (Entry<String, Loc> entry : filesLocInfo.get(i).entrySet()) { 
					
					Loc infoLoc = entry.getValue();
					
					//Lower bound of theese values must be 0
					int size = (infoLoc.getTotalAdded()-infoLoc.getTotalRemoved());
					if (size < 0)
						size = 0;
					if (infoLoc.getTotalChurn() < 0)
						infoLoc.setTotalChurn(0);

					if (infoLoc.getAverageChurn() < 0)
						infoLoc.setAverageChurn(0);

					if (infoLoc.getMaxChurn() < 0)
						infoLoc.setMaxChurn(0);
					
					//Write all metrics
					ChgSet infoChg = filesChg.get(i).get(entry.getKey());
					fw.write(i+1 + ", " + entry.getKey() + ", " + size + ", " +
					(infoLoc.getTotalAdded()+infoLoc.getTotalRemoved()) + ", "+ infoLoc.getTotalAdded() + ", " + 
					infoLoc.getMaxAdded() + ", " + infoLoc.getAverageAdded() + ", " + 
					infoLoc.getTotalChurn() + ", " + infoLoc.getMaxChurn() + ", " + infoLoc.getAverageChurn() + ", " +
					infoChg.getTotalChg() + ", " + infoChg.getMaxChg() + ", " + infoChg.getAverageChg() + ", " +
					filesRevisions.get(i).get(entry.getKey()) + ", " +
					filesAuthors.get(i).get(entry.getKey()).getTotal() + "\n");
					
				}
				
			}
		}
		
		
	}
	
	//Initialize version dates taking values from the file
	private static void initVersionDates(String fileName) throws FileNotFoundException {
		
		File versionCSV = new File(fileName);
		
		try (Scanner fr = new Scanner(versionCSV)) {
			
			versionDates = new ArrayList<>();
			
			//Throw away first line
			fr.nextLine();
			
			while (fr.hasNextLine()) {
	          String line = fr.nextLine();
	          String[] splitted = line.split(",");
	          versionDates.add(splitted[3]);
	        }
		}
		
		
	}
	
	//Function that updates the number of authors of a list of files in a specific version
	private static void updateAuthors(List<String> files, String author, int version) {
		
		//Iterate trhoug files
		for (int h = 0; h < files.size(); h++) {
			
			String curr = files.get(h);
			Authors currAuth = filesAuthors.get(version).get(curr);
			
			//Check if file is in HASH
			if (currAuth == null) {
				//Add it if its not
				Authors newAuth = new Authors();
				newAuth.addAuthor(author);
				filesAuthors.get(version).put(curr, newAuth);
			}
			else {
				//Check if author has been already counted
				if (!currAuth.isInList(author)) {
					currAuth.addAuthor(author);
					filesAuthors.get(version).put(curr, currAuth);
				}
			
			}
		}
	}
	
	//Function that updates the ChgSet of a list of files in a specific version
	private static void updateChg(List<String> files, int version) {
		
		//Every file has a chg of files.size()-1
		int chgToAdd = files.size()-1; 
		
		//Iterate trough files
		for (int h = 0; h < files.size(); h++) {
			String curr = files.get(h);
			ChgSet currChg = filesChg.get(version).get(curr);
			
			//Check if file is in HASH
			if (currChg == null) {
			
				//Add it if its not
				ChgSet newChg = new ChgSet();
				newChg.addTotal(chgToAdd);
				newChg.setMaxChg(chgToAdd);
				newChg.setAverageChg(chgToAdd);
				
				//Update structure
				filesChg.get(version).put(curr, newChg);
			}
			else {
				
				//Update statistics
				currChg.addTotal(chgToAdd);
				if (currChg.getMaxChg() < chgToAdd)
					currChg.setMaxChg(chgToAdd);
				currChg.setAverageChg((int) Math.round(currChg.getTotalChg()*1.0/currChg.getUpdates()*1.0));
				
				//Update structure
				filesChg.get(version).put(curr, currChg);
			}
		}
		
		
	}
	
	//Functions that updates the Loc of a file in a specific version
	private static void updateLoc(int added, int removed, String filename, int version) {
		
		Loc toAdd = filesLocInfo.get(version).get(filename);
		
		int churn = added-removed;
		
		//Check if the file has been seen before
		if (toAdd == null) {
			//if its not i set all the values
			
			toAdd = new Loc();
			toAdd.addTotalAdded(added);
			toAdd.addTotalRemoved(removed);
			toAdd.addTotalChurn(churn);
			
			toAdd.setMaxChurn(churn);
			toAdd.setMaxAdded(added);
			toAdd.setMaxRemoved(removed);
			
			toAdd.setAverageAdded(added);
			toAdd.setAverageRemoved(removed);
			toAdd.setAverageChurn(churn);
			
			toAdd.increaseUpdates();
			
			//Update the structure
			filesLocInfo.get(version).put(filename, toAdd);
		} else {
			//if it is we update the values un hashmap
			
			//Add totals
			toAdd.addTotalAdded(added);
			toAdd.addTotalRemoved(removed);
			toAdd.addTotalChurn(churn);
			toAdd.increaseUpdates();
			
			//Calculate max
			if (toAdd.getMaxChurn() < churn)
				toAdd.setMaxChurn(churn);

			if (toAdd.getMaxAdded() < added)
				toAdd.setMaxAdded(added);

			if (toAdd.getAverageRemoved() < removed)
				toAdd.setMaxRemoved(removed);
		
			//Calculate averages
			toAdd.setAverageAdded((int) Math.round(toAdd.getTotalAdded()*1.0/toAdd.getUpdates()*1.0));
			toAdd.setAverageRemoved((int) Math.round(toAdd.getTotalRemoved()*1.0/toAdd.getUpdates()*1.0));
			toAdd.setAverageChurn((int) Math.round(toAdd.getTotalChurn()*1.0/toAdd.getUpdates()*1.0));
			
			//Add info to structure
			filesLocInfo.get(version).put(filename, toAdd);
			
		}
		
		
	}
	
	//Function that analizes the version
	private static void analizeVersion(int version) throws IOException {
		
		//Initialize HashMap
		filesRevisions.add(new HashMap<>());
		filesAuthors.add(new HashMap<>());
		filesChg.add(new HashMap<>());
		filesLocInfo.add(new HashMap<>());
		
		//Log progress
		Logger logger = Logger.getLogger("metrics");
		logger.log(Level.INFO, "Analized version {0}", version);
		
		//Get all commits in the version
		List<String> info = gitManager.getFilesModifiedBeforeDate(versionDates.get(version));
		List<String> currFiles = new ArrayList<>();
		
		//Analize commits one by one
		for (int j = 0; j < info.size(); j++) {
			
			String curr = info.get(j);
			
			//Check if author or filename
			if (info.get(j).startsWith("'")) {
				
				//If it is an autor, it means one commit is over, we update Authors and chgset
				updateAuthors(currFiles, info.get(j), version);
				updateChg(currFiles, version);
				
				currFiles = new ArrayList<>();
				
			} else {
				
				//Separate filename from lines added and removed
				String[] values = curr.split("\t");
				
				//Try catch beacuse some files have no info
				try {
					
					int added = Integer.parseInt(values[0]);
					int removed = Integer.parseInt(values[1]);
					
					//Ignore movement of files
					if (!values[2].contains("=>")) {
						//Update count of revisions
						if (filesRevisions.get(version).get(values[2]) == null)
							filesRevisions.get(version).put(values[2], 1);
						else
							filesRevisions.get(version).put(values[2], filesRevisions.get(version).get(values[2])+1);
						
						//Add to file list
						currFiles.add(values[2]);
						
						//Update LOC
						updateLoc(added, removed, values[2], version);
					}

				} catch(NumberFormatException e) {
					//Added and removed not available
					//do nothing
				}

			}
		}
	}
	
	
	//Function that calculates the metrics
	private static void getAllModifications() throws IOException {
		
		//Initialize all the structures to hold the calculations
		filesRevisions = new ArrayList<>();
		filesAuthors = new ArrayList<>();
		filesChg = new ArrayList<>();
		filesLocInfo = new ArrayList<>();
		
		for (int i = 0; i < versionDates.size(); i++) {
			analizeVersion(i);
		}
		
		
	}

	
	public static void main(String[] args) throws IOException {

		gitManager = new Git(Filenames.REPO_NAME, "..");
		initVersionDates(Filenames.VERS_FILE);
		
		//Read all commits
		getAllModifications();
		
		//Save to file
		saveToCSV(Filenames.METRICS_FILE);
	}

}
