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

	protected static Window window;
	private static BSPFileFilter BSPFilter = new BSPFileFilter();
	private static LOGFileFilter LOGFilter = new LOGFileFilter();
	
	private static JFrame frame;

	// main method
	// Creates an Object of this class and launches the GUI. Entry point to the whole program.
	public static void main(String[] args) {
		if(args.length==0) {
			UIManager myUI=new UIManager();
			try {
				myUI.setLookAndFeel(myUI.getSystemLookAndFeelClassName());
			} catch(java.lang.Exception e) {
				;
			}
			
			frame = new JFrame("BSP Decompiler by 005");
			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	
			window = new Window(frame.getContentPane());
			
			frame.setIconImage(new ImageIcon("icon32x32.PNG").getImage());
	
			frame.setPreferredSize(new Dimension(620, 450));
	
			frame.pack();
			frame.setResizable(false);
			frame.setVisible(true);
		} else {
			window = new Window(args);
		}
		window.print("Got a bug to report? Want to see something added?\nCreate an issue report at\nhttp://code.google.com/p/jbn-bsp-lump-tools/issues/list\n\n");
	}

	// All GUI components get initialized here
	private static JFileChooser file_selector;
	private static JFileChooser file_saver;
	private static JButton btn_open;
	private static JButton btn_decomp;
	private static JTextField txt_file;
	private static JTextField txt_coef;
	private static JTextArea consolebox;
	private static JLabel lbl_spacer;
	private static JLabel lbl_coef;
	private static JScrollPane console_pane;
	private static JCheckBox chk_planar;
	private static JCheckBox chk_skipPlaneFlip;
	private static JCheckBox chk_calcVerts;
	private static JCheckBox chk_roundNums;
	private static JRadioButton rad_VMF;
	private static JRadioButton rad_MAP;
	private static JButton btn_dumplog;
	private static JProgressBar progressBar;
	private static JProgressBar totalProgressBar;
	
	// Private variables for a Window object
	private boolean vertexDecomp=true;
	private boolean correctPlaneFlip=true;
	private double planePointCoef=100;
	private boolean toVMF=true;
	private boolean calcVerts=false;
	private boolean roundNums=false;

	// This constructor configures and displays the GUI
	public Window(Container pane) {
		pane.setLayout(new GridBagLayout());
		
		// First row
		
		txt_file = new JTextField(40);
		
		GridBagConstraints fileConstraints = new GridBagConstraints();
		fileConstraints.fill = GridBagConstraints.NONE;
		fileConstraints.gridx = 0;
		fileConstraints.gridy = 0;
		fileConstraints.gridwidth = 2;
		fileConstraints.gridheight = 1;
		pane.add(txt_file, fileConstraints);
		
		btn_open = new JButton("Browse");
		
		GridBagConstraints openConstraints = new GridBagConstraints();
		openConstraints.fill = GridBagConstraints.NONE;
		openConstraints.gridx = 2;
		openConstraints.gridy = 0;
		openConstraints.gridwidth = 1;
		openConstraints.gridheight = 1;
		pane.add(btn_open, openConstraints);
		
		btn_open.addActionListener(this);
		
		btn_decomp = new JButton("Decompile BSP");
		
		GridBagConstraints decompConstraints = new GridBagConstraints();
		decompConstraints.fill = GridBagConstraints.BOTH;
		decompConstraints.gridx = 3;
		decompConstraints.gridy = 0;
		decompConstraints.gridwidth = 2;
		decompConstraints.gridheight = 1;
		pane.add(btn_decomp, decompConstraints);
		
		btn_decomp.addActionListener(this);
		
		// Second row
		
		chk_planar = new JCheckBox("Planar Decompilation Only");
		chk_planar.setToolTipText("Don't use vertices to aid decompilation. May result in longer decompilations, but may solve problems.");
		
		GridBagConstraints planarConstraints = new GridBagConstraints();
		planarConstraints.fill = GridBagConstraints.NONE;
		planarConstraints.gridx = 0;
		planarConstraints.gridy = 1;
		planarConstraints.gridwidth = 1;
		planarConstraints.gridheight = 1;
		pane.add(chk_planar, planarConstraints);
		
		chk_planar.addActionListener(this);
		
		chk_skipPlaneFlip = new JCheckBox("Skip plane flip");
		chk_skipPlaneFlip.setToolTipText("Don't make sure brush planes are facing the right direction. Speeds up decompilation in some cases, but may cause problems.");
		
		GridBagConstraints SkipFlipConstraints = new GridBagConstraints();
		SkipFlipConstraints.fill = GridBagConstraints.NONE;
		SkipFlipConstraints.gridx = 1;
		SkipFlipConstraints.gridy = 1;
		SkipFlipConstraints.gridwidth = 1;
		SkipFlipConstraints.gridheight = 1;
		pane.add(chk_skipPlaneFlip, SkipFlipConstraints);
		
		chk_skipPlaneFlip.addActionListener(this);
		
		chk_calcVerts = new JCheckBox("Calculate Brush Corners");
		chk_calcVerts.setToolTipText("Calculate every brush's corners. May solve problems arising from decompilation of faces with no vertex information.");
		
		GridBagConstraints CalcVertConstraints = new GridBagConstraints();
		CalcVertConstraints.fill = GridBagConstraints.NONE;
		CalcVertConstraints.gridx = 2;
		CalcVertConstraints.gridy = 1;
		CalcVertConstraints.gridwidth = 1;
		CalcVertConstraints.gridheight = 1;
		pane.add(chk_calcVerts, CalcVertConstraints);
		
		chk_calcVerts.addActionListener(this);
		
		chk_roundNums = new JCheckBox("Round decimals");
		chk_roundNums.setToolTipText("Rounds all vertices to six decimals and texture scales to four. Might make map editors (namely GearCraft) happier.");
		
		GridBagConstraints RoundNumConstraints = new GridBagConstraints();
		RoundNumConstraints.fill = GridBagConstraints.NONE;
		RoundNumConstraints.gridx = 3;
		RoundNumConstraints.gridy = 1;
		RoundNumConstraints.gridwidth = 2;
		RoundNumConstraints.gridheight = 1;
		pane.add(chk_roundNums, RoundNumConstraints);
		
		chk_roundNums.addActionListener(this);
		
		// Third row
		
		lbl_coef = new JLabel("Plane points coefficient: ");
		
		GridBagConstraints coeflblConstraints = new GridBagConstraints();
		coeflblConstraints.fill = GridBagConstraints.NONE;
		coeflblConstraints.gridx = 0;
		coeflblConstraints.gridy = 2;
		coeflblConstraints.gridwidth = 1;
		coeflblConstraints.gridheight = 1;
		pane.add(lbl_coef, coeflblConstraints);
		
		txt_coef = new JTextField(5);
		txt_coef.setText("100");
		
		GridBagConstraints coefConstraints = new GridBagConstraints();
		coefConstraints.fill = GridBagConstraints.NONE;
		coefConstraints.gridx = 1;
		coefConstraints.gridy = 2;
		coefConstraints.gridwidth = 1;
		coefConstraints.gridheight = 1;
		pane.add(txt_coef, coefConstraints);
		
		rad_VMF = new JRadioButton("VMF");
		rad_VMF.setSelected(true);
		
		GridBagConstraints VMFConstraints = new GridBagConstraints();
		VMFConstraints.fill = GridBagConstraints.NONE;
		VMFConstraints.gridx = 3;
		VMFConstraints.gridy = 2;
		VMFConstraints.gridwidth = 1;
		VMFConstraints.gridheight = 1;
		pane.add(rad_VMF, VMFConstraints);
		
		rad_VMF.addActionListener(this);
		
		rad_MAP = new JRadioButton("MAP");
		
		GridBagConstraints MAPConstraints = new GridBagConstraints();
		MAPConstraints.fill = GridBagConstraints.NONE;
		MAPConstraints.gridx = 4;
		MAPConstraints.gridy = 2;
		MAPConstraints.gridwidth = 1;
		MAPConstraints.gridheight = 1;
		pane.add(rad_MAP, MAPConstraints);
		
		rad_MAP.addActionListener(this);
		
		ButtonGroup mapType = new ButtonGroup();
		mapType.add(rad_VMF);
		mapType.add(rad_MAP);
		
		// Fourth row
		
		lbl_spacer = new JLabel(" ");
		
		GridBagConstraints spacerConstraints = new GridBagConstraints();
		spacerConstraints.fill = GridBagConstraints.NONE;
		spacerConstraints.gridx = 0;
		spacerConstraints.gridy = 3;
		spacerConstraints.gridwidth = 5;
		spacerConstraints.gridheight = 1;
		pane.add(lbl_spacer, spacerConstraints);
		
		// Fifth row
		
		consolebox = new JTextArea(15, 70);
		
		console_pane = new JScrollPane(consolebox);
		console_pane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		GridBagConstraints consoleConstraints = new GridBagConstraints();
		consoleConstraints.fill = GridBagConstraints.NONE;
		consoleConstraints.gridx = 0;
		consoleConstraints.gridy = 4;
		consoleConstraints.gridwidth = 5;
		consoleConstraints.gridheight = 1;
		pane.add(console_pane, consoleConstraints);
		
		// Sixth row
		
		progressBar = new JProgressBar(0, 1);
		
		GridBagConstraints barConstraints = new GridBagConstraints();
		barConstraints.fill = GridBagConstraints.NONE;
		barConstraints.gridx = 1;
		barConstraints.gridy = 5;
		barConstraints.gridwidth = 2;
		barConstraints.gridheight = 1;
		pane.add(progressBar, barConstraints);
		progressBar.setStringPainted(true);
		progressBar.setValue(0);
		progressBar.setString("0%");
		
		totalProgressBar = new JProgressBar(0, 1);
		
		GridBagConstraints totalBarConstraints = new GridBagConstraints();
		totalBarConstraints.fill = GridBagConstraints.NONE;
		totalBarConstraints.gridx = 0;
		totalBarConstraints.gridy = 5;
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
		dumpConstraints.gridy = 5;
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
				toVMF=false;
			} else {
				if(!out.equals("")) {
					out+=","+args[i];
				} else {
					out=args[i];
				}
			}
		}
		startDecompilerThread(out);
	}

	// actionPerformed(ActionEvent)
	// Any time something happens on the GUI, this is called. However we're only
	// going to perform actions when certain things are clicked. The rest are discarded.
	public void actionPerformed(ActionEvent action) {
		// User clicks the "open" button
		if (action.getSource() == btn_open) {
			if(!txt_file.getText().equals("")) {
				file_selector = new JFileChooser(txt_file.getText());
			} else {
				file_selector = new JFileChooser("/"); // TODO: set to current folder
			}
			file_selector.addChoosableFileFilter(BSPFilter);
			file_selector.setMultiSelectionEnabled(true);
			// file_selector.setIconImage(new ImageIcon("folder32x32.PNG").getImage());
			int returnVal = file_selector.showOpenDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File[] file = file_selector.getSelectedFiles();
				String out=file[0].getAbsolutePath();
				for(int i=1;i<file.length;i++) {
					out+=","+file[i].getAbsolutePath();
				}
				txt_file.setText(out);
			}
		}
		
		// User clicks the "decompile" button
		if(action.getSource() == btn_decomp) {
			try {
				planePointCoef=Double.parseDouble(txt_coef.getText());
			} catch(java.lang.NumberFormatException e) {
				;
			}
			startDecompilerThread(txt_file.getText());
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
						Window.window.println("Unable to create file "+logfile.getAbsolutePath()+"\nEnsure the filesystem is not read only!");
					}
				}
			}
		}
		
		if(action.getSource() == chk_planar) {
			vertexDecomp=!chk_planar.isSelected();
		}
		
		if(action.getSource() == chk_skipPlaneFlip) {
			correctPlaneFlip=!chk_skipPlaneFlip.isSelected();
		}
		
		if(action.getSource() == chk_calcVerts) {
			calcVerts=chk_calcVerts.isSelected();
		}
		
		if(action.getSource() == chk_roundNums) {
			roundNums=chk_roundNums.isSelected();
		}
		
		if(action.getSource() == rad_VMF || action.getSource() == rad_MAP) {
			toVMF=rad_VMF.isSelected();
		}
		
		if(action.getSource() == chk_planar || action.getSource() == chk_skipPlaneFlip) {
			chk_calcVerts.setEnabled(!(chk_planar.isSelected() && !chk_skipPlaneFlip.isSelected()));
			if(chk_planar.isSelected() && !chk_skipPlaneFlip.isSelected()) {
				chk_calcVerts.setSelected(true);
				calcVerts=true;
			}
		}
	}
	
	private void startDecompilerThread(String fileList) {
		clearConsole();
		int numFiles=1;
		for(int i=0;i<fileList.length();i++) {
			if(fileList.charAt(i)==',') {
				numFiles++;
			}
		}
		String[] fileArray=new String[numFiles];
		for(int i=0;i<numFiles;i++) {
			fileArray[i]="";
		}
		int currentFile=0;
		for(int i=0;i<fileList.length();i++) {
			if(fileList.charAt(i)==',') {
				currentFile++;
			} else {
				fileArray[currentFile]+=fileList.charAt(i);
			}
		}
		File[] BSPFiles=new File[numFiles];
		setTotalProgress(0, BSPFiles.length);
		for(int i=0;i<numFiles;i++) {
			BSPFiles[i]=new File(fileArray[i]);
		}
		Runnable decompiler=new DecompilerThread(BSPFiles, vertexDecomp, correctPlaneFlip, calcVerts, roundNums, toVMF, planePointCoef);
		Thread decompilerworker = new Thread(decompiler);
		decompilerworker.setName("Decompiler");
		decompilerworker.start();
	}
	
	protected static void print(String out) {
		if(consolebox!=null) {
			consolebox.append(out);
		} else {
			System.out.print(out);
		}
	}
	
	protected static void println(String out) {
		print(out);
		print("\n");
	}
	
	protected static void clearConsole() {
		if(consolebox!=null) {
			consolebox.replaceRange("", 0, consolebox.getText().length());
		}
	}
	
	protected static void setProgress(int in, int max, String map) {
		if(progressBar!=null) {
			progressBar.setMaximum(max);
			progressBar.setValue(in);
			if(in==max) {
				progressBar.setString("Done!");
			} else {
				progressBar.setString(map+" "+(int)((in/(float)max)*100)+"%");
			}
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
	
	protected static void setConsoleEnabled(boolean in) {
		if(consolebox!=null) {
			consolebox.setEnabled(in);
		}
	}
	
	protected static void setDecompileButtonEnabled(boolean in) {
		if(btn_decomp!=null) {
			btn_decomp.setEnabled(in);
		}
	}
}
