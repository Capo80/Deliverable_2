package it.deliv2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import it.deliv2.helpers.Filenames;
import it.deliv2.helpers.Git;
import it.deliv2.helpers.Issue;
import it.deliv2.helpers.JsonManager;

public class GetClassBuggyness {

	//useful structures
	private static HashMap<String, Integer> versionIDMap;
	private static HashMap<String, Issue> issuesMap;
	private static List<String> versionDates;
	private static List<Integer> versionIDs;
	private static float[] averages;
	private static int[] totals;
	private static Git gitManager;
	private static List<HashMap<String, Boolean>> files;
	private static Logger logger = Logger.getLogger("buggyness");
	
	
	//Function that creates a file if it does not exists
	private static File createFile(String filename) throws IOException {
		File newFile = new File(filename);
		if (!newFile.exists() && !newFile.createNewFile()) {
				//Log error
				logger.log(Level.SEVERE, "Cannot create file for BuggynessInfo");
			}
		return newFile;
	}
	
	//Save the file information to a csv
	private static void saveToCSV(String fileName) throws IOException {
			
		//Create file if it does not exist
		File newCSV = createFile(fileName);
	
		try (FileWriter fw = new FileWriter(newCSV)) {
			
			fw.write("Version, File, Buggy\n");
			
			//Iterate trought versions
			for (int i = 0;  i < files.size(); i++) {
				
				//Iterate trought single files
				for (Entry<String, Boolean> entry : files.get(i).entrySet()) { 
					
					if (Boolean.TRUE.equals(entry.getValue()))
						fw.write(i+1 + ", " + entry.getKey() + ", Yes\n" );
					else
						fw.write(i+1 + ", " + entry.getKey() + ", No\n" );
					
				}
				
			}
		}
		
		
	}
	
	//Function to obtain the full list of files per version
	private static void getAllFiles() throws IOException {
		
		//Initialize list object
		files = new ArrayList<>();
		
		for (int i = 0; i < versionDates.size(); i++) {
			
			//Initialize HashMap
			files.add(new HashMap<>());
			
			//Get list of files
			List<String> currFiles = gitManager.getFilesModifiedBeforeDate(versionDates.get(i));

			//Add files to hashmap
			for (int j = 0; j < currFiles.size(); j++) {
				
				String curr = currFiles.get(j);
				
				String[] values = curr.split("\t");
				//Some file dont have the info necessary to calculate the metrics - i remove them
				if (!curr.startsWith("'") && values.length == 3 && values[0].compareTo("-") != 0 && values[1].compareTo("-") != 0 && !curr.contains("=>"))
					files.get(i).put(values[2], false);
			}
		}
		
	}
	
	//Function that sets each file as buggy based on the issuemap
	private static void setBuggyness() throws IOException {
		
		
		//Iterate trought each key
		for (Entry<String, Issue> entry : issuesMap.entrySet()) { 
			
			String key = entry.getKey();
			Issue issue = entry.getValue();
			
			//Recover all files modified while working on this issue
			List<String> modFiles = gitManager.getFilesByKey(key);
			
			//For every affected version set each file to buggy
			for (int i = issue.getIntroVersion()-1; i < issue.getFixVersion()-1; i++) {
				for (int j = 0; j < modFiles.size(); j++) {
					if (files.get(i).get(modFiles.get(j)) != null)	
						files.get(i).put(modFiles.get(j), true);
				}
			}
		  
		}
	
	}
	
	//Function that calculates all the missing IV with proportion
	private static void calculateMissingIVs() {
		
		//Iterate torught issues
		for (Entry<String, Issue> entry : issuesMap.entrySet()) {
		    String key = entry.getKey();
		    Issue value = entry.getValue();
		    //Find issues with no IV
		    if (value.getIntroVersion() == Issue.INTRO_DEF && value.getFixVersion() != Issue.FIX_DEF) {
		    	//Calculate with proportion
		    	int newIntroVers = Math.round(value.getFixVersion() - (value.getFixVersion() - value.getOpenVersion())*averages[value.getFixVersion()-1]);
		    	
		    	//Special cases
		    	if (newIntroVers <= 0)
		    		newIntroVers = 1;
		    	if (newIntroVers > value.getOpenVersion())
		    		newIntroVers = value.getOpenVersion();
		    	
		    	//Update map
		    	value.setIntroVersion(newIntroVers);
		    	issuesMap.put(key, value);
		    }
		}
	
	}
	
	//Updates sums for average
	private static void calculateSums(int introVers, int openVers, int fixVers) {

		//Check if the numbers are valid
    	if (introVers != Issue.INTRO_DEF && fixVers != Issue.FIX_DEF) {
    		
    		//Update each version
    		for(int h = fixVers+1; h < versionIDs.size(); h++) {
    			totals[h]++;
    			//Some special cases
    			if (fixVers != openVers) {
    				averages[h] += (fixVers - introVers)*1.0/(fixVers - openVers)*1.0;
    			} else {
    				if (openVers != introVers) {
        				averages[h] += (fixVers - introVers);
        			} else {
    					averages[h] += 1;
    				}
    			}
    		}
    	}
		
	}
	//Function to recover version IDs from the file
	private static HashMap<String, Integer> recoverVersionIDs(String fileName) throws FileNotFoundException {
		
		//Initialize structure
		HashMap<String, Integer> versionIDs = new HashMap<>();
		
		//Read file
		File versionCSV = new File(fileName);
		
		try (Scanner fr = new Scanner(versionCSV)) {
			
			//Throw away first line
			fr.nextLine();
			
	      while (fr.hasNextLine()) {
	          String line = fr.nextLine();
	          String[] splitted = line.split(",");
	          versionIDs.put(splitted[1], Integer.parseInt(splitted[0]));
	        }
		}
		
		return versionIDs;
		
	}
	
