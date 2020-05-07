package it.deliv2;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GetClassBuggyness {


	private static String projName = "BOOKKEEPER";
	private static String versionFileName = projName + "VersionInfo.csv";
	private static String repoURL = "https://github.com/Capo80/"+projName.toLowerCase()+".git";
	private static HashMap<String, Integer> versionIDMap;
	private static HashMap<String, Issue> issuesMap;
	private static List<String> versionDates;
	private static List<Integer> versionIDs;
	private static float[] averages;
	private static int[] totals;
	private static Git gitManager;
	private static List<HashMap<String, Boolean>> files;
	
	//Save the file information to a csv
	private static void saveToCSV(String fileName) throws IOException {
		
		File newCSV = new File(fileName);
		if (!newCSV.exists())
			newCSV.createNewFile();
		
		try (FileWriter fw = new FileWriter(newCSV)) {
			
			fw.write("Version, File, Buggy\n");
			
			//Iterate trought versions
			for (int i = 0;  i < files.size(); i++) {
				
				//Iterate trought single files
				for (Entry<String, Boolean> entry : files.get(i).entrySet()) { 
					//System.out.println(entry.getValue());
					if (entry.getValue())
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
		files = new ArrayList<HashMap<String, Boolean>>();
		
		for (int i = 0; i < versionDates.size(); i++) {
			
			//Initialize HashMap
			files.add(new HashMap<String, Boolean>());
			
			//Get list of files
			List<String> curr_files = gitManager.getFilesBeforeDate(versionDates.get(i));
			
			
			//System.out.println(curr_files.size());
			
			//Add files to hashmap
			for (int j = 0; j < curr_files.size(); j++) {
				files.get(i).put(curr_files.get(j), false);
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
			
			//System.out.println(modFiles.size());
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
		
		for (Entry<String, Issue> entry : issuesMap.entrySet()) {
		    String key = entry.getKey();
		    Issue value = entry.getValue();
		    if (value.getIntroVersion() == Issue.INTRO_DEF && value.getFixVersion() != Issue.FIX_DEF) {
		    	int newIntroVers = Math.round(value.getFixVersion() - (value.getFixVersion() - value.getOpenVersion())*averages[value.getFixVersion()-1]);
		    	if (newIntroVers <= 0)
		    		newIntroVers = 1;
		    	if (newIntroVers > value.getOpenVersion())
		    		newIntroVers = value.getOpenVersion();
		    	value.setIntroVersion(newIntroVers);
		    	issuesMap.put(key, value);
		    }
		}
	
	}
	
	private static void calculateSums(int introVers, int openVers, int fixVers) {
		
    	if (introVers != Issue.INTRO_DEF && fixVers != Issue.FIX_DEF) {
    		for(int h = fixVers+1; h < versionIDs.size(); h++) {
    			totals[h]++;
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
	private static HashMap<String, Integer> recoverVersionIDs(String fileName) throws FileNotFoundException {
		
		HashMap<String, Integer> versionIDs = new HashMap<String, Integer>();
		
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
	
	//Recovers the fixVersion
	//first it check the commits, if there are none, it takes the fix version in jira
	private static int getMaxVersion(JSONArray versions, String key) throws JSONException, IOException {
		
    	int fixVers = Issue.FIX_DEF;
    	
    	String lastCommitDate = gitManager.getLastCommitDate(key);
    	if (lastCommitDate.compareTo("") != 0)
    		fixVers =  getIDfromDate(lastCommitDate);
    	
    	if (fixVers != Issue.FIX_DEF)
    		return fixVers;
    	
    	for(int h = 0; h < versions.length(); h++) {
    		int currVers = versionIDMap.get(versions.getJSONObject(h).get("id").toString());
    		if (currVers > fixVers)
    			fixVers = currVers;
    	}
		
		return fixVers;
		
	}
	
	private static int getMinVersion(JSONArray versions) throws JSONException {
    	int introVers = Issue.INTRO_DEF;
    	for(int h = 0; h < versions.length(); h++) {
    		int currVers = versionIDMap.get(versions.getJSONObject(h).get("id").toString());
    		if (currVers < introVers)
    			introVers = currVers;
    	}
		return introVers;
	}
	
	private static void recoverInfoVersions(JSONArray issues, int total) throws JSONException, IOException {
		
		for (int i = 0; i < total && i < 1000; i++) {
			
        	String key = issues.optJSONObject(i).get("key").toString();
        	
        	JSONObject fields = issues.getJSONObject(i).getJSONObject("fields");
        
        	//Recover affected versions field
        	JSONArray versions = fields.getJSONArray("versions");
        	
        	//Find earliest affected version
        	int introVers = getMinVersion(versions);
        	
        	//Recover opning date of the issue
        	String date = fields.getString("created").split("T")[0];
        	
        	
        	//Find opening version
        	int openVers = getIDfromDate(date);
        	
        	//System.out.println(versionDates.get(m-1) + " " +  date + " " + versionDates.get(m));
        	
        	//Recover fixed version
        	JSONArray fixVersions = fields.getJSONArray("fixVersions");
        	
        	//Find latest fixed version
        	int fixVers = getMaxVersion(fixVersions, key);
        	
        	if (introVers != Issue.INTRO_DEF && openVers < introVers)
        		introVers = Issue.INTRO_DEF;
        	
        	if (fixVers != Issue.FIX_DEF)
        		issuesMap.put(key, new Issue(key, introVers, openVers ,  fixVers));
        	
        	//Calculate sums for average
        	calculateSums(introVers, openVers, fixVers);
        	
        	//System.out.println(key + " " + introVers + " " + openVers + " " + fixVers);
        }
		
	}
	public static void main(String[] args) throws IOException, JSONException {
		
		//import repository if its not there
		gitManager = new Git(repoURL, "..");
		
		//Read info from version file
		versionIDMap = recoverVersionIDs(versionFileName);
		initVersionDates(versionFileName);

		Integer j = 0;
		Integer i = 0; 
		Integer total = 1;
		averages = new float[versionIDs.size()];
		totals = new int[versionIDs.size()];
		Arrays.fill(averages, 0);
		Arrays.fill(totals, 0);
		
		//Initialize issue map
		issuesMap = new HashMap<String, Issue>();
		do {
			j = i + 1000;
			//Send query to jira for issues (1000 at the time)
			String url = "https://issues.apache.org/jira/rest/api/2/search?jql=project%20=%22"+projName+"%22AND%22issueType%22=%22Bug%22AND%20(%22status%22%20=%20CLOSED%20OR%20%22status%22=RESOLVED)%20AND%22Resolution%22%20=Fixed&fields=versions,fixVersions,created&startAt="+ i.toString() + "&maxResults=" + j.toString();
			JSONObject json = JsonManager.readJsonFromUrl(url);
			
			total = json.getInt("total");
			JSONArray issues = json.getJSONArray("issues");	
			
			//Iterate trought the issues
	        recoverInfoVersions(issues, total);
	        i = i + 1000;
	        
		} while (i < total);
		
		//Calculate P averages for each version
		for (int h = 0; h < versionIDs.size(); h++) {
			//System.out.println(averages[h] + " " + totals[h]);
			if (totals[h] != 0)	
				averages[h] = averages[h]/totals[h];
			else
				averages[h] = 0;
			System.out.println(averages[h] + " " + totals[h]);
		}
		

		//Calculate missing IV with proportion
		calculateMissingIVs();
		
		//Recover filenames per version
		getAllFiles();
		
		//Find if a file is bugged
		setBuggyness();
		
		//Save everything to a .csv
		saveToCSV(projName+"BuggynessInfo.csv");
		
		/*
		 * for (Entry<String, Issue> entry : issuesMap.entrySet()) { String key =
		 * entry.getKey(); Issue value = entry.getValue(); System.out.println(key + " "
		 * + value.getIntroVersion() + " " + value.getOpenVersion() + " " +
		 * value.getFixVersion()); }
		 */
		 
		 
	}

}
