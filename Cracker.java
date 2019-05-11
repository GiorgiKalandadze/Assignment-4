import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

public class Cracker {
	// Array of chars used to produce strings
	public static final char[] CHARS = "abcdefghijklmnopqrstuvwxyz0123456789.,-!".toCharArray();
	private CountDownLatch latch; // CountDownLatch for main thread to wait for worker threads
	private static final int maxNumberOfThreads = 40; // Maximum number of worker threads
	private ArrayList<String> result = new ArrayList<>(); // Collection(list) to keep all possible resuls
	private String hashValue; // Hash value which should be cracked

	// Main
	public static void main(String[] args) {
		if (!checkInput(args)) {
			throw new java.lang.Error("Wrong input");
		}
		if (args.length == 1) { //Generation Mode
			System.out.println(generateHash(args[0]));
		} else { //Cracking Mode
			Cracker cr = new Cracker();
			cr.crackingProcess(args[0], Integer.parseInt(args[1]), Integer.parseInt(args[2]));
		}
	}
	/*
	 * Check if input is correct. (Corresponds to problem's content)
	 */
	private static boolean checkInput(String[] input) {
		if (input.length == 1) {
			return true;
		} else if (input.length == 3) { // If input's length is three, last two should be numbers
			if (isNumber(input[1]) && isNumber(input[2])) {
				return true;
			}
		} else if (Integer.parseInt(input[2]) > maxNumberOfThreads) {
			return false;
		}
		return false;
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

	/*
	 * Provides cracking process
	 */
	private void crackingProcess(String input, int size, int numOfThreads) {
		this.hashValue = input;
		latch = new CountDownLatch(numOfThreads);
		startWorkerThreads(size, numOfThreads);

		try {
			latch.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		printFinalResult();
	}

	// Print final result. (All passwords which's hash values corresponds to given hash)
	private void printFinalResult() {
		for (int i = 0; i < result.size(); i++) {
			System.out.println(result.get(i));
		}
	}
	
	/*
	 * Start worker threads.
	 */
	private void startWorkerThreads(int size, int numOfThreads) {
		int startIndex, endIndex, average;
		startIndex = endIndex = average = 0; // Initialize variables
		average = CHARS.length / numOfThreads;
		for (int i = 0; i < numOfThreads - 1; i++) {
			startIndex = i * average;
			endIndex = startIndex + average - 1;
			new Worker(startIndex, endIndex, size).start();
		}
		
		// Edge Case. Start last thread
		new Worker(endIndex + 1, CHARS.length - 1, size).start();
	}

	//Inncer worker class
	private class Worker extends Thread {
		private char[] set;
		private int passwordLength;
		private Set<String> resultSet = new HashSet<>();

		// Consturctor
		Worker(int startIndex, int endIndex, int size) {
			set = new char[endIndex - startIndex + 1];
			this.passwordLength = size;
			for (int i = startIndex; i <= endIndex; i++) {
				set[i - startIndex] = CHARS[i];
			}
		}

		@Override
		public void run() {
			findPermutations();
			latch.countDown();
		}
		//https://www.geeksforgeeks.org/print-all-combinations-of-given-length/
		//From geekforgeeks
		// Generate permutations started with corresponding character at first
		private void findPermutations() {
			char startCharacter;
			for (int i = 0; i < this.set.length; i++) {
				// System.out.println(Thread.currentThread().getName() + " - " + i);
				startCharacter = set[i];
				String prefix = "";
				prefix += startCharacter;
				recursion(prefix, this.passwordLength - 1);
			}
		}
		
		/*
		 * Finds all permutations of size k 
		 */
		private void recursion(String prefix, int k) {
			// Base case: k is 0,
			// print prefix
			if (k == 0) {
				if (checkPass(prefix) && !resultSet.contains(prefix)) {
					synchronized (result) {
						result.add(prefix);
					}
					synchronized (resultSet) {
						resultSet.add(prefix);
					}
				}
				return;
			}

			// One by one add all characters
			// from set and recursively
			// call for k equals to k-1
			for (int i = 0; i < CHARS.length; ++i) {

				// Next character of input added
				String newPrefix = prefix + CHARS[i];

				// k is decreased, because
				// we have added a new character
				recursion(newPrefix, k - 1);
			}
		}
		/*
		 * Checks if password is what we are looking for.
		 * In other words, it's hash value is the same as the given hash
		 */
		private boolean checkPass(String prefix) {
			if (generateHash(prefix).equals(hashValue)) {
				return true;
			}
			return false;
		}
	}

	/*
	 * This method generates hash value for given text using "SHA" algorithm
	 */
	private static String generateHash(String text) {
		String hash = "";
		try {
			MessageDigest md = MessageDigest.getInstance("SHA");
			md.update(text.getBytes());
			byte[] dgBytes = md.digest();
			hash = hexToString(dgBytes);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return hash;
	}

	/*
	 * Given a byte[] array, produces a hex String, such as "234a6f". with 2 chars
	 * for each byte in the array. (provided code)
	 */
	public static String hexToString(byte[] bytes) {
		StringBuffer buff = new StringBuffer();
		for (int i = 0; i < bytes.length; i++) {
			int val = bytes[i];
			val = val & 0xff; // remove higher bits, sign
			if (val < 16)
				buff.append('0'); // leading 0
			buff.append(Integer.toString(val, 16));
		}
		return buff.toString();
	}

	/*
	 * Given a string of hex byte values such as "24a26f", creates a byte[] array of
	 * those values, one byte value -128..127 for each 2 chars. (provided code)
	 */
	public static byte[] hexToArray(String hex) {
		byte[] result = new byte[hex.length() / 2];
		for (int i = 0; i < hex.length(); i += 2) {
			result[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
		}
		return result;
	}

	// possible test values:
	// a 86f7e437faa5a7fce15d1ddcb9eaeaea377667b8
	// fm adeb6f2a18fe33af368d91b09587b68e3abcb9a7
	// a! 34800e15707fae815d7c90d49de44aca97e2d759
	// xyz 66b27417d37e024c46526c2f6d358a754fc552f3

}
