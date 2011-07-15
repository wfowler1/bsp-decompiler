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

	private JFileChooser file_selector;
	private JButton btn_open;
	private JButton btn_decomp;
	private JTextField txt_file;
	private JTextField txt_coef;
	private JTextArea consolebox;
	private JLabel lbl_spacer;
	private JLabel lbl_coef;
	private JScrollPane console_pane;
	private JCheckBox chk_planar;
	private JCheckBox chk_skipVertCheck;
	private JCheckBox chk_facesOnly;

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
		consolebox.setEnabled(false);
		
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
		
		// facial decompilation not implemented yet
		/*chk_facesOnly = new JCheckBox("Decompile faces only");
		
		GridBagConstraints FacesOnlyConstraints = new GridBagConstraints();
		FacesOnlyConstraints.fill = GridBagConstraints.NONE;
		FacesOnlyConstraints.gridx = 2;
		FacesOnlyConstraints.gridy = 1;
		FacesOnlyConstraints.gridwidth = 1;
		FacesOnlyConstraints.gridheight = 1;
		pane.add(chk_facesOnly, FacesOnlyConstraints);*/
	} // constructor

	public void actionPerformed(ActionEvent action) {
		Decompiler decompiler;
		File theBSP;
		
		if (action.getSource() == btn_open) {
			file_selector = new JFileChooser();
			int returnVal = file_selector.showOpenDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = file_selector.getSelectedFile();
				txt_file.setText(file_selector.getSelectedFile().getAbsolutePath());
			}
		}
		
		if(action.getSource() == btn_decomp) {
			theBSP=new File(txt_file.getText());
			if(!theBSP.exists()) {
				JOptionPane.showMessageDialog(this, "File \""+txt_file.getText()+"\" not found!");
			} else {
				btn_decomp.setEnabled(false);
				decompiler = new Decompiler(theBSP.getAbsolutePath(), !chk_planar.isSelected(), !chk_skipVertCheck.isSelected(), false, Double.parseDouble(txt_coef.getText()));
				String savePath=txt_file.getText().substring(0, txt_file.getText().length()-4);
				decompiler.decompile(savePath+".map");
				btn_decomp.setEnabled(true);
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
