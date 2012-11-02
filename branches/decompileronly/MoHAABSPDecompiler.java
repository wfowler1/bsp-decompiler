// MoHAABSPDecompiler
// Decompiles a v46 BSP

import java.util.Date;

public class MoHAABSPDecompiler {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public static final int A = 0;
	public static final int B = 1;
	public static final int C = 2;
	
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;
	
	// These are lowercase so as not to conflict with A B and C
	// Light entity attributes; red, green, blue, strength (can't use i for intensity :P)
	public static final int r = 0;
	public static final int g = 1;
	public static final int b = 2;
	public static final int s = 3;
	
	private int jobnum;
	
	private Entities mapFile; // Most MAP file formats (including GearCraft) are simply a bunch of nested entities
	private int numBrshs;
	private int numSimpleCorrects=0;
	private int numAdvancedCorrects=0;
	private int numGoodBrushes=0;
	
	private MoHAABSP BSP;
	
	// CONSTRUCTORS
	
	// This constructor sets everything according to specified settings.
	public MoHAABSPDecompiler(MoHAABSP BSP, int jobnum) {
		// Set up global variables
		this.BSP=BSP;
		this.jobnum=jobnum;
	}
	
	// METHODS
	
	// +decompile()
	// Attempts to convert the BSP file back into a .MAP file.
	public void decompile() throws java.io.IOException {
		Date begin=new Date();
		// Begin by copying all the entities into another Lump00 object. This is
		// necessary because if I just modified the current entity list then it
		// could be saved back into the BSP and really mess some stuff up.
		mapFile=new Entities(BSP.getEntities());
		int numTotalItems=0;
		// I need to go through each entity and see if it's brush-based.
		// Worldspawn is brush-based as well as any entity with model *#.
		for(int i=0;i<BSP.getEntities().length();i++) { // For each entity
			Window.println("Entity "+i+": "+mapFile.getEntity(i).getAttribute("classname"),Window.VERBOSITY_ENTITIES);
			numBrshs=0; // Reset the brush count for each entity
			// getModelNumber() returns 0 for worldspawn, the *# for brush based entities, and -1 for everything else
			int currentModel=mapFile.getEntity(i).getModelNumber();
			if(currentModel>-1) { // If this is still -1 then it's strictly a point-based entity. Move on to the next one.
				double[] origin=mapFile.getEntity(i).getOrigin();
				int firstBrush=BSP.getModels().getElement(currentModel).getFirstBrush();
				int numBrushes=BSP.getModels().getElement(currentModel).getNumBrushes();
				numBrshs=0;
				for(int j=0;j<numBrushes;j++) { // For each brush
					Window.print("Brush "+(j+firstBrush),Window.VERBOSITY_BRUSHCREATION);
					decompileBrush(BSP.getBrushes().getElement(j+firstBrush), i); // Decompile the brush
					numBrshs++;
					numTotalItems++;
					Window.setProgress(jobnum, numTotalItems, BSP.getBrushes().length()+BSP.getEntities().length(), "Decompiling...");
				}
			}
			numTotalItems++;
			Window.setProgress(jobnum, numTotalItems, BSP.getBrushes().length()+BSP.getEntities().length(), "Decompiling...");
		}
		Window.setProgress(jobnum, numTotalItems, BSP.getBrushes().length()+BSP.getEntities().length(), "Saving...");
		MAPMaker.outputMaps(mapFile, BSP.getMapNameNoExtension(), BSP.getFolder(), BSP.VERSION);
		Window.println("Process completed!",Window.VERBOSITY_ALWAYS);
		if(!Window.skipFlipIsSelected()) {
			Window.println("Num simple corrected brushes: "+numSimpleCorrects,Window.VERBOSITY_MAPSTATS); 
			Window.println("Num advanced corrected brushes: "+numAdvancedCorrects,Window.VERBOSITY_MAPSTATS); 
			Window.println("Num good brushes: "+numGoodBrushes,Window.VERBOSITY_MAPSTATS); 
		}
		Date end=new Date();
		Window.println("Time taken: "+(end.getTime()-begin.getTime())+"ms"+(char)0x0D+(char)0x0A,Window.VERBOSITY_ALWAYS);
	}
	
