// BSP46Decompiler
// Hi

import java.util.Date;

public class BSP46Decompiler {

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
	
	private v46BSP BSP46;
	
	// CONSTRUCTORS
	
	// This constructor sets everything according to specified settings.
	public BSP46Decompiler(v46BSP BSP46, int jobnum) {
		// Set up global variables
		this.BSP46=BSP46;
		this.jobnum=jobnum;
	}

	// METHODS

	// Attempt to turn the Quake 3 BSP into a .MAP file
	public void decompile() throws java.io.IOException {
		Date begin=new Date();
		// Begin by copying all the entities into another Entities object.
		mapFile=new Entities(BSP46.getEntities());
		int numTotalItems=0;
		boolean[] detailBrush=new boolean[BSP46.getBrushes().getNumElements()];
		// Then make a list of detail brushes (does Quake 3 use details, and how?)
		/*for(int j=0;j<BSP46.getBrushes().getNumElements();j++) {	// For every brush
			// TODO: Figure out how to find detail brushes from a Quake 3 map, if possible
			if(BSP46.getBrushes().getBrush(j).getAttributes()[1]==0x02) { // This attribute seems to indicate no affect on vis
				detailBrush[j]=true; // Flag the brush as detail
			}
		}*/
		// Then I need to go through each entity and see if it's brush-based.
		// Worldspawn is brush-based as well as any entity with model *#.
		for(int i=0;i<BSP46.getEntities().getNumElements();i++) { // For each entity
			Window.println("Entity "+i+": "+mapFile.getEntity(i).getAttribute("classname"),4);
			numBrshs=0; // Reset the brush count for each entity
			// getModelNumber() returns 0 for worldspawn, the *# for brush based entities, and -1 for everything else
			int currentModel=mapFile.getEntity(i).getModelNumber();
			
			if(currentModel!=-1) { // If this is still -1 then it's strictly a point-based entity. Move on to the next one.
				double[] origin=mapFile.getEntity(i).getOrigin();
				int firstBrush=BSP46.getModels().getModel(currentModel).getBrush();
				int numBrushes=BSP46.getModels().getModel(currentModel).getNumBrushes();
				numBrshs=0;
				for(int j=0;j<numBrushes;j++) { // For each brush referenced
					if(detailBrush[firstBrush+j] && currentModel==0) {
						decompileBrush46(BSP46.getBrushes().getBrush(firstBrush+j), i, true); // Decompile the brush, as not detail
					} else {
						decompileBrush46(BSP46.getBrushes().getBrush(firstBrush+j), i, false); // Decompile the brush, as detail
					}
					numBrshs++;
					numTotalItems++;
					Window.setProgress(jobnum, numTotalItems, BSP46.getBrushes().getNumElements()+BSP46.getEntities().getNumElements(), "Decompiling...");
				}
			}
			numTotalItems++;
			Window.setProgress(jobnum, numTotalItems, BSP46.getBrushes().getNumElements()+BSP46.getEntities().getNumElements(), "Decompiling...");
		} 
		Window.setProgress(jobnum, numTotalItems, BSP46.getBrushes().getNumElements()+BSP46.getEntities().getNumElements(), "Saving..."); 
		if(Window.toVMF()) {
			VMFWriter VMFMaker;
			if(Window.getOutputFolder().equals("default")) {
				Window.println("Saving "+BSP46.getPath().substring(0, BSP46.getPath().length()-4)+".vmf...",0);
				VMFMaker=new VMFWriter(mapFile, BSP46.getPath().substring(0, BSP46.getPath().length()-4),46);
			} else {
				Window.println("Saving "+Window.getOutputFolder()+"\\"+BSP46.getMapName().substring(0, BSP46.getMapName().length()-4)+".vmf...",0);
				VMFMaker=new VMFWriter(mapFile, Window.getOutputFolder()+"\\"+BSP46.getMapName().substring(0, BSP46.getMapName().length()-4),46);
			}
			VMFMaker.write();
		}
		if(Window.toMOH()) {
			MOHRadiantMAPWriter MAPMaker;
			if(Window.getOutputFolder().equals("default")) {
				Window.println("Saving "+BSP46.getPath().substring(0, BSP46.getPath().length()-4)+"_MOH.map...",0);
				MAPMaker=new MOHRadiantMAPWriter(mapFile, BSP46.getPath().substring(0, BSP46.getPath().length()-4)+"_MOH",46);
			} else {
				Window.println("Saving "+Window.getOutputFolder()+"\\"+BSP46.getMapName().substring(0, BSP46.getMapName().length()-4)+"_MOH.map...",0);
				MAPMaker=new MOHRadiantMAPWriter(mapFile, Window.getOutputFolder()+"\\"+BSP46.getMapName().substring(0, BSP46.getMapName().length()-4)+"_MOH",46);
			}
			MAPMaker.write();
		}
		if(Window.toGCMAP()) {
			MAP510Writer MAPMaker;
			if(Window.getOutputFolder().equals("default")) {
				Window.println("Saving "+BSP46.getPath().substring(0, BSP46.getPath().length()-4)+".map...",0);
				MAPMaker=new MAP510Writer(mapFile, BSP46.getPath().substring(0, BSP46.getPath().length()-4),46);
			} else {
				Window.println("Saving "+Window.getOutputFolder()+"\\"+BSP46.getMapName().substring(0, BSP46.getMapName().length()-4)+".map...",0);
				MAPMaker=new MAP510Writer(mapFile, Window.getOutputFolder()+"\\"+BSP46.getMapName().substring(0, BSP46.getMapName().length()-4),46);
			}
			MAPMaker.write();
		}
		Window.println("Process completed!",0);
		Date end=new Date();
		Window.println("Time taken: "+(end.getTime()-begin.getTime())+"ms"+(char)0x0D+(char)0x0A,0);
	}
	
