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
		if (!newCSV.exists())
			newCSV.createNewFile();
		
		try (FileWriter fw = new FileWriter(newCSV)) {
			
			fw.write("Version, File, size, LOC_touched, LOC_added, MAX_LOC_Added, AVG_LOC_Added, Churn, MAX_Churn, AVG_Churn, ChgSetSize, MAX_ChgSet, AVG_CHGSET, NR, NAuth\n");
			
			//Iterate trought versions
			for (int i = 0;  i < filesLocInfo.size(); i++) {
				
				//Iterate trought single files
				for (Entry<String, Loc> entry : filesLocInfo.get(i).entrySet()) { 
					//System.out.println(entry.getValue());
					Loc infoLoc = entry.getValue();
					ChgSet infoChg = filesChg.get(i).get(entry.getKey());
					fw.write(i+1 + ", " + entry.getKey() + ", " + (infoLoc.getTotalAdded()-infoLoc.getTotalRemoved()) + ", " +
					(infoLoc.getTotalAdded()+infoLoc.getTotalRemoved()) + ", "+ infoLoc.getTotalAdded() + ", " + 
					infoLoc.getAverageAdded() + ", " + infoLoc.getMaxAdded() + ", " + 
					infoLoc.getTotalChurn() + ", " + infoLoc.getMaxChurn() + ", " + infoLoc.getAverageChurn() + ", " +
					infoChg.getTotalChg() + ", " + infoChg.getMaxChg() + ", " + infoChg.getAverageChg() + ", " +
					filesRevisions.get(i).get(entry.getKey()) + ", " +
					filesAuthors.get(i).get(entry.getKey()).getTotal() +"\n");
					
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
	
	private static int getIDfromDate(String date) {
    	int m = 0;
    	while (m < versionDates.size() && date.compareTo(versionDates.get(m)) > 0) {
    		m++;
    	}
    	if (m >= versionIDs.size())
    		return Issue.FIX_DEF;
    	else
    		return versionIDs.get(m);
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
	private static void getAllModifications() throws IOException {
		
		filesRevisions = new ArrayList<HashMap<String, Integer>>();
		filesAuthors = new ArrayList<HashMap<String,Authors>>();
		filesChg = new ArrayList<HashMap<String,ChgSet>>();
		
		for (int i = 0; i < versionDates.size(); i++) {
			
			//Initialize HashMap
			filesRevisions.add(new HashMap<String, Integer>());
			filesAuthors.add(new HashMap<String, Authors>());
			filesChg.add(new HashMap<String, ChgSet>());
			
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
					if (filesRevisions.get(i).get(curr) == null)
						filesRevisions.get(i).put(curr, 1);
					else
						filesRevisions.get(i).put(curr, filesRevisions.get(i).get(curr)+1);
					
					//Add to file list
					curr_files.add(curr);
				}
			}
		}
		
		
	}
	//Function to obtain the full list of files per version
	private static void getAllFiles() throws IOException {
		
		//Initialize list object
		filesLocInfo = new ArrayList<HashMap<String, Loc>>();
		
		for (int i = 0; i < versionDates.size(); i++) {
			
			//Initialize HashMap
			filesLocInfo.add(new HashMap<String, Loc>());
			
			//Get list of files
			List<String> curr_files = gitManager.getFilesAddedBeforeDate(versionDates.get(i));
			
			
			//System.out.println(curr_files.size());
			
			//Add files to hashmap
			for (int j = 0; j < curr_files.size(); j++) {
				try {	
					Loc info = getLocFile(curr_files.get(j), i+1);
					filesLocInfo.get(i).put(curr_files.get(j), info);
					
					//System.out.println(i+1 + ", " + curr_files.get(j) + ", " + (info.getTotalAdded()-info.getTotalRemoved()) + ", " + (info.getTotalAdded()+info.getTotalRemoved()) + ", "+ info.getTotalAdded() + ", " + info.getAverageAdded() + ", " + info.getMaxAdded());
					System.out.println(i+1 + ", " + curr_files.get(j) + ", " + info.getTotalChurn() +", " + info.getAverageChurn() + ", " + info.getMaxChurn());
				}
				catch (NumberFormatException e) {
					//do nothing
				}
			}
		}
		
	}
	
	
	private static Loc getLocFile(String filename, int version) throws IOException, NumberFormatException {
		
		HashMap<String, String> infoLoc = gitManager.getFileModififications(filename);
		
		Loc toRet = new Loc();
		int total = 0;
		for (Entry<String, String> entry : infoLoc.entrySet()) { 
			
			String key = entry.getKey();
			int id = getIDfromDate(key);
			if (id <= version && id != -1) {
				String[] values = entry.getValue().split(" ");
				
				int added = Integer.parseInt(values[0]);
				int removed = Integer.parseInt(values[1]);
				int churn = added-removed;
				//System.out.println(filename + " " + values[0] + " " + values[1]);
				toRet.addTotalAdded(added);
				toRet.addTotalRemoved(removed);
				toRet.addTotalChurn(churn);
				total++;
				
				if (toRet.getMaxChurn() < churn)
					toRet.setMaxChurn(churn);

				if (toRet.getMaxAdded() < added)
					toRet.setMaxAdded(added);

				if (toRet.getAverageRemoved() < removed)
					toRet.setMaxAdded(removed);

			}
			
			//String value = entry.getValue();
			//System.out.println(key + " "+ value + " " + getIDfromDate(key) + " " + (getIDfromDate(key) <= version && id != -1)); 
		}
		if (total != 0) {
			toRet.setAverageAdded((int) Math.round(toRet.getTotalAdded()*1.0/total*1.0));
			toRet.setAverageRemoved((int) Math.round(toRet.getTotalRemoved()*1.0/total*1.0));
			toRet.setAverageChurn((int) Math.round(toRet.getTotalChurn()*1.0/total*1.0));
		}
		//System.out.println(toRet.getAdded() + " " + toRet.getRemoved() + " " + (toRet.getAdded() - toRet.getRemoved())); 
		
		return toRet;
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
		
		
		getAllFiles();

		//saveToCSV(projName+"metrics.csv");
	}

}
