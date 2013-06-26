// MAPMaker class
// Takes Entities classes and uses map writer classes to output editor mapfiles.

import java.io.File;
import java.io.FileOutputStream;

public class MAPMaker {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// CONSTRUCTORS
	
	// METHODS
	public static void outputMaps(Entities data, String mapname, String mapfolder, int version) throws java.io.IOException, java.lang.InterruptedException {
		if(Window.toAuto()) { // If "auto" is selected, output to one format appropriate for the source game
			switch(version) {
				// Gearcraft
				case BSP.TYPE_NIGHTFIRE:
					MAP510Writer GCMAPMaker;
					if(Window.getOutputFolder().equals("default")) {
						Window.println("Saving "+mapfolder+mapname+".map...",Window.VERBOSITY_ALWAYS);
						GCMAPMaker=new MAP510Writer(data, mapfolder+mapname,version);
					} else {
						Window.println("Saving "+Window.getOutputFolder()+"\\"+mapname+".map...",Window.VERBOSITY_ALWAYS);
						GCMAPMaker=new MAP510Writer(data, Window.getOutputFolder()+"\\"+mapname,version);
					}
					GCMAPMaker.write();
					break;
				// MOHRadiant
				case BSP.TYPE_MOHAA:
					MOHRadiantMAPWriter MOHMAPMaker;
					if(Window.getOutputFolder().equals("default")) {
						Window.println("Saving "+mapfolder+mapname+".map...",Window.VERBOSITY_ALWAYS);
						MOHMAPMaker=new MOHRadiantMAPWriter(data, mapfolder+mapname,version);
					} else {
						Window.println("Saving "+Window.getOutputFolder()+"\\"+mapname+".map...",Window.VERBOSITY_ALWAYS);
						MOHMAPMaker=new MOHRadiantMAPWriter(data, Window.getOutputFolder()+"\\"+mapname,version);
					}
					MOHMAPMaker.write();
					break;
				// GTK Radiant
				case BSP.TYPE_QUAKE:
				case BSP.TYPE_STEF2:
				case BSP.TYPE_STEF2DEMO:
				case BSP.TYPE_SIN:
				case BSP.TYPE_SOF:
				case BSP.TYPE_RAVEN:
				case BSP.TYPE_QUAKE2:
				case BSP.TYPE_DAIKATANA:
				case BSP.TYPE_QUAKE3:
				case BSP.TYPE_COD:
				case BSP.TYPE_FAKK:
					GTKRadiantMapWriter RadMAPMaker;
					if(Window.getOutputFolder().equals("default")) {
						Window.println("Saving "+mapfolder+mapname+".map...",Window.VERBOSITY_ALWAYS);
						RadMAPMaker=new GTKRadiantMapWriter(data, mapfolder+mapname,version);
					} else {
						Window.println("Saving "+Window.getOutputFolder()+"\\"+mapname+".map...",Window.VERBOSITY_ALWAYS);
						RadMAPMaker=new GTKRadiantMapWriter(data, Window.getOutputFolder()+"\\"+mapname,version);
					}
					RadMAPMaker.write();
					break;
				// Hammer VMF
				case BSP.TYPE_SOURCE17:
				case BSP.TYPE_SOURCE18:
				case BSP.TYPE_SOURCE19:
				case BSP.TYPE_SOURCE20:
				case BSP.TYPE_SOURCE21:
				case BSP.TYPE_SOURCE22:
				case BSP.TYPE_SOURCE23:
				case BSP.TYPE_VINDICTUS:
				case BSP.TYPE_DMOMAM:
				case BSP.TYPE_TACTICALINTERVENTION:
					VMFWriter VMFMaker;
					if(Window.getOutputFolder().equals("default")) {
						Window.println("Saving "+mapfolder+mapname+".vmf...",Window.VERBOSITY_ALWAYS);
						VMFMaker=new VMFWriter(data, mapfolder+mapname,version);
					} else {
						Window.println("Saving "+Window.getOutputFolder()+"\\"+mapname+".vmf...",Window.VERBOSITY_ALWAYS);
						VMFMaker=new VMFWriter(data, Window.getOutputFolder()+"\\"+mapname,version);
					}
					VMFMaker.write();
					break;
				case DoomMap.TYPE_DOOM: // DoomEdit seems somehow appropriate.
					DoomEditMapWriter DOOMMAPMaker;
					if(Window.getOutputFolder().equals("default")) {
						Window.println("Saving "+mapfolder+mapname+".map...",Window.VERBOSITY_ALWAYS);
						DOOMMAPMaker=new DoomEditMapWriter(data, mapfolder+mapname,version);
					} else {
						Window.println("Saving "+Window.getOutputFolder()+"\\"+mapname+".vmf...",Window.VERBOSITY_ALWAYS);
						DOOMMAPMaker=new DoomEditMapWriter(data, Window.getOutputFolder()+"\\"+mapname,version);
					}
					VMFMaker.write();
					break;
				default:
					Window.println("WARNING: No default format specified for BSP version "+version+", defaulting to GearCraft.", Window.VERBOSITY_WARNINGS);
					MAP510Writer GCMAPMaker2;
					if(Window.getOutputFolder().equals("default")) {
						Window.println("Saving "+mapfolder+mapname+".map...",Window.VERBOSITY_ALWAYS);
						GCMAPMaker2=new MAP510Writer(data, mapfolder+mapname,version);
					} else {
						Window.println("Saving "+Window.getOutputFolder()+"\\"+mapname+".map...",Window.VERBOSITY_ALWAYS);
						GCMAPMaker2=new MAP510Writer(data, Window.getOutputFolder()+"\\"+mapname,version);
					}
					GCMAPMaker2.write();
					break;
			}
		} else {
			Entities from;
			if(Window.toDoomEdit()) {
				DoomEditMapWriter mapMaker;
				if(Window.toVMF() || Window.toMOH() || Window.toGCMAP() || Window.toRadiantMAP()) {
					from=new Entities(data);
				} else {
					from=data;
				}
				if(Window.getOutputFolder().equals("default")) {
					Window.println("Saving "+mapfolder+mapname+"_doomEdit.map...",Window.VERBOSITY_ALWAYS);
					mapMaker=new DoomEditMapWriter(from, mapfolder+mapname+"_doomEdit",version);
				} else {
					Window.println("Saving "+Window.getOutputFolder()+"\\"+mapname+"_doomEdit.map...",Window.VERBOSITY_ALWAYS);
					mapMaker=new DoomEditMapWriter(from, Window.getOutputFolder()+"\\"+mapname+"_doomEdit",version);
				}
				mapMaker.write();
			}
			if(Window.toVMF()) {
				VMFWriter VMFMaker;
				if(Window.toMOH() || Window.toGCMAP() || Window.toRadiantMAP()) {
					from=new Entities(data);
				} else {
					from=data;
				}
				if(Window.getOutputFolder().equals("default")) {
					Window.println("Saving "+mapfolder+mapname+".vmf...",Window.VERBOSITY_ALWAYS);
					VMFMaker=new VMFWriter(from, mapfolder+mapname,version);
				} else {
					Window.println("Saving "+Window.getOutputFolder()+"\\"+mapname+".vmf...",Window.VERBOSITY_ALWAYS);
					VMFMaker=new VMFWriter(from, Window.getOutputFolder()+"\\"+mapname,version);
				}
				VMFMaker.write();
			}
			if(Window.toMOH()) {
				MOHRadiantMAPWriter mapMaker;
				if(Window.toGCMAP() || Window.toRadiantMAP()) {
					from=new Entities(data);
				} else {
					from=data;
				}
				if(Window.getOutputFolder().equals("default")) {
					Window.println("Saving "+mapfolder+mapname+"_MOH.map...",Window.VERBOSITY_ALWAYS);
					mapMaker=new MOHRadiantMAPWriter(from, mapfolder+mapname+"_MOH",version);
				} else {
					Window.println("Saving "+Window.getOutputFolder()+"\\"+mapname+"_MOH.map...",Window.VERBOSITY_ALWAYS);
					mapMaker=new MOHRadiantMAPWriter(from, Window.getOutputFolder()+"\\"+mapname+"_MOH",version);
				}
				mapMaker.write();
			}
			if(Window.toGCMAP()) {
				MAP510Writer mapMaker;
				if(Window.toRadiantMAP()) {
					from=new Entities(data);
				} else {
					from=data;
				}
				if(Window.getOutputFolder().equals("default")) {
					Window.println("Saving "+mapfolder+mapname+"_gc.map...",Window.VERBOSITY_ALWAYS);
					mapMaker=new MAP510Writer(from, mapfolder+mapname+"_gc",version);
				} else {
					Window.println("Saving "+Window.getOutputFolder()+"\\"+mapname+"_gc.map...",Window.VERBOSITY_ALWAYS);
					mapMaker=new MAP510Writer(from, Window.getOutputFolder()+"\\"+mapname+"_gc",version);
				}
				mapMaker.write();
			}
			if(Window.toRadiantMAP()) {
				GTKRadiantMapWriter mapMaker;
				from=data;
				if(Window.getOutputFolder().equals("default")) {
					Window.println("Saving "+mapfolder+mapname+"_radiant.map...",Window.VERBOSITY_ALWAYS);
					mapMaker=new GTKRadiantMapWriter(data, mapfolder+mapname+"_radiant",version);
				} else {
					Window.println("Saving "+Window.getOutputFolder()+"\\"+mapname+"_radiant.map...",Window.VERBOSITY_ALWAYS);
					mapMaker=new GTKRadiantMapWriter(data, Window.getOutputFolder()+"\\"+mapname+"_radiant",version);
				}
				mapMaker.write();
			}
		}
	}
	
