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
	private int openAs=-1;
	
	public DecompilerThread(File BSPFile, int jobnum, int threadnum) {
		// Set up global variables
		this.BSPFile=BSPFile;
		this.jobnum=jobnum;
		this.threadnum=threadnum;
	}
	
	public DecompilerThread(File BSPFile, int jobnum, int threadnum, int openAs) {
		// Set up global variables
		this.BSPFile=BSPFile;
		this.jobnum=jobnum;
		this.threadnum=threadnum;
		this.openAs=openAs;
	}
	
	public DecompilerThread(File BSPFile) {
		// Set up global variables
		this.BSPFile=BSPFile;
		this.jobnum=-1;
		this.threadnum=0;
	}
	
	public DecompilerThread(DoomMap doomMap, int jobNum, int threadNum) {
		this.doomMap=doomMap;
		this.threadnum=threadNum;
		this.jobnum=jobNum;
	}
	
	public void run() {
		if(!Thread.currentThread().interrupted()) {
			try {
				if(doomMap!=null) { // If this is a Doom map extracted from a WAD
					Window.setProgress(jobnum, 0, doomMap.getSubSectors().length(), "Decompiling...");
					WADDecompiler decompiler = new WADDecompiler(doomMap, jobnum);
					decompiler.decompile();
				} else {
					Window.println("Opening file "+BSPFile.getAbsolutePath(),Window.VERBOSITY_ALWAYS);
					Window.setProgress(jobnum, 0, 1, "Reading...");
					BSPReader reader = new BSPReader(BSPFile, openAs);
					reader.readBSP();
					if(!reader.isWAD()) {
						try {
							Window.setProgress(jobnum, 0, reader.getBSPObject().getBrushes().length()+reader.getBSPObject().getEntities().length(), "Decompiling...");
						} catch(java.lang.NullPointerException e) {
							Window.setProgress(jobnum, 0, reader.getBSPObject().getLeaves().length()+reader.getBSPObject().getEntities().length(), "Decompiling...");
						}
							
						switch(reader.getVersion()) {
							case BSP.TYPE_QUAKE:
								Window.println("ERROR: Algorithm for decompiling Quake BSPs not written yet.",Window.VERBOSITY_ALWAYS);
								throw new java.lang.Exception(); // Throw an exception to the exception handler to indicate it didn't work
								//break;
							case BSP.TYPE_NIGHTFIRE:
								BSP42Decompiler decompiler42 = new BSP42Decompiler(reader.getBSPObject(), jobnum);
								decompiler42.decompile();
								break;
							case BSP.TYPE_QUAKE2:
							case BSP.TYPE_SIN:
							case BSP.TYPE_SOF:
								BSP38Decompiler decompiler38 = new BSP38Decompiler(reader.getBSPObject(), jobnum);
								decompiler38.decompile();
								break;
							case BSP.TYPE_SOURCE17:
							case BSP.TYPE_SOURCE18:
							case BSP.TYPE_SOURCE19:
							case BSP.TYPE_SOURCE20:
							case BSP.TYPE_SOURCE21:
							case BSP.TYPE_SOURCE22:
							case BSP.TYPE_SOURCE23:
								SourceBSPDecompiler sourceDecompiler = new SourceBSPDecompiler(reader.getBSPObject(), jobnum);
								sourceDecompiler.decompile();
								break;
							case BSP.TYPE_QUAKE3:
							case BSP.TYPE_RAVEN:
							case BSP.TYPE_COD:
							case BSP.TYPE_COD2:
							case BSP.TYPE_COD4:
							case BSP.TYPE_STEF2:
							case BSP.TYPE_STEF2DEMO:
							case BSP.TYPE_MOHAA:
							case BSP.TYPE_FAKK:
								BSP46Decompiler decompiler46 = new BSP46Decompiler(reader.getBSPObject(), jobnum);
								decompiler46.decompile();
								break;
							default:
								Window.println("wtf", Window.VERBOSITY_ALWAYS);
						}
					}
				}
				Window.setProgress(jobnum, 1, 1, "Done!");
				Window.setProgressColor(jobnum, new Color(64, 192, 64));
			} catch(java.lang.InterruptedException e) {
				Window.print("Job "+(jobnum+1)+" aborted by user.",Window.VERBOSITY_ALWAYS);
				Window.print(" When: "+e.toString().substring(32), Window.VERBOSITY_WARNINGS);
				Window.println();
				Window.setProgress(jobnum, 1, 1, "Aborted!");
				Window.setProgressColor(jobnum, new Color(255, 128, 128));
				Thread.currentThread().interrupt();
			} catch (java.lang.Exception e) {
				if(openAs!=-1) {
					Window.println(""+(char)0x0D+(char)0x0A+"Exception caught in job "+(jobnum+1)+": "+e+(char)0x0D+(char)0x0A+"Are you using \"Open as...\" with the wrong game?"+(char)0x0D+(char)0x0A+"If not, please let me know on the issue tracker!"+(char)0x0D+(char)0x0A+"http://code.google.com/p/jbn-bsp-lump-tools/issues/entry",Window.VERBOSITY_ALWAYS);
				} else {
					Window.println(""+(char)0x0D+(char)0x0A+"Exception caught in job "+(jobnum+1)+": "+e+(char)0x0D+(char)0x0A+"Please let me know on the issue tracker!"+(char)0x0D+(char)0x0A+"http://code.google.com/p/jbn-bsp-lump-tools/issues/entry",Window.VERBOSITY_ALWAYS);
				}
				String stackTrace="";
				StackTraceElement[] trace=e.getStackTrace();
				for(int i=0;i<trace.length;i++) {
					stackTrace+=trace.toString()+Window.LF;
				}
				Window.println(e.getMessage()+Window.LF+stackTrace,Window.VERBOSITY_WARNINGS);
				Window.setProgress(jobnum, 1, 1, "ERROR! See log!");
				Window.setProgressColor(jobnum, new Color(255, 128, 128));
			} catch(java.lang.OutOfMemoryError e) {
				if(openAs!=-1) {
					Window.println("VM ran out of memory on job "+(jobnum+1)+". Are you using \"Open as...\" with the wrong game?"+(char)0x0D+(char)0x0A+"If not, please let me know on the issue tracker!"+(char)0x0D+(char)0x0A+"http://code.google.com/p/jbn-bsp-lump-tools/issues/entry",Window.VERBOSITY_ALWAYS);
				} else {
					Window.println("VM ran out of memory on job "+(jobnum+1)+"."+(char)0x0D+(char)0x0A+"Please let me know on the issue tracker!"+(char)0x0D+(char)0x0A+"http://code.google.com/p/jbn-bsp-lump-tools/issues/entry",Window.VERBOSITY_ALWAYS);
				}
				Window.setProgress(jobnum, 1, 1, "ERROR! See log!");
				Window.setProgressColor(jobnum, new Color(255, 128, 128));
			}
		} else {
			Window.print("Job "+(jobnum+1)+" aborted by user.",Window.VERBOSITY_ALWAYS);
			Window.print(" When: While initializing job.", Window.VERBOSITY_WARNINGS);
			Window.println();
			Window.setProgress(jobnum, 1, 1, "Aborted!");
			Window.setProgressColor(jobnum, new Color(255, 128, 128));
			Thread.currentThread().interrupt();
		}
		Window.setAbortButtonEnabled(jobnum, false);
		if(!Thread.currentThread().interrupted()) { // If this thread was interrupted, it was intended to be canceled and shouldn't start the next
			DecompilerDriver.window.startNextJob(true, threadnum);
		}
	}
}