	// -decompileBrush(Brush, int)
	// Decompiles the Brush and adds it to entitiy #currentEntity as MAPBrush classes.
	private void decompileBrush(Brush brush, int currentEntity) {
		double[] origin=mapFile.getEntity(currentEntity).getOrigin();
		int firstSide=brush.getFirstSide();
		int numSides=brush.getNumSides();
		MAPBrushSide[] brushSides=new MAPBrushSide[0];
		boolean isDetail=false;
		int brushTextureIndex=brush.getTexture();
		byte[] contents=new byte[4];
		if(brushTextureIndex>=0) {
			contents=BSP.getTextures().getElement(brushTextureIndex).getContents();
		}
		if(!Window.noDetailIsSelected() && (contents[3] & ((byte)1 << 3)) != 0) { // This is the flag according to q3 source
			isDetail=true; // it's the same as Q2 (and Source)
		}
		MAPBrush mapBrush = new MAPBrush(numBrshs, currentEntity, isDetail);
		int numRealFaces=0;
		Plane[] brushPlanes=new Plane[0];
		Window.println(": "+numSides+" sides",Window.VERBOSITY_BRUSHCREATION);
		if(!Window.noWaterIsSelected() && (contents[0] & ((byte)1 << 5)) != 0) {
			mapBrush.setWater(true);
		}
		boolean isVisBrush=false;
		for(int i=0;i<numSides;i++) { // For each side of the brush
			BrushSide currentSide=BSP.getBrushSides().getElement(firstSide+i);
			Plane currentPlane=BSP.getPlanes().getElement(currentSide.getPlane());
			Vector3D[] triangle=new Vector3D[0];
			String texture="common/nodraw";
			int currentTextureIndex=currentSide.getTexture();
			boolean masked=false;
			if(currentTextureIndex>=0) {
				String mask=BSP.getTextures().getElement(currentTextureIndex).getMask();
				if(mask.equalsIgnoreCase("ignore") || mask.length()==0) {
					texture=BSP.getTextures().getElement(currentTextureIndex).getName();
				} else {
					texture=mask.substring(0,mask.length()-4); // Because mask includes file extensions
					masked=true;
				}
			} else { // If neither face or brush side has texture info, fall all the way back to brush. I don't know if this ever happens.
				if(brushTextureIndex>=0) { // If none of them have any info, noshader
					String mask=BSP.getTextures().getElement(currentTextureIndex).getMask();
					if(mask.equalsIgnoreCase("ignore") || mask.length()==0) {
						texture=BSP.getTextures().getElement(currentTextureIndex).getName();
					} else {
						texture=mask.substring(0,mask.length()-4); // Because mask includes file extensions
						masked=true;
					}
				}
			}
			if(texture.equalsIgnoreCase("textures/common/vis")) {
				isVisBrush=true;
				break;
			}
			double[] textureS=new double[3];
			double[] textureT=new double[3];
			double texScaleS;
			double texScaleT;
			double originShiftS;
			double textureShiftS;
			double originShiftT;
			double textureShiftT;
			texScaleS=1;
			Vector3D[] textureAxes=BSP46Decompiler.textureAxisFromPlane(currentPlane);
			textureS[X]=textureAxes[0].getX();
			textureS[Y]=textureAxes[0].getY();
			textureS[Z]=textureAxes[0].getZ();
			originShiftS=(textureS[X]*origin[X])+(textureS[Y]*origin[Y])+(textureS[Z]*origin[Z]);
			textureShiftS=0-originShiftS;
			texScaleT=1;
			textureT[X]=textureAxes[1].getX();
			textureT[Y]=textureAxes[1].getY();
			textureT[Z]=textureAxes[1].getZ();
			originShiftT=(textureT[X]*origin[X])+(textureT[Y]*origin[Y])+(textureT[Z]*origin[Z]);
			textureShiftT=0-originShiftT;
			String material;
			if(masked) {
				material="wld_masked";
			} else {
				material="wld_lightmap";
			}
			float texRot=0;
			double lgtScale=16;
			double lgtRot=0;
			MAPBrushSide[] newList=new MAPBrushSide[brushSides.length+1];
			for(int j=0;j<brushSides.length;j++) {
				newList[j]=brushSides[j];
			}
			int flags;
			//if(Window.noFaceFlagsIsSelected()) {
				flags=0;
			//}
			/*if(pointsWorked) {
				newList[brushSides.length]=new MAPBrushSide(currentPlane, triangle, texture, textureS, textureShiftS, textureT, textureShiftT,
				                                            texRot, texScaleS, texScaleT, flags, material, lgtScale, lgtRot);
			} else {*/
				newList[brushSides.length]=new MAPBrushSide(currentPlane, texture, textureS, textureShiftS, textureT, textureShiftT,
				                                            texRot, texScaleS, texScaleT, flags, material, lgtScale, lgtRot);
			//}
			brushSides=newList;
			numRealFaces++;
		}
		
		for(int i=0;i<brushSides.length;i++) {
			mapBrush.add(brushSides[i]);
		}
		
		brushPlanes=new Plane[mapBrush.getNumSides()];
		for(int i=0;i<brushPlanes.length;i++) {
			brushPlanes[i]=mapBrush.getSide(i).getPlane();
		}
		
		if(!Window.skipFlipIsSelected()) {
			if(mapBrush.hasBadSide()) { // If there's a side that might be backward
				if(mapBrush.hasGoodSide()) { // If there's a side that is forward
					mapBrush=GenericMethods.SimpleCorrectPlanes(mapBrush);
					numSimpleCorrects++;
					if(Window.calcVertsIsSelected()) { // This is performed in advancedcorrect, so don't use it if that's happening
						try {
							mapBrush=GenericMethods.CalcBrushVertices(mapBrush);
						} catch(java.lang.NullPointerException e) {
							Window.println("WARNING: Brush vertex calculation failed on entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"",Window.VERBOSITY_WARNINGS);
						}
					}
				} else { // If no forward side exists
					try {
						mapBrush=GenericMethods.AdvancedCorrectPlanes(mapBrush);
						numAdvancedCorrects++;
					} catch(java.lang.ArithmeticException e) {
						Window.println("WARNING: Plane correct returned 0 triangles for entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"",Window.VERBOSITY_WARNINGS);
					}
				}
			} else {
				numGoodBrushes++;
			}
		} else {
			if(Window.calcVertsIsSelected()) { // This is performed in advancedcorrect, so don't use it if that's happening
				try {
					mapBrush=GenericMethods.CalcBrushVertices(mapBrush);
				} catch(java.lang.NullPointerException e) {
					Window.println("WARNING: Brush vertex calculation failed on entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"",Window.VERBOSITY_WARNINGS);
				}
			}
		}
		
		// This adds the brush we've been finding and creating to
		// the current entity as an attribute. The way I've coded
		// this whole program and the entities parser, this shouldn't
		// cause any issues at all.
		if(!isVisBrush) {
			if(Window.brushesToWorldIsSelected()) {
				mapBrush.setWater(false);
				mapFile.getEntity(0).addBrush(mapBrush);
			} else {
				mapFile.getEntity(currentEntity).addBrush(mapBrush);
			}
		}
	}
}
