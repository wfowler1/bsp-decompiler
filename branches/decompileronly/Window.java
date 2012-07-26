// Window class

// GUI for the decompiler.
// For a list of swing components check here:
// http://download.oracle.com/javase/tutorial/ui/features/components.html

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Scanner;
import java.io.FileOutputStream;
import java.util.Date;

public class Window extends JPanel implements ActionListener {

	private static Runtime r = Runtime.getRuntime(); // Get a runtime object. This is for calling
	                                                 // Java's garbage collector and does not need
	                                                 // to be ported. I try not to leave memory leaks
	                                                 // but since Java has no way explicitly reallocate
	                                                 // unused memory I have to tell it when a good
	                                                 // time is to run the garbage collector, by
	                                                 // calling gc(). Also, it is used to execute EXEs
	                                                 // from within the program by calling .exec(path).

	protected static Window window;
	private static BSPFileFilter BSPFilter = new BSPFileFilter();
	private static LOGFileFilter LOGFilter = new LOGFileFilter();

	private static JFrame frame;
	private static JMenuBar menuBar;

	private static JMenu fileMenu;
	private static JMenuItem decompVMFItem;
	private static JMenuItem decompMAPItem;
	private static JMenuItem exitItem;

	private static JMenu optionsMenu;
	private static JMenuItem replaceEntitiesItem;
	private static JMenuItem replaceTexturesItem;

	private static File[] jobs=new File[0];
	private static DoomMap[] doomJobs=new DoomMap[0];
	private static boolean[] toVMF;
	private static int[] threadNum;
	private static Runnable runMe;
	private static Thread[] decompilerworkers=null;
	private static int numThreads;

	private static String lastUsedFolder;
	
	public static final double PRECISION=0.4;

	// main method
	// Creates an Object of this class and launches the GUI. Entry point to the whole program.
	public static void main(String[] args) {
		//if(args.length==0) {
			UIManager myUI=new UIManager();
			try {
				myUI.setLookAndFeel(myUI.getSystemLookAndFeelClassName());
			} catch(java.lang.Exception e) {
				;
			}
			
			frame = new JFrame("BSP Decompiler by 005");
	
			window = new Window(frame.getContentPane());
		//} else {
		//	window = new Window(args);
		//}
		window.print("Got a bug to report? Want to see something added?"+(char)0x0D+(char)0x0A+"Create an issue report at"+(char)0x0D+(char)0x0A+"http://code.google.com/p/jbn-bsp-lump-tools/issues/entry"+(char)0x0D+(char)0x0A+(char)0x0D+(char)0x0A);
	}

	// All GUI components get initialized here
	private static JFileChooser file_selector;
	private static JFileChooser file_saver;
	private static JButton[] btn_abort;
	private static JTextField txt_coef;
	private static JTextField txt_threads;
	private static JTextArea consolebox;
	private static JLabel lbl_coef;
	private static JLabel lbl_threads;
	private static JSplitPane consoleTableSplitter;
	private static JScrollPane console_pane;
	private static JScrollPane table_pane;
	private static JCheckBoxMenuItem chk_planarItem;
	private static JCheckBoxMenuItem chk_skipPlaneFlipItem;
	private static JCheckBoxMenuItem chk_calcVertsItem;
	private static JCheckBoxMenuItem chk_roundNumsItem;
	private static JButton btn_dumplog;
	private static JProgressBar[] progressBar;
	private static JProgressBar totalProgressBar;
	private static JPanel pnl_jobs;
	private static JLabel lbl_spacer;
	
	// Private variables for a Window object
	private boolean vertexDecomp=true;
	private boolean correctPlaneFlip=true;
	private static double planePointCoef=100;
	private boolean calcVerts=true;
	private boolean roundNums=true;
	private static int numJobs;
	private static volatile int nextJob=0;

