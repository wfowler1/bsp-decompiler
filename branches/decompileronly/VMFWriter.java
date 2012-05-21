// VMFWriter class
//
// Writes a Hammer .VMF file from a passed Entities object

import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;

public class VMFWriter {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private String path;
	private Entities data;
	private File mapFile;
	private boolean hammerDecs;
	
	int nextID=1;
	
	private static DecimalFormat fmt = new DecimalFormat("0.##########");
	
	// CONSTRUCTORS
	
	public VMFWriter(Entities from, String to, boolean hammerDecs) {
		this.data=from;
		this.path=to;
		this.hammerDecs=hammerDecs;
		this.mapFile=new File(path);
	}
	
	// METHODS
	
	// write()
	// Saves the lump to the specified path.
	// Handling file I/O with Strings is generally a bad idea. If you have maybe a couple hundred
	// Strings to write then it'll probably be okay, but when you have on the order of 10,000 Strings
	// it gets VERY slow, even if you concatenate them all before writing.
	public void write() throws java.io.IOException {
		if(!path.substring(path.length()-4).equalsIgnoreCase(".vmf")) {
			mapFile=new File(path+".vmf");
		}
		try {
			File absolutepath=new File(mapFile.getParent()+"\\");
			if(!absolutepath.exists()) {
				absolutepath.mkdir();
			}
			if(!mapFile.exists()) {
				mapFile.createNewFile();
			} else {
				mapFile.delete();
				mapFile.createNewFile();
			}
			
			byte[] temp;
			
			FileOutputStream mapWriter=new FileOutputStream(mapFile);
			String tempString="versioninfo"+(char)0x0D+(char)0x0A+"{"+(char)0x0D+(char)0x0A+"	\"editorversion\" \"400\""+(char)0x0D+(char)0x0A+"	\"editorbuild\" \"3325\""+(char)0x0D+(char)0x0A+"	\"mapversion\" \"0\""+(char)0x0D+(char)0x0A+"	\"formatversion\" \"100\""+(char)0x0D+(char)0x0A+"	\"prefab\" \"0\""+(char)0x0D+(char)0x0A+"}"+(char)0x0D+(char)0x0A+"";
			tempString+="visgroups"+(char)0x0D+(char)0x0A+"{"+(char)0x0D+(char)0x0A+"}"+(char)0x0D+(char)0x0A+"";
			tempString+="viewsettings"+(char)0x0D+(char)0x0A+"{"+(char)0x0D+(char)0x0A+"	\"bSnapToGrid\" \"1\""+(char)0x0D+(char)0x0A+"	\"bShowGrid\" \"1\""+(char)0x0D+(char)0x0A+"	\"bShowLogicalGrid\" \"0\""+(char)0x0D+(char)0x0A+"	\"nGridSpacing\" \"64\""+(char)0x0D+(char)0x0A+"	\"bShow3DGrid\" \"0\""+(char)0x0D+(char)0x0A+"}"+(char)0x0D+(char)0x0A+"";
			temp=tempString.getBytes();
			
			mapWriter.write(temp);
			
			byte[][] entityBytes=new byte[data.getNumElements()][];
			int totalLength=0;
			for(int i=0;i<data.getNumElements();i++) {
				entityBytes[i]=entityToByteArray(data.getEntity(i));
				totalLength+=entityBytes[i].length;
			}
			byte[] allEnts=new byte[totalLength];
			int offset=0;
			for(int i=0;i<data.getNumElements();i++) {
				for(int j=0;j<entityBytes[i].length;j++) {
					allEnts[offset+j]=entityBytes[i][j];
				}
				offset+=entityBytes[i].length;
			}
			mapWriter.write(allEnts);
			mapWriter.close();
		} catch(java.io.IOException e) {
			Window.window.println("ERROR: Could not save "+mapFile.getPath()+", ensure the file is not open in another program and the path "+path+" exists");
			throw e;
		}
	}
	
