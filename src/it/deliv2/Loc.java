package it.deliv2;

public class Loc {

	int totalAdded;
	int totalRemoved;
	int averageRemoved;
	int averageAdded;
	int maxAdded;
	int maxRemoved;
	
	public Loc() {
		this.totalAdded = 0;
		this.totalRemoved = 0;
		this.averageAdded = 0;
		this.averageRemoved = 0;
		this.maxAdded = 0;
		this.maxRemoved = 0;
	}
	
	public void addTotalAdded(int toAdd) {
		this.totalAdded += toAdd;	
	}
	public void addTotalRemoved(int toAdd) {
		this.totalRemoved += toAdd;	
	}

	public int getTotalAdded() {
		return totalAdded;
	}

	public void setTotalAdded(int totalAdded) {
		this.totalAdded = totalAdded;
	}

	public int getTotalRemoved() {
		return totalRemoved;
	}

	public void setTotalRemoved(int totalRemoved) {
		this.totalRemoved = totalRemoved;
	}

	public int getAverageRemoved() {
		return averageRemoved;
	}

	public void setAverageRemoved(int averageRemoved) {
		this.averageRemoved = averageRemoved;
	}

	public int getAverageAdded() {
		return averageAdded;
	}

	public void setAverageAdded(int averageAdded) {
		this.averageAdded = averageAdded;
	}

	public int getMaxAdded() {
		return maxAdded;
	}

	public void setMaxAdded(int maxAdded) {
		this.maxAdded = maxAdded;
	}

	public int getMaxRemoved() {
		return maxRemoved;
	}

	public void setMaxRemoved(int maxRemoved) {
		this.maxRemoved = maxRemoved;
	}
	
	
	
}
