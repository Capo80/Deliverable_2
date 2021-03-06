package it.deliv2;


import java.io.IOException;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.time.LocalDate;
import java.time.LocalDateTime;

import org.json.JSONException;
import org.json.JSONObject;

import it.deliv2.helpers.Filenames;
import it.deliv2.helpers.JsonManager;

import org.json.JSONArray;

public class GetReleaseInfo {
	   private static HashMap<LocalDateTime, String> releaseNames;
	   private static HashMap<LocalDateTime, String> releaseID;
	   private static ArrayList<LocalDateTime> releases;
	   private static String projName =Filenames.PROJ_NAME;
	   private static String outname = Filenames.VERS_FILE;
	   

	   public static void main(String[] args) throws IOException, JSONException {
		   
		   //Fills the arraylist with releases dates and orders them
		   //Ignores releases with missing dates
		   releases = new ArrayList<>();
		   Integer i;
		   String url = "https://issues.apache.org/jira/rest/api/2/project/" + projName;
		   JSONObject json = JsonManager.readJsonFromUrl(url);
		   JSONArray versions = json.getJSONArray("versions");
		   releaseNames = new HashMap<>();
		   releaseID = new HashMap<> ();
		   for (i = 0; i < versions.length(); i++ ) {
			   String name = "";
			   String id = "";
			   if(versions.getJSONObject(i).has("releaseDate")) {
				   if (versions.getJSONObject(i).has("name"))
					   name = versions.getJSONObject(i).get("name").toString();
				   if (versions.getJSONObject(i).has("id"))
					   id = versions.getJSONObject(i).get("id").toString();
				   addRelease(versions.getJSONObject(i).get("releaseDate").toString(), name,id);
			   }
		   }
		   // order releases by date
		   Collections.sort(releases, (o1,o2) -> o1.compareTo(o2));
		   if (releases.size() < 6)
			   return;
		   
		   //Name of CSV for output   
		   try (FileWriter fileWriter = new FileWriter(outname);) {
			    fileWriter.append("Index,Version ID,Version Name,Date");
			    fileWriter.append("\n");
				
				for ( i = 0; i < releases.size(); i++) {
					   Integer index = i + 1;
		               fileWriter.append(index.toString());
		               fileWriter.append(",");
		               fileWriter.append(releaseID.get(releases.get(i)));
		               fileWriter.append(",");
		               fileWriter.append(releaseNames.get(releases.get(i)));
		               fileWriter.append(",");
		               String fullDate = releases.get(i).toString();
		               fileWriter.append(fullDate.split("T")[0]);
		               fileWriter.append("\n");
		         }

		   }
	}
 
	
	public static void addRelease(String strDate, String name, String id) {
		LocalDate date = LocalDate.parse(strDate);
		LocalDateTime dateTime = date.atStartOfDay();
		if (!releases.contains(dateTime))
			releases.add(dateTime);
		releaseNames.put(dateTime, name);
		releaseID.put(dateTime, id);
	}


	
}
