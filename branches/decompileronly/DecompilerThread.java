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
					if(reader.isRaven() && !reader.isSin()) {
						Window.setProgress(jobnum, 0, reader.ravenBSP.getBrushes().length()+reader.ravenBSP.getEntities().length(), "Decompiling...");
						RavenBSPDecompiler decompiler = new RavenBSPDecompiler(reader.ravenBSP, jobnum);
						decompiler.decompile();
					} else {
						if(reader.isMOHAA()) {
							Window.setProgress(jobnum, 0, reader.MOHAABSP.getBrushes().length()+reader.MOHAABSP.getEntities().length(), "Decompiling...");
							MoHAABSPDecompiler MOHAAdecompiler = new MoHAABSPDecompiler(reader.MOHAABSP, jobnum);
							MOHAAdecompiler.decompile();
						} else {
							if(reader.isEF2()) {
								Window.setProgress(jobnum, 0, reader.STEF2BSP.getBrushes().length()+reader.STEF2BSP.getEntities().length(), "Decompiling...");
								EF2Decompiler decompiler = new EF2Decompiler(reader.STEF2BSP, jobnum);
								decompiler.decompile();
							} else {
								if(reader.isSin()) {
									Window.setProgress(jobnum, 0, reader.SINBSP.getBrushes().length()+reader.SINBSP.getEntities().length(), "Decompiling...");
									SiNBSPDecompiler decompiler = new SiNBSPDecompiler(reader.SINBSP, jobnum);
									decompiler.decompile();
								}
								switch(reader.getVersion()) {
									case 38:
										Window.setProgress(jobnum, 0, reader.BSP38.getBrushes().length()+reader.BSP38.getEntities().length(), "Decompiling...");
										BSP38Decompiler decompiler38 = new BSP38Decompiler(reader.BSP38, jobnum);
										decompiler38.decompile();
										break;
									case 42:
										Window.setProgress(jobnum, 0, reader.BSP42.getBrushes().length()+reader.BSP42.getEntities().length(), "Decompiling...");
										BSP42Decompiler decompiler42 = new BSP42Decompiler(reader.BSP42, jobnum);
										decompiler42.decompile();
										break;
									case 46:
									case 47:
										Window.setProgress(jobnum, 0, reader.BSP46.getBrushes().length()+reader.BSP46.getEntities().length(), "Decompiling...");
										BSP46Decompiler decompiler46 = new BSP46Decompiler(reader.BSP46, jobnum);
										decompiler46.decompile();
										break;
									case 4:
									case 22:
									case 59:
										Window.setProgress(jobnum, 0, reader.CODBSP.getBrushes().length()+reader.CODBSP.getEntities().length(), "Decompiling...");
										CoDBSPDecompiler CoDdecompiler = new CoDBSPDecompiler(reader.CODBSP, jobnum);
										CoDdecompiler.decompile();
										break;
								}
							}
						}
					}
				}
			}
			Window.setProgress(jobnum, 1, 1, "Done!");
			Window.setProgressColor(jobnum, new Color(64, 192, 64));
		} catch (java.io.IOException e) {
			Window.println(""+(char)0x0D+(char)0x0A+"Exception caught in job "+(jobnum+1)+": "+e+(char)0x0D+(char)0x0A+"Please let me know on the issue tracker!\nhttp://code.google.com/p/jbn-bsp-lump-tools/issues/entry",Window.VERBOSITY_ALWAYS);
			String stackTrace="";
			StackTraceElement[] trace=e.getStackTrace();
			for(int i=0;i<trace.length;i++) {
				stackTrace+=trace.toString()+Window.LF;
			}
			Window.println(e.getMessage()+Window.LF+stackTrace,Window.VERBOSITY_WARNINGS);
			Window.setProgress(jobnum, 1, 1, "ERROR! See log!");
			Window.setProgressColor(jobnum, new Color(255, 128, 128));
		}
		Window.setAbortButtonEnabled(jobnum, false);
		Window.window.startNextJob(true, threadnum);
	}
}
