
public class Transaction {
	private int sourceId;
	private int destinationId;
	private int amount;
	
	//Constructor
	public Transaction(int from, int to, int money) {
		this.sourceId = from;
		this.destinationId = to;
		this.amount = money;
	}
	
	//Getter methods
	public int getSourceId() {
		return this.sourceId;
	}
	
	public int getDestinationId() {
		return this.destinationId;
	}
	
	public int getAmount() {
		return this.amount;
	}
	
	/* toString method for Transaction class to return info about transaction
	 */
	@Override
	public String toString() {
		String result = "";
		result += "From:" + this.sourceId;
		result += " To:" + this.destinationId;
		result += " Amount:" + this.amount;
		return result;
	}
}
