// DecompilerThread class
// Multithreads the decompiler, allowing queueing of decompilation jobs while
// preventing the program GUI from freezing during operation

import java.io.File;
import java.util.Date;
import java.awt.Color;

public class DecompilerThread implements Runnable {
	
	private File BSP;
	private DoomMap doomMap;
	private int jobnum;
	private int threadnum;
	
	public DecompilerThread(File BSP, int jobnum, int threadnum) {
		// Set up global variables
		this.BSP=BSP;
		this.jobnum=jobnum;
		this.threadnum=threadnum;
	}
	
	public DecompilerThread(DoomMap doomMap, int jobNum, int threadNum) {
		this.doomMap=doomMap;
		this.threadnum=threadNum;
		this.jobnum=jobNum;
	}
	
	public void run() {
		try {
			if(doomMap!=null) { // If this is a Doom map extracted from a WAD
				Window.setProgress(jobnum, 0, doomMap.getSubSectors().getNumElements(), "Decompiling...");
				WADDecompiler decompiler = new WADDecompiler(doomMap, jobnum);
				decompiler.decompile();
			} else {
				Window.println("Opening file "+BSP.getAbsolutePath(),Window.VERBOSITY_ALWAYS);
				Window.setProgress(jobnum, 0, 1, "Reading...");
				BSPReader reader = new BSPReader(BSP);
				reader.readBSP();
				if(reader.isSource()) {
					Window.setProgress(jobnum, 0, reader.SourceBSPObject.getBrushes().length()+reader.SourceBSPObject.getEntities().length(), "Decompiling...");
					SourceBSPDecompiler decompiler = new SourceBSPDecompiler(reader.SourceBSPObject, jobnum);
					decompiler.decompile();
				} else {
					if(reader.isRaven()) {
						Window.setProgress(jobnum, 0, reader.ravenBSP.getBrushes().getNumElements()+reader.ravenBSP.getEntities().length(), "Decompiling...");
						RavenBSPDecompiler decompiler = new RavenBSPDecompiler(reader.ravenBSP, jobnum);
						decompiler.decompile();
					} else {
						switch(reader.getVersion()) {
							case 38:
								Window.setProgress(jobnum, 0, reader.BSP38.getBrushes().length()+reader.BSP38.getEntities().length(), "Decompiling...");
								BSP38Decompiler decompiler38 = new BSP38Decompiler(reader.BSP38, jobnum);
								decompiler38.decompile();
								break;
							case 42:
								Window.setProgress(jobnum, 0, reader.BSP42.getBrushes().getNumElements()+reader.BSP42.getEntities().length(), "Decompiling...");
								BSP42Decompiler decompiler42 = new BSP42Decompiler(reader.BSP42, jobnum);
								decompiler42.decompile();
								break;
							case 46:
							case 47:
								Window.setProgress(jobnum, 0, reader.BSP46.getBrushes().getNumElements()+reader.BSP46.getEntities().length(), "Decompiling...");
								BSP46Decompiler decompiler46 = new BSP46Decompiler(reader.BSP46, jobnum);
								decompiler46.decompile();
								break;
						}
					}
				}
			}
			Window.setProgress(jobnum, 1, 1, "Done!");
			Window.setProgressColor(jobnum, new Color(64, 192, 64));
		} catch (java.lang.Exception e) {
			Window.println(""+(char)0x0D+(char)0x0A+"Exception caught in job "+(jobnum+1)+": "+e+(char)0x0D+(char)0x0A+"Please let me know on the issue tracker!\nhttp://code.google.com/p/jbn-bsp-lump-tools/issues/entry",Window.VERBOSITY_ALWAYS);
			Window.setProgress(jobnum, 1, 1, "ERROR! See log!");
			Window.setProgressColor(jobnum, new Color(255, 128, 128));
		}
		Window.setAbortButtonEnabled(jobnum, false);
		Window.window.startNextJob(true, threadnum);
	}
}