	// -entityToByteArray()
	// Converts the entity and its brushes into byte arrays rather than Strings,
	// which can then be written to a file much faster. Concatenating Strings is
	// a costly operation, especially when hundreds of thousands of Strings are
	// in play. This is one of two parts to writing a file quickly. The second
	// part is to call the FileOutputStream.write() method only once, with a
	// gigantic array, rather than several times with many small arrays. File I/O
	// from a hard drive is another costly operation, best done by handling
	// massive amounts of data in one go, rather than tiny amounts of data thousands
	// of times.
	private byte[] entityToByteArray(Entity in) {
		in.setAttribute("id", new Integer(nextID++).toString());
		byte[] out;
		double[] origin=new double[3];
		if(in.getBrushes().length>0) {
			origin=in.getOrigin();
		}
		int len=0;
		// Get the lengths of all attributes together
		for(int i=0;i<in.getAttributes().length;i++) {
			if(in.getAttributes()[i].equals("{")) {
				len+=10; // "world"+(char)0x0D+(char)0x0A+"{"+(char)0x0D+(char)0x0A;
				if(!in.getAttribute("classname").equalsIgnoreCase("worldspawn")) {
					len+=1; // "entity"+(char)0x0D+(char)0x0A+"{"+(char)0x0D+(char)0x0A; instead of world
				}
			} else {
				if(in.getAttributes()[i].equals("}")) {
					len+=3;
				} else {
					len+=in.getAttributes()[i].length()+3; // Three for a tab and a newline
				}
			}
		}
		out=new byte[len];
		int offset=0;
		for(int i=0;i<in.getAttributes().length;i++) { // For each attribute
			if(in.getAttributes()[i].equals("{")) {
				if(in.getAttribute("classname").equalsIgnoreCase("worldspawn")) {
					in.getAttributes()[i]="world"+(char)0x0D+(char)0x0A+"{";
				} else {
					in.getAttributes()[i]="entity"+(char)0x0D+(char)0x0A+"{"; // instead of world
				}
			} else {
				if(in.getAttributes()[i].equals("}")) {
					int brushArraySize=0;
					byte[][] brushes=new byte[in.getBrushes().length][];
					for(int j=0;j<in.getBrushes().length;j++) { // For each brush in the entity
						in.getBrush(j).shift(new Vector3D(origin));
						brushes[j]=brushToByteArray(in.getBrush(j));
						brushArraySize+=brushes[j].length;
					}
					int brushoffset=0;
					byte[] brushArray=new byte[brushArraySize];
					for(int j=0;j<in.getBrushes().length;j++) { // For each brush in the entity
						for(int k=0;k<brushes[j].length;k++) {
							brushArray[brushoffset+k]=brushes[j][k];
						}
						brushoffset+=brushes[j].length;
					}
					if(brushArray.length!=0) {
						len+=brushArray.length;
						byte[] newOut=new byte[len];
						for(int j=0;j<out.length;j++) {
							newOut[j]=out[j];
						}
						for(int j=0;j<brushArray.length;j++) {
							newOut[j+out.length-3]=brushArray[j];
						}
						offset+=brushArray.length;
						out=newOut;
					}
				} else { // If the attribute is neither { nor }
					out[offset++]=0x09; // Tabulation
				}
			}
			for(int j=0;j<in.getAttributes()[i].length();j++) { // Then for each byte in the attribute
				out[j+offset]=(byte)in.getAttributes()[i].charAt(j); // add it to the output array
			}
			out[offset+in.getAttributes()[i].length()]=(byte)0x0D;
			offset+=in.getAttributes()[i].length()+1;
			out[offset]=(byte)0x0A;
			offset++;
		}
		return out;
	}
	
	private byte[] brushToByteArray(MAPBrush in) {
		String brush=(char)0x09+"solid"+(char)0x0D+(char)0x0A+(char)0x09+"{"+(char)0x0D+(char)0x0A+(char)0x09+(char)0x09+"\"id\" \""+(nextID++)+"\""+(char)0x0D+(char)0x0A;
		for(int i=0;i<in.getNumSides();i++) {
			brush+=brushSideToString(in.getSide(i));
		}
		brush+=(char)0x09+"}"+(char)0x0D+(char)0x0A;
		byte[] brushbytes=new byte[brush.length()];
		for(int i=0;i<brush.length();i++) {
			brushbytes[i]=(byte)brush.charAt(i);
		}
		return brushbytes;
	}
	
