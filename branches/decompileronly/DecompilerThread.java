// DecompilerThread class
// Multithreads the decompiler, allowing queueing of decompilation jobs while
// preventing the program GUI from freezing during operation

import java.io.File;
import java.util.Date;

public class DecompilerThread implements Runnable {
	
	private File BSP;
	private boolean vertexDecomp;
	private boolean correctPlaneFlip;
	private double planePointCoef;
	private boolean toVMF;
	private boolean calcVerts;
	private boolean roundNums;
	private int jobnum;
	private int threadnum;
	
	public DecompilerThread(File BSP, boolean vertexDecomp, boolean correctPlaneFlip, boolean calcVerts, boolean roundNums, boolean toVMF, double planePointCoef, int jobnum, int threadnum) {
		// Set up global variables
		this.vertexDecomp=vertexDecomp;
		this.correctPlaneFlip=correctPlaneFlip;
		this.planePointCoef=planePointCoef;
		this.toVMF=toVMF;
		this.calcVerts=calcVerts;
		this.BSP=BSP;
		this.roundNums=roundNums;
		this.jobnum=jobnum;
		this.threadnum=threadnum;
	}
	
	public void run() {
		Window.setConsoleEnabled(false);
		try {
			Window.window.println("Opening file "+BSP.getAbsolutePath());
			Window.setProgress(jobnum, 0, 1, "Reading...");
			BSPReader reader = new BSPReader(BSP.getAbsolutePath());
			reader.readBSP();
			Decompiler decompiler=null;
			switch(reader.getVersion()) {
				case 38:
					Window.setProgress(jobnum, 0, reader.BSP38.getBrushes().getNumElements()+reader.BSP38.getEntities().getNumElements(), "Decompiling...");
					decompiler = new Decompiler(reader.BSP38, vertexDecomp, correctPlaneFlip, calcVerts, roundNums, toVMF, planePointCoef, jobnum);
					decompiler.decompile();
					break;
				case 42:
					Window.setProgress(jobnum, 0, reader.BSP42.getBrushes().getNumElements()+reader.BSP42.getEntities().getNumElements(), "Decompiling...");
					decompiler = new Decompiler(reader.BSP42, vertexDecomp, correctPlaneFlip, calcVerts, roundNums, toVMF, planePointCoef, jobnum);
					decompiler.decompile();
					break;
				case 46:
					Window.setProgress(jobnum, 0, reader.BSP46.getBrushes().getNumElements()+reader.BSP46.getEntities().getNumElements(), "Decompiling...");
					decompiler = new Decompiler(reader.BSP46, vertexDecomp, correctPlaneFlip, calcVerts, roundNums, toVMF, planePointCoef, jobnum);
					decompiler.decompile();
					break;
			}
			Window.setProgress(jobnum, 1, 1, "Done!");
		} catch (java.lang.Exception e) {
			Window.window.println("\nException caught: "+e+"\nPlease let me know on the issue tracker!\nhttp://code.google.com/p/jbn-bsp-lump-tools/issues/entry");
			Window.setProgress(jobnum, 0, 1, "ERROR! See log!");
		}
		Window.setAbortButtonEnabled(jobnum, false);
		Window.window.startNextJob(true, threadnum);
	}
}
