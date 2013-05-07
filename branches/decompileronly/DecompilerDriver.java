// DecompilerDriver class
// If main() is given no args, runs the GUI.
// If main() is given args, runs as a cmd application.

import java.io.File;

public class DecompilerDriver {

	protected static Window window;
	
	public static void main(String[] args) {
		if(args.length<1) {
			window=new Window();
		} else {
			for(int i=0;i<args.length;i++) {
				Runnable runMe = new DecompilerThread(new File(args[i]));
				Thread decompThread = new Thread(runMe);
				decompThread.run();
			}
		}
	}
	
	public void printOptions() {
		System.out.println("BSP Decompiler v3 by 005");
		System.out.println("Usage: decompiler filename0.bsp [filename1.bsp] [filename2.bsp] ...\n");
		System.out.println("Run this program with no parameters to use the GUI.");
	}
}