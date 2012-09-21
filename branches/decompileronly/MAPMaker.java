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
				Window.println("Saving "+mapfolder.substring(0, mapfolder.length()-4)+".vmf...",Window.VERBOSITY_ALWAYS);
				VMFMaker=new VMFWriter(data, mapfolder.substring(0, mapfolder.length()-4),version);
			} else {
				Window.println("Saving "+Window.getOutputFolder()+"\\"+mapname.substring(0, mapname.length()-4)+".vmf...",Window.VERBOSITY_ALWAYS);
				VMFMaker=new VMFWriter(data, Window.getOutputFolder()+"\\"+mapname.substring(0, mapname.length()-4),version);
			}
			VMFMaker.write();
		}
		if(Window.toMOH()) {
			MOHRadiantMAPWriter MAPMaker;
			if(Window.getOutputFolder().equals("default")) {
				Window.println("Saving "+mapfolder.substring(0, mapfolder.length()-4)+"_MOH.map...",Window.VERBOSITY_ALWAYS);
				MAPMaker=new MOHRadiantMAPWriter(data, mapfolder.substring(0, mapfolder.length()-4)+"_MOH",version);
			} else {
				Window.println("Saving "+Window.getOutputFolder()+"\\"+mapname.substring(0, mapname.length()-4)+"_MOH.map...",Window.VERBOSITY_ALWAYS);
				MAPMaker=new MOHRadiantMAPWriter(data, Window.getOutputFolder()+"\\"+mapname.substring(0, mapname.length()-4)+"_MOH",version);
			}
			MAPMaker.write();
		}
		if(Window.toGCMAP()) {
			MAP510Writer MAPMaker;
			if(Window.getOutputFolder().equals("default")) {
				Window.println("Saving "+mapfolder.substring(0, mapfolder.length()-4)+".map...",Window.VERBOSITY_ALWAYS);
				MAPMaker=new MAP510Writer(data, mapfolder.substring(0, mapfolder.length()-4),version);
			} else {
				Window.println("Saving "+Window.getOutputFolder()+"\\"+mapname.substring(0, mapname.length()-4)+".map...",Window.VERBOSITY_ALWAYS);
				MAPMaker=new MAP510Writer(data, Window.getOutputFolder()+"\\"+mapname.substring(0, mapname.length()-4),version);
			}
			MAPMaker.write();
		}
		if(Window.toRadiantMAP()) {
			GTKRadiantMapWriter MAPMaker;
			if(Window.getOutputFolder().equals("default")) {
				Window.println("Saving "+mapfolder.substring(0, mapfolder.length()-4)+"_radiant.map...",Window.VERBOSITY_ALWAYS);
				MAPMaker=new GTKRadiantMapWriter(data, mapfolder.substring(0, mapfolder.length()-4)+"_radiant",version);
			} else {
				Window.println("Saving "+Window.getOutputFolder()+"\\"+mapname.substring(0, mapname.length()-4)+"_radiant.map...",Window.VERBOSITY_ALWAYS);
				MAPMaker=new GTKRadiantMapWriter(data, Window.getOutputFolder()+"\\"+mapname.substring(0, mapname.length()-4)+"_radiant",version);
			}
			MAPMaker.write();
		}
	}
	
	// ACCESSORS/MUTATORS
	
	// INTERNAL CLASSES
	
}