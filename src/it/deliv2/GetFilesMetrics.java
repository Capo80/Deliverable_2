package it.deliv2;

import java.awt.BufferCapabilities.FlipContents;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Map.Entry;

public class GetFilesMetrics {

	private static Git gitManager;
	private static String projName = "BOOKKEEPER";
	private static String versionFileName = projName + "VersionInfo.csv";
	private static String repoURL = "https://github.com/Capo80/"+projName.toLowerCase()+".git";
	private static List<HashMap<String, Loc>> filesLocInfo;
	private static List<HashMap<String, Integer>> filesRevisions;
	private static List<HashMap<String, Authors>> filesAuthors;
	private static List<HashMap<String, ChgSet>> filesChg;
	private static List<String> versionDates;
	private static List<Integer> versionIDs;
	
	//Save the file information to a csv
	private static void saveToCSV(String fileName) throws IOException {
		
		File newCSV = new File(fileName);
		if (newCSV.exists())
			newCSV.delete();
		
		newCSV.createNewFile();
		
		try (FileWriter fw = new FileWriter(newCSV)) {
			
			fw.write("Version, File, size, LOC_touched, LOC_added, MAX_LOC_Added, AVG_LOC_Added, Churn, MAX_Churn, AVG_Churn, ChgSetSize, MAX_ChgSet, AVG_CHGSET, NR, NAuth\n");
			
			//Iterate trought versions
			for (int i = 0;  i < filesLocInfo.size(); i++) {
				
				//Iterate trought single files
				for (Entry<String, Loc> entry : filesLocInfo.get(i).entrySet()) { 
					//System.out.println(entry.getValue());
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
	
	private static void initVersionDates(String fileName) throws FileNotFoundException {
		
		File versionCSV = new File(fileName);
		
		try (Scanner fr = new Scanner(versionCSV)) {
			
			versionDates = new ArrayList<>();
			versionIDs = new ArrayList<>();
			//Throw away first line
			fr.nextLine();
			
			while (fr.hasNextLine()) {
	          String line = fr.nextLine();
	          String[] splitted = line.split(",");
	          versionIDs.add(Integer.parseInt(splitted[0]));
	          versionDates.add(splitted[3]);
	        }
		}
		
		
	}
	
	private static void updateAuthors(List<String> files, String author, int version) {
		
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
	
	private static void updateChg(List<String> files, int version) {
		
		//Every file has a chg of files.size()-1
		int chgToAdd = files.size()-1; 
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
				filesChg.get(version).put(curr, newChg);
			}
			else {
				//Update statistics
				currChg.addTotal(chgToAdd);
				if (currChg.getMaxChg() < chgToAdd)
					currChg.setMaxChg(chgToAdd);
				currChg.setAverageChg((int) Math.round(currChg.getTotalChg()*1.0/currChg.getUpdates()*1.0));
				filesChg.get(version).put(curr, currChg);
			}
		}
		
		
	}
	
	private static void updateLoc(int added, int removed, String filename, int version) {
		
		Loc toAdd = filesLocInfo.get(version).get(filename);
		
		int churn = added-removed;
		//System.out.println(filename + " " + values[0] + " " + values[1]);
		//Check if the file has been seen before
		if (toAdd == null) {
			//if not i set all the values
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
			filesLocInfo.get(version).put(filename, toAdd);
		} else {
			//if it is we update the values un hashmap
			toAdd.addTotalAdded(added);
			toAdd.addTotalRemoved(removed);
			toAdd.addTotalChurn(churn);
			toAdd.increaseUpdates();
			
			if (toAdd.getMaxChurn() < churn)
				toAdd.setMaxChurn(churn);

			if (toAdd.getMaxAdded() < added)
				toAdd.setMaxAdded(added);

			if (toAdd.getAverageRemoved() < removed)
				toAdd.setMaxRemoved(removed);
		
			toAdd.setAverageAdded((int) Math.round(toAdd.getTotalAdded()*1.0/toAdd.getUpdates()*1.0));
			toAdd.setAverageRemoved((int) Math.round(toAdd.getTotalRemoved()*1.0/toAdd.getUpdates()*1.0));
			toAdd.setAverageChurn((int) Math.round(toAdd.getTotalChurn()*1.0/toAdd.getUpdates()*1.0));
			
			filesLocInfo.get(version).put(filename, toAdd);
			
		}
		
		
	}
	private static void getAllModifications() throws IOException {
		
		filesRevisions = new ArrayList<HashMap<String, Integer>>();
		filesAuthors = new ArrayList<HashMap<String,Authors>>();
		filesChg = new ArrayList<HashMap<String,ChgSet>>();
		filesLocInfo = new ArrayList<HashMap<String, Loc>>();
		
		for (int i = 0; i < versionDates.size(); i++) {
			
			//Initialize HashMap
			filesRevisions.add(new HashMap<String, Integer>());
			filesAuthors.add(new HashMap<String, Authors>());
			filesChg.add(new HashMap<String, ChgSet>());
			filesLocInfo.add(new HashMap<String, Loc>());
			
			System.out.println("\n---\n" + i + "\n-----");
			
			List<String> info = gitManager.getFilesModifiedBeforeDate(versionDates.get(i));
			List<String> curr_files = new ArrayList<String>();
			
			for (int j = 0; j < info.size(); j++) {
				
				String curr = info.get(j);
				System.out.println(curr);
				
				//Check if author or filename
				if (info.get(j).startsWith("'")) {
					System.out.println("Auth: " + info.get(j));
					updateAuthors(curr_files, info.get(j), i);
					updateChg(curr_files, i);
					
					//System.out.println("-----");
					//for(int m = 0; m < curr_files.size(); m++)
					//	System.out.println(curr_files.get(m));
					//System.out.println(curr_files.size());
					//System.out.println("-----");
					curr_files = new ArrayList<String>();
				} else {					
					//Count revision
					String[] values = curr.split("\t");
					try {
						int added = Integer.parseInt(values[0]);
						int removed = Integer.parseInt(values[1]);
						
						//Ignore movement of files
						if (!values[2].contains("=>")) {
							//Update count of revisions
							if (filesRevisions.get(i).get(values[2]) == null)
								filesRevisions.get(i).put(values[2], 1);
							else
								filesRevisions.get(i).put(values[2], filesRevisions.get(i).get(values[2])+1);
							
							//Add to file list
							curr_files.add(values[2]);
							
							//Update LOC
							updateLoc(added, removed, values[2], i);
						}

					} catch(NumberFormatException e) {
						//Added and removed not available
						//do nothing
					}

				}
			}
		}
		
		
	}

	
	public static void main(String[] args) throws IOException {

		gitManager = new Git(repoURL, "..");
		initVersionDates(versionFileName);
		
		getAllModifications();
		for (int i = 0;  i < filesRevisions.size(); i++) {
			
			//Iterate trought single files
			for (Entry<String, Integer> entry : filesRevisions.get(i).entrySet()) { 
				//System.out.println(entry.getValue());
				//System.out.println(entry.getKey() + " " + entry.getValue());
			}
			
		}
		for (int i = 0;  i < filesAuthors.size(); i++) {
			
			//Iterate trought single files
			for (Entry<String, Authors> entry : filesAuthors.get(i).entrySet()) { 
				//System.out.println(entry.getValue());
				//System.out.println(entry.getKey() + " " + entry.getValue().getTotal());
				//entry.getValue().printAuth();
				//System.out.println(entry.getValue().getTotal());
				//System.out.println("---");
			}
			
		}
		
		for (int i = 0;  i < filesChg.size(); i++) {
			
			//Iterate trought single files
			for (Entry<String, ChgSet> entry : filesChg.get(i).entrySet()) { 
				//System.out.println(entry.getValue());
				//System.out.println(entry.getKey() + " " + entry.getValue().getTotal());
				System.out.println(entry.getKey() + " " + entry.getValue().getTotalChg() + " " + entry.getValue().getMaxChg() + " "+ entry.getValue().getAverageChg() + " " + entry.getValue().getMaxChg());
			}
			
		}
		
		

		saveToCSV(projName+"metrics.csv");
	}

}