	// This constructor configures and displays the GUI
	public Window(Container pane) {
		// Set up most of the window's properties, since we definitely have a window
		// Good thing frame is a global object
		frame.setIconImage(new ImageIcon("icon32x32.PNG").getImage());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setPreferredSize(new Dimension(640, 480));

		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);			
		
		
		
		/// Menu Bar
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		optionsMenu = new JMenu("Options");
		menuBar.add(optionsMenu);
		
		// File menu
		decompVMFItem = new JMenuItem("Decompile to VMF");
		fileMenu.add(decompVMFItem);
		decompVMFItem.addActionListener(this);
		decompMAPItem = new JMenuItem("Decompile to MAP");
		fileMenu.add(decompMAPItem);
		decompMAPItem.addActionListener(this);
		fileMenu.addSeparator();
		exitItem = new JMenuItem("Exit");
		fileMenu.add(exitItem);
		exitItem.addActionListener(this);
		
		// Options menu
		//replaceEntitiesItem = new JMenuItem("Entity replacements...");
		//optionsMenu.add(replaceEntitiesItem);
		//replaceTexturesItem = new JMenuItem("Texture replacements...");
		//optionsMenu.add(replaceTexturesItem);
		//optionsMenu.addSeparator();
		chk_planarItem = new JCheckBoxMenuItem("Planar decompilation only");
		chk_planarItem.setToolTipText("Don't use vertices to aid decompilation. May result in longer decompilations, but may solve problems.");
		optionsMenu.add(chk_planarItem);
		chk_planarItem.addActionListener(this);
		chk_skipPlaneFlipItem = new JCheckBoxMenuItem("Skip plane flip");
		chk_skipPlaneFlipItem.setToolTipText("Don't make sure brush planes are facing the right direction. Speeds up decompilation in some cases, but may cause problems.");
		optionsMenu.add(chk_skipPlaneFlipItem);
		chk_skipPlaneFlipItem.addActionListener(this);
		chk_calcVertsItem = new JCheckBoxMenuItem("Calculate brush corners");
		chk_calcVertsItem.setToolTipText("Calculate every brush's corners. May solve problems arising from decompilation of faces with no vertex information.");
		chk_calcVertsItem.setSelected(false);
		optionsMenu.add(chk_calcVertsItem);
		chk_calcVertsItem.addActionListener(this);
		chk_roundNumsItem = new JCheckBoxMenuItem("Editor-style decimals");
		chk_roundNumsItem.setToolTipText("Rounds all decimals to the same precision as each map editor uses for its map format. Might make editors happier.");
		chk_roundNumsItem.setSelected(true);
		optionsMenu.add(chk_roundNumsItem);
		chk_roundNumsItem.addActionListener(this);
		
		frame.setJMenuBar(menuBar);
		
		
		
		/// Window contents
		pane.setLayout(new GridBagLayout());
		
		// First row
		
		lbl_coef = new JLabel("Plane points coefficient: ");
		
		GridBagConstraints coeflblConstraints = new GridBagConstraints();
		coeflblConstraints.fill = GridBagConstraints.NONE;
		coeflblConstraints.gridx = 0;
		coeflblConstraints.gridy = 0;
		coeflblConstraints.gridwidth = 1;
		coeflblConstraints.gridheight = 1;
		pane.add(lbl_coef, coeflblConstraints);
		
		txt_coef = new JTextField(5);
		txt_coef.setText("100");
		
		GridBagConstraints coefConstraints = new GridBagConstraints();
		coefConstraints.fill = GridBagConstraints.NONE;
		coefConstraints.gridx = 1;
		coefConstraints.gridy = 0;
		coefConstraints.gridwidth = 1;
		coefConstraints.gridheight = 1;
		pane.add(txt_coef, coefConstraints);
		
		lbl_spacer = new JLabel("                                                   ");
		
