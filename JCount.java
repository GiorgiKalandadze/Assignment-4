import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import com.sun.crypto.provider.JceKeyStore;

public class JCount extends JPanel{
	private JTextField textField;
	private JLabel label;
	private JButton buttonStart;
	private JButton buttonStop;
	private int counter;
	private WorkerThread currentWorker;
	private int countEnd;
	
	private static void createAndShowGUI() {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS)); //!!!
		
		
		//Create JCounts
		for(int i = 0; i < numberOfPanels; i++) {
			JCount panel = new JCount();
			frame.add(panel);
			frame.add(Box.createRigidArea(new Dimension(0,40)));
		}
		
		frame.pack();
		frame.setVisible(true);
	}
	//Constructor
	public JCount() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

		textField = new JTextField();
		add(textField);
		
		label = new JLabel(Integer.toString(countStart));
		add(label);
		
		buttonStart = new JButton("Start");
		buttonStart.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				countEnd = Integer.parseInt(textField.getText());
				startWorkerThread();
			}
		});
		add(buttonStart);
		
		buttonStop = new JButton("Stop");
		buttonStop.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				stopWorkerThread();
				
			}
		});
		add(buttonStop);
	}
	/*
	 * Action which should be performed after pushing "Start" button
	 * Start new counting thread
	 */
	private void startWorkerThread() {
		if(currentWorker != null) {
			currentWorker.interrupt();
		}
		currentWorker = new WorkerThread();
		currentWorker.start();
	}
	/*
	 * Action which should be performed after pushing "Stop" button
	 * Current thread must end without completing counting work. 
	 */
	private void stopWorkerThread() {
		if(currentWorker != null) {
			currentWorker.interrupt();
		}
	}
	/*
	 * Main
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createAndShowGUI();
			}
		});
	}
	
	
	/*
	 * Inner worker class
	 * It stops working if it has end counting or is interrupted
	 */
	private class WorkerThread extends Thread{
		@Override
		public void run() {
			for(int i = 0; i <= countEnd; i++) {
				
				if(isInterrupted()) break; //Finish if interrupted
				
				if(i % INTERVAl == 0) { //In Each interval sleep and update label
					try {
						currentWorker.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						System.out.println("Interupted");
						break;
					}
					int tmp = i;
					SwingUtilities.invokeLater(new Runnable() {
						public void run() {
							label.setText(Integer.toString(tmp));
						}
					});
				}
				
			}
		}
	}

	private static final int numberOfPanels = 4;
	private static final int countStart = 0;
	private static final int INTERVAl = 10000;
}
