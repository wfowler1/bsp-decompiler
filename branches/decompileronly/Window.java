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
import java.util.Scanner;

public class Window extends JPanel implements ActionListener {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// Constants
	public static final String LF=""+(char)0x0D+(char)0x0A;
	
	public static final int VERBOSITY_ALWAYS=0;
	public static final int VERBOSITY_WARNINGS=1;
	public static final int VERBOSITY_MAPSTATS=2;
	public static final int VERBOSITY_ENTITIES=3;
	public static final int VERBOSITY_BRUSHCORRECTION=4;
	public static final int VERBOSITY_BRUSHCREATION=5;

	private static Runtime r = Runtime.getRuntime(); // Get a runtime object. This is for calling
	                                                 // Java's garbage collector and does not need
	                                                 // to be ported. I try not to leave memory leaks
	                                                 // but since Java has no way explicitly reallocate
	                                                 // unused memory I have to tell it when a good
	                                                 // time is to run the garbage collector, by
	                                                 // calling gc(). Also, it is used to execute EXEs
	                                                 // from within the program by calling .exec(path).

	// Window container
	protected static Window window; // Window

	// Inside the window we have...
	private static JMenuBar menuBar; // Menu bar
	private static JFrame frame; // Frame

	// Menu bar components
	// "File" menu
	private static JMenu fileMenu;
	private static JMenuItem openItem;
	private static JCheckBoxMenuItem decompVMFItem;
	private static JCheckBoxMenuItem decompMAPItem; 
	private static JCheckBoxMenuItem decompMOHRadiantItem;
	private static JCheckBoxMenuItem decompRadiantItem;
	private static JMenuItem exitItem;

	// "Options" menu
	private static JMenu optionsMenu;
	private static JMenuItem setPlanePointCoefItem;
	private static JCheckBoxMenuItem chk_planarItem;
	private static JCheckBoxMenuItem chk_skipPlaneFlipItem;
	private static JCheckBoxMenuItem chk_calcVertsItem;
	private static JCheckBoxMenuItem chk_roundNumsItem;
	private static JCheckBoxMenuItem chk_extractZipItem;
	private static JMenuItem setThreadsItem;
	private static JMenuItem setOutFolderItem;

	// "Debug" menu
	private static JMenu debugMenu;
	private static JCheckBoxMenuItem chk_brushesToWorldItem;
	private static JCheckBoxMenuItem chk_noOriginBrushItem;
	private static JCheckBoxMenuItem chk_noDetailsItem;
	private static JCheckBoxMenuItem chk_noWaterItem;
	private static JCheckBoxMenuItem chk_noFaceFlagsItem;
	private static JCheckBoxMenuItem chk_dontCorrectEntitiesItem;
	private static JCheckBoxMenuItem chk_dontCorrectTexturesItem;
	private static JMenuItem setErrorItem;
	private static JMenuItem setOriginBrushSizeItem;
	private static JMenuItem saveLogItem;
	// "Special requests" submenu
	private static JMenu specialMenu;
	private static JCheckBoxMenuItem chk_replaceWithNull;
	private static JCheckBoxMenuItem chk_visLeafBBoxes;
	private static JCheckBoxMenuItem chk_dontCull;
	// "Log Verbosity" submenu
	private static JMenu verbosityMenu;
	private static ButtonGroup verbosityGroup;
	private static JRadioButtonMenuItem rad_verbosity_0;
	private static JRadioButtonMenuItem rad_verbosity_1;
	private static JRadioButtonMenuItem rad_verbosity_2;
	private static JRadioButtonMenuItem rad_verbosity_3;
	private static JRadioButtonMenuItem rad_verbosity_4;
	private static JRadioButtonMenuItem rad_verbosity_5;
	
	// Frame components
	// There's really only one, the Split Pane
	private static JSplitPane consoleTableSplitter;
	// On top...
	private static JScrollPane console_pane; // And inside that...
	private static JTextArea consolebox;
	// On bottom...
	private static JScrollPane table_pane; // And inside that...
	private static JPanel pnl_jobs; // This panel gets the jobs table in it.
	private static JLabel lbl_mapName;
	private static JProgressBar totalProgressBar;
	private static JButton btn_abort_all;
	private static JLabel[] mapNames;
	private static JProgressBar[] progressBar;
	private static JButton[] btn_abort;
	
