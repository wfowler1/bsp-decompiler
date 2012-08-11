// DecompilerThread class
// Multithreads the decompiler, allowing queueing of decompilation jobs while
// preventing the program GUI from freezing during operation

import java.io.File;
import java.util.Date;
import java.awt.Color;

public class DecompilerThread implements Runnable {
	
	private File BSP;
	private DoomMap doomMap;
	private boolean vertexDecomp;
	private boolean correctPlaneFlip;
	private boolean toHammer;
	private boolean toRadiant;
	private boolean toGearcraft;
	private boolean calcVerts;
	private boolean roundNums;
	private int jobnum;
	private int threadnum;
	
	public DecompilerThread(File BSP, boolean vertexDecomp, boolean correctPlaneFlip, boolean calcVerts, boolean roundNums, boolean toHammer, boolean toRadiant, boolean toGearcraft, int jobnum, int threadnum) {
		// Set up global variables
		this.vertexDecomp=vertexDecomp;
		this.correctPlaneFlip=correctPlaneFlip;
		this.toHammer=toHammer;
		this.toRadiant=toRadiant;
		this.toGearcraft=toGearcraft;
		this.calcVerts=calcVerts;
		this.BSP=BSP;
		this.roundNums=roundNums;
		this.jobnum=jobnum;
		this.threadnum=threadnum;
	}
	
	public DecompilerThread(DoomMap doomMap, boolean toHammer, boolean toRadiant, boolean toGearcraft, boolean roundNums, int jobNum, int threadNum) {
		this.doomMap=doomMap;
		this.toHammer=toHammer;
		this.toRadiant=toRadiant;
		this.toGearcraft=toGearcraft;
		this.threadnum=threadNum;
		this.jobnum=jobNum;
		this.roundNums=roundNums;
	}
	
	public void run() {
		try {
			if(doomMap!=null) { // If this is a Doom map extracted from a WAD
				Window.setProgress(jobnum, 0, doomMap.getSubSectors().getNumElements(), "Decompiling...");
				WADDecompiler decompiler = new WADDecompiler(doomMap, roundNums, toHammer, toRadiant, toGearcraft, jobnum);
				decompiler.decompile();
			} else {
				Window.window.println("Opening file "+BSP.getAbsolutePath());
				Window.setProgress(jobnum, 0, 1, "Reading...");
				BSPReader reader = new BSPReader(BSP, toHammer, toRadiant, toGearcraft);
				reader.readBSP();
				switch(reader.getVersion()) {
					case 38:
						Window.setProgress(jobnum, 0, reader.BSP38.getBrushes().length()+reader.BSP38.getEntities().getNumElements(), "Decompiling...");
						BSP38Decompiler decompiler38 = new BSP38Decompiler(reader.BSP38, vertexDecomp, correctPlaneFlip, calcVerts, roundNums, toHammer, toRadiant, toGearcraft, jobnum);
						decompiler38.decompile();
						break;
					case 42:
						Window.setProgress(jobnum, 0, reader.BSP42.getBrushes().getNumElements()+reader.BSP42.getEntities().getNumElements(), "Decompiling...");
						BSP42Decompiler decompiler42 = new BSP42Decompiler(reader.BSP42, vertexDecomp, correctPlaneFlip, calcVerts, roundNums, toHammer, toRadiant, toGearcraft, jobnum);
						decompiler42.decompile();
						break;
					case 46:
						Window.setProgress(jobnum, 0, reader.BSP46.getBrushes().getNumElements()+reader.BSP46.getEntities().getNumElements(), "Decompiling...");
						BSP46Decompiler decompiler46 = new BSP46Decompiler(reader.BSP46, vertexDecomp, correctPlaneFlip, calcVerts, roundNums, toHammer, toRadiant, toGearcraft, jobnum);
						decompiler46.decompile();
						break;
				}
			}
			Window.setProgress(jobnum, 1, 1, "Done!");
			Window.setProgressColor(jobnum, new Color(64, 192, 64));
		} catch (java.lang.Exception e) {
			Window.window.println(""+(char)0x0D+(char)0x0A+"Exception caught in job "+jobnum+": "+e+(char)0x0D+(char)0x0A+"Please let me know on the issue tracker!\nhttp://code.google.com/p/jbn-bsp-lump-tools/issues/entry");
			Window.setProgress(jobnum, 1, 1, "ERROR! See log!");
			Window.setProgressColor(jobnum, new Color(255, 128, 128));
		}
		Window.setAbortButtonEnabled(jobnum, false);
		Window.window.startNextJob(true, threadnum);
	}
}
