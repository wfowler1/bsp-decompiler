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
	private JTextArea consolebox;
	private JLabel lbl_spacer;
	private JScrollPane console_pane;

	public Window(Container pane) {
		pane.setLayout(new GridBagLayout());
		
		btn_open = new JButton("Find BSP");
		
		GridBagConstraints openConstraints = new GridBagConstraints();
		openConstraints.fill = GridBagConstraints.NONE;
		openConstraints.gridx = 1;
		openConstraints.gridy = 0;
		openConstraints.gridwidth = 1;
		openConstraints.gridheight = 1;
		pane.add(btn_open, openConstraints);
		
		btn_open.addActionListener(this);
		
		btn_decomp = new JButton("Decompile BSP");
		
		GridBagConstraints decompConstraints = new GridBagConstraints();
		decompConstraints.fill = GridBagConstraints.BOTH;
		decompConstraints.gridx = 0;
		decompConstraints.gridy = 1;
		decompConstraints.gridwidth = 2;
		decompConstraints.gridheight = 1;
		pane.add(btn_decomp, decompConstraints);
		
		btn_decomp.addActionListener(this);
		
		txt_file = new JTextField(40);
		
		GridBagConstraints fileConstraints = new GridBagConstraints();
		fileConstraints.fill = GridBagConstraints.NONE;
		fileConstraints.gridx = 0;
		fileConstraints.gridy = 0;
		fileConstraints.gridwidth = 1;
		fileConstraints.gridheight = 1;
		pane.add(txt_file, fileConstraints);
		
		consolebox = new JTextArea(15, 50);
		consolebox.setEnabled(false);
		
		console_pane = new JScrollPane(consolebox);
		
		GridBagConstraints consoleConstraints = new GridBagConstraints();
		consoleConstraints.fill = GridBagConstraints.NONE;
		consoleConstraints.gridx = 0;
		consoleConstraints.gridy = 3;
		consoleConstraints.gridwidth = 2;
		consoleConstraints.gridheight = 1;
		pane.add(console_pane, consoleConstraints);
		
		lbl_spacer = new JLabel(" ");
		
		GridBagConstraints spacerConstraints = new GridBagConstraints();
		spacerConstraints.fill = GridBagConstraints.NONE;
		spacerConstraints.gridx = 0;
		spacerConstraints.gridy = 2;
		spacerConstraints.gridwidth = 2;
		spacerConstraints.gridheight = 1;
		pane.add(lbl_spacer, spacerConstraints);
	} // constructor

	public void actionPerformed(ActionEvent action) {
		if (action.getSource() == btn_open) {
			file_selector = new JFileChooser();
			int returnVal = file_selector.showOpenDialog(this);
			
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = file_selector.getSelectedFile();
				txt_file.setText(file_selector.getSelectedFile().getAbsolutePath());
			}
		}
		Decompiler decompiler;
		File theBSP;
		if(action.getSource() == btn_decomp) {
			theBSP=new File(txt_file.getText());
			if(!theBSP.exists()) {
				JOptionPane.showMessageDialog(this, "File \""+txt_file.getText()+"\" not found!");
			} else {
				btn_decomp.setEnabled(false);
				decompiler = new Decompiler(theBSP.getAbsolutePath());
				String savePath=txt_file.getText().substring(0, txt_file.getText().length()-4);
				decompiler.decompile(savePath+".map");
				btn_decomp.setEnabled(true);
			}
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