	// Global variables, used internally
	private static int numJobs;
	private static volatile int nextJob=0;
	private static File[] jobs=new File[0];
	private static DoomMap[] doomJobs=new DoomMap[0];
	private static int[] threadNum;
	private static Runnable runMe;
	private static Thread[] decompilerworkers=null;
	private static String lastUsedFolder;
	
	// Global variables, can be freely set by user
	private static int numThreads=1;
	private static double planePointCoef=32;
	private static double originBrushSize=16;
	private static int verbosity=0;
	private static String outputFolder="default";
	private static double precision=0.01;

	// main method
	// Creates an Object of this class and launches the GUI. Entry point to the whole program.
	public static void main(String[] args) {
		UIManager myUI=new UIManager();
		try {
			myUI.setLookAndFeel(myUI.getSystemLookAndFeelClassName());
		} catch(java.lang.Exception e) {
			;
		}
			
		frame = new JFrame("BSP Decompiler by 005");
	
		window = new Window(frame.getContentPane());
		print("Got a bug to report? Want to request a feature?"+LF+"Create an issue report at"+LF+"http://code.google.com/p/jbn-bsp-lump-tools/issues/entry"+LF+LF, VERBOSITY_ALWAYS);
		print("Currently supported engines:"+LF+"James Bond 007: Nightfire"+LF+"Quake 2"+LF+"Quake 3 (incomplete)"+LF+"Doom WADfiles (incomplete)"+LF+"Source VBSP (incomplete)"+LF+"Star Wars: Jedi Outcast/Soldier of Fortune 2 RBSP (incomplete)"+LF+"Return to Castle Wolfenstein (incomplete)"+LF, VERBOSITY_ALWAYS);
	}

