// EF2Decompiler
// Decompiles a v46 BSP

import java.util.Date;

public class EF2Decompiler {

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
	
	private EF2BSP BSP;
	
	// CONSTRUCTORS
	
	// This constructor sets everything according to specified settings.
	public EF2Decompiler(EF2BSP BSP, int jobnum) {
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
			if(currentModel!=-1) { // If this is still -1 then it's strictly a point-based entity. Move on to the next one.
				double[] origin=mapFile.getEntity(i).getOrigin();
				int firstBrush=BSP.getModels().getModel(currentModel).getBrush();
				int numBrushes=BSP.getModels().getModel(currentModel).getNumBrushes();
				numBrshs=0;
				for(int j=0;j<numBrushes;j++) { // For each brush
					Window.print("Brush "+(j+firstBrush),Window.VERBOSITY_BRUSHCREATION);
					decompileBrush(BSP.getBrushes().getBrush(j+firstBrush), i); // Decompile the brush
					numBrshs++;
					numTotalItems++;
					Window.setProgress(jobnum, numTotalItems, BSP.getBrushes().getNumElements()+BSP.getEntities().length(), "Decompiling...");
				}
			}
			numTotalItems++;
			Window.setProgress(jobnum, numTotalItems, BSP.getBrushes().getNumElements()+BSP.getEntities().length(), "Decompiling...");
		}
		Window.setProgress(jobnum, numTotalItems, BSP.getBrushes().getNumElements()+BSP.getEntities().length(), "Saving...");
		MAPMaker.outputMaps(mapFile, BSP.getMapName(), BSP.getPath(), BSP.VERSION);
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
	private void decompileBrush(v46Brush brush, int currentEntity) {
		double[] origin=mapFile.getEntity(currentEntity).getOrigin();
		int firstSide;
		int numSides;
		if(BSP.isDemo()) {
			firstSide=brush.getFirstSide();
			numSides=brush.getNumSides();
		} else { // The retail version of EF2 is switched for no apparent reason
			firstSide=brush.getNumSides();
			numSides=brush.getFirstSide();
		}
		MAPBrushSide[] brushSides=new MAPBrushSide[0];
		boolean isDetail=false;
		int brushTextureIndex=brush.getTexture();
		byte[] contents=new byte[4];
		if(brushTextureIndex>=0) {
			contents=BSP.getFTextures().getElement(brushTextureIndex).getContents();
		}
		if(!Window.noDetailIsSelected() && (contents[3] & ((byte)1 << 3)) != 0) { // This is the flag according to q3 source
			isDetail=true; // it's the same as Q2 (and Source), but I haven't found any Q3 maps that use it, so far
		}
		MAPBrush mapBrush = new MAPBrush(numBrshs, currentEntity, isDetail);
		int numRealFaces=0;
		Plane[] brushPlanes=new Plane[0];
		Window.println(": "+numSides+" sides",Window.VERBOSITY_BRUSHCREATION);
		if(!Window.noWaterIsSelected() && (contents[0] & ((byte)1 << 5)) != 0) {
			mapBrush.setWater(true);
		}
		for(int i=0;i<numSides;i++) { // For each side of the brush
			CoDBrushSide currentSide=BSP.getBrushSides().getElement(firstSide+i);
			Plane currentPlane;
			if(BSP.isDemo()) {
				currentPlane=BSP.getPlanes().getPlane(currentSide.getPlane());
			} else {
				currentPlane=BSP.getPlanes().getPlane(currentSide.getTexture());
			}
			Vector3D[] triangle=new Vector3D[0];
			String texture="noshader";
			int currentTextureIndex;
			if(BSP.isDemo()) {
				currentTextureIndex=currentSide.getTexture();
			} else {
				currentTextureIndex=currentSide.getPlane();
			}
			if(currentTextureIndex>=0) {
				texture=BSP.getFTextures().getElement(currentTextureIndex).getTexture();
			} else { // If neither face or brush side has texture info, fall all the way back to brush. I don't know if this ever happens.
				if(brushTextureIndex>=0) { // If none of them have any info, noshader
					texture=BSP.getFTextures().getElement(brushTextureIndex).getTexture();
				}
			}
			// Get the lengths of the axis vectors.
			// TODO figure out this bullshit
			double UAxisLength=1;
			double VAxisLength=1;
			// In compiled maps, shorter vectors=longer textures and vice versa. This will convert their lengths back to 1. We'll use the actual scale values for length.
			double texScaleS=1;
			double texScaleT=1;
			Vector3D[] textureAxes=BSP46Decompiler.textureAxisFromPlane(currentPlane);
			double originShiftS=0;
			double textureShiftS=0;
			double originShiftT=0;
			double textureShiftT=0;
			textureShiftS=0;
			textureShiftT=0;
			float texRot=0;
			String material="wld_lightmap";
			double lgtScale=16;
			double lgtRot=0;
			MAPBrushSide[] newList=new MAPBrushSide[brushSides.length+1];
			for(int j=0;j<brushSides.length;j++) {
				newList[j]=brushSides[j];
			}
			//if(Window.noFaceFlagsIsSelected()) {
				int flags=0;
			//}
			/*if(pointsWorked) {
				newList[brushSides.length]=new MAPBrushSide(currentPlane, triangle, texture, textureS, textureShiftS, textureT, textureShiftT,
				                                            texRot, texScaleS, texScaleT, flags, material, lgtScale, lgtRot);
			} else {*/
				newList[brushSides.length]=new MAPBrushSide(currentPlane, texture, textureAxes[0].getPoint(), textureShiftS, textureAxes[1].getPoint(), textureShiftT,
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
		if(Window.brushesToWorldIsSelected()) {
			mapBrush.setWater(false);
			mapFile.getEntity(0).addBrush(mapBrush);
		} else {
			mapFile.getEntity(currentEntity).addBrush(mapBrush);
		}
	}
}