	private String brushSideToString(MAPBrushSide in) {
		try {
			Vector3D[] triangle=in.getTriangle();
			String texture=in.getTexture();
			Vector3D textureS=in.getTextureS();
			Vector3D textureT=in.getTextureT();
			double textureShiftS=in.getTextureShiftS();
			double textureShiftT=in.getTextureShiftT();
			double texScaleX=in.getTexScaleX();
			double texScaleY=in.getTexScaleY();
			float texRot=in.getTexRot();
			double lgtScale=in.getLgtScale();
			if(hammerDecs) {
				String out="		side"+(char)0x0D+(char)0x0A+"		{"+(char)0x0D+(char)0x0A;
				out+="			\"id\" \""+(nextID++)+"\""+(char)0x0D+(char)0x0A;
				out+="			\"plane\" \"("+fmt.format((double)Math.round(triangle[0].getX()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[0].getY()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[0].getZ()*1000000.0)/1000000.0)+") ";
				out+="("+fmt.format((double)Math.round(triangle[1].getX()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[1].getY()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[1].getZ()*1000000.0)/1000000.0)+") ";
				out+="("+fmt.format((double)Math.round(triangle[2].getX()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[2].getY()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(triangle[2].getZ()*1000000.0)/1000000.0)+")\""+(char)0x0D+(char)0x0A;
				out+="			\"material\" \"" + texture + "\""+(char)0x0D+(char)0x0A;
				out+="			\"uaxis\" \"["+fmt.format((double)Math.round(textureS.getX()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(textureS.getY()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(textureS.getZ()*1000000.0)/1000000.0)+" "+Math.round(textureShiftS)+"] "+fmt.format((double)Math.round(texScaleX*10000.0)/10000.0)+"\""+(char)0x0D+(char)0x0A;
				out+="			\"vaxis\" \"["+fmt.format((double)Math.round(textureT.getX()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(textureT.getY()*1000000.0)/1000000.0)+" "+fmt.format((double)Math.round(textureT.getZ()*1000000.0)/1000000.0)+" "+Math.round(textureShiftT)+"] "+fmt.format((double)Math.round(texScaleY*10000.0)/10000.0)+"\""+(char)0x0D+(char)0x0A;
				out+="			\"rotation\" \""+fmt.format((double)Math.round(texRot*10000.0)/10000.0)+"\""+(char)0x0D+(char)0x0A;
				out+="			\"lightmapscale\" \""+fmt.format((double)Math.round(lgtScale*10000.0)/10000.0)+"\""+(char)0x0D+(char)0x0A;
				out+="			\"smoothing_groups\" \"0\""+(char)0x0D+(char)0x0A+"		}"+(char)0x0D+(char)0x0A;
				return out;
			} else {
				String out="		side"+(char)0x0D+(char)0x0A+"		{"+(char)0x0D+(char)0x0A;
				out+="			\"id\" \""+(nextID++)+"\""+(char)0x0D+(char)0x0A;
				out+="			\"plane\" \"("+triangle[0].getX()+" "+triangle[0].getY()+" "+triangle[0].getZ()+") ";
				out+="("+triangle[1].getX()+" "+triangle[1].getY()+" "+triangle[1].getZ()+") ";
				out+="("+triangle[2].getX()+" "+triangle[2].getY()+" "+triangle[2].getZ()+")\""+(char)0x0D+(char)0x0A;
				out+="			\"material\" \"" + texture + "\""+(char)0x0D+(char)0x0A;
				out+="			\"uaxis\" \"["+textureS.getX()+" "+textureS.getY()+" "+textureS.getZ()+" "+textureShiftS+"] "+texScaleX+"\""+(char)0x0D+(char)0x0A;
				out+="			\"vaxis\" \"["+textureT.getX()+" "+textureT.getY()+" "+textureT.getZ()+" "+textureShiftT+"] "+texScaleY+"\""+(char)0x0D+(char)0x0A;
				out+="			\"rotation\" \""+texRot+"\""+(char)0x0D+(char)0x0A;
				out+="			\"lightmapscale\" \""+lgtScale+"\""+(char)0x0D+(char)0x0A;
				out+="			\"smoothing_groups\" \"0\""+(char)0x0D+(char)0x0A+"		}"+(char)0x0D+(char)0x0A;
				return out;
			}
		} catch(java.lang.NullPointerException e) {
			Window.window.println("Side with bad data! Not exported!");
			return null;
		}
	}
	
	// ACCESSORS/MUTATORS
	
}