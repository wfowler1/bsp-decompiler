// LOGFileFilter class

// Used by the GUI to add a file filter to the JFileChooser dialog box.
// This is ridiculous, I shouldn't have to create a class for a file filter.

import javax.swing.*;
import java.io.File;

public class LOGFileFilter extends javax.swing.filechooser.FileFilter {
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