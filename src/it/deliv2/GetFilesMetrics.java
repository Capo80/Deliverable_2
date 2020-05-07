package it.deliv2;

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
	private static List<String> versionDates;
	private static List<Integer> versionIDs;
	
	//Save the file information to a csv
	private static void saveToCSV(String fileName) throws IOException {
		
		File newCSV = new File(fileName);
		if (!newCSV.exists())
			newCSV.createNewFile();
		
		try (FileWriter fw = new FileWriter(newCSV)) {
			
			fw.write("Version, File, size, LOC_touched, LOC_added, MAX_LOC_Added, AVG_LOC_Added\n");
			
			//Iterate trought versions
			for (int i = 0;  i < filesLocInfo.size(); i++) {
				
				//Iterate trought single files
				for (Entry<String, Loc> entry : filesLocInfo.get(i).entrySet()) { 
					//System.out.println(entry.getValue());
					Loc info = entry.getValue();
					
					fw.write(i+1 + ", " + entry.getKey() + ", " + (info.getTotalAdded()-info.getTotalRemoved()) + ", " + (info.getTotalAdded()+info.getTotalRemoved()) + ", "+ info.getTotalAdded() + ", " + info.getAverageAdded() + ", " + info.getMaxAdded() + "\n");
					
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
	
	//Function to obtain the full list of files per version
	private static void getAllFiles() throws IOException {
		
		//Initialize list object
		filesLocInfo = new ArrayList<HashMap<String, Loc>>();
		
		for (int i = 0; i < versionDates.size(); i++) {
			
			//Initialize HashMap
			filesLocInfo.add(new HashMap<String, Loc>());
			
			//Get list of files
			List<String> curr_files = gitManager.getFilesBeforeDate(versionDates.get(i));
			
			
			//System.out.println(curr_files.size());
			
			//Add files to hashmap
			for (int j = 0; j < curr_files.size(); j++) {
				try {	
					Loc info = getLocFile(curr_files.get(j), i+1);
					filesLocInfo.get(i).put(curr_files.get(j), info);
					
					System.out.println(i+1 + ", " + curr_files.get(j) + ", " + (info.getTotalAdded()-info.getTotalRemoved()) + ", " + (info.getTotalAdded()+info.getTotalRemoved()) + ", "+ info.getTotalAdded() + ", " + info.getAverageAdded() + ", " + info.getMaxAdded());
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
				//System.out.println(filename + " " + values[0] + " " + values[1]);
				toRet.addTotalAdded(added);
				toRet.addTotalRemoved(removed);
				total++;
				
				if (toRet.getMaxAdded() < added)
					toRet.setMaxAdded(added);

				if (toRet.getAverageRemoved() < removed)
					toRet.setMaxAdded(removed);

			}
			
			//String value = entry.getValue();
			//System.out.println(key + " "+ value + " " + getIDfromDate(key) + " " + (getIDfromDate(key) <= version && id != -1)); 
		}
		toRet.setAverageAdded((int) Math.round(toRet.getTotalAdded()*1.0/total*1.0));
		toRet.setAverageRemoved((int) Math.round(toRet.getTotalRemoved()*1.0/total*1.0));
		
		//System.out.println(toRet.getAdded() + " " + toRet.getRemoved() + " " + (toRet.getAdded() - toRet.getRemoved())); 
		
		return toRet;
	}
	public static void main(String[] args) throws IOException {

		gitManager = new Git(repoURL, "..");
		initVersionDates(versionFileName);
		
		//getLocFile("pippi", 6);
		getAllFiles();

		saveToCSV(projName+"metrics.csv");
	}

}
