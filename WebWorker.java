import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;

public class WebWorker extends Thread {
	private WebFrame webFrame;
	private int row;
	private String urlString;

	public WebWorker(String url, int row, WebFrame wf) {
		this.urlString = url;
		this.webFrame = wf;
		this.row = row;
	}
	
	@Override
	public void run() {
		System.out.println("I Started " + row);
		this.webFrame.increaseRunningsCount();
		download();
		this.webFrame.decreaseRunningsCount();
		this.webFrame.increaseCompletedCount();
		this.webFrame.semRelease();
		System.out.println("I finished " + row);
	}

	
	//  This is the core web/download i/o code...
	private void download() {
	    // finding the time before the operation is executed
	    long start = System.currentTimeMillis();

 		InputStream input = null;
		StringBuilder contents = null;
		String st = "";
		try {
			URL url = new URL(urlString); 
			URLConnection connection = url.openConnection();
		
			// Set connect() to throw an IOException
			// if connection does not succeed in this many msecs.
			connection.setConnectTimeout(5000);
			
			connection.connect();
			input = connection.getInputStream();

			BufferedReader reader  = new BufferedReader(new InputStreamReader(input));
		
			char[] array = new char[1000];
			int len;
			contents = new StringBuilder(1000);
			while ((len = reader.read(array, 0, array.length)) > 0) {
				//Check if is interrupted
				if(isInterrupted()) {
					this.webFrame.updateGUI(row, INTERRUPTED_STATUS);
					break; 
				}
				
				contents.append(array, 0, len);
				Thread.sleep(100);
			}
			// Successful download if we get here
			// finding the time after the operation is executed
		    long end = System.currentTimeMillis();
		    //finding the time difference and converting it into seconds
		    float sec = (end - start) / 1000F;
		    
		    st = currentTime() + " " + sec + "ms " + contents.length() + " bytes";
		    this.webFrame.updateGUI(row, st);
		}
		// Otherwise control jumps to a catch...
		catch(MalformedURLException ignored) {
			this.webFrame.updateGUI(row, ERROR_STATUS);
		}
		catch(InterruptedException exception) {
			this.webFrame.updateGUI(row, INTERRUPTED_STATUS);
		}
		catch(IOException ignored) {
			this.webFrame.updateGUI(row, ERROR_STATUS);
		}
		// "finally" clause, to close the input stream
		// in any case
		finally {
			try{
				if (input != null) input.close();
			}
			catch(IOException ignored) {
				this.webFrame.updateGUI(row, ERROR_STATUS);
			}
		}
		
		
	}
	
	/*
	 * Method that returns current time in string format
	 */
	private String currentTime() {
		DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
		Date date = new Date();
		return dateFormat.format(date);
	}
	
	private static final String ERROR_STATUS = "err";
	private static final String INTERRUPTED_STATUS = "interrupted";
}