	// This constructor configures and displays the GUI
	public Window(Container pane) {
		// Set up most of the window's properties, since we definitely have a window
		// Good thing frame is a global object
		// Set up frame's properties here
		java.net.URL imageURL = Window.class.getResource("icon32x32.PNG");
		frame.setIconImage(new ImageIcon(imageURL).getImage());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setPreferredSize(new Dimension(640, 460));
		frame.setMinimumSize(new Dimension(316, 240));
		frame.pack();
		frame.setResizable(true);
		frame.setVisible(true);
		
		// Menu Bar
		menuBar = new JMenuBar();
		fileMenu = new JMenu("File");
		menuBar.add(fileMenu);
		optionsMenu = new JMenu("Options");
		menuBar.add(optionsMenu);
		debugMenu = new JMenu("Debug");
		menuBar.add(debugMenu);
		
		// File menu
		openItem = new JMenuItem("Open map...");
		fileMenu.add(openItem);
		openItem.addActionListener(this);
		fileMenu.addSeparator();
		decompVMFItem = new JCheckBoxMenuItem("Output Hammer VMF");
		fileMenu.add(decompVMFItem);
		decompVMFItem.addActionListener(this);
		decompVMFItem.setSelected(true);
		decompMAPItem = new JCheckBoxMenuItem("Output Gearcraft MAP");
		fileMenu.add(decompMAPItem);
		decompMAPItem.addActionListener(this);
		decompMOHRadiantItem = new JCheckBoxMenuItem("Output MOHRadiant MAP");
		fileMenu.add(decompMOHRadiantItem);
		decompMOHRadiantItem.addActionListener(this);
		decompRadiantItem = new JCheckBoxMenuItem("Output GTKRadiant MAP");
		fileMenu.add(decompRadiantItem);
		decompRadiantItem.addActionListener(this);
		fileMenu.addSeparator();
		exitItem = new JMenuItem("Exit");
		fileMenu.add(exitItem);
		exitItem.addActionListener(this);
		
		// Options menu
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
		chk_roundNumsItem = new JCheckBoxMenuItem("Editor-style decimals");
		chk_roundNumsItem.setToolTipText("Rounds all decimals to the same precision as each map editor uses for its map format. Might make editors happier.");
		chk_roundNumsItem.setSelected(true);
		optionsMenu.add(chk_roundNumsItem);
		optionsMenu.addSeparator();
		
		chk_extractZipItem=new JCheckBoxMenuItem("Extract internal PAK file");
		chk_extractZipItem.setToolTipText("Source engine maps contain an internal PAK file containing map-specific files. Extraction takes a little while and creates another file.");
		chk_extractZipItem.setSelected(true);
		optionsMenu.add(chk_extractZipItem);
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
		chk_noOriginBrushItem = new JCheckBoxMenuItem("No origin brushes");
		chk_noOriginBrushItem.setToolTipText("Do not generate origin brushes for map formats that use them for brushbased entities with \"origin\" attributes.");
		chk_noOriginBrushItem.setSelected(false);
		debugMenu.add(chk_noOriginBrushItem);
		chk_noDetailsItem = new JCheckBoxMenuItem("Ignore detail flags");
		chk_noDetailsItem.setToolTipText("Disregard detail flags on brushes. All detail brushes will be world geometry, and will block VIS.");
		chk_noDetailsItem.setSelected(false);
		debugMenu.add(chk_noDetailsItem);
		chk_noWaterItem = new JCheckBoxMenuItem("Ignore water flags");
		chk_noWaterItem.setToolTipText("Disregard water flags on brushes. Does not affect map formats which use func_water, but won't extract water from formats which don't.");
		chk_noWaterItem.setSelected(false);
		debugMenu.add(chk_noWaterItem);
		chk_noFaceFlagsItem = new JCheckBoxMenuItem("Ignore face flags");
		chk_noFaceFlagsItem.setToolTipText("Disregard face flags (NODRAW, NOIMPACTS, etc.)");
		chk_noFaceFlagsItem.setSelected(false);
		debugMenu.add(chk_noFaceFlagsItem);
		chk_dontCorrectEntitiesItem = new JCheckBoxMenuItem("Don't correct entities");
		chk_dontCorrectEntitiesItem.setToolTipText("Don't correct entities depending on the output format. This will keep all entities as-is, rather than renaming them for the output format.");
		chk_dontCorrectEntitiesItem.setSelected(false);
		debugMenu.add(chk_dontCorrectEntitiesItem);
		chk_dontCorrectTexturesItem = new JCheckBoxMenuItem("Don't correct textures");
		chk_dontCorrectTexturesItem.setToolTipText("Don't correct texture names depending on the output format. This will keep all original texture names, instead of those used by the proper editor.");
		chk_dontCorrectTexturesItem.setSelected(false);
		debugMenu.add(chk_dontCorrectTexturesItem);
		setErrorItem = new JMenuItem("Set error tolerance...");
		setErrorItem.setToolTipText("Allows customization of error tolerance of double precision calculations.");
		debugMenu.add(setErrorItem);
		setErrorItem.addActionListener(this);
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
		chk_dontCull = new JCheckBoxMenuItem("Don't cull extra planes in Doom maps");
		chk_dontCull.setSelected(false);
		specialMenu.add(chk_dontCull);
		
		verbosityGroup=new ButtonGroup();
		rad_verbosity_0=new JRadioButtonMenuItem("Status only");
		rad_verbosity_1=new JRadioButtonMenuItem("Show warnings");
		rad_verbosity_2=new JRadioButtonMenuItem("Output map statistics");
		rad_verbosity_3=new JRadioButtonMenuItem("Show entities");
		rad_verbosity_4=new JRadioButtonMenuItem("Show brush correction method calls");
		rad_verbosity_5=new JRadioButtonMenuItem("Show all brush creation method calls");
		verbosityGroup.add(rad_verbosity_0);
		verbosityGroup.add(rad_verbosity_1);
		verbosityGroup.add(rad_verbosity_2);
		verbosityGroup.add(rad_verbosity_3);
		verbosityGroup.add(rad_verbosity_4);
		verbosityGroup.add(rad_verbosity_5);
		verbosityMenu.add(rad_verbosity_0);
		verbosityMenu.add(rad_verbosity_1);
		verbosityMenu.add(rad_verbosity_2);
		verbosityMenu.add(rad_verbosity_3);
		verbosityMenu.add(rad_verbosity_4);
		verbosityMenu.add(rad_verbosity_5);
		rad_verbosity_0.setSelected(true);
		rad_verbosity_0.addActionListener(this);
		rad_verbosity_1.addActionListener(this);
		rad_verbosity_2.addActionListener(this);
		rad_verbosity_3.addActionListener(this);
		rad_verbosity_4.addActionListener(this);
		rad_verbosity_5.addActionListener(this);
		
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
		consoleTableSplitter.setDividerLocation(240);
		
		GridBagConstraints consoleConstraints = new GridBagConstraints();
		consoleConstraints.fill = GridBagConstraints.NONE;
		consoleConstraints.gridx = 0;
		consoleConstraints.gridy = 0;
		consoleConstraints.gridwidth = 1;
		consoleConstraints.gridheight = 1;
		pane.add(consoleTableSplitter, consoleConstraints);
		
		// This crap allows the window to be resized, and sets the size of the GUI components as needed.
		// Maximizing/restoring the window
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
		// Resizing the window
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
	
	// TODO: make a command-line constructor here

	// actionPerformed(ActionEvent)
	// Any time something happens on the GUI, this is called. However we're only
	// going to perform actions when certain things are clicked. The rest are discarded.
	public void actionPerformed(ActionEvent action) {
		// It's an abort button
		try {
			if(action.getActionCommand().substring(0,6).equals("Abort ")) {
			// Try to figure out which one
				try {
					int cancelJob=new Integer(action.getActionCommand().substring(6));
					stopDecompilerThread(cancelJob);
				} catch(java.lang.NumberFormatException e) { // If it wasn't numbered, it was the "Abort all" button
					for(int i=numJobs;i>0;i--) { // Start from the last one and work towards the first.
						if(btn_abort[i-1].getText().substring(0,6).equals("Abort ")) {
							stopDecompilerThread(i); // This prevents new threads from starting while this loop is running.
						}
					}
				}
			}
		} catch(java.lang.StringIndexOutOfBoundsException f) {
			;
		}
		
		if(action.getSource() == decompMAPItem) {
			if(!decompRadiantItem.isSelected() && !decompMAPItem.isSelected() && !decompMOHRadiantItem.isSelected() && !decompVMFItem.isSelected()) {
				decompMAPItem.setSelected(true);
				println("Must output to at least one MAP format!",VERBOSITY_ALWAYS);
			}
		}
		
		if(action.getSource() == decompVMFItem) {
			if(!decompRadiantItem.isSelected() && !decompMAPItem.isSelected() && !decompMOHRadiantItem.isSelected() && !decompVMFItem.isSelected()) {
				decompVMFItem.setSelected(true);
				println("Must output to at least one MAP format!",VERBOSITY_ALWAYS);
			}
		}
		
		if(action.getSource() == decompMOHRadiantItem) {
			if(!decompRadiantItem.isSelected() && !decompMAPItem.isSelected() && !decompMOHRadiantItem.isSelected() && !decompVMFItem.isSelected()) {
				decompMOHRadiantItem.setSelected(true);
				println("Must output to at least one MAP format!",VERBOSITY_ALWAYS);
			}
		}
		
		if(action.getSource() == decompRadiantItem) {
			if(!decompRadiantItem.isSelected() && !decompMAPItem.isSelected() && !decompMOHRadiantItem.isSelected() && !decompVMFItem.isSelected()) {
				decompRadiantItem.setSelected(true);
				println("Must output to at least one MAP format!",VERBOSITY_ALWAYS);
			}
		}
		
		// User clicks the "open" button
		if(action.getSource() == openItem) {
			JFileChooser file_selector;
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
			file_selector.addChoosableFileFilter(new CustomFileFilter("All supported files", new String[] { "BSP", "WAD" }));
			file_selector.addChoosableFileFilter(new CustomFileFilter("Binary Space Partition files", new String[] { "BSP" }));
			file_selector.addChoosableFileFilter(new CustomFileFilter("WAD files", new String[] { "WAD" }));
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
				addJobs(files, new DoomMap[files.length]);
			}
		}
		
		// User clicks the "Save log" button
		if(action.getSource() == saveLogItem) {
			JFileChooser file_selector = new JFileChooser();
			file_selector.setSelectedFile(new File("DecompilerConsole.log"));
			file_selector.setAcceptAllFileFilterUsed(false);
			file_selector.addChoosableFileFilter(new CustomFileFilter("Log File", new String[] { "LOG" }));
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
						Window.println("Log file saved!",VERBOSITY_ALWAYS);
					} catch(java.io.IOException e) {
						Window.println("Unable to create file "+logfile.getAbsolutePath()+LF+"Ensure the filesystem is not read only!",VERBOSITY_ALWAYS);
					}
				}
			}
		}
		
		// change the output folder
		if(action.getSource() == setOutFolderItem) {
			JFileChooser file_selector = new JFileChooser();
			file_selector.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			file_selector.setAcceptAllFileFilterUsed(false);
			file_selector.setMultiSelectionEnabled(false);
			int returnVal = file_selector.showSaveDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File saveHere=file_selector.getSelectedFile();
				if(saveHere!=null) {
					if(!saveHere.exists()) {
						Window.println(saveHere.getAbsolutePath()+" does not exist! Setting to \"default\" instead.",VERBOSITY_ALWAYS);
						outputFolder="default";
					} else {
						if(!saveHere.isDirectory()) {
							Window.println(saveHere.getAbsolutePath()+" is not a directory! Setting to \"default\" instead.",VERBOSITY_ALWAYS);
							outputFolder="default";
						} else {
							Window.println("Output directory set to "+saveHere.getAbsolutePath(),VERBOSITY_ALWAYS);
							outputFolder=saveHere.getAbsolutePath();
						}
					}
				} else {
					Window.println("Output folder set to default.",VERBOSITY_ALWAYS);
					outputFolder="default";
				}
			} else {
				Window.println("Output folder set to default.",VERBOSITY_ALWAYS);
				outputFolder="default";
			}
		}
		
		// The planar decompilation and skip plane flip items will enable/disable the calculate vertices option in a certain case.
		if(action.getSource() == chk_planarItem || action.getSource() == chk_skipPlaneFlipItem) {
			chk_calcVertsItem.setEnabled(!(chk_planarItem.isSelected() && !chk_skipPlaneFlipItem.isSelected()));
			if(chk_planarItem.isSelected() && !chk_skipPlaneFlipItem.isSelected()) {
				chk_calcVertsItem.setSelected(true);
			}
		}
		
		// File -> Exit
		if(action.getSource() == exitItem) {
			//if(decompilerworkers[0]!=null) {
			//	add some warning dialog here?
			//} else {
				System.exit(0);
			//}
		}
		
		// Set error tolerance
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
					println("Error tolerance set to "+precision+".",VERBOSITY_ALWAYS);
				}
			} catch(java.lang.NumberFormatException e) {
				println("Invalid error tolerance! Tolerance is "+precision+" instead!",VERBOSITY_ALWAYS);
			} catch(java.lang.NullPointerException e) { // Happens when user hits "cancel". Do nothing.
				;
			}
		}
		
		// Set origin brush size
		if(action.getSource() == setOriginBrushSizeItem) {
			String st=(String)JOptionPane.showInputDialog(frame,"Please enter a new origin brush size.\n"+
			          "Current value: "+originBrushSize,"Enter new origin brush size",JOptionPane.QUESTION_MESSAGE,null,null,originBrushSize);
			double temp=originBrushSize;
			try {
				temp = Double.parseDouble(st);
				if(temp<=0) {
					throw new java.lang.NumberFormatException();
				} else {
					originBrushSize=temp;
					println("Origin brush size set to "+originBrushSize+".",VERBOSITY_ALWAYS);
				}
			} catch(java.lang.NumberFormatException e) {
				if(st.equalsIgnoreCase("penis") || temp==0) { // Is it Easter time already?! :D
					println("Brush size cannot be zero! Size remains "+originBrushSize+" instead!",VERBOSITY_ALWAYS);
				} else {
					println("Invalid brush size! Size remains "+originBrushSize+" instead!",VERBOSITY_ALWAYS);
				}
			} catch(java.lang.NullPointerException e) { // Happens when user hits "cancel". Do nothing.
				;
			}
		}
		
		// Set num threads
		if(action.getSource() == setThreadsItem) {
			String st=(String)JOptionPane.showInputDialog(frame,"Please enter number of concurrent decompiles allowed.\n"+
			          "Current value: "+numThreads,"Enter new thread amount",JOptionPane.QUESTION_MESSAGE,null,null,numThreads);
			try {
				int temp = Integer.parseInt(st);
				if(temp<1) {
					throw new java.lang.NumberFormatException();
				} else {
					numThreads=temp;
					println("Num threads set to "+numThreads+".",VERBOSITY_ALWAYS);
				}
			} catch(java.lang.NumberFormatException e) {
				println("Invalid number of threads! Thread count remains "+numThreads+" instead!",VERBOSITY_ALWAYS);
			} catch(java.lang.NullPointerException e) { // Happens when user hits "cancel". Do nothing.
				;
			}
		}
		
		// Set plane point coefficient
		if(action.getSource() == setPlanePointCoefItem) {
			String st=(String)JOptionPane.showInputDialog(frame,"Please enter plane point coefficient.\n"+
			          "Current value: "+planePointCoef,"Enter new coefficient",JOptionPane.QUESTION_MESSAGE,null,null,planePointCoef);
			try {
				double temp = Double.parseDouble(st);
				if(temp==0) {
					throw new java.lang.NumberFormatException();
				} else {
					planePointCoef=temp;
					println("Coefficient set to "+planePointCoef+".",VERBOSITY_ALWAYS);
				}
			} catch(java.lang.NumberFormatException e) {
				println("Invalid coefficient! Coefficient remains "+planePointCoef+" instead!",VERBOSITY_ALWAYS);
			} catch(java.lang.NullPointerException e) { // Happens when user hits "cancel". Do nothing.
				;
			}
		}
		
		// Log verbosity items
		
		if(action.getSource() == rad_verbosity_0) {
			verbosity=VERBOSITY_ALWAYS;
		}
		
		if(action.getSource() == rad_verbosity_1) {
			verbosity=VERBOSITY_WARNINGS;
		}
		
		if(action.getSource() == rad_verbosity_2) {
			verbosity=VERBOSITY_MAPSTATS;
		}
		
		if(action.getSource() == rad_verbosity_3) {
			verbosity=VERBOSITY_ENTITIES;
		}
		
		if(action.getSource() == rad_verbosity_4) {
			verbosity=VERBOSITY_BRUSHCORRECTION;
		}
		
		if(action.getSource() == rad_verbosity_5) {
			verbosity=VERBOSITY_BRUSHCREATION;
		}
	}
	
	// This method actually starts a thread for the specified job
	private void startDecompilerThread(int newThread, int jobNum) {
		File job=jobs[jobNum];
		// I'd really like to use Thread.join() or something here, but the stupid thread never dies.
		// But if somethingFinished is true, then one of the threads is telling us it's finished anyway.
		if(doomJobs[jobNum]!=null) {
			runMe=new DecompilerThread(doomJobs[jobNum], jobNum, newThread);
		} else {
			runMe=new DecompilerThread(job, jobNum, newThread);
		}
		decompilerworkers[newThread] = new Thread(runMe);
		decompilerworkers[newThread].setName("Decompiler "+newThread+" job "+jobNum);
		decompilerworkers[newThread].setPriority(Thread.MIN_PRIORITY);
		println("Starting job #"+(jobNum+1),VERBOSITY_ALWAYS);
		decompilerworkers[newThread].start();
		threadNum[jobNum]=newThread;
		progressBar[jobNum].setIndeterminate(false);
	}
	
	private void stopDecompilerThread(int job) {
		int currentThread=threadNum[job-1];
		if(currentThread>-1) {
			decompilerworkers[currentThread].stop();          // The Java API lists this method of stopping a thread as deprecated.
			startNextJob(true, currentThread);                // For the purposes of this program, I'm not sure it's an issue though.
			println("Job number "+job+" aborted by user.",VERBOSITY_ALWAYS); // More info: http://docs.oracle.com/javase/6/docs/technotes/guides/concurrency/threadPrimitiveDeprecation.html
			setProgress(job-1, 1, 1, "Aborted!");
		} else {
			println("Job number "+job+" canceled by user.",VERBOSITY_ALWAYS);
			setProgress(job-1, 1, 1, "Canceled!");
		}
		setProgressColor(job-1, new Color(255, 128, 128));
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
		print(out+LF,priority);
	}
	
	protected static void println() {
		print(LF,VERBOSITY_ALWAYS);
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
			updateTotalProgress();
		}
	}
	
	protected static void setProgressColor(int jobmod, Color color) {
		if(progressBar[jobmod]!=null) {
			progressBar[jobmod].setForeground(color);
		}
	}
	
	protected static void updateTotalProgress() {
		if(totalProgressBar!=null) {
			int max=progressBar.length*100;
			int progress=0;
			for(int i=0;i<progressBar.length;i++) { // For each progress bar
				progress+=Math.round(progressBar[i].getPercentComplete()*100.0);
			}
			totalProgressBar.setMaximum(max);
			totalProgressBar.setValue(progress);
			if(progress==max) {
				totalProgressBar.setString("Done!");
			} else {
				totalProgressBar.setString("Total: "+(int)((progress/(float)max)*100)+"%");
			}
		}
	}
	
	protected static void setAbortButtonEnabled(int jobmod, boolean in) {
		if(btn_abort[jobmod]!=null) {
			btn_abort[jobmod].setEnabled(in);
		}
	}
	
	protected void addJob(File newJob, DoomMap newDoomJob) {
		if(newJob!=null || newDoomJob!=null) {
			File[] newList = new File[jobs.length+1];
			DoomMap[] newDoomList = new DoomMap[jobs.length+1];
			int[] newThreadNo=new int[jobs.length+1];
			for(int j=0;j<jobs.length;j++) {
				newList[j]=jobs[j];
				newDoomList[j]=doomJobs[j];
				newThreadNo[j]=threadNum[j];
			}
			newList[jobs.length]=newJob;
			newDoomList[jobs.length]=newDoomJob;
			newThreadNo[jobs.length]=-1;
			jobs=newList;
			doomJobs=newDoomList;
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
	
	protected void addJobs(File[] newJobs, DoomMap[] newDoomJobs) {
		int realNewJobs=0;
		for(int i=0;i<newJobs.length;i++) {
			if(newJobs[i]!=null || newDoomJobs[i]!=null) {
				realNewJobs++;
			}
		}
		println("Adding "+realNewJobs+" new jobs to queue",VERBOSITY_ALWAYS);
		for(int i=0;i<newJobs.length;i++) {
			if(newJobs[i]!=null || newDoomJobs[i]!=null) {
				addJob(newJobs[i], newDoomJobs[i]);
			}
		}
	}
		
	// This method queues up the next job and makes sure to run a thread
	protected void startNextJob(boolean somethingFinished, int threadFinished) {
		int myJob=nextJob++; // Increment this right away, then use myJob. For thread safety, in case two threads are using this method at once.
		// This isn't a perfect, or ideal solution, and it's not 100% failproof. But it is much safer.
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
		chk_roundNumsItem.setEnabled(in);
		chk_brushesToWorldItem.setEnabled(in);
		chk_noDetailsItem.setEnabled(in);
		chk_noWaterItem.setEnabled(in);
		chk_noFaceFlagsItem.setEnabled(in);
		setErrorItem.setEnabled(in);
		chk_replaceWithNull.setEnabled(in);
		chk_visLeafBBoxes.setEnabled(in);
		setOriginBrushSizeItem.setEnabled(in);
		chk_dontCull.setEnabled(in);
		chk_extractZipItem.setEnabled(in);
		chk_dontCorrectTexturesItem.setEnabled(in);
		chk_dontCorrectEntitiesItem.setEnabled(in);
		chk_noOriginBrushItem.setEnabled(in);
		if(in) {
			chk_calcVertsItem.setEnabled(!(chk_planarItem.isSelected() && !chk_skipPlaneFlipItem.isSelected()));
			if(chk_planarItem.isSelected() && !chk_skipPlaneFlipItem.isSelected()) {
				chk_calcVertsItem.setSelected(true);
			}
		} else {
			chk_calcVertsItem.setEnabled(false);
		}
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
	
	public static boolean noWaterIsSelected() {
		return chk_noWaterItem.isSelected();
	}
	
	public static boolean noEntCorrectionsIsSelected() {
		return chk_dontCorrectEntitiesItem.isSelected();
	}
	
	public static boolean noTexCorrectionsIsSelected() {
		return chk_dontCorrectTexturesItem.isSelected();
	}
	
	public static boolean calcVertsIsSelected() {
		return chk_calcVertsItem.isSelected();
	}
	
	public static boolean planarDecompIsSelected() {
		return chk_planarItem.isSelected();
	}
	
	public static boolean skipFlipIsSelected() {
		return chk_skipPlaneFlipItem.isSelected();
	}
	
	public static boolean roundNumsIsSelected() {
		return chk_roundNumsItem.isSelected();
	}
	
	public static boolean extractZipIsSelected() {
		return chk_extractZipItem.isSelected();
	}
	
	public static boolean noFaceFlagsIsSelected() {
		return chk_noFaceFlagsItem.isSelected();
	}
	
	public static boolean replaceWithNullIsSelected() {
		return chk_replaceWithNull.isSelected();
	}
	
	public static boolean noOriginBrushesIsSelected() {
		return chk_noOriginBrushItem.isSelected();
	}
	
	public static boolean visLeafBBoxesIsSelected() {
		return chk_visLeafBBoxes.isSelected();
	}
	
	public static boolean dontCullIsSelected() {
		return chk_dontCull.isSelected();
	}
	
	public static double getOriginBrushSize() {
		return originBrushSize;
	}
	
	public static String getOutputFolder() {
		return outputFolder;
	}
	
	public static boolean toVMF() {
		return decompVMFItem.isSelected();
	}
	
	public static boolean toMOH() {
		return decompMOHRadiantItem.isSelected();
	}
	
	public static boolean toRadiantMAP() {
		return decompRadiantItem.isSelected();
	}
	
	public static boolean toGCMAP() {
		return decompMAPItem.isSelected();
	}
	
	// INTERNAL CLASSES

	private class CustomFileFilter extends javax.swing.filechooser.FileFilter {
		private String description = "";
		private String[] extensions=new String[0];
		
		public CustomFileFilter(String description, String[] extensions) {
			this.description=description;
			if(extensions.length>0) {
				this.description+=" (*."+extensions[0];
				for(int i=1;i<extensions.length;i++) {
					this.description+=", *."+extensions[i];
				}
				this.description+=")";
			}
			this.extensions=extensions;
		}
		
		public boolean accept(File file) {
			if (file.isDirectory()) {
				return true; // Must return true for a directory, otherwise you can't navigate the filesystem using the GUI!
			} else {
				if (file.isFile()) {
					for(int i=0;i<extensions.length;i++) {
						try { // Avoid StringIndexOutOfBoundsExceptions
							if (file.getName().substring(file.getName().length()-extensions[i].length()-1).equalsIgnoreCase("."+extensions[i])) {
								return true;
							}
						} catch(java.lang.StringIndexOutOfBoundsException e) {
							; // The entire name of the requested file is shorter than the specified extension
						}
					}
				}
			}
			return false; // It's not a dir or a filetype listed in extensions
		}
	
		public String getDescription() {
			return description;
		}
	}
}
