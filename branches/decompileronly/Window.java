// Window class

// GUI for the decompiler.
// For a list of swing components check here:
// http://download.oracle.com/javase/tutorial/ui/features/components.html

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowAdapter;
import javax.swing.*;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.Scanner;

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

	private static JFrame frame;
	private static JMenuBar menuBar;

	private static JMenu fileMenu;
	private static JMenuItem decompVMFItem;
	private static JMenuItem decompMAPItem; 
	private static JMenuItem decompRadiantItem; 
	private static JMenuItem exitItem;

	private static JMenu optionsMenu;
	// private static JMenuItem replaceEntitiesItem;
	// private static JMenuItem replaceTexturesItem;
	private static JMenuItem setPlanePointCoefItem;
	private static JMenuItem setThreadsItem;
	private static JMenuItem setOutFolderItem;
	private static JCheckBoxMenuItem chk_planarItem;
	private static JCheckBoxMenuItem chk_skipPlaneFlipItem;
	private static JCheckBoxMenuItem chk_calcVertsItem;
	private static JCheckBoxMenuItem chk_roundNumsItem;

	private static JMenu debugMenu;
	private static JCheckBoxMenuItem chk_brushesToWorldItem;
	private static JCheckBoxMenuItem chk_noDetailsItem;
	private static JCheckBoxMenuItem chk_noFaceFlagsItem;
	private static JMenuItem setErrorItem;
	private static JMenuItem setOriginBrushSizeItem;
	private static JMenu specialMenu;
	private static JCheckBoxMenuItem chk_replaceWithNull;
	private static JCheckBoxMenuItem chk_visLeafBBoxes;
	private static JMenuItem saveLogItem;
	private static JMenu verbosityMenu;
	private static ButtonGroup verbosityGroup;
	private static JRadioButtonMenuItem rad_verbosity_0;
	private static JRadioButtonMenuItem rad_verbosity_1;
	private static JRadioButtonMenuItem rad_verbosity_2;
	private static JRadioButtonMenuItem rad_verbosity_3;
	private static JRadioButtonMenuItem rad_verbosity_4;

	private static File[] jobs=new File[0];
	private static DoomMap[] doomJobs=new DoomMap[0];
	private static boolean[] toHammer;
	private static boolean[] toGC;
	private static boolean[] toRadiant;
	private static int[] threadNum;
	private static Runnable runMe;
	private static Thread[] decompilerworkers=null;
	private static int numThreads=1;
	private static double planePointCoef=100;

	private static String lastUsedFolder;
	
	private static double precision=0.01;
	// The number of decimal places of error allowed for double-precision decimal
	// calculations. Down the line, this error can propagate fast.
	// 0.01 is a reasonable level. 0.00001 is quite strict. 0.1 is a little ridiculous.

	// main method
	// Creates an Object of this class and launches the GUI. Entry point to the whole program.
	private static double originBrushSize=16;
	private static int verbosity=0;
	private static String outputFolder="default";
	
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
		window.print("Got a bug to report? Want to request a feature?"+(char)0x0D+(char)0x0A+"Create an issue report at"+(char)0x0D+(char)0x0A+"http://code.google.com/p/jbn-bsp-lump-tools/issues/entry"+(char)0x0D+(char)0x0A+(char)0x0D+(char)0x0A, 0);
	}

	// All GUI components get initialized here
	private static JFileChooser file_selector;
	private static JButton[] btn_abort;
	private static JButton btn_abort_all;
	private static JTextArea consolebox;
	private static JLabel lbl_coef;
	private static JLabel lbl_threads;
	private static JLabel lbl_mapName;
	private static JSplitPane consoleTableSplitter;
	private static JScrollPane console_pane;
	private static JScrollPane table_pane;
	private static JLabel[] mapNames;
	private static JProgressBar[] progressBar;
	private static JProgressBar totalProgressBar;
	private static JPanel pnl_jobs;
	private static JLabel lbl_spacer;
	
	// Private variables for a Window object
	private boolean vertexDecomp=true;
	private boolean correctPlaneFlip=true;
	private boolean calcVerts=false;
	private boolean roundNums=true;
	private static int numJobs;
	private static volatile int nextJob=0;

	// This constructor configures and displays the GUI
	public Window(Container pane) {
		// Set up most of the window's properties, since we definitely have a window
		// Good thing frame is a global object
		frame.setIconImage(new ImageIcon("icon32x32.PNG").getImage());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.setPreferredSize(new Dimension(640, 460));

		frame.pack();
		frame.setResizable(true);
		frame.setVisible(true);
		
		frame.setMinimumSize(new Dimension(316, 240));
		
		// Menu Bar
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		optionsMenu = new JMenu("Options");
		menuBar.add(optionsMenu);
		debugMenu = new JMenu("Debug");
		menuBar.add(debugMenu);
		
		// File menu
		decompVMFItem = new JMenuItem("Decompile to VMF");
		fileMenu.add(decompVMFItem);
		decompVMFItem.addActionListener(this);
		decompMAPItem = new JMenuItem("Decompile to Gearcraft MAP");
		fileMenu.add(decompMAPItem);
		decompMAPItem.addActionListener(this); 
		decompRadiantItem = new JMenuItem("Decompile to Radiant MAP");
		fileMenu.add(decompRadiantItem);
		decompRadiantItem.addActionListener(this); 
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
		setPlanePointCoefItem=new JMenuItem("Set plane point coefficient...");
		setPlanePointCoefItem.setToolTipText("If not calculating brush corners, this is used in calculating points on planes. Might tweak invalid solids into working.");
		setPlanePointCoefItem.addActionListener(this);
		optionsMenu.add(setPlanePointCoefItem);
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
		optionsMenu.addSeparator();
		
		setThreadsItem=new JMenuItem("Set number of threads...");
		setThreadsItem.setToolTipText("The job system is multithreaded and multiple maps can be decompiled simultaneously, especially on multiprocessor CPUs.");
		setThreadsItem.addActionListener(this);
		optionsMenu.add(setThreadsItem);
		setOutFolderItem=new JMenuItem("Set output folder...");
		setOutFolderItem.setToolTipText("Set where to save output mapfiles. Click cancel to use the folder where the map came from.");
		setOutFolderItem.addActionListener(this);
		optionsMenu.add(setOutFolderItem);
		
		// Debug menu
		chk_brushesToWorldItem = new JCheckBoxMenuItem("Dump all brushes to world");
		chk_brushesToWorldItem.setToolTipText("Send all brushes to world entity, rather than to their entities.");
		chk_brushesToWorldItem.setSelected(false);
		debugMenu.add(chk_brushesToWorldItem);
		chk_noDetailsItem = new JCheckBoxMenuItem("Ignore detail flags");
		chk_noDetailsItem.setToolTipText("Disregard detail flags on brushes. All detail brushes will be world geometry, and will block VIS.");
		chk_noDetailsItem.setSelected(false);
		debugMenu.add(chk_noDetailsItem);
		chk_noFaceFlagsItem = new JCheckBoxMenuItem("Ignore face flags");
		chk_noFaceFlagsItem.setToolTipText("Disregard face flags (NODRAW, NOIMPACTS, etc.)");
		chk_noFaceFlagsItem.setSelected(false);
		debugMenu.add(chk_noFaceFlagsItem);
		setErrorItem = new JMenuItem("Set error tolerance...");
		setErrorItem.setToolTipText("Allows customization of error tolerance of double precision calculations.");
		debugMenu.add(setErrorItem);
		setOriginBrushSizeItem = new JMenuItem("Set origin brush size...");
		setOriginBrushSizeItem.setToolTipText("Origin brushes are generated on the fly. This allows customization of their size.");
		debugMenu.add(setOriginBrushSizeItem);
		setOriginBrushSizeItem.addActionListener(this);
		
		debugMenu.addSeparator();
		
		specialMenu = new JMenu("Special requests");
		debugMenu.add(specialMenu);
		debugMenu.addSeparator();
		verbosityMenu = new JMenu("Log verbosity");
		debugMenu.add(verbosityMenu);
		saveLogItem = new JMenuItem("Save log");
		saveLogItem.setToolTipText("Save all text in output log to a file.");
		saveLogItem.addActionListener(this);
		debugMenu.add(saveLogItem);
		
		chk_replaceWithNull = new JCheckBoxMenuItem("Replace flag 512 with special/null");
		chk_replaceWithNull.setSelected(false);
		specialMenu.add(chk_replaceWithNull);
		chk_visLeafBBoxes = new JCheckBoxMenuItem("Place brushes on visleaf bounding boxes");
		chk_visLeafBBoxes.setSelected(false);
		specialMenu.add(chk_visLeafBBoxes);
		
		verbosityGroup=new ButtonGroup();
		rad_verbosity_0=new JRadioButtonMenuItem("Status only");
		rad_verbosity_1=new JRadioButtonMenuItem("Output map statistics");
		rad_verbosity_2=new JRadioButtonMenuItem("Show warnings");
		rad_verbosity_3=new JRadioButtonMenuItem("Show brush correction method calls");
		rad_verbosity_4=new JRadioButtonMenuItem("Show all brush creation method calls");
		verbosityGroup.add(rad_verbosity_0);
		verbosityGroup.add(rad_verbosity_1);
		verbosityGroup.add(rad_verbosity_2);
		verbosityGroup.add(rad_verbosity_3);
		verbosityGroup.add(rad_verbosity_4);
		verbosityMenu.add(rad_verbosity_0);
		verbosityMenu.add(rad_verbosity_1);
		verbosityMenu.add(rad_verbosity_2);
		verbosityMenu.add(rad_verbosity_3);
		verbosityMenu.add(rad_verbosity_4);
		rad_verbosity_0.setSelected(true);
		rad_verbosity_0.addActionListener(this);
		rad_verbosity_1.addActionListener(this);
		rad_verbosity_2.addActionListener(this);
		rad_verbosity_3.addActionListener(this);
		rad_verbosity_4.addActionListener(this);
		
		frame.setJMenuBar(menuBar);
		
		
		
		/// Window contents
		pane.setLayout(new GridBagLayout());
		
		// First row
		
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
		
		lbl_mapName = new JLabel("Map name");
		lbl_mapName.setPreferredSize(new Dimension(325, 12));
		GridBagConstraints mapNameConstraints = new GridBagConstraints();
		mapNameConstraints.fill = GridBagConstraints.NONE;
		mapNameConstraints.gridx = 1;
		mapNameConstraints.gridy = 0;
		mapNameConstraints.gridwidth = 1;
		mapNameConstraints.gridheight = 1;
		pnl_jobs.add(lbl_mapName, mapNameConstraints);
		
		totalProgressBar = new JProgressBar(0, 1);
		
		GridBagConstraints totalBarConstraints = new GridBagConstraints();
		totalBarConstraints.fill = GridBagConstraints.NONE;
		totalBarConstraints.gridx = 2;
		totalBarConstraints.gridy = 0;
		totalBarConstraints.gridwidth = 1;
		totalBarConstraints.gridheight = 1;
		pnl_jobs.add(totalProgressBar, totalBarConstraints);
		totalProgressBar.setStringPainted(true);
		totalProgressBar.setValue(0);
		totalProgressBar.setString("Total: 0%");
		
		JButton btn_abort_all = new JButton("Abort all");
		btn_abort_all.setPreferredSize(new Dimension(75, 20));
		GridBagConstraints abortbuttonConstraints = new GridBagConstraints();
		abortbuttonConstraints.fill = GridBagConstraints.NONE;
		abortbuttonConstraints.gridx = 3;
		abortbuttonConstraints.gridy = 0;
		abortbuttonConstraints.gridwidth = 1;
		abortbuttonConstraints.gridheight = 1;
		pnl_jobs.add(btn_abort_all, abortbuttonConstraints);
		btn_abort_all.addActionListener(this);
		
		table_pane = new JScrollPane(pnl_jobs);
		
		consoleTableSplitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, console_pane, table_pane);
		consoleTableSplitter.setPreferredSize(new Dimension(616, 390));
		consoleTableSplitter.setOneTouchExpandable(true);
		consoleTableSplitter.setDividerLocation(190);
		
		GridBagConstraints consoleConstraints = new GridBagConstraints();
		consoleConstraints.fill = GridBagConstraints.NONE;
		consoleConstraints.gridx = 0;
		consoleConstraints.gridy = 0;
		consoleConstraints.gridwidth = 1;
		consoleConstraints.gridheight = 1;
		pane.add(consoleTableSplitter, consoleConstraints);
		
		// This crap allows the window to be resized, and sets the size of the GUI components as needed.
		frame.addWindowStateListener(new WindowAdapter() {
			public void windowStateChanged(WindowEvent event) {
				consoleTableSplitter.setPreferredSize(new Dimension(frame.getWidth()-24, frame.getHeight()-70));
				lbl_mapName.setPreferredSize(new Dimension(frame.getWidth()-315, 12));
				for(int i=0;i<numJobs;i++) {
					mapNames[i].setPreferredSize(new Dimension(frame.getWidth()-315, 12));
				}
				frame.validate();
			}
		});
		frame.addComponentListener(new ComponentAdapter() {
			public void componentResized(ComponentEvent comp) {
				consoleTableSplitter.setPreferredSize(new Dimension(frame.getWidth()-24, frame.getHeight()-70));
				lbl_mapName.setPreferredSize(new Dimension(frame.getWidth()-315, 12));
				for(int i=0;i<numJobs;i++) {
					mapNames[i].setPreferredSize(new Dimension(frame.getWidth()-315, 12));
				}
				frame.validate();
			}
		});
		
		frame.validate(); // make sure everything is actually SHOWN
		
	} // constructor
	
	public Window(String[] args) {
		System.out.println("BSP Decompiler by 005"); // This stuff only shows if run from console or cmd
		String out="";
		
		for(int i=0;i<args.length;i++) {
			try {
				if(args[i].equalsIgnoreCase("-coef")) {
					planePointCoef=Double.parseDouble(args[++i]);
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
			try {
				int cancelJob=new Integer(action.getActionCommand().substring(6));
				stopDecompilerThread(cancelJob);
			} catch(java.lang.NumberFormatException e) {
				for(int i=numJobs;i>0;i--) { // Start from the last one and work towards the first.
					try {
						if(btn_abort[i-1].getText().substring(0,6).equals("Abort ")) {
							stopDecompilerThread(i); // This prevents new threads from starting while this loop is running.
						}
					} catch(java.lang.StringIndexOutOfBoundsException f) {
						;
					}
				}
			}
		}
		
		// User clicks the "open" button
		if (action.getSource() == decompMAPItem || action.getSource() == decompVMFItem || action.getSource() == decompRadiantItem) {
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
			file_selector.setAcceptAllFileFilterUsed(false); // "all files". I would like this to be AFTER the others.
			file_selector.addChoosableFileFilter(new SupportedFileFilter());
			file_selector.addChoosableFileFilter(new BSPFileFilter());
			file_selector.addChoosableFileFilter(new WADFileFilter());
			file_selector.setAcceptAllFileFilterUsed(true); // Setting this false above then true here forces the "all files" filter to be last.
			file_selector.setMultiSelectionEnabled(true);
			int returnVal = file_selector.showOpenDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File[] files = file_selector.getSelectedFiles();
				for(int i=0;i<files.length;i++) {
					if(!files[i].exists() || files[i].isDirectory()) {
						files[i]=null; // Set any invalid files to null entries; this is easy to check later
					}
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
					addJobs(files, new DoomMap[files.length], true, false, false);
				} else { 
					if(action.getSource() == decompRadiantItem) {
						addJobs(files, new DoomMap[files.length], false, true, false);
					} else {
						addJobs(files, new DoomMap[files.length], false, false, true);
					}
				}
			}
		}
		
		// User clicks the "Save log" button
		if(action.getSource() == saveLogItem) {
			file_selector = new JFileChooser();
			file_selector.setSelectedFile(new File("DecompilerConsole.log"));
			file_selector.addChoosableFileFilter(new LOGFileFilter());
			file_selector.setAcceptAllFileFilterUsed(false);
			file_selector.setMultiSelectionEnabled(false);
			int returnVal = file_selector.showSaveDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				String saveHere=file_selector.getSelectedFile().getAbsolutePath();
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
						Window.println("Log file saved!",0);
					} catch(java.io.IOException e) {
						Window.println("Unable to create file "+logfile.getAbsolutePath()+(char)0x0D+(char)0x0A+"Ensure the filesystem is not read only!",0);
					}
				}
			}
		}
		
		// change the output folder
		if(action.getSource() == setOutFolderItem) {
			file_selector = new JFileChooser();
			file_selector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			file_selector.setAcceptAllFileFilterUsed(false);
			file_selector.setMultiSelectionEnabled(false);
			int returnVal = file_selector.showSaveDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File saveHere=file_selector.getSelectedFile();
				if(saveHere!=null) {
					if(!saveHere.exists()) {
						Window.println(saveHere.getAbsolutePath()+" does not exist! Setting to \"default\" instead.",0);
						outputFolder="default";
					} else {
						if(!saveHere.isDirectory()) {
							Window.println(saveHere.getAbsolutePath()+" is not a directory! Setting to \"default\" instead.",0);
							outputFolder="default";
						} else {
							Window.println("Output directory set to "+saveHere.getAbsolutePath(),0);
							outputFolder=saveHere.getAbsolutePath();
						}
					}
				} else {
					Window.println("Output folder set to default.",0);
					outputFolder="default";
				}
			} else {
				Window.println("Output folder set to default.",0);
				outputFolder="default";
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
		
		if(action.getSource() == setErrorItem) {
			String st=(String)JOptionPane.showInputDialog(frame,"Please enter a new error tolerance value.\n"+
			          "This value is used to compensate for error propagation in double precision calculations.\n"+
			          "Typical values are between 0.00001 and 0.5. Current value: "+precision,"Enter new error tolerance",
			          JOptionPane.QUESTION_MESSAGE,null,null,precision);
			try {
				double temp = Double.parseDouble(st);
				if(temp<0) {
					throw new java.lang.NumberFormatException();
				} else {
					precision=temp;
					println("Error tolerance set to "+precision+".",0);
				}
			} catch(java.lang.NumberFormatException e) {
				println("Invalid error tolerance! Tolerance is "+precision+" instead!",0);
			} catch(java.lang.NullPointerException e) { // Happens when user hits "cancel". Do nothing.
				;
			}
		}
		
		if(action.getSource() == setOriginBrushSizeItem) {
			String st=(String)JOptionPane.showInputDialog(frame,"Please enter a new origin brush size.\n"+
			          "Current value: "+originBrushSize,"Enter new origin brush size",JOptionPane.QUESTION_MESSAGE,null,null,originBrushSize);
			try {
				double temp = Double.parseDouble(st);
				if(temp<0) {
					throw new java.lang.NumberFormatException();
				} else {
					originBrushSize=temp;
					println("Origin brush size set to "+originBrushSize+".",0);
				}
			} catch(java.lang.NumberFormatException e) {
				println("Invalid brush size! Size remains "+originBrushSize+" instead!",0);
			} catch(java.lang.NullPointerException e) { // Happens when user hits "cancel". Do nothing.
				;
			}
		}
		
		if(action.getSource() == setThreadsItem) {
			String st=(String)JOptionPane.showInputDialog(frame,"Please enter number of concurrent decompiles allowed.\n"+
			          "Current value: "+numThreads,"Enter new thread amount",JOptionPane.QUESTION_MESSAGE,null,null,numThreads);
			try {
				int temp = Integer.parseInt(st);
				if(temp<0) {
					throw new java.lang.NumberFormatException();
				} else {
					numThreads=temp;
					println("Num threads set to "+numThreads+".",0);
				}
			} catch(java.lang.NumberFormatException e) {
				println("Invalid number of threads! Thread count remains "+numThreads+" instead!",0);
			} catch(java.lang.NullPointerException e) { // Happens when user hits "cancel". Do nothing.
				;
			}
		}
		
		if(action.getSource() == setPlanePointCoefItem) {
			String st=(String)JOptionPane.showInputDialog(frame,"Please enter plane point coefficient.\n"+
			          "Current value: "+planePointCoef,"Enter new coefficient",JOptionPane.QUESTION_MESSAGE,null,null,planePointCoef);
			try {
				double temp = Double.parseDouble(st);
				if(temp==0) {
					throw new java.lang.NumberFormatException();
				} else {
					planePointCoef=temp;
					println("Coefficient set to "+planePointCoef+".",0);
				}
			} catch(java.lang.NumberFormatException e) {
				println("Invalid coefficient! Coefficient remains "+planePointCoef+" instead!",0);
			} catch(java.lang.NullPointerException e) { // Happens when user hits "cancel". Do nothing.
				;
			}
		}
		
		if(action.getSource() == rad_verbosity_0) {
			verbosity=0;
		}
		
		if(action.getSource() == rad_verbosity_1) {
			verbosity=1;
		}
		
		if(action.getSource() == rad_verbosity_2) {
			verbosity=2;
		}
		
		if(action.getSource() == rad_verbosity_3) {
			verbosity=3;
		}
		
		if(action.getSource() == rad_verbosity_4) {
			verbosity=4;
		}
	}
	
	// This method actually starts a thread for the specified job
	private void startDecompilerThread(int newThread, int jobNum) {
		File job=jobs[jobNum];
		// I'd really like to use Thread.join() or something here, but the stupid thread never dies.
		// But if somethingFinished is true, then one of the threads is telling us it's finished anyway.
		if(doomJobs[jobNum]!=null) {
			runMe=new DecompilerThread(doomJobs[jobNum], toHammer[jobNum], toRadiant[jobNum], toGC[jobNum], roundNums, jobNum, newThread);
		} else {
			runMe=new DecompilerThread(job, vertexDecomp, correctPlaneFlip, calcVerts, roundNums, toHammer[jobNum], toRadiant[jobNum], toGC[jobNum], jobNum, newThread);
		}
		decompilerworkers[newThread] = new Thread(runMe);
		decompilerworkers[newThread].setName("Decompiler "+newThread+" job "+jobNum);
		decompilerworkers[newThread].setPriority(Thread.MIN_PRIORITY);
		println("Starting job #"+(jobNum+1),0);
		decompilerworkers[newThread].start();
		threadNum[jobNum]=newThread;
		progressBar[jobNum].setIndeterminate(false);
	}
	
	private void stopDecompilerThread(int job) {
		int currentThread=threadNum[job-1];
		if(currentThread>-1) {
			decompilerworkers[currentThread].stop();          // The Java API lists this method of stopping a thread as deprecated.
			startNextJob(true, currentThread);                // For the purposes of this program, I'm not sure it's an issue though.
			println("Job number "+job+" aborted by user.",0); // More info: http://docs.oracle.com/javase/6/docs/technotes/guides/concurrency/threadPrimitiveDeprecation.html
		} else {
			println("Job number "+job+" canceled by user.",0);
		}
		jobs[job-1]=null;
		doomJobs[job-1]=null;
		btn_abort[job-1].setEnabled(false);
		btn_abort[job-1].setText("Aborted!");
		progressBar[job-1].setIndeterminate(false);
		progressBar[job-1].setString("Aborted!");
	}
	
	protected static void print(String out, int priority) {
		if(priority <= verbosity) {
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
	}
	
	protected static void println(String out,int priority) {
		print(out+(char)0x0D+(char)0x0A,priority);
	}
	
	protected static void println() {
		print(""+(char)0x0D+(char)0x0A,0);
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
				if(status.equals("Done!") || status.substring(0,3).equals("ERR")) {
					btn_abort[jobmod].setText("Done");
				}
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
	
	protected void addJob(File newJob, DoomMap newDoomJob, boolean hammer, boolean radiant, boolean gearcraft) {
		if(newJob!=null || newDoomJob!=null) {
			File[] newList = new File[jobs.length+1];
			DoomMap[] newDoomList = new DoomMap[jobs.length+1];
			boolean[] newToHammer = new boolean[jobs.length+1]; 
			boolean[] newToGC = new boolean[jobs.length+1]; 
			boolean[] newToRadiant = new boolean[jobs.length+1]; 
			int[] newThreadNo=new int[jobs.length+1];
			for(int j=0;j<jobs.length;j++) {
				newList[j]=jobs[j];
				newDoomList[j]=doomJobs[j];
				newToHammer[j]=toHammer[j];
				newToGC[j]=toGC[j];
				newToRadiant[j]=toRadiant[j];
				newThreadNo[j]=threadNum[j];
			}
			newList[jobs.length]=newJob;
			newDoomList[jobs.length]=newDoomJob;
			newToHammer[jobs.length]=hammer;
			newToGC[jobs.length]=gearcraft;
			newToRadiant[jobs.length]=radiant;
			newThreadNo[jobs.length]=-1;
			jobs=newList;
			doomJobs=newDoomList;
			toHammer=newToHammer;
			toGC=newToGC;
			toRadiant=newToRadiant;
			
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
			
			JLabel[] newMapNames=new JLabel[numJobs];
			for(int j=0;j<numJobs-1;j++) {
				newMapNames[j]=mapNames[j];
			}
			if(newDoomJob!=null) { // If this isn't null it's a DoomMap
				newMapNames[numJobs-1] = new JLabel(newDoomJob.getWadName()+"\\"+newDoomJob.getMapName());
			} else {
				newMapNames[numJobs-1] = new JLabel(newJob.getName());
			}
			mapNames=newMapNames;
			newMapNames[numJobs-1].setPreferredSize(new Dimension(frame.getWidth()-315, 12));
			GridBagConstraints mapNameConstraints = new GridBagConstraints();
			mapNameConstraints.fill = GridBagConstraints.NONE;
			mapNameConstraints.gridx = 1;
			mapNameConstraints.gridy = numJobs;
			mapNameConstraints.gridwidth = 1;
			mapNameConstraints.gridheight = 1;
			pnl_jobs.add(newMapNames[numJobs-1], mapNameConstraints);
			
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
	
	protected void addJobs(File[] newJobs, DoomMap[] newDoomJobs, boolean hammer, boolean radiant, boolean gearcraft) {
		int realNewJobs=0;
		for(int i=0;i<newJobs.length;i++) {
			if(newJobs[i]!=null || newDoomJobs[i]!=null) {
				realNewJobs++;
			}
		}
		println("Adding "+realNewJobs+" new jobs to queue",0);
		for(int i=0;i<newJobs.length;i++) {
			if(newJobs[i]!=null || newDoomJobs[i]!=null) {
				addJob(newJobs[i], newDoomJobs[i], hammer, radiant, gearcraft);
			}
		}
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
	
	// ACCESSORS/MUTATORS
	
	protected static void setOptionsEnabled(boolean in) {
		setThreadsItem.setEnabled(in);
		setPlanePointCoefItem.setEnabled(in);
		chk_planarItem.setEnabled(in);
		chk_skipPlaneFlipItem.setEnabled(in);
		chk_calcVertsItem.setEnabled(in);
		chk_roundNumsItem.setEnabled(in);
		chk_brushesToWorldItem.setEnabled(in);
		chk_noDetailsItem.setEnabled(in);
		chk_noFaceFlagsItem.setEnabled(in);
		setErrorItem.setEnabled(in);
		chk_replaceWithNull.setEnabled(in);
		chk_visLeafBBoxes.setEnabled(in);
		setOriginBrushSizeItem.setEnabled(in);
	}
	
	public static double getPrecision() {
		return precision;
	}
	
	public static double getPlanePointCoef() {
		return planePointCoef;
	}
	
	public static boolean brushesToWorldIsSelected() {
		return chk_brushesToWorldItem.isSelected();
	}
	
	public static boolean noDetailIsSelected() {
		return chk_noDetailsItem.isSelected();
	}
	
	public static boolean noFaceFlagsIsSelected() {
		return chk_noFaceFlagsItem.isSelected();
	}
	
	public static boolean replaceWithNullIsSelected() {
		return chk_replaceWithNull.isSelected();
	}
	
	public static boolean visLeafBBoxesIsSelected() {
		return chk_visLeafBBoxes.isSelected();
	}
	
	public static double getOriginBrushSize() {
		return originBrushSize;
	}
	
	public static String getOutputFolder() {
		return outputFolder;
	}
	
	// INTERNAL CLASSES

	private class SupportedFileFilter extends javax.swing.filechooser.FileFilter {
		private String description = "All supported files (*.BSP, *.WAD)";

		public boolean accept(File file) {
			if (file.isDirectory()) {
				return true; // Must return true for a directory, otherwise you can't navigate the filesystem using the GUI!
			} else {
				if (file.isFile()) {
					try { // Avoid StringIndexOutOfBoundsExceptions
						if(file.getName().substring(file.getName().length()-4).equalsIgnoreCase(".BSP") || file.getName().substring(file.getName().length()-4).equalsIgnoreCase(".WAD")) {
							return true; // Return true if the last four characters in the filename are ".BSP", not case sensitive
						}
					} catch(java.lang.StringIndexOutOfBoundsException e) {
						return false;
					}
				}
			}
			return false; // It's not a dir or a BSP file
		}
	
		public String getDescription() {
			return description;
		}
	}

	private class BSPFileFilter extends javax.swing.filechooser.FileFilter {
		private String description = "Binary Space Partition files (*.BSP)";

		public boolean accept(File file) {
			if (file.isDirectory()) {
				return true; // Must return true for a directory, otherwise you can't navigate the filesystem using the GUI!
			} else {
				if (file.isFile()) {
					try { // Avoid StringIndexOutOfBoundsExceptions
						if(file.getName().substring(file.getName().length()-4).equalsIgnoreCase(".BSP")) {
							return true; // Return true if the last four characters in the filename are ".BSP", not case sensitive
						}
					} catch(java.lang.StringIndexOutOfBoundsException e) {
						return false;
					}
				}
			}
			return false; // It's not a dir or a BSP file
		}
	
		public String getDescription() {
			return description;
		}
	}

	private class WADFileFilter extends javax.swing.filechooser.FileFilter {
		private String description = "WAD files (*.WAD)";

		public boolean accept(File file) {
			if (file.isDirectory()) {
				return true; // Must return true for a directory, otherwise you can't navigate the filesystem using the GUI!
			} else {
				if (file.isFile()) {
					try { // Avoid StringIndexOutOfBoundsExceptions
						if(file.getName().substring(file.getName().length()-4).equalsIgnoreCase(".WAD")) {
							return true; // Return true if the last four characters in the filename are ".BSP", not case sensitive
						}
					} catch(java.lang.StringIndexOutOfBoundsException e) {
						return false;
					}
				}
			}
			return false; // It's not a dir or a BSP file
		}
	
		public String getDescription() {
			return description;
		}
	}
	
	private class LOGFileFilter extends javax.swing.filechooser.FileFilter {
		private String description = "Log File (*.LOG)";
	
		public boolean accept(File file) {
			if (file.isDirectory()) {
				return true; // Must return true for a directory, otherwise you can't navigate the filesystem using the GUI!
			} else {
				if (file.isFile()) {
					try { // Avoid StringIndexOutOfBoundsExceptions
						if(file.getName().substring(file.getName().length()-4).equalsIgnoreCase(".LOG")) {
							return true; // Return true if the last four characters in the filename are ".LOG", not case sensitive
						}
					} catch(java.lang.StringIndexOutOfBoundsException e) {
						return false;
					}
				}
			}
			return false; // It's not a dir or a LOG file
		}
	
		public String getDescription() {
			return description;
		}
	}
}