	// If only one thread is allowed to use this method at once, only one map will be saved at once, meaning less
	// jumping hard drive seek time used.
	public static synchronized void write(byte[] data, String destinationString, boolean toVMF) throws java.io.IOException {
		try{
			if(!destinationString.substring(destinationString.length()-4).equalsIgnoreCase(".map") && !destinationString.substring(destinationString.length()-4).equalsIgnoreCase(".vmf")) {
				if(toVMF) {
					destinationString=destinationString+".vmf";
				} else {
					destinationString=destinationString+".map";
				}
			}
		} catch(java.lang.StringIndexOutOfBoundsException e) {
			if(toVMF) {
				destinationString=destinationString+".vmf";
			} else {
				destinationString=destinationString+".map";
			}
		}
		File destination=new File(destinationString);
		try {
			File absolutepath=new File(destination.getParent()+"\\");
			if(!absolutepath.exists()) {
				absolutepath.mkdir();
			}
			if(!destination.exists()) {
				destination.createNewFile();
			} else {
				destination.delete();
				destination.createNewFile();
			}
			FileOutputStream mapWriter = new FileOutputStream(destination);
			mapWriter.write(data);
			mapWriter.close();
		} catch(java.io.IOException e) {
			Window.println("ERROR: Could not save "+destination.getPath()+", ensure the file is not open in another program.",Window.VERBOSITY_ALWAYS);
			throw e;
		}
	}
	
	// ACCESSORS/MUTATORS
	
	// INTERNAL CLASSES
	
}
