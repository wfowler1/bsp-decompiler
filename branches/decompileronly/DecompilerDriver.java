// DecompilerDriver class

// Sets up the basic interface for decompiling a BSP v42.

import java.util.Scanner;
import java.io.File;

public class DecompilerDriver {
	public static void main(String[] args) throws java.io.FileNotFoundException, java.io.IOException {
		Scanner keyboard = new Scanner(System.in);

		System.out.println("BSP v42 Decompiler by 005");
		System.out.println("Enter path to map.\nThis path can either be to a folder of separated lumps or to a BSP.");
		System.out.print("Path to lumps folder: ");
		String filepath=keyboard.nextLine();

		Decompiler decompiler = new Decompiler(filepath);
		
		// Uncomment this to force planar decompilation
		/*for(int i=0;i<myBSP.getLump09().getNumElements();i++) {
			myBSP.getLump09().getFace(i).setVert(0);
			myBSP.getLump09().getFace(i).setNumVerts(0);
			myBSP.getLump09().getFace(i).setMeshs(0);
			myBSP.getLump09().getFace(i).setNumMeshs(0);
		}*/
		
		System.out.print("Path to save decompiled map to: ");
		String decompfolder=keyboard.nextLine();
		decompiler.decompile(decompfolder);

		// A dirty hack to delete the folder where the lumps are. It works, I don't care that it's hacky.
		File dir=new File(filepath.substring(0,filepath.length()-4));
		// For some reason Java refuses to delete a directory if there are files in it. So explicitly delete every single file before deleting the dir.
		File L0=new File(filepath.substring(0,filepath.length()-4)+"\\00 - "+Decompiler.LUMPNAMES[0]+".txt");
		File L1=new File(filepath.substring(0,filepath.length()-4)+"\\01 - "+Decompiler.LUMPNAMES[1]+".hex");
		File L2=new File(filepath.substring(0,filepath.length()-4)+"\\02 - "+Decompiler.LUMPNAMES[2]+".hex");
		File L3=new File(filepath.substring(0,filepath.length()-4)+"\\03 - "+Decompiler.LUMPNAMES[3]+".hex");
		File L4=new File(filepath.substring(0,filepath.length()-4)+"\\04 - "+Decompiler.LUMPNAMES[4]+".hex");
		File L6=new File(filepath.substring(0,filepath.length()-4)+"\\06 - "+Decompiler.LUMPNAMES[6]+".hex");
		File L9=new File(filepath.substring(0,filepath.length()-4)+"\\09 - "+Decompiler.LUMPNAMES[9]+".hex");
		File L11=new File(filepath.substring(0,filepath.length()-4)+"\\11 - "+Decompiler.LUMPNAMES[11]+".hex");
		File L13=new File(filepath.substring(0,filepath.length()-4)+"\\13 - "+Decompiler.LUMPNAMES[13]+".hex");
		File L14=new File(filepath.substring(0,filepath.length()-4)+"\\14 - "+Decompiler.LUMPNAMES[14]+".hex");
		File L15=new File(filepath.substring(0,filepath.length()-4)+"\\15 - "+Decompiler.LUMPNAMES[15]+".hex");
		File L16=new File(filepath.substring(0,filepath.length()-4)+"\\16 - "+Decompiler.LUMPNAMES[16]+".hex");
		File L17=new File(filepath.substring(0,filepath.length()-4)+"\\17 - "+Decompiler.LUMPNAMES[17]+".hex");
		
		L0.delete();
		L1.delete();
		L2.delete();
		L3.delete();
		L4.delete();
		L6.delete();
		L9.delete();
		L11.delete();
		L13.delete();
		L14.delete();
		L15.delete();
		L16.delete();
		L17.delete();
		
		// Of course, there's no guarantee this will work if someone makes new files in the lumps folder
		dir.delete();
	}
}
