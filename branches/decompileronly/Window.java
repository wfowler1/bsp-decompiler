// Window class

// My first attempt at a GUI for the decompiler.
// For a list of swing components check here:
// http://download.oracle.com/javase/tutorial/ui/features/components.html

import java.awt.*;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;


public class Window extends JPanel implements ActionListener {

	protected static Window window;
	private static BSPFileFilter BSPFilter = new BSPFileFilter();

	// main method
	// Creates an Object of this class and launches the GUI. Entry point to the whole program.
	public static void main(String[] args) {
		
		UIManager myUI=new UIManager();
		try {
			myUI.setLookAndFeel(myUI.getSystemLookAndFeelClassName());
		} catch(java.lang.Exception e) {
			;
		}
		
		JFrame frame = new JFrame("BSP v42 Decompiler by 005");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		window = new Window(frame.getContentPane());
		
		frame.setIconImage(new ImageIcon("icon32x32.PNG").getImage());

		frame.setPreferredSize(new Dimension(600, 400));

		frame.pack();
		frame.setResizable(false);
		frame.setVisible(true);
		
		window.print("Got a bug to report? Want to see something added?\nCreate an issue report at\nhttp://code.google.com/p/jbn-bsp-lump-tools/issues/list");
	}

	private static JFileChooser file_selector;
	private static JButton btn_open;
	protected static JButton btn_decomp;
	private static JTextField txt_file;
	private static JTextField txt_coef;
	private static JTextArea consolebox;
	private static JLabel lbl_spacer;
	private static JLabel lbl_coef;
	private static JScrollPane console_pane;
	private static JCheckBox chk_planar;
	private static JCheckBox chk_skipVertCheck;
	private static JCheckBox chk_skipPlaneFlip;


	public Window(Container pane) {
		pane.setLayout(new GridBagLayout());
		
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
		
		txt_file = new JTextField(40);
		
		GridBagConstraints fileConstraints = new GridBagConstraints();
		fileConstraints.fill = GridBagConstraints.NONE;
		fileConstraints.gridx = 0;
		fileConstraints.gridy = 0;
		fileConstraints.gridwidth = 2;
		fileConstraints.gridheight = 1;
		pane.add(txt_file, fileConstraints);
		
		lbl_coef = new JLabel("Plane points coefficient: ");
		
		GridBagConstraints coeflblConstraints = new GridBagConstraints();
		coeflblConstraints.fill = GridBagConstraints.NONE;
		coeflblConstraints.gridx = 3;
		coeflblConstraints.gridy = 1;
		coeflblConstraints.gridwidth = 1;
		coeflblConstraints.gridheight = 1;
		pane.add(lbl_coef, coeflblConstraints);
		
		txt_coef = new JTextField(5);
		txt_coef.setText("100");
		
		GridBagConstraints coefConstraints = new GridBagConstraints();
		coefConstraints.fill = GridBagConstraints.NONE;
		coefConstraints.gridx = 4;
		coefConstraints.gridy = 1;
		coefConstraints.gridwidth = 1;
		coefConstraints.gridheight = 1;
		pane.add(txt_coef, coefConstraints);
		
		consolebox = new JTextArea(15, 70);
		
		console_pane = new JScrollPane(consolebox);
		
		GridBagConstraints consoleConstraints = new GridBagConstraints();
		consoleConstraints.fill = GridBagConstraints.NONE;
		consoleConstraints.gridx = 0;
		consoleConstraints.gridy = 3;
		consoleConstraints.gridwidth = 5;
		consoleConstraints.gridheight = 1;
		pane.add(console_pane, consoleConstraints);
		
		lbl_spacer = new JLabel(" ");
		
		GridBagConstraints spacerConstraints = new GridBagConstraints();
		spacerConstraints.fill = GridBagConstraints.NONE;
		spacerConstraints.gridx = 0;
		spacerConstraints.gridy = 2;
		spacerConstraints.gridwidth = 5;
		spacerConstraints.gridheight = 1;
		pane.add(lbl_spacer, spacerConstraints);
		
		chk_planar = new JCheckBox("Planar Decompilation Only");
		
		GridBagConstraints planarConstraints = new GridBagConstraints();
		planarConstraints.fill = GridBagConstraints.NONE;
		planarConstraints.gridx = 0;
		planarConstraints.gridy = 1;
		planarConstraints.gridwidth = 1;
		planarConstraints.gridheight = 1;
		pane.add(chk_planar, planarConstraints);
		
		chk_planar.addActionListener(this);
		
		chk_skipVertCheck = new JCheckBox("Skip Vertex Checking");
		
		GridBagConstraints vertCheckConstraints = new GridBagConstraints();
		vertCheckConstraints.fill = GridBagConstraints.NONE;
		vertCheckConstraints.gridx = 1;
		vertCheckConstraints.gridy = 1;
		vertCheckConstraints.gridwidth = 1;
		vertCheckConstraints.gridheight = 1;
		pane.add(chk_skipVertCheck, vertCheckConstraints);
		
		chk_skipVertCheck.addActionListener(this);
		
		chk_skipPlaneFlip = new JCheckBox("Skip plane flip");
		
		GridBagConstraints FacesOnlyConstraints = new GridBagConstraints();
		FacesOnlyConstraints.fill = GridBagConstraints.NONE;
		FacesOnlyConstraints.gridx = 2;
		FacesOnlyConstraints.gridy = 1;
		FacesOnlyConstraints.gridwidth = 1;
		FacesOnlyConstraints.gridheight = 1;
		pane.add(chk_skipPlaneFlip, FacesOnlyConstraints);
	} // constructor

	public void actionPerformed(ActionEvent action) {
		File theBSP;
		
		if (action.getSource() == btn_open) {
			file_selector = new JFileChooser();
			file_selector.addChoosableFileFilter(BSPFilter);
			// file_selector.setIconImage(new ImageIcon("folder32x32.PNG").getImage());
			int returnVal = file_selector.showOpenDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = file_selector.getSelectedFile();
				txt_file.setText(file_selector.getSelectedFile().getAbsolutePath());
			}
		}
		
		if(action.getSource() == btn_decomp) {
			theBSP=new File(txt_file.getText());
			clearConsole();
			if(!theBSP.exists()) {
				println("File \""+txt_file.getText()+"\" not found!");
			} else {
				consolebox.setEnabled(false);
				btn_decomp.setEnabled(false);
				Runnable decompiler = new Decompiler(txt_file.getText(), !chk_planar.isSelected(), !chk_skipVertCheck.isSelected(), !chk_skipPlaneFlip.isSelected(), Double.parseDouble(txt_coef.getText()));
				Thread worker = new Thread(decompiler);
				worker.setName("Decompiler");
				worker.start();
			}
		}
		
		if(action.getSource() == chk_planar) {
			if(chk_planar.isSelected()) {
				chk_skipVertCheck.setEnabled(false);
				chk_skipVertCheck.setSelected(false);
			} else {
				chk_skipVertCheck.setEnabled(true);
			}
		}
		
		if(action.getSource() == chk_skipVertCheck) {
			;
		}
	}
	
	protected void print(String out) {
		consolebox.append(out);
	}
	
	protected void println(String out) {
		print(out);
		print("\n");
	}
	
	protected void clearConsole() {
		consolebox.replaceRange("", 0, consolebox.getText().length());
	}
}
