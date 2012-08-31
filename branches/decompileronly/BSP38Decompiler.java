// BSP38Decompiler class
// Decompile BSP v38

import java.util.Date;

public class BSP38Decompiler {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public static final int A = 0;
	public static final int B = 1;
	public static final int C = 2;
	
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;
	
	private int jobnum;
	
	private Entities mapFile; // Most MAP file formats (including GearCraft) are simply a bunch of nested entities
	private int numBrshs;
	private int numSimpleCorrects=0;
	private int numAdvancedCorrects=0;
	private int numGoodBrushes=0;
	
	private v38BSP BSP38;
	
	// CONSTRUCTORS

	// This constructor sets everything according to specified settings.
	public BSP38Decompiler(v38BSP BSP38, int jobnum) {
		// Set up global variables
		this.BSP38=BSP38;
		this.jobnum=jobnum;
	}
	
	// METHODS

	// Attempt to turn the Quake 2 BSP into a .MAP file
	public void decompile() throws java.io.IOException {
		Date begin=new Date();
		// Begin by copying all the entities into another Lump00 object. This is
		// necessary because if I just modified the current entity list then it
		// could be saved back into the BSP and really mess some stuff up.
		mapFile=new Entities(BSP38.getEntities());
		//int numAreaPortals=0;
		int numTotalItems=0;
		boolean containsAreaPortals=false;
		for(int i=0;i<BSP38.getEntities().getNumElements();i++) { // For each entity
			Window.println("Entity "+i+": "+mapFile.getEntity(i).getAttribute("classname"),4);
			// Deal with area portals.
			if(mapFile.getEntity(i).getAttribute("classname").equalsIgnoreCase("func_areaportal")) {
				mapFile.getEntity(i).deleteAttribute("style");
				containsAreaPortals=true;
			}
			// getModelNumber() returns 0 for worldspawn, the *# for brush based entities, and -1 for everything else
			int currentModel=mapFile.getEntity(i).getModelNumber();
			
			if(currentModel!=-1) { // If this is still -1 then it's strictly a point-based entity. Move on to the next one.
				double[] origin=mapFile.getEntity(i).getOrigin();
				int firstLeaf=BSP38.getModels().getModel(currentModel).getHead();
				v38Leaf[] leaves=BSP38.getLeavesInModel(currentModel);
				int numLeaves=leaves.length;
				boolean[] brushesUsed=new boolean[BSP38.getBrushes().length()]; // Keep a list of brushes already in the model, since sometimes the leaves lump references one brush several times
				numBrshs=0; // Reset the brush count for each entity
				for(int j=0;j<numLeaves;j++) { // For each leaf in the bunch
					v38Leaf currentLeaf=leaves[j];
					if(Window.visLeafBBoxesIsSelected()) {
						mapFile.getEntity(0).addBrush(GenericMethods.createBrush(currentLeaf.getMins(), currentLeaf.getMaxs(), "special/hint"));
					}
					short firstBrushIndex=currentLeaf.getFirstMarkBrush();
					short numBrushIndices=currentLeaf.getNumMarkBrushes();
					if(numBrushIndices>0) { // A lot of leaves reference no brushes. If this is one, this iteration of the j loop is finished
						for(int k=0;k<numBrushIndices;k++) { // For each brush referenced
							if(!brushesUsed[BSP38.getMarkBrushes().getShort(firstBrushIndex+k)]) { // If the current brush has NOT been used in this entity
								Window.print("Brush "+(k+numBrushIndices),4);
								brushesUsed[BSP38.getMarkBrushes().getShort(firstBrushIndex+k)]=true;
								Brush brush=BSP38.getBrushes().getBrush(BSP38.getMarkBrushes().getShort(firstBrushIndex+k));
								if(!(brush.getAttributes()[1]==-128)) {
									decompileBrush(brush, i); // Decompile the brush
								} else {
									containsAreaPortals=true;
								}
								numBrshs++;
								numTotalItems++;
								Window.setProgress(jobnum, numTotalItems, BSP38.getBrushes().length()+BSP38.getEntities().getNumElements(), "Decompiling...");
							}
						}
					}
				}
			}
			numTotalItems++; // This entity
			Window.setProgress(jobnum, numTotalItems, BSP38.getBrushes().length()+BSP38.getEntities().getNumElements(), "Decompiling...");
		}
		if(containsAreaPortals) { // If this map was found to have area portals
			int j=0;
			for(int i=0;i<BSP38.getBrushes().length();i++) { // For each brush in this map
				if(BSP38.getBrushes().getBrush(i).getAttributes()[1]==-128) { // If the brush is an area portal brush
					for(j++;j<BSP38.getEntities().getNumElements();j++) { // Find an areaportal entity
						if(BSP38.getEntities().getEntity(j).getAttribute("classname").equalsIgnoreCase("func_areaportal")) {
							decompileBrush(BSP38.getBrushes().getBrush(i), j); // Add the brush to that entity
							break; // And break out of the inner loop, but remember your place.
						}
					}
					if(j==BSP38.getEntities().getNumElements()) { // If we're out of entities, stop this whole thing.
						break;
					}
				}
			}
		}
		Window.setProgress(jobnum, numTotalItems, BSP38.getBrushes().length()+BSP38.getEntities().getNumElements(), "Saving...");
		if(Window.toVMF()) {
			VMFWriter VMFMaker;
			if(Window.getOutputFolder().equals("default")) {
				Window.println("Saving "+BSP38.getPath().substring(0, BSP38.getPath().length()-4)+".vmf...",0);
				VMFMaker=new VMFWriter(mapFile, BSP38.getPath().substring(0, BSP38.getPath().length()-4),38);
			} else {
				Window.println("Saving "+Window.getOutputFolder()+"\\"+BSP38.getMapName().substring(0, BSP38.getMapName().length()-4)+".vmf...",0);
				VMFMaker=new VMFWriter(mapFile, Window.getOutputFolder()+"\\"+BSP38.getMapName().substring(0, BSP38.getMapName().length()-4),38);
			}
			VMFMaker.write();
		}
		if(Window.toMOH()) {
			MOHRadiantMAPWriter MAPMaker;
			if(Window.getOutputFolder().equals("default")) {
				Window.println("Saving "+BSP38.getPath().substring(0, BSP38.getPath().length()-4)+"_MOH.map...",0);
				MAPMaker=new MOHRadiantMAPWriter(mapFile, BSP38.getPath().substring(0, BSP38.getPath().length()-4)+"_MOH",38);
			} else {
				Window.println("Saving "+Window.getOutputFolder()+"\\"+BSP38.getMapName().substring(0, BSP38.getMapName().length()-4)+"_MOH.map...",0);
				MAPMaker=new MOHRadiantMAPWriter(mapFile, Window.getOutputFolder()+"\\"+BSP38.getMapName().substring(0, BSP38.getMapName().length()-4)+"_MOH",38);
			}
			MAPMaker.write();
		}
		if(Window.toGCMAP()) {
			MAP510Writer MAPMaker;
			if(Window.getOutputFolder().equals("default")) {
				Window.println("Saving "+BSP38.getPath().substring(0, BSP38.getPath().length()-4)+".map...",0);
				MAPMaker=new MAP510Writer(mapFile, BSP38.getPath().substring(0, BSP38.getPath().length()-4),38);
			} else {
				Window.println("Saving "+Window.getOutputFolder()+"\\"+BSP38.getMapName().substring(0, BSP38.getMapName().length()-4)+".map...",0);
				MAPMaker=new MAP510Writer(mapFile, Window.getOutputFolder()+"\\"+BSP38.getMapName().substring(0, BSP38.getMapName().length()-4),38);
			}
			MAPMaker.write();
		}
		Window.println("Process completed!",0);
		if(!Window.skipFlipIsSelected()) {
			Window.println("Num simple corrected brushes: "+numSimpleCorrects,1); 
			Window.println("Num advanced corrected brushes: "+numAdvancedCorrects,1); 
			Window.println("Num good brushes: "+numGoodBrushes,1); 
		}
		Date end=new Date();
		Window.window.println("Time taken: "+(end.getTime()-begin.getTime())+"ms"+(char)0x0D+(char)0x0A,0);
	}

