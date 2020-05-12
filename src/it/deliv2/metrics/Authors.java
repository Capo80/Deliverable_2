package it.deliv2.metrics;

import java.util.ArrayList;
import java.util.List;

public class Authors {
	
	private int total;
	private List<String> authorsList;
	
	public Authors() {
		total = 0;
		authorsList = new ArrayList<>();
	}
	
	public void increaseTotal() {
		total++;
	}
	public int getTotal() {
		return total;
	}
	public  void addAuthor(String newAuthor) {
		authorsList.add(newAuthor);
		total++;
	}

	public boolean isInList(String author) {
		return authorsList.contains(author);
	}
	

}
