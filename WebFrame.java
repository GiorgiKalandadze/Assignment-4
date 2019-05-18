import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.sun.xml.internal.bind.AccessorFactory;
import com.sun.xml.internal.bind.v2.runtime.reflect.Accessor;

import sun.launcher.resources.launcher;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.concurrent.Semaphore;

public class WebFrame {
	private Launcher launcher;
	private ArrayList<String> urlList = new ArrayList<>();
	private ArrayList<WebWorker> workersList = new ArrayList<>();
	private int runningThreadCount = 0;
	private int completedThreadCount = 0;
	private int progresValue = 0;
	
	private DefaultTableModel model;
	private JTable table;
	private JPanel panel;
	private JLabel run;
	private JLabel comp;
	private JLabel elaps;
	private JFrame frame;
	private JButton singleButton;
	private JButton concurrentButton; 
	private JButton stopButton;
	private JTextField textField;
	private JProgressBar progressBar;
	
	/*
	 * Constructor
	 */
	public WebFrame(String fileName) {
		panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		frame = new JFrame("WebLoader");
		model = new DefaultTableModel(new String[] { "url", "status"}, 0);
		table = new JTable(model);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		JScrollPane scrollpane = new JScrollPane(table);
		scrollpane.setPreferredSize(new Dimension(600,300));
		panel.add(scrollpane);
		
		drawBelowTable();
	
		try {
			readFile(fileName);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	/* 
	 * Read File, fill model with url's and empty status 
	 */
	private void readFile(String fileName) throws IOException {
		BufferedReader rd = new BufferedReader(new FileReader(fileName));
		String line; // String variable to get line by line info form input
		while (true) {
			line = rd.readLine();
			if (line == null)
				break;
			urlList.add(line);
			model.addRow(new String [] {line, ""});
		}
		rd.close();
	}
	
	/*
	 * Draw table and initialize graphical variables
	 */
	private void drawBelowTable() {
		addButtons();
		addLabels();
		addTextField();
		addProgressBar();
		addItemsToPanel();
		addPanelToFrame();
	}
	/*
	 * Add panel to frame
	 */
	private void addPanelToFrame() {
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);		
	}
	/*
	 * Add items to panel
	 */
	private void addItemsToPanel() {
		panel.add(singleButton);
		panel.add(Box.createRigidArea(new Dimension(0, EMPTY_HIGH))); 
		panel.add(concurrentButton);
		panel.add(Box.createRigidArea(new Dimension(0, EMPTY_HIGH))); 
		panel.add(textField);
		panel.add(Box.createRigidArea(new Dimension(0, EMPTY_HIGH))); 
		panel.add(run);
		panel.add(Box.createRigidArea(new Dimension(0, EMPTY_HIGH))); 
		panel.add(comp);
		panel.add(Box.createRigidArea(new Dimension(0, EMPTY_HIGH))); 
		panel.add(elaps);
		panel.add(Box.createRigidArea(new Dimension(0, EMPTY_HIGH))); 
		panel.add(progressBar);
		panel.add(Box.createRigidArea(new Dimension(0, EMPTY_HIGH))); 
		panel.add(stopButton);
		
		panel.setVisible(true);	
	}
	/*
	 * Add progress bar
	 */
	private void addProgressBar() {
		progressBar = new JProgressBar();	
		
	}
	/*
	 * Add text field
	 */
	private void addTextField() {
		textField  = new JTextField();
		Dimension d = new Dimension(30, 10);
		textField.setMaximumSize(d);
	}
	/*
	 * Add labels
	 */
	private void addLabels() {
		run = new JLabel("Running:0");
		comp = new JLabel("Completed:0");
		elaps = new JLabel("Elapsed:0");
	}
	/*
	 * Add buttons
	 */
	private void addButtons() {
		singleButton = new JButton("Single Thread Fetch");
		concurrentButton = new JButton("Concurrent Fetch");
		stopButton = new JButton("Stop");
		stopButton.setEnabled(false);
		
		singleButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reset();
				runningState();
				launcher = new Launcher(1);
				launcher.start();
			}
		});
		
		
		concurrentButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				reset();
				runningState();
				launcher = new Launcher(Integer.parseInt(textField.getText()));	
				launcher.start();
			}
		});
		
	
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				readyState();
				launcher.interrupt();
				
			}
		});
	}
	/*
	 * Reset variables, counter, labesl...
	 */
	private void reset() {
		resetCounterVariables();
		elaps = new JLabel("Elapsed:0");
		workersList.clear();
	}
	/*
	 * Running state
	 */
	private void runningState() {
		resetStatusStrings();
		singleButton.setEnabled(false);
		concurrentButton.setEnabled(false);
		stopButton.setEnabled(true);
		progressBar.setMaximum(urlList.size());
		progressBar.setValue(progresValue); 
	}

	/*
	 * Reset counter variables
	 */
	private void resetCounterVariables() {
		runningThreadCount = 0;
		completedThreadCount = 0;
		progresValue = 0;
	}
	/*
	 * Write empty string at all status column
	 */
	private void resetStatusStrings() {
		for(int i = 0; i < model.getRowCount(); i++) {
			model.setValueAt("", i, 1);
		}	
	}
	/*
	 * Ready State
	 */
	private void readyState() {
		singleButton.setEnabled(true);
		concurrentButton.setEnabled(true);
		stopButton.setEnabled(false);
	}
	
	//Public Methods
	public  void increaseRunningsCount() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				runningThreadCount++;
				run.setText("Running:" + runningThreadCount);
			}
		});
	}
	
	public  void decreaseRunningsCount() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				runningThreadCount--;
				run.setText("Running:" + runningThreadCount);
			}
		});
	}
	
	public  void increaseCompletedCount() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				completedThreadCount++;
				comp.setText("Completed:" + completedThreadCount);
			}
		});
	}
	
	public  void decreaseCompletedCount() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				completedThreadCount--;
				comp.setText("Completed:" + completedThreadCount);
			}
		});	
	}
	
	//Update status after successful/fail download
	private void updateStatus(int row, String st) {
		model.setValueAt(st, row, 1);
	}
	public  void semRelease() {
		launcher.semaphoreRelease();
	}
	
	public void updateGUI(int row, String status) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				progresValue++;
				progressBar.setValue(progresValue);
				updateStatus(row, status);
			}
		});
	}
	//Main
	public static void main(String[] args) {
		WebFrame w = new WebFrame(args[0]);
	}
	
	//Inner Launcher Class
	private class Launcher extends Thread{
		private Semaphore sem;
		private int urlIndex = 0;
		
		public Launcher(int limit) {
			sem = new Semaphore(limit);
		}
		
		@Override
		public void run() {
			 // finding the time before the operation is executed
		    long start = System.currentTimeMillis();
		  
		    increaseRunningsCount();
		    createWebWorkers();
		    decreaseRunningsCount();
		    
			// finding the time after the operation is executed
		    long end = System.currentTimeMillis();
		    //finding the time difference and converting it into second
		    float sec = (end - start) / 1000F; 
		    elaps.setText("Elapsed:" + sec);
		    readyState();			
		}
		
		/*
		 * Run a loop to create and start WebWorker objects
		 */
		private void createWebWorkers() {
			while(true) {
				/*
				 * Case 1 work is done. 
				 * Case 2 launcher is interrupted
				 */
				if(urlIndex == urlList.size() || isInterrupted()) {
					interruptWorkers();
					break; 
				}
				
				try {
					sem.acquire();
					WebWorker currWorker = new WebWorker(urlList.get(urlIndex), urlIndex, WebFrame.this);
					currWorker.start();
					workersList.add(currWorker);
					urlIndex++;
				} catch (InterruptedException e) {
					interruptWorkers();
					e.printStackTrace();
				}
			}
		}
		/*
		 * Release semaphore
		 */
		public  void semaphoreRelease() {
			this.sem.release();
		}
		
		/*
		 * Launcher interrupts worker threads after its interruption
		 */
		private void interruptWorkers() {
			for(int i = 0; i < workersList.size(); i++) {
				workersList.get(i).interrupt();
			}
		}
	}
	private static final int EMPTY_HIGH = 3;
}
