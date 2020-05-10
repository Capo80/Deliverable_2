package it.deliv2.metrics;

public class Loc {

	private int totalAdded;
	private int totalRemoved;
	private int averageRemoved;
	private int averageAdded;
	private int maxAdded;
	private int maxRemoved;
	private int totalChurn;
	private int averageChurn;
	private int maxChurn;
	private int updates;
	
	
	public Loc() {
		this.totalAdded = 0;
		this.totalRemoved = 0;
		this.averageAdded = 0;
		this.averageRemoved = 0;
		this.maxAdded = 0;
		this.maxRemoved = 0;
		this.updates = 0;
	}
	
	public void increaseUpdates() {
		this.updates++;
	}
	
	public int getUpdates() {
		return updates;
	}
	public void addTotalChurn(int toAdd) {
		this.totalChurn += toAdd;	
	}
	
	public int getTotalChurn() {
		return totalChurn;
	}

	public void setTotalChurn(int totalChurn) {
		this.totalChurn = totalChurn;
	}

	public int getAverageChurn() {
		return averageChurn;
	}

	public void setAverageChurn(int averageChurn) {
		this.averageChurn = averageChurn;
	}

	public int getMaxChurn() {
		return maxChurn;
	}

	public void setMaxChurn(int maxChurn) {
		this.maxChurn = maxChurn;
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
