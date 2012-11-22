// DecompilerThread class
// Multithreads the decompiler, allowing queueing of decompilation jobs while
// preventing the program GUI from freezing during operation

import java.io.File;
import java.util.Date;
import java.awt.Color;

public class DecompilerThread implements Runnable {
	
	private File BSPFile;
	private DoomMap doomMap;
	private int jobnum;
	private int threadnum;
	
	public DecompilerThread(File BSPFile, int jobnum, int threadnum) {
		// Set up global variables
		this.BSPFile=BSPFile;
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
				Window.println("Opening file "+BSPFile.getAbsolutePath(),Window.VERBOSITY_ALWAYS);
				Window.setProgress(jobnum, 0, 1, "Reading...");
				BSPReader reader = new BSPReader(BSPFile);
				reader.readBSP();
				if(reader.isSource()) {
					Window.setProgress(jobnum, 0, reader.BSPObject.getBrushes().length()+reader.BSPObject.getEntities().length(), "Decompiling...");
					SourceBSPDecompiler decompiler = new SourceBSPDecompiler(reader.BSPObject, jobnum);
					decompiler.decompile();
				} else {
					if(reader.isRaven() && !reader.isSin()) {
						Window.setProgress(jobnum, 0, reader.BSPObject.getBrushes().length()+reader.BSPObject.getEntities().length(), "Decompiling...");
						BSP46Decompiler decompiler = new BSP46Decompiler(reader.BSPObject, jobnum);
						decompiler.decompile();
					} else {
						if(reader.isMOHAA()) {
							Window.setProgress(jobnum, 0, reader.BSPObject.getBrushes().length()+reader.BSPObject.getEntities().length(), "Decompiling...");
							BSP46Decompiler MOHAAdecompiler = new BSP46Decompiler(reader.BSPObject, jobnum);
							MOHAAdecompiler.decompile();
						} else {
							if(reader.isEF2()) {
								Window.setProgress(jobnum, 0, reader.BSPObject.getBrushes().length()+reader.BSPObject.getEntities().length(), "Decompiling...");
								BSP46Decompiler decompiler = new BSP46Decompiler(reader.BSPObject, jobnum);
								decompiler.decompile();
							} else {
								if(reader.isSin()) {
									Window.setProgress(jobnum, 0, reader.BSPObject.getBrushes().length()+reader.BSPObject.getEntities().length(), "Decompiling...");
									BSP38Decompiler decompiler = new BSP38Decompiler(reader.BSPObject, jobnum);
									decompiler.decompile();
								} else {
									if(reader.isFAKK()) {
										switch(reader.getVersion()) {
											case 12:
											case 42:
												Window.setProgress(jobnum, 0, reader.BSPObject.getBrushes().length()+reader.BSPObject.getEntities().length(), "Decompiling...");
												BSP46Decompiler decompiler = new BSP46Decompiler(reader.BSPObject, jobnum);
												decompiler.decompile();
												break;
										}
									} else {
										switch(reader.getVersion()) {
											case 38:
												Window.setProgress(jobnum, 0, reader.BSPObject.getBrushes().length()+reader.BSPObject.getEntities().length(), "Decompiling...");
												BSP38Decompiler decompiler38 = new BSP38Decompiler(reader.BSPObject, jobnum);
												decompiler38.decompile();
												break;
											case 42:
												Window.setProgress(jobnum, 0, reader.BSPObject.getBrushes().length()+reader.BSPObject.getEntities().length(), "Decompiling...");
												BSP42Decompiler decompiler42 = new BSP42Decompiler(reader.BSPObject, jobnum);
												decompiler42.decompile();
												break;
											case 46:
											case 47:
												Window.setProgress(jobnum, 0, reader.BSPObject.getBrushes().length()+reader.BSPObject.getEntities().length(), "Decompiling...");
												BSP46Decompiler decompiler46 = new BSP46Decompiler(reader.BSPObject, jobnum);
												decompiler46.decompile();
												break;
											case 4:
											case 22:
											case 59:
												Window.setProgress(jobnum, 0, reader.BSPObject.getBrushes().length()+reader.BSPObject.getEntities().length(), "Decompiling...");
												BSP46Decompiler CoDdecompiler = new BSP46Decompiler(reader.BSPObject, jobnum);
												CoDdecompiler.decompile();
												break;
										}
									}
								}
							}
						}
					}
				}
			}
			Window.setProgress(jobnum, 1, 1, "Done!");
			Window.setProgressColor(jobnum, new Color(64, 192, 64));
		} catch (java.lang.Exception e) {
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
