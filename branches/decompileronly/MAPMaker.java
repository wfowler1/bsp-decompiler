// MAPMaker class
// Takes Entities classes and uses map writer classes to output editor mapfiles.

public class MAPMaker {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// CONSTRUCTORS
	
	// METHODS
	public static void outputMaps(Entities data, String mapname, String mapfolder, int version) throws java.io.IOException {
		if(Window.toAuto()) { // If "auto" is selected, output to one format appropriate for the source game
			switch(version) {
				// Gearcraft
				case BSP.TYPE_NIGHTFIRE:
				case DoomMap.VERSION: // I don't know where else to put this
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
				case BSP.TYPE_RAVEN:
				case BSP.TYPE_QUAKE2:
				case BSP.TYPE_QUAKE3:
				case BSP.TYPE_COD:
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
				MOHRadiantMAPWriter MAPMaker;
				if(Window.toGCMAP() || Window.toRadiantMAP()) {
					from=new Entities(data);
				} else {
					from=data;
				}
				if(Window.getOutputFolder().equals("default")) {
					Window.println("Saving "+mapfolder+mapname+"_MOH.map...",Window.VERBOSITY_ALWAYS);
					MAPMaker=new MOHRadiantMAPWriter(from, mapfolder+mapname+"_MOH",version);
				} else {
					Window.println("Saving "+Window.getOutputFolder()+"\\"+mapname+"_MOH.map...",Window.VERBOSITY_ALWAYS);
					MAPMaker=new MOHRadiantMAPWriter(from, Window.getOutputFolder()+"\\"+mapname+"_MOH",version);
				}
				MAPMaker.write();
			}
			if(Window.toGCMAP()) {
				MAP510Writer MAPMaker;
				if(Window.toRadiantMAP()) {
					from=new Entities(data);
				} else {
					from=data;
				}
				if(Window.getOutputFolder().equals("default")) {
					Window.println("Saving "+mapfolder+mapname+"_gc.map...",Window.VERBOSITY_ALWAYS);
					MAPMaker=new MAP510Writer(from, mapfolder+mapname+"_gc",version);
				} else {
					Window.println("Saving "+Window.getOutputFolder()+"\\"+mapname+"_gc.map...",Window.VERBOSITY_ALWAYS);
					MAPMaker=new MAP510Writer(from, Window.getOutputFolder()+"\\"+mapname+"_gc",version);
				}
				MAPMaker.write();
			}
			if(Window.toRadiantMAP()) {
				GTKRadiantMapWriter MAPMaker;
				from=data;
				if(Window.getOutputFolder().equals("default")) {
					Window.println("Saving "+mapfolder+mapname+"_radiant.map...",Window.VERBOSITY_ALWAYS);
					MAPMaker=new GTKRadiantMapWriter(data, mapfolder+mapname+"_radiant",version);
				} else {
					Window.println("Saving "+Window.getOutputFolder()+"\\"+mapname+"_radiant.map...",Window.VERBOSITY_ALWAYS);
					MAPMaker=new GTKRadiantMapWriter(data, Window.getOutputFolder()+"\\"+mapname+"_radiant",version);
				}
				MAPMaker.write();
			}
		}
	}
	
	// ACCESSORS/MUTATORS
	
	// INTERNAL CLASSES
	
}
