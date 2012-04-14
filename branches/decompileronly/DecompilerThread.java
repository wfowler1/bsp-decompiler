// DecompilerThread class
// Multithreads the decompiler, allowing queueing of decompilation jobs while
// preventing the program GUI from freezing during operation

import java.io.File;
import java.util.Date;

public class DecompilerThread implements Runnable {
	
	private File[] BSPs;
	private boolean vertexDecomp;
	private boolean correctPlaneFlip;
	private double planePointCoef;
	private boolean toVMF;
	private boolean calcVerts;
	private boolean roundNums;
	
	private static Runtime r = Runtime.getRuntime(); // Get a runtime object. This is for calling
	                                                 // Java's garbage collector and does not need
	                                                 // to be ported. I try not to leave memory leaks
	                                                 // but since Java has no way explicitly reallocate
	                                                 // unused memory I have to tell it when a good
	                                                 // time is to run the garbage collector, by
	                                                 // calling gc(). Also, it is used to execute EXEs
	                                                 // from within the program by calling .exec(path).
	
	public DecompilerThread(File[] BSPs, boolean vertexDecomp, boolean correctPlaneFlip, boolean calcVerts, boolean roundNums, boolean toVMF, double planePointCoef) {
		// Set up global variables
		this.vertexDecomp=vertexDecomp;
		this.correctPlaneFlip=correctPlaneFlip;
		this.planePointCoef=planePointCoef;
		this.toVMF=toVMF;
		this.calcVerts=calcVerts;
		this.BSPs=BSPs;
		this.roundNums=roundNums;
	}
	
	public void run() {
		Date begin=new Date();
		if(BSPs.length>1) {
			Window.window.println("Decompiling "+BSPs.length+" maps");
		}
		for(int i=0;i<BSPs.length;i++) {
			if(!BSPs[i].exists()) {
				Window.window.println("File \""+BSPs[i].getAbsolutePath()+"\" not found!");
				Window.setAbortButtonEnabled(false);
				System.out.println("Run with -? switch for command line help"); // Will only print to console or cmd!
			} else {
				Window.setConsoleEnabled(false);
				Window.setDecompileButtonEnabled(false);
				Window.setAbortButtonEnabled(true);
				try {
					Window.window.println("Opening file "+BSPs[i].getAbsolutePath());
					BSPReader reader = new BSPReader(BSPs[i].getAbsolutePath());
					reader.readBSP();
					Decompiler decompiler=null;
					switch(reader.getVersion()) {
						case 38:
							Window.setProgress(0, reader.BSP38.getBrushes().getNumElements()+reader.BSP38.getEntities().getNumElements(), BSPs[i].getName());
							decompiler = new Decompiler(reader.BSP38, vertexDecomp, correctPlaneFlip, calcVerts, roundNums, toVMF, planePointCoef);
							decompiler.decompile();
							break;
						case 42:
							Window.setProgress(0, reader.BSP42.getBrushes().getNumElements()+reader.BSP42.getEntities().getNumElements(), BSPs[i].getName());
							decompiler = new Decompiler(reader.BSP42, vertexDecomp, correctPlaneFlip, calcVerts, roundNums, toVMF, planePointCoef);
							decompiler.decompile();
							break;
						case 46:
							Window.setProgress(0, reader.BSP46.getBrushes().getNumElements()+reader.BSP46.getEntities().getNumElements(), BSPs[i].getName());
							decompiler = new Decompiler(reader.BSP46, vertexDecomp, correctPlaneFlip, calcVerts, roundNums, toVMF, planePointCoef);
							decompiler.decompile();
							break;
					}
				} catch (java.lang.Exception e) {
					Window.window.println("\nException caught: "+e+"\nPlease let me know on the issue tracker!\nhttp://code.google.com/p/jbn-bsp-lump-tools/issues/list");
					Window.setConsoleEnabled(true);
					Window.setDecompileButtonEnabled(true);
					Window.setAbortButtonEnabled(false);
				}
			}
			Window.setTotalProgress(i+1, BSPs.length);
		}
		Window.setDecompileButtonEnabled(true); // Once the thread is finished running, reenable the Decompile button
		Window.setAbortButtonEnabled(false);
		r.gc(); // Now the program has time to rest while the user does whatever. Collect garbage.
		if(BSPs.length>1) {
			Date end=new Date();
			Window.window.println("All files decompiled in "+(end.getTime()-begin.getTime())+"ms");
		}
	}
}