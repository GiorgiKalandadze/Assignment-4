import javax.swing.plaf.synth.SynthSpinnerUI;

public class Account {
	private int idNumber;
	private int currentBalance;
	private int numberOfTransactions;
	
	//Constructor
	public Account(int id, int balance) {
		this.idNumber = id;
		this.currentBalance = balance;
		this.numberOfTransactions = 0;
	}
	
	//In withdra method we should decrease some amount money from current balance
	public synchronized void withdraw(int amount) {
		this.currentBalance -= amount;
		this.numberOfTransactions++;
	}
	
	//In deposit method we should increase current balance with some amount of money
	public synchronized void deposit(int amount) {
		this.currentBalance += amount;
		this.numberOfTransactions++;
	}
	
	//Getter methods
	public int getId() {
		return this.idNumber;
	}
	
	public int getCurrentBalance() {
		return this.currentBalance;
	}
	
	public int getNumberOfTransactions() {
		return this.numberOfTransactions;
	}
	
	
	/* toString method for Account class to return info about account
	 */
	@Override
	public String toString() {
		String result = "";
		result += "acct:" + this.idNumber;
		result += " bal:" + this.currentBalance;
		result += " trans:" + this.numberOfTransactions;
		return result;
	}
}