		GridBagConstraints spacelblConstraints = new GridBagConstraints();
		spacelblConstraints.fill = GridBagConstraints.NONE;
		spacelblConstraints.gridx = 2;
		spacelblConstraints.gridy = 0;
		spacelblConstraints.gridwidth = 1;
		spacelblConstraints.gridheight = 1;
		pane.add(lbl_spacer, spacelblConstraints);
		
		lbl_threads = new JLabel("Max concurrent decompiles: ");
		
		GridBagConstraints threadlblConstraints = new GridBagConstraints();
		threadlblConstraints.fill = GridBagConstraints.NONE;
		threadlblConstraints.gridx = 3;
		threadlblConstraints.gridy = 0;
		threadlblConstraints.gridwidth = 1;
		threadlblConstraints.gridheight = 1;
		pane.add(lbl_threads, threadlblConstraints);
		
		txt_threads = new JTextField(5);
		txt_threads.setText("1");
		
		GridBagConstraints threadConstraints = new GridBagConstraints();
		threadConstraints.fill = GridBagConstraints.NONE;
		threadConstraints.gridx = 4;
		threadConstraints.gridy = 0;
		threadConstraints.gridwidth = 1;
		threadConstraints.gridheight = 1;
		pane.add(txt_threads, threadConstraints);
		
		// Second row
		
		consolebox = new JTextArea();
		consolebox.setEditable(false);
		consolebox.setCursor(Cursor.getPredefinedCursor(Cursor.TEXT_CURSOR));
		
		console_pane = new JScrollPane(consolebox);
		console_pane.setBackground(new Color(255,255,255));
		
		pnl_jobs = new JPanel(new GridBagLayout());
		
		// Set up the initial jobs pane right here
		JLabel lbl_jobno = new JLabel("Job no.");
		lbl_jobno.setPreferredSize(new Dimension(40, 12));
		GridBagConstraints jobNoConstraints = new GridBagConstraints();
		jobNoConstraints.fill = GridBagConstraints.NONE;
		jobNoConstraints.gridx = 0;
		jobNoConstraints.gridy = 0;
		jobNoConstraints.gridwidth = 1;
		jobNoConstraints.gridheight = 1;
		pnl_jobs.add(lbl_jobno, jobNoConstraints);
		
		JLabel lbl_mapName = new JLabel("Map name");
		lbl_mapName.setPreferredSize(new Dimension(325, 12));
		GridBagConstraints mapNameConstraints = new GridBagConstraints();
		mapNameConstraints.fill = GridBagConstraints.NONE;
		mapNameConstraints.gridx = 1;
		mapNameConstraints.gridy = 0;
		mapNameConstraints.gridwidth = 1;
		mapNameConstraints.gridheight = 1;
		pnl_jobs.add(lbl_mapName, mapNameConstraints);
		
		JLabel lbl_progress = new JLabel("Status");
		lbl_progress.setPreferredSize(new Dimension(150, 12));
		GridBagConstraints progressConstraints = new GridBagConstraints();
		progressConstraints.fill = GridBagConstraints.NONE;
		progressConstraints.gridx = 2;
		progressConstraints.gridy = 0;
		progressConstraints.gridwidth = 1;
		progressConstraints.gridheight = 1;
		pnl_jobs.add(lbl_progress, progressConstraints);
		
		JLabel lbl_abort = new JLabel("Abort");
		lbl_abort.setPreferredSize(new Dimension(50, 12));
		GridBagConstraints abortlabelConstraints = new GridBagConstraints();
		abortlabelConstraints.fill = GridBagConstraints.NONE;
		abortlabelConstraints.gridx = 3;
		abortlabelConstraints.gridy = 0;
		abortlabelConstraints.gridwidth = 1;
		abortlabelConstraints.gridheight = 1;
		pnl_jobs.add(lbl_abort, abortlabelConstraints);
		
		table_pane = new JScrollPane(pnl_jobs);
		
		consoleTableSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, console_pane, table_pane);
		consoleTableSplitter.setPreferredSize(new Dimension(620, 380));
		consoleTableSplitter.setOneTouchExpandable(true);
		consoleTableSplitter.setDividerLocation(190);
		
		GridBagConstraints consoleConstraints = new GridBagConstraints();
		consoleConstraints.fill = GridBagConstraints.NONE;
		consoleConstraints.gridx = 0;
		consoleConstraints.gridy = 1;
		consoleConstraints.gridwidth = 5;
		consoleConstraints.gridheight = 1;
		pane.add(consoleTableSplitter, consoleConstraints);
		
		// Third row
		
		totalProgressBar = new JProgressBar(0, 1);
		
		GridBagConstraints totalBarConstraints = new GridBagConstraints();
		totalBarConstraints.fill = GridBagConstraints.NONE;
		totalBarConstraints.gridx = 0;
		totalBarConstraints.gridy = 2;
		totalBarConstraints.gridwidth = 1;
		totalBarConstraints.gridheight = 1;
		pane.add(totalProgressBar, totalBarConstraints);
		totalProgressBar.setStringPainted(true);
		totalProgressBar.setValue(0);
		totalProgressBar.setString("Total: 0%");
		
		btn_dumplog = new JButton("Save log");
		
		GridBagConstraints dumpConstraints = new GridBagConstraints();
		dumpConstraints.fill = GridBagConstraints.NONE;
		dumpConstraints.gridx = 4;
		dumpConstraints.gridy = 2;
		dumpConstraints.gridwidth = 1;
		dumpConstraints.gridheight = 1;
		pane.add(btn_dumplog, dumpConstraints);
		
		btn_dumplog.addActionListener(this);
		
	} // constructor
	
	public Window(String[] args) {
		System.out.println("BSP Decompiler by 005"); // This stuff only shows if run from console or cmd
		System.out.println("With special help from Alex \"UltimateSniper\" Herrod\n");
		String out="";
		
		for(int i=0;i<args.length;i++) {
			try {
				if(args[i].equalsIgnoreCase("-coef")) {
					Window.planePointCoef=Double.parseDouble(args[++i]);
				}
			} catch(java.lang.NumberFormatException e) { // if -coef is provided but something besides a number follows
				;
			} catch(java.lang.ArrayIndexOutOfBoundsException e) { // if -coef is provided but no further args exist
				;
			}
			if(args[i].equalsIgnoreCase("-c")) {
				calcVerts=true;
			}
			if(args[i].equalsIgnoreCase("-s")) {
				roundNums=true;
			}
			if(args[i].equalsIgnoreCase("-p")) {
				vertexDecomp=false;
			}
			if(args[i].equalsIgnoreCase("-f")) {
				correctPlaneFlip=false;
			}
			if(args[i].equalsIgnoreCase("-?")) {
				System.out.println("Usage:");
				System.out.println("decompiler.jar [options] <\"mappath\" \"mappath 2\" \"mappath 3\" etc.>");
				System.out.println("Options:");
				System.out.println("-toMAP: Decompile to Gearcraft MAP instead of Hammer 4.1 VMF format");
				System.out.println("-c: Calculate Brush Corners. Automatically set if -p without -s");
				System.out.println("-s: Snap to coordinates");
				System.out.println("-p: Planar Decompilation Only");
				System.out.println("-f: Skip plane flip");
				System.out.println("-coef #: Plane point coefficient (default 100)");
				System.out.println("-?: Show this help text");
			}
			if(args[i].equalsIgnoreCase("-toMAP")) {
			//	toVMF=false;
			} else {
				if(!out.equals("")) {
					out+=","+args[i];
				} else {
					out=args[i];
				}
			}
		}
		//startDecompilerThread(out);
	}

	// actionPerformed(ActionEvent)
	// Any time something happens on the GUI, this is called. However we're only
	// going to perform actions when certain things are clicked. The rest are discarded.
	public void actionPerformed(ActionEvent action) {
		if(action.getActionCommand().substring(0,6).equals("Abort ")) {
			int cancelJob=new Integer(action.getActionCommand().substring(6));
			stopDecompilerThread(cancelJob);
		}
		
		// TODO: Clean this up, perhaps use a switch instead
		// User clicks the "open" button
		if (action.getSource() == decompMAPItem || action.getSource() == decompVMFItem) {
			if(lastUsedFolder==null) {
				try {
					File dir = new File (".");
					file_selector = new JFileChooser(dir.getCanonicalPath());
				} catch(java.io.IOException e) {
					file_selector = new JFileChooser("/");
				}
			} else {
				file_selector = new JFileChooser(lastUsedFolder);
			}
			file_selector.addChoosableFileFilter(BSPFilter);
			file_selector.setMultiSelectionEnabled(true);
			int returnVal = file_selector.showOpenDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File[] files = file_selector.getSelectedFiles();
				for(int i=0;i<files.length;i++) {
					if(!files[i].exists() || files[i].isDirectory()) {
						files[i]=null; // Set any invalid files to null entries; this is easy to check later
					}
				}
				try {
					numThreads=new Integer(txt_threads.getText());
					if(numThreads<1) {
						throw new NumberFormatException();
					}
				} catch(NumberFormatException e) {
					numThreads=1;
				}
				if(decompilerworkers==null) {
					decompilerworkers=new Thread[numThreads];
				} else {
					if(decompilerworkers.length!=numThreads) {
						decompilerworkers=new Thread[numThreads];
					}
				}
				lastUsedFolder=files[files.length-1].getParent();
				if(action.getSource() == decompVMFItem) {
					addJobs(files, new DoomMap[files.length], true);
				} else { // There's only one other possibility :P
					addJobs(files, new DoomMap[files.length], false);
				}
			}
		}
		
		// User clicks the "Save log" button
		if(action.getSource() == btn_dumplog) {
			file_saver = new JFileChooser();
			file_saver.setSelectedFile(new File("DecompilerConsole.log"));
			file_saver.addChoosableFileFilter(LOGFilter);
			file_saver.setMultiSelectionEnabled(false);
			// file_selector.setIconImage(new ImageIcon("folder32x32.PNG").getImage());
			int returnVal = file_saver.showSaveDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String saveHere=file_saver.getSelectedFile().getAbsolutePath();
				try {
					if(!saveHere.substring(saveHere.length()-4).equalsIgnoreCase(".LOG")) {
						saveHere+=".log";
					}
				} catch(java.lang.StringIndexOutOfBoundsException e) {
					saveHere+=".log";
				}
				if(saveHere!=null) {
					File logfile = new File(saveHere);
					try {
						logfile.createNewFile();
						byte[] out=consolebox.getText().getBytes();
						FileOutputStream logWriter=new FileOutputStream(logfile);
						logWriter.write(out);
						logWriter.close();
						Window.window.println("Log file saved!");
					} catch(java.io.IOException e) {
						Window.window.println("Unable to create file "+logfile.getAbsolutePath()+(char)0x0D+(char)0x0A+"Ensure the filesystem is not read only!");
					}
				}
			}
		}
		
		if(action.getSource() == chk_planarItem) {
			vertexDecomp=!chk_planarItem.isSelected();
		}
		
		if(action.getSource() == chk_skipPlaneFlipItem) {
			correctPlaneFlip=!chk_skipPlaneFlipItem.isSelected();
		}
		
		if(action.getSource() == chk_calcVertsItem) {
			calcVerts=chk_calcVertsItem.isSelected();
		}
		
		if(action.getSource() == chk_roundNumsItem) {
			roundNums=chk_roundNumsItem.isSelected();
		}
		
		if(action.getSource() == chk_planarItem || action.getSource() == chk_skipPlaneFlipItem) {
			chk_calcVertsItem.setEnabled(!(chk_planarItem.isSelected() && !chk_skipPlaneFlipItem.isSelected()));
			if(chk_planarItem.isSelected() && !chk_skipPlaneFlipItem.isSelected()) {
				chk_calcVertsItem.setSelected(true);
				calcVerts=true;
			}
		}
		
		if(action.getSource() == exitItem) {
			//if(decompilerworkers[0]!=null) {
			//	add some warning dialog here?
			//} else {
				System.exit(0);
			//}
		}
	}
	
	// This method actually starts a thread for the specified job
	private void startDecompilerThread(int newThread, int jobNum) {
		File job=jobs[jobNum];
		// I'd really like to use Thread.join() or something here, but the stupid thread never dies.
		// But if somethingFinished is true, then one of the threads is telling us it's finished anyway.
		if(doomJobs[jobNum]!=null) {
			runMe=new DecompilerThread(doomJobs[jobNum], toVMF[jobNum], roundNums, jobNum, newThread);
		} else {
			runMe=new DecompilerThread(job, vertexDecomp, correctPlaneFlip, calcVerts, roundNums, toVMF[jobNum], jobNum, newThread);
		}
		decompilerworkers[newThread] = new Thread(runMe);
		decompilerworkers[newThread].setName("Decompiler "+newThread+" job "+jobNum);
		decompilerworkers[newThread].setPriority(Thread.MIN_PRIORITY);
		decompilerworkers[newThread].start();
		threadNum[jobNum]=newThread;
		println("Started job #"+(jobNum+1));
		progressBar[jobNum].setIndeterminate(false);
	}
	
	private void stopDecompilerThread(int job) {
		int currentThread=threadNum[job-1];
		if(currentThread>-1) {
			decompilerworkers[currentThread].stop(); // The Java API lists this method of stopping a thread as deprecated.
			startNextJob(true, currentThread);       // For the purposes of this program, I'm not sure it's an issue though.
		}                                           // More info: http://docs.oracle.com/javase/6/docs/technotes/guides/concurrency/threadPrimitiveDeprecation.html
		println("Job number "+job+" aborted by user.");               
		jobs[job-1]=null;
		btn_abort[job-1].setEnabled(false);
		btn_abort[job-1].setText("Aborted!");
		progressBar[job-1].setIndeterminate(false);
		progressBar[job-1].setString("Aborted!");
	}
	
	protected static void print(String out) {
		if(consolebox!=null) {
			int start,end;
			start=consolebox.getSelectionStart();
			end=consolebox.getSelectionEnd();
			consolebox.append(out);
			if(start==end) {
				consolebox.setCaretPosition(consolebox.getText().length());
			} else { // In case user is highlighting something while adding text
				consolebox.setSelectionStart(start);
				consolebox.setSelectionEnd(end);
			}
		} else {
			System.out.print(out);
		}
	}
	
	protected static void println(String out) {
		print(out+(char)0x0D+(char)0x0A);
	}
	
	protected static void clearConsole() {
		if(consolebox!=null) {
			consolebox.replaceRange("", 0, consolebox.getText().length());
		}
	}
	
	protected static void setProgress(int jobmod, int in, int max, String status) {
		if(progressBar[jobmod]!=null) {
			progressBar[jobmod].setMaximum(max);
			progressBar[jobmod].setValue(in);
			if((int)((in/(float)max)*100)==100 || (int)((in/(float)max)*100)==0) {
				progressBar[jobmod].setString(status);
			} else {
				progressBar[jobmod].setString(status+" "+(int)((in/(float)max)*100)+"%");
			}
		}
	}
	
	protected static void setProgressColor(int jobmod, Color color) {
		if(progressBar[jobmod]!=null) {
			progressBar[jobmod].setForeground(color);
		}
	}
	
	protected static void setTotalProgress(int in, int max) {
		if(totalProgressBar!=null) {
			totalProgressBar.setMaximum(max);
			totalProgressBar.setValue(in);
			if(in==max) {
				totalProgressBar.setString("Done!");
			} else {
				totalProgressBar.setString("Total: "+(int)((in/(float)max)*100)+"%");
			}
		}
	}
	
	protected static void setAbortButtonEnabled(int jobmod, boolean in) {
		if(btn_abort[jobmod]!=null) {
			btn_abort[jobmod].setEnabled(in);
		}
	}
	
	protected static void setOptionsEnabled(boolean in) {
		txt_threads.setEnabled(in);
		chk_planarItem.setEnabled(in);
		chk_skipPlaneFlipItem.setEnabled(in);
		chk_calcVertsItem.setEnabled(in);
		chk_roundNumsItem.setEnabled(in);
	}
	
	protected void addJob(File newJob, DoomMap newDoomJob, boolean VMFDecompile) {
		if(newJob!=null || newDoomJob!=null) {
			File[] newList = new File[jobs.length+1];
			DoomMap[] newDoomList = new DoomMap[jobs.length+1];
			boolean[] newToVMF = new boolean[jobs.length+1];
			int[] newThreadNo=new int[jobs.length+1];
			for(int j=0;j<jobs.length;j++) {
				newList[j]=jobs[j];
				newDoomList[j]=doomJobs[j];
				newToVMF[j]=toVMF[j];
				newThreadNo[j]=threadNum[j];
			}
			newList[jobs.length]=newJob;
			newDoomList[jobs.length]=newDoomJob;
			newToVMF[jobs.length]=VMFDecompile;
			newThreadNo[jobs.length]=-1;
			jobs=newList;
			doomJobs=newDoomList;
			toVMF=newToVMF;
			threadNum=newThreadNo;
			
			// Add another row to the jobs pane
			JLabel lbl_jobno = new JLabel(++numJobs+"");
			lbl_jobno.setPreferredSize(new Dimension(40, 12));
			GridBagConstraints jobNoConstraints = new GridBagConstraints();
			jobNoConstraints.fill = GridBagConstraints.NONE;
			jobNoConstraints.gridx = 0;
			jobNoConstraints.gridy = numJobs;
			jobNoConstraints.gridwidth = 1;
			jobNoConstraints.gridheight = 1;
			pnl_jobs.add(lbl_jobno, jobNoConstraints);
			
			JLabel lbl_mapName;
			if(newDoomJob!=null) { // If this isn't null it's a DoomMap
				lbl_mapName = new JLabel(newDoomJob.getWadName()+"\\"+newDoomJob.getMapName());
			} else {
				lbl_mapName = new JLabel(newJob.getName());
			}
			lbl_mapName.setPreferredSize(new Dimension(325, 12));
			GridBagConstraints mapNameConstraints = new GridBagConstraints();
			mapNameConstraints.fill = GridBagConstraints.NONE;
			mapNameConstraints.gridx = 1;
			mapNameConstraints.gridy = numJobs;
			mapNameConstraints.gridwidth = 1;
			mapNameConstraints.gridheight = 1;
			pnl_jobs.add(lbl_mapName, mapNameConstraints);
			
			JProgressBar[] newBars=new JProgressBar[numJobs];
			for(int j=0;j<numJobs-1;j++) {
				newBars[j]=progressBar[j];
			}
			newBars[numJobs-1]=new JProgressBar(0, 1);
			progressBar=newBars;
			progressBar[numJobs-1].setString("Queued");
			progressBar[numJobs-1].setStringPainted(true);
			progressBar[numJobs-1].setValue(0);
			progressBar[numJobs-1].setIndeterminate(true);
			GridBagConstraints progressConstraints = new GridBagConstraints();
			progressConstraints.fill = GridBagConstraints.NONE;
			progressConstraints.gridx = 2;
			progressConstraints.gridy = numJobs;
			progressConstraints.gridwidth = 1;
			progressConstraints.gridheight = 1;
			pnl_jobs.add(progressBar[numJobs-1], progressConstraints);
			
			JButton[] newButtons=new JButton[numJobs];
			for(int j=0;j<numJobs-1;j++) {
				newButtons[j]=btn_abort[j];
			}
			newButtons[numJobs-1]=new JButton("Abort "+numJobs);
			btn_abort=newButtons;
			GridBagConstraints abortbuttonConstraints = new GridBagConstraints();
			abortbuttonConstraints.fill = GridBagConstraints.NONE;
			abortbuttonConstraints.gridx = 3;
			abortbuttonConstraints.gridy = numJobs;
			abortbuttonConstraints.gridwidth = 1;
			abortbuttonConstraints.gridheight = 1;
			pnl_jobs.add(btn_abort[numJobs-1], abortbuttonConstraints);
			btn_abort[numJobs-1].addActionListener(this);
			startNextJob(false, -1);
		}
		table_pane.updateUI(); // If you don't do this, none of the changes are reflected
		                       // in the UI, I don't know why, or how this fixes it, or
		                       // if this is even the correct way to do this. But it works.
	}
	
	protected void addJobs(File[] newJobs, DoomMap[] newDoomJobs, boolean VMFDecompile) {
		int realNewJobs=0;
		for(int i=0;i<newJobs.length;i++) {
			if(newJobs[i]!=null || newDoomJobs[i]!=null) {
				realNewJobs++;
				addJob(newJobs[i], newDoomJobs[i], VMFDecompile);
			}
		}
		println("Added "+realNewJobs+" new jobs to queue");
	}
		
	// This method queues up the next job and makes sure to run a thread
	protected void startNextJob(boolean somethingFinished, int threadFinished) {
		int myJob=nextJob++; // Increment this right away, then use myJob. For thread safety, in case two threads are using this method at once.
		// This isn't a perfect, or ideal solution, and it's not 100% failproof. But it is much safer.
		setTotalProgress(myJob, numJobs);
		if(myJob==0) {
			clearConsole();
		}
		if(myJob<numJobs) {
			if(jobs[myJob]==null) { // If this job was aborted, and/or doesn't exist
				if(doomJobs[myJob]==null) { // And it's not a Doom map
					startNextJob(somethingFinished, threadFinished); // Try the next one. If it was also aborted this will recurse until either we get to a job or finish
				} else {
					if(somethingFinished) {
						startDecompilerThread(threadFinished, myJob);
						setOptionsEnabled(false);
					} else {
						for(int i=0;i<numThreads;i++) {
							try {
								if(!decompilerworkers[i].isAlive()) {
									startDecompilerThread(i, myJob);
									setOptionsEnabled(false);
									break;
								}
							} catch(java.lang.NullPointerException e) {
								startDecompilerThread(i, myJob);
								setOptionsEnabled(false);
								break;
							}
							if(i+1==numThreads) {
								nextJob--;
							}
						}
					}
				}
			} else {
				if(somethingFinished) {
					startDecompilerThread(threadFinished, myJob);
					setOptionsEnabled(false);
				} else {
					for(int i=0;i<numThreads;i++) {
						try {
							if(!decompilerworkers[i].isAlive()) {
								startDecompilerThread(i, myJob);
								setOptionsEnabled(false);
								break;
							}
						} catch(java.lang.NullPointerException e) {
							startDecompilerThread(i, myJob);
							setOptionsEnabled(false);
							break;
						}
						if(i+1==numThreads) {
							// If we reach this point the thread hasn't been started yet
							nextJob--; // If the thread can't start (yet), undo the changes to nextJob and stop.
							// This is one of the caveats of this way of doing things. If the thread can't start you
							// need to undo this. Luckily, I believe the only time we'll run into this case is when
							// a job is added when decompiles are already running.
						}
					}
				}
			}
		} else {
			setOptionsEnabled(true); // Even if this is called when one thread is finished, if another is starting it doesn't matter
			nextJob--;
			r.gc(); // Now the program has time to rest while the user does whatever. Collect garbage.
		}
	}
	
	public static double getPlanePointCoef() {
		return planePointCoef;
	}
}