	//Function to update structure with version dates from the file
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
	
	//Recovers the version id that follows a date
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
	
	//Recovers the fixVersion
	//first it check the commits, if there are none, it takes the fix version in jira
	private static int getMaxVersion(JSONArray versions, String key) throws JSONException, IOException {
		
    	int fixVers = Issue.FIX_DEF;
    	
    	//Check commits
    	String lastCommitDate = gitManager.getLastCommitDate(key);
    	if (lastCommitDate.compareTo("") != 0)
    		fixVers =  getIDfromDate(lastCommitDate);
    	
    	if (fixVers != Issue.FIX_DEF)
    		return fixVers;
    	
    	//Check JIRA
    	for(int h = 0; h < versions.length(); h++) {
    		String toFind = versions.getJSONObject(h).get("id").toString();
    		Object temp = versionIDMap.get(toFind);
    		Integer currVers = 1;
    		if (temp != null)
    			currVers = (Integer) temp;
    		else {
    			//Log warinig if no version fuond
				logger.log(Level.WARNING, "Missing date for ID: {0}", toFind);
    			return -1;
    		}
    		
    		if (currVers > fixVers)
    			fixVers = currVers;
    	}
		
		return fixVers;
		
	}
	
	//Recovers the IV from JIRA
	private static int getMinVersion(JSONArray versions) throws JSONException {
    	int introVers = Issue.INTRO_DEF;
    	
    	//Check all the versions, take the lowest
    	for(int h = 0; h < versions.length(); h++) {
    		String toFind = versions.getJSONObject(h).get("id").toString();
    		Object temp = versionIDMap.get(toFind);
    		Integer currVers = 0;
    		if (temp != null)
    			currVers = (Integer) temp;
    		else{
    			//Log warinig if no version fuond
				logger.log(Level.WARNING, "Missing date for ID: {0}", toFind);
    			return -1;
    		}
    		//correct errors
    		if (currVers < introVers)
    			introVers = currVers;
    	}
		return introVers;
	}
	
	//Functions that extract all the version info from the json array
	private static void recoverInfoVersions(JSONArray issues, int total) throws JSONException, IOException {
		
		for (int i = 0; i < total && i < 1000; i++) {
			
			String key = issues.optJSONObject(i).get("key").toString();
        	
        	JSONObject fields = issues.getJSONObject(i).getJSONObject("fields");
        
        	//Recover affected versions field
        	JSONArray versions = fields.getJSONArray("versions");
        	
        	//Find earliest affected version
        	int introVers = getMinVersion(versions);
        
        	//Recover fixed version
        	JSONArray fixVersions = fields.getJSONArray("fixVersions");
        	
        	//Find latest fixed version
        	int fixVers = getMaxVersion(fixVersions, key);
        	
        	//Missing version
        	if (fixVers == -1 || introVers == -1)
        		continue;
           	
        	//Recover opning date of the issue
        	String date = fields.getString("created").split("T")[0];
        
        	
        	//Find opening version
        	int openVers = getIDfromDate(date);
        	
        	
        	if (introVers != Issue.INTRO_DEF && openVers < introVers)
        		introVers = Issue.INTRO_DEF;
        	
        	if (fixVers != Issue.FIX_DEF)
        		issuesMap.put(key, new Issue(key, introVers, openVers ,  fixVers));
        	
        	//Calculate sums for average
        	calculateSums(introVers, openVers, fixVers);
        	
        }
		
	}
	public static void main(String[] args) throws IOException, JSONException {
		
		//import repository if its not there
		gitManager = new Git(Filenames.REPO_NAME, "..");
		
		//Read info from version file
		versionIDMap = recoverVersionIDs(Filenames.VERS_FILE);
		initVersionDates(Filenames.VERS_FILE);

		logger.log(Level.INFO, "Recovered version info");
		
		Integer j = 0;
		Integer i = 0; 
		Integer total = 1;
		averages = new float[versionIDs.size()];
		totals = new int[versionIDs.size()];
		Arrays.fill(averages, 0);
		Arrays.fill(totals, 0);
		
		//Initialize issue map
		issuesMap = new HashMap<>();
		do {
			j = i + 1000;
			//Send query to jira for issues (1000 at the time)
			String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project%20=%22"+Filenames.PROJ_NAME+"%22AND%22issueType%22=%22Bug%22AND%20(%22status%22%20=%20CLOSED%20OR%20%22status%22=RESOLVED)%20AND%22Resolution%22%20=Fixed&fields=versions,fixVersions,created&startAt="+ i.toString() + "&maxResults=" + j.toString();
		
			JSONObject json = JsonManager.readJsonFromUrl(url);
			
			total = json.getInt("total");
			JSONArray issues = json.getJSONArray("issues");	
			
			//Iterate trought the issues
	        recoverInfoVersions(issues, total-i);
	        i = i + 1000;
	        
		} while (i < total);
		
		logger.log(Level.INFO, "Recovered all existing info for reported bugs");
		
		//Calculate P averages for each version
		for (int h = 0; h < versionIDs.size(); h++) {
			if (totals[h] != 0)	
				averages[h] = averages[h]/totals[h];
			else
				averages[h] = 0;
		}
		

		//Calculate missing IV with proportion
		calculateMissingIVs();
		
		logger.log(Level.INFO, "Calcualted missing info with proportion");
		
		//Recover filenames per version
		getAllFiles();
		
		//Find if a file is bugged
		setBuggyness();
		
		logger.log(Level.INFO, "Found all bugged files");
		
		//Save everything to a .csv
		saveToCSV(Filenames.BUG_FILE);
		
		logger.log(Level.INFO, "Saved results in .csv file");
		 
	}

}
