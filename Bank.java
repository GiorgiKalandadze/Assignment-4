import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import sun.misc.Lock;

public class Bank {
	private static final int startBalance = 1000;
	private static final int numberOfAccounts = 20;
	private final Transaction nullTrans = new Transaction(-1, 0, 0);
	private static int numberOfThreads;
	ArrayList<Account> accountList = new ArrayList<>();
	HashMap<Integer, Account> map = new HashMap<>();
	BlockingQueue<Transaction> transactionQueue = new ArrayBlockingQueue<>(numberOfAccounts);
	private CountDownLatch latch;

	// Constructor empty
	public Bank() {

	}

	public static void main(String[] args) {
		checkInput(args);
		Bank bank = new Bank();
		bank.startSimulate(args);
	}

	/*
	 * This method checks whether input is correct or not
	 */
	private static void checkInput(String[] input) {
		if (input.length != 2) {
			throw new java.lang.Error("Wrong input");
		} else if (!isNumber(input[1])) {
			throw new java.lang.Error("Wrong input");
		}
	}

	/*
	 * This method checks whether given string corresponds number or not
	 */
	private static boolean isNumber(String text) {
		if (text.length() < 1)
			return false;
		if (text.length() > 1 && text.charAt(0) == '0')
			return false;
		for (int i = 0; i < text.length(); i++) {
			if (!Character.isDigit(text.charAt(i)))
				return false;
		}
		return true;
	}

	private void startSimulate(String[] args) {
		String fileName = args[0];
		numberOfThreads = Integer.parseInt(args[1]);

		initializeAccountBase();

		latch = new CountDownLatch(numberOfThreads);

		startWorkerThreads();

		try {
			readFile(fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		printResult();
	}

	private void readFile(String fileName) throws IOException {
		BufferedReader rd = new BufferedReader(new FileReader(fileName));
		StringTokenizer st;
		String line; // String variable to get line by line info form input
		int srcId, destId, amount; // variables to keep info from input
		Transaction currentTransaction;
		while (true) {
			line = rd.readLine();
			if (line == null)
				break;
			st = new StringTokenizer(line);
			// Keep info from input
			srcId = Integer.parseInt(st.nextToken());
			destId = Integer.parseInt(st.nextToken());
			amount = Integer.parseInt(st.nextToken());
			currentTransaction = new Transaction(srcId, destId, amount);
			try {
				transactionQueue.put(currentTransaction);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		rd.close();
		addNullTransactions();
	}

	private void addNullTransactions() {
		for (int i = 0; i < numberOfThreads; i++) {
			try {
				transactionQueue.put(nullTrans);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/*
	 * Fills map with account objects with key of their id
	 */
	private void initializeAccountBase() {
		Account ac;
		for (int i = 0; i < numberOfAccounts; i++) {
			ac = new Account(i, startBalance);
			map.put(i, ac);
			accountList.add(ac);
		}
	}

	// Print final result
	public void printResult() {
		Account currentAccount;
		for (int i = 0; i < accountList.size(); i++) {
			currentAccount = accountList.get(i);
			System.out.println(currentAccount.toString());
		}
	}

	private class Worker extends Thread {
		@Override
		public void run() {
			boolean finished;
			Thread running = Thread.currentThread();
			//System.out.println(running.getName() + " starts");
			while (true) {
				finished = checkQueue();
				if (finished)
					break;
			}
			latch.countDown();
			//System.out.println(running.getName() + " finishes");
		}

		// Checks transaction queues and if necessary processes it
		private boolean checkQueue() {
			Transaction currTransaction;
			try {
				currTransaction = transactionQueue.take();

				// System.out.println(transactionQueue.size());
				if (currTransaction == nullTrans) {
					return true;
				} else {
					processTransaction(currTransaction);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return false;
		}

		// Processes transaction. Updates source and destination accounts
		private void processTransaction(Transaction t) {

			int srcId = t.getSourceId();
			int destId = t.getDestinationId();
			int amount = t.getAmount();
			Account current;

			// Source account
			current = map.get(srcId);
			// System.out.println("Before " + current.getCurrentBalance());
			current.withdraw(amount);
			// System.out.println("After " + current.getCurrentBalance());
			map.put(srcId, current);
			accountList.set(srcId, current);

			// Destination accout
			current = map.get(destId);
			current.deposit(amount);
			map.put(destId, current);
			accountList.set(destId, current);
		}
	}

	// Starts up worker threads
	private void startWorkerThreads() {
		Thread t;
		for (int i = 0; i < numberOfThreads; i++) {
			t = new Worker();
			t.start();
		}
	}
}