	// -decompileBrush38(Brush, int, boolean)
	// Decompiles the Brush and adds it to entitiy #currentEntity as .MAP data.
	private void decompileBrush(Brush brush, int currentEntity) {
		double[] origin=mapFile.getEntity(currentEntity).getOrigin();
		boolean isWater=false;
		int firstSide=brush.getFirstSide();
		int numSides=brush.getNumSides();
		MAPBrushSide[] brushSides=new MAPBrushSide[numSides];
		MAPBrush mapBrush = new MAPBrush(numBrshs, currentEntity, false);
		Window.println(": "+numSides+" sides",4);
		for(int l=0;l<numSides;l++) { // For each side of the brush
			Vector3D[] plane=new Vector3D[3]; // Three points define a plane. All I have to do is find three points on that plane.
			v38BrushSide currentSide=BSP38.getBrushSides().getBrushSide(firstSide+l);
			Plane currentPlane=BSP38.getPlanes().getPlane(currentSide.getPlane()).getPlane(); // To find those three points, I must extrapolate from planes until I find a way to associate faces with brushes
			v38Texture currentTexture;
			boolean isDuplicate=false;
			for(int i=l+1;i<numSides;i++) { // For each subsequent side of the brush
				if(currentPlane.equals(BSP38.getPlanes().getPlane(BSP38.getBrushSides().getBrushSide(firstSide+i).getPlane()))) {
					Window.println("WARNING: Duplicate planes in a brush, sides "+l+" and "+i,2);
					isDuplicate=true;
				}
			}
			if(!isDuplicate) {
				if(currentSide.getTexInfo()>-1) {
					currentTexture=BSP38.getTextures().getTexture(currentSide.getTexInfo());
				} else {
					currentTexture=createPerpTexture38(currentPlane); // Create a texture plane perpendicular to current plane's normal
				}
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
				}*/
				//if(numVertices==0 || !pointsWorked) { // Fallback to planar decompilation. Since there are no explicitly defined points anymore,
					                                   // we must find them ourselves using the A, B, C and D values.
					plane=GenericMethods.extrapPlanePoints(currentPlane);
				//}
				String texture=currentTexture.getTexture();
				if(brush.getAttributes()[0]==32) {
					isWater=true;
				}
				double[] textureS=new double[3];
				double[] textureT=new double[3];
				// Get the lengths of the axis vectors
				double UAxisLength=Math.sqrt(Math.pow((double)currentTexture.getU().getX(),2)+Math.pow((double)currentTexture.getU().getY(),2)+Math.pow((double)currentTexture.getU().getZ(),2));
				double VAxisLength=Math.sqrt(Math.pow((double)currentTexture.getV().getX(),2)+Math.pow((double)currentTexture.getV().getY(),2)+Math.pow((double)currentTexture.getV().getZ(),2));
				// In compiled maps, shorter vectors=longer textures and vice versa. This will convert their lengths back to 1. We'll use the actual scale values for length.
				double texScaleS=(1/UAxisLength);// Let's use these values using the lengths of the U and V axes we found above.
				double texScaleT=(1/VAxisLength);
				textureS[0]=((double)currentTexture.getU().getX()/UAxisLength);
				textureS[1]=((double)currentTexture.getU().getY()/UAxisLength);
				textureS[2]=((double)currentTexture.getU().getZ()/UAxisLength);
				double originShiftS=(((double)currentTexture.getU().getX()/UAxisLength)*origin[X]+((double)currentTexture.getU().getY()/UAxisLength)*origin[Y]+((double)currentTexture.getU().getZ()/UAxisLength)*origin[Z])/texScaleS;
				double textureShiftS=(double)currentTexture.getUShift()-originShiftS;
				textureT[0]=((double)currentTexture.getV().getX()/VAxisLength);
				textureT[1]=((double)currentTexture.getV().getY()/VAxisLength);
				textureT[2]=((double)currentTexture.getV().getZ()/VAxisLength);
				double originShiftT=(((double)currentTexture.getV().getX()/VAxisLength)*origin[X]+((double)currentTexture.getV().getY()/VAxisLength)*origin[Y]+((double)currentTexture.getV().getZ()/VAxisLength)*origin[Z])/texScaleT;
				double textureShiftT=(double)currentTexture.getVShift()-originShiftT;
				float texRot=0; // In compiled maps this is calculated into the U and V axes, so set it to 0 until I can figure out a good way to determine a better value.
				int flags=0; // Set this to 0 until we can somehow associate faces with brushes
				String material="wld_lightmap"; // Since materials are a NightFire only thing, set this to a good default
				double lgtScale=16; // These values are impossible to get from a compiled map since they
				double lgtRot=0;    // are used by RAD for generating lightmaps, then are discarded, I believe.
				brushSides[l]=new MAPBrushSide(plane, texture, textureS, textureShiftS, textureT, textureShiftT,
				                               texRot, texScaleS, texScaleT, flags, material, lgtScale, lgtRot);
				if(brushSides[l]!=null) {
					mapBrush.add(brushSides[l]);
				}
			}
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
							Window.println("WARNING: Brush vertex calculation failed on entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"",2);
						}
					}
				} else { // If no forward side exists
					try {
						mapBrush=GenericMethods.AdvancedCorrectPlanes(mapBrush);
						numAdvancedCorrects++;
					} catch(java.lang.ArithmeticException e) {
						Window.println("WARNING: Plane correct returned 0 triangles for entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"",2);
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
					Window.println("WARNING: Brush vertex calculation failed on entity "+mapBrush.getEntnum()+" brush "+mapBrush.getBrushnum()+"",2);
				}
			}
		}
		
		// This adds the brush we've been finding and creating to
		// the current entity as an attribute. The way I've coded
		// this whole program and the entities parser, this shouldn't
		// cause any issues at all.
		if(Window.brushesToWorldIsSelected()) {
			mapFile.getEntity(0).addBrush(mapBrush);
		} else {
			if(isWater) {
				Entity newWaterEntity=new Entity("func_water");
				newWaterEntity.setAttribute("rendercolor", "0 0 0");
				newWaterEntity.setAttribute("speed", "100");
				newWaterEntity.setAttribute("wait", "4");
				newWaterEntity.setAttribute("skin", "-3");
				newWaterEntity.setAttribute("WaveHeight", "3.2");
				newWaterEntity.addBrush(mapBrush);
				mapFile.add(newWaterEntity);
			} else {
				mapFile.getEntity(currentEntity).addBrush(mapBrush);
			}
		}
	}
	
	public v38Texture createPerpTexture38(Plane in) {
		Vector3D[] points=GenericMethods.extrapPlanePoints(in);
		Vector3D U=new Vector3D(points[1].getX()-points[0].getX(), points[1].getY()-points[0].getY(), points[1].getZ()-points[0].getZ());
		Vector3D V=new Vector3D(points[1].getX()-points[2].getX(), points[1].getY()-points[2].getY(), points[1].getZ()-points[2].getZ());
		U.normalize();
		V.normalize();
		v38Texture currentTexture= new v38Texture(U, 0, V, 0, 0, 0, "special/clip", 0);
		return currentTexture;
	} 
}
