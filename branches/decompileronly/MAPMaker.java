// MAPMaker class
// Takes Entities classes and uses map writer classes to output editor mapfiles.

public class MAPMaker {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	// CONSTRUCTORS
	
	// METHODS
	public static void outputMaps(Entities data, String mapname, String mapfolder, int version) throws java.io.IOException {
		if(Window.toVMF()) {
			VMFWriter VMFMaker;
			if(Window.getOutputFolder().equals("default")) {
				Window.println("Saving "+mapfolder+mapname+".vmf...",Window.VERBOSITY_ALWAYS);
				VMFMaker=new VMFWriter(data, mapfolder+mapname,version);
			} else {
				Window.println("Saving "+Window.getOutputFolder()+"\\"+mapname+".vmf...",Window.VERBOSITY_ALWAYS);
				VMFMaker=new VMFWriter(data, Window.getOutputFolder()+"\\"+mapname,version);
			}
			VMFMaker.write();
		}
		if(Window.toMOH()) {
			MOHRadiantMAPWriter MAPMaker;
			if(Window.getOutputFolder().equals("default")) {
				Window.println("Saving "+mapfolder+mapname+"_MOH.map...",Window.VERBOSITY_ALWAYS);
				MAPMaker=new MOHRadiantMAPWriter(data, mapfolder+mapname+"_MOH",version);
			} else {
				Window.println("Saving "+Window.getOutputFolder()+"\\"+mapname+"_MOH.map...",Window.VERBOSITY_ALWAYS);
				MAPMaker=new MOHRadiantMAPWriter(data, Window.getOutputFolder()+"\\"+mapname+"_MOH",version);
			}
			MAPMaker.write();
		}
		if(Window.toGCMAP()) {
			MAP510Writer MAPMaker;
			if(Window.getOutputFolder().equals("default")) {
				Window.println("Saving "+mapfolder+mapname+"_gc.map...",Window.VERBOSITY_ALWAYS);
				MAPMaker=new MAP510Writer(data, mapfolder+mapname+"_gc",version);
			} else {
				Window.println("Saving "+Window.getOutputFolder()+"\\"+mapname+"_gc.map...",Window.VERBOSITY_ALWAYS);
				MAPMaker=new MAP510Writer(data, Window.getOutputFolder()+"\\"+mapname+"_gc",version);
			}
			MAPMaker.write();
		}
		if(Window.toRadiantMAP()) {
			GTKRadiantMapWriter MAPMaker;
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
	
	// ACCESSORS/MUTATORS
	
	// INTERNAL CLASSES
	
}