	// -decompileBrush46(Brush, int, boolean)
	// Decompiles the Brush and adds it to entitiy #currentEntity as .MAP data.
	private void decompileBrush46(v46Brush brush, int currentEntity, boolean isDetailBrush) {
		double[] origin=mapFile.getEntity(currentEntity).getOrigin();
		int firstSide=brush.getFirstSide();
		int numSides=brush.getNumSides();
		MAPBrushSide[] brushSides=new MAPBrushSide[numSides];
		MAPBrush mapBrush = new MAPBrush(numBrshs, currentEntity, isDetailBrush);
		for(int l=0;l<numSides;l++) { // For each side of the brush
			Vector3D[] plane=new Vector3D[3]; // Three points define a plane. All I have to do is find three points on that plane.
			v46BrushSide currentSide=BSP46.getBrushSides().getBrushSide(firstSide+l);
			Plane currentPlane=BSP46.getPlanes().getPlane(currentSide.getPlane()); // To find those three points, I can use vertices referenced by faces.
			v46Texture currentTexture=BSP46.getTextures().getTexture(currentSide.getTexture()); // To find those three points, I can use vertices referenced by faces.
			//int firstVertex=currentFace.getVert();
			//int numVertices=currentFace.getNumVerts();
			// boolean pointsWorked=false; // Need to figure out how to get faces from brush sides, then use vertices
			/*if(numVertices!=0 && !Window.planarDecompIsSelected()) { // If the face actually references a set of vertices
				plane[0]=new Vector3D(BSP42.getVertices().getVertex(firstVertex)); // Grab and store the first one
				int m=1;
				for(m=1;m<numVertices;m++) { // For each point after the first one
					plane[1]=new Vector3D(BSP42.getVertices().getVertex(firstVertex+m));
					if(!plane[0].equals(plane[1])) { // Make sure the point isn't the same as the first one
						break; // If it isn't the same, this point is good
					}
				}
				for(m=m+1;m<numVertices;m++) { // For each point after the previous one used
					plane[2]=new Vector3D(BSP42.getVertices().getVertex(firstVertex+m));
					if(!plane[2].equals(plane[0]) && !plane[2].equals(plane[1])) { // Make sure no point is equal to the third one
						if((Vector3D.crossProduct(plane[0].subtract(plane[1]), plane[0].subtract(plane[2])).getX()!=0) || // Make sure all
						   (Vector3D.crossProduct(plane[0].subtract(plane[1]), plane[0].subtract(plane[2])).getY()!=0) || // three points 
						   (Vector3D.crossProduct(plane[0].subtract(plane[1]), plane[0].subtract(plane[2])).getZ()!=0)) { // are not collinear
								pointsWorked=true;
							break;
						}
					}
				}
			}
			if(numVertices==0 || !pointsWorked) { // Fallback to planar decompilation. Since there are no explicitly defined points anymore,
				                                   // we must find them ourselves using the A, B, C and D values.*/
				plane=GenericMethods.extrapPlanePoints(currentPlane);
			//}
			String texture=BSP46.getTextures().getTexture(currentSide.getTexture()).getTexture();
			double[] textureS=new double[3];
			double[] textureT=new double[3];
			/*
			v42TexMatrix currentTexMatrix=BSP42.getTextureMatrices().getTexMatrix(currentFace.getTexStyle());
			// Get the lengths of the axis vectors
			double UAxisLength=Math.sqrt(Math.pow((double)currentTexMatrix.getUAxisX(),2)+Math.pow((double)currentTexMatrix.getUAxisY(),2)+Math.pow((double)currentTexMatrix.getUAxisZ(),2));
			double VAxisLength=Math.sqrt(Math.pow((double)currentTexMatrix.getVAxisX(),2)+Math.pow((double)currentTexMatrix.getVAxisY(),2)+Math.pow((double)currentTexMatrix.getVAxisZ(),2));
			// In compiled maps, shorter vectors=longer textures and vice versa. This will convert their lengths back to 1. We'll use the actual scale values for length.
			double texScaleS=(1/UAxisLength);// Let's use these values using the lengths of the U and V axes we found above.
			double texScaleT=(1/VAxisLength);
			textureS[0]=((double)currentTexMatrix.getUAxisX()/UAxisLength);
			textureS[1]=((double)currentTexMatrix.getUAxisY()/UAxisLength);
			textureS[2]=((double)currentTexMatrix.getUAxisZ()/UAxisLength);
			double originShiftS=(((double)currentTexMatrix.getUAxisX()/UAxisLength)*origin[X]+((double)currentTexMatrix.getUAxisY()/UAxisLength)*origin[Y]+((double)currentTexMatrix.getUAxisZ()/UAxisLength)*origin[Z])/texScaleS;
			double textureShiftS=(double)currentTexMatrix.getUShift()-originShiftS;
			textureT[0]=((double)currentTexMatrix.getVAxisX()/VAxisLength);
			textureT[1]=((double)currentTexMatrix.getVAxisY()/VAxisLength);
			textureT[2]=((double)currentTexMatrix.getVAxisZ()/VAxisLength);
			double originShiftT=(((double)currentTexMatrix.getVAxisX()/VAxisLength)*origin[X]+((double)currentTexMatrix.getVAxisY()/VAxisLength)*origin[Y]+((double)currentTexMatrix.getVAxisZ()/VAxisLength)*origin[Z])/texScaleT;
			double textureShiftT=(double)currentTexMatrix.getVShift()-originShiftT;
			float texRot=0; // In compiled maps this is calculated into the U and V axes, so set it to 0 until I can figure out a good way to determine a better value.
			int flags=currentFace.getType(); // This is actually a set of flags. Whatever.
			String material=BSP42.getMaterials().getString(currentFace.getMaterial());
			double lgtScale=16; // These values are impossible to get from a compiled map since they
			double lgtRot=0;    // are used by RAD for generating lightmaps, then are discarded, I believe.
			brushSides[l]=new MAPBrushSide(plane, texture, textureS, textureShiftS, textureT, textureShiftT,
			                               texRot, texScaleS, texScaleT, flags, material, lgtScale, lgtRot);
			numRealFaces++;
			if(brushSides[l]!=null) {
				if(pointsWorked) {
					mapBrush.add(brushSides[l], plane, currentPlane, true); // Add the MAPBrushSide to the current brush
				} else {
					mapBrush.add(brushSides[l], new Vector3D[0], currentPlane, false);
				}
			}*/
		}
	
		if(!Window.skipFlipIsSelected()) {
			if(mapBrush.hasBadSide()) { // If there's a side that might be backward
				if(mapBrush.hasGoodSide()) { // If there's a side that is forward
					mapBrush=GenericMethods.SimpleCorrectPlanes(mapBrush);
					if(Window.calcVertsIsSelected()) { // This is performed in advancedcorrect, so don't use it if that's happening
						mapBrush=GenericMethods.CalcBrushVertices(mapBrush);
					}
				} else { // If no forward side exists
					try {
						mapBrush=GenericMethods.AdvancedCorrectPlanes(mapBrush);
					} catch(java.lang.ArithmeticException e) {
						Window.println("WARNING: Plane correct returned 0 triangles for entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"",2);
					}
				}
			}
		} else {
			if(Window.calcVertsIsSelected()) { // This is performed in advancedcorrect, so don't use it if that's happening
				mapBrush=GenericMethods.CalcBrushVertices(mapBrush);
			}
		}
		
		// This adds the brush we've been finding and creating to
		// the current entity as an attribute. The way I've coded
		// this whole program and the entities parser, this shouldn't
		// cause any issues at all.
		if(Window.brushesToWorldIsSelected()) {
			mapFile.getEntity(0).addBrush(mapBrush);
		} else {
			mapFile.getEntity(currentEntity).addBrush(mapBrush);
		}
	}
	
	public void addOriginBrush(int ent) {
		double[] origin=new double[3];
		MAPBrush newOriginBrush=new MAPBrush(numBrshs++, ent, false);
		Vector3D[][] planes=new Vector3D[6][3]; // Six planes for a cube brush, three vertices for each plane
		double[][] textureS=new double[6][3];
		double[][] textureT=new double[6][3];
		// The planes and their texture scales
		// I got these from an origin brush created by Gearcraft. Don't worry where these numbers came from, they work.
		// Top
		planes[0][0]=new Vector3D(-16+origin[0], 16+origin[1], 16+origin[2]);
		planes[0][1]=new Vector3D(16+origin[0], 16+origin[1], 16+origin[2]);
		planes[0][2]=new Vector3D(16+origin[0], -16+origin[1], 16+origin[2]);
		textureS[0][0]=1;
		textureT[0][1]=-1;
		// Bottom
		planes[1][0]=new Vector3D(-16+origin[0], -16+origin[1], -16+origin[2]);
		planes[1][1]=new Vector3D(16+origin[0], -16+origin[1], -16+origin[2]);
		planes[1][2]=new Vector3D(16+origin[0], 16+origin[1], -16+origin[2]);
		textureS[1][0]=1;
		textureT[1][1]=-1;
		// Left
		planes[2][0]=new Vector3D(-16+origin[0], 16+origin[1], 16+origin[2]);
		planes[2][1]=new Vector3D(-16+origin[0], -16+origin[1], 16+origin[2]);
		planes[2][2]=new Vector3D(-16+origin[0], -16+origin[1], -16+origin[2]);
		textureS[2][1]=1;
		textureT[2][2]=-1;
		// Right
		planes[3][0]=new Vector3D(16+origin[0], 16+origin[1], -16+origin[2]);
		planes[3][1]=new Vector3D(16+origin[0], -16+origin[1], -16+origin[2]);
		planes[3][2]=new Vector3D(16+origin[0], -16+origin[1], 16+origin[2]);
		textureS[3][1]=1;
		textureT[3][2]=-1;
		// Near
		planes[4][0]=new Vector3D(16+origin[0], 16+origin[1], 16+origin[2]);
		planes[4][1]=new Vector3D(-16+origin[0], 16+origin[1], 16+origin[2]);
		planes[4][2]=new Vector3D(-16+origin[0], 16+origin[1], -16+origin[2]);
		textureS[4][0]=1;
		textureT[4][2]=-1;
		// Far
		planes[5][0]=new Vector3D(16+origin[0], -16+origin[1], -16+origin[2]);
		planes[5][1]=new Vector3D(-16+origin[0], -16+origin[1], -16+origin[2]);
		planes[5][2]=new Vector3D(-16+origin[0], -16+origin[1], 16+origin[2]);
		textureS[5][0]=1;
		textureT[5][2]=-1;
		
		for(int j=0;j<6;j++) {
			MAPBrushSide currentEdge=new MAPBrushSide(planes[j], "common/origin", textureS[j], 0, textureT[j], 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			newOriginBrush.add(currentEdge);
		}
		mapFile.getEntity(ent).addBrush(newOriginBrush);
	}
}
