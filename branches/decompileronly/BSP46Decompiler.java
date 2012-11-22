// BSP46Decompiler
// Decompiles a v46 BSP

import java.util.Date;

public class BSP46Decompiler {

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
	private int currentSideIndex=0;
	private boolean isCoD=false;
	
	private BSP BSPObject;
	
	// CONSTRUCTORS
	
	// This constructor sets everything according to specified settings.
	public BSP46Decompiler(BSP BSPObject, int jobnum) {
		// Set up global variables
		this.BSPObject=BSPObject;
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
		mapFile=new Entities(BSPObject.getEntities());
		int numTotalItems=0;
		// I need to go through each entity and see if it's brush-based.
		// Worldspawn is brush-based as well as any entity with model *#.
		for(int i=0;i<BSPObject.getEntities().length();i++) { // For each entity
			Window.println("Entity "+i+": "+mapFile.getEntity(i).getAttribute("classname"),Window.VERBOSITY_ENTITIES);
			numBrshs=0; // Reset the brush count for each entity
			// getModelNumber() returns 0 for worldspawn, the *# for brush based entities, and -1 for everything else
			int currentModel=mapFile.getEntity(i).getModelNumber();
			if(currentModel>-1) { // If this is still -1 then it's strictly a point-based entity. Move on to the next one.
				double[] origin=mapFile.getEntity(i).getOrigin();
				int firstBrush=BSPObject.getModels().getElement(currentModel).getFirstBrush();
				int numBrushes=BSPObject.getModels().getElement(currentModel).getNumBrushes();
				numBrshs=0;
				for(int j=0;j<numBrushes;j++) { // For each brush
					Window.print("Brush "+(j+firstBrush),Window.VERBOSITY_BRUSHCREATION);
					decompileBrush(BSPObject.getBrushes().getElement(j+firstBrush), i); // Decompile the brush
					numBrshs++;
					numTotalItems++;
					Window.setProgress(jobnum, numTotalItems, BSPObject.getBrushes().length()+BSPObject.getEntities().length(), "Decompiling...");
				}
				mapFile.getEntity(i).deleteAttribute("model");
			}
			numTotalItems++;
			Window.setProgress(jobnum, numTotalItems, BSPObject.getBrushes().length()+BSPObject.getEntities().length(), "Decompiling...");
		}
		Window.setProgress(jobnum, numTotalItems, BSPObject.getBrushes().length()+BSPObject.getEntities().length(), "Saving...");
		MAPMaker.outputMaps(mapFile, BSPObject.getMapNameNoExtension(), BSPObject.getFolder(), BSPObject.getVersion());
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
		if(firstSide<0) {
			isCoD=true;
			firstSide=currentSideIndex;
			currentSideIndex+=numSides;
		}
		MAPBrushSide[] brushSides=new MAPBrushSide[0];
		boolean isDetail=false;
		int brushTextureIndex=brush.getTexture();
		byte[] contents=new byte[4];
		if(brushTextureIndex>=0) {
			contents=BSPObject.getTextures().getElement(brushTextureIndex).getContents();
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
		boolean isVisBrush=false;
		for(int i=0;i<numSides;i++) { // For each side of the brush
			BrushSide currentSide=BSPObject.getBrushSides().getElement(firstSide+i);
			int currentFaceIndex=currentSide.getFace();
			Plane currentPlane;
			if(isCoD) {
				if(i==0) { // XMin
					currentPlane=new Plane((double)-1, (double)0, (double)0, (double)-currentSide.getDist());
				} else {
					if(i==1) { // XMax
						currentPlane=new Plane((double)1, (double)0, (double)0, (double)currentSide.getDist());
					} else {
						if(i==2) { // YMin
							currentPlane=new Plane((double)0, (double)-1, (double)0, (double)-currentSide.getDist());
						} else {
							if(i==3) { // YMax
								currentPlane=new Plane((double)0, (double)1, (double)0, (double)currentSide.getDist());
							} else {
								if(i==4) { // ZMin
									currentPlane=new Plane((double)0, (double)0, (double)-1, (double)-currentSide.getDist());
								} else {
									if(i==5) { // ZMax
										currentPlane=new Plane((double)0, (double)0, (double)1, (double)currentSide.getDist());
									} else {
										currentPlane=BSPObject.getPlanes().getElement(currentSide.getPlane());
									}
								}
							}
						}
					}
				}
			} else {
				currentPlane=BSPObject.getPlanes().getElement(currentSide.getPlane());
			}
			Vector3D[] triangle=new Vector3D[0];
			boolean pointsWorked=false;
			int firstVertex=-1;
			int numVertices=0;
			String texture="noshader";
			boolean masked=false;
			if(currentFaceIndex>-1) {
				Face currentFace=BSPObject.getFaces().getElement(currentFaceIndex);
				int currentTextureIndex=currentFace.getTexture();
				firstVertex=currentFace.getFirstVertex();
				numVertices=currentFace.getNumVertices();
				String mask=BSPObject.getTextures().getElement(currentTextureIndex).getMask();
				if(mask.equalsIgnoreCase("ignore") || mask.length()==0) {
					texture=BSPObject.getTextures().getElement(currentTextureIndex).getName();
				} else {
					texture=mask.substring(0,mask.length()-4); // Because mask includes file extensions
					masked=true;
				}
				if(numVertices!=0 && !Window.planarDecompIsSelected()) { // If the face actually references a set of vertices
					triangle=new Vector3D[3]; // Three points define a plane. All I have to do is find three points on that plane.
					triangle[0]=new Vector3D(BSPObject.getVertices().getElement(firstVertex).getVertex()); // Grab and store the first one
					int j=1;
					for(;j<numVertices;j++) { // For each point after the first one
						triangle[1]=new Vector3D(BSPObject.getVertices().getElement(firstVertex+j).getVertex());
						if(!triangle[0].equals(triangle[1])) { // Make sure the point isn't the same as the first one
							break; // If it isn't the same, this point is good
						}
					}
					for(j=j+1;j<numVertices;j++) { // For each point after the previous one used
						triangle[2]=new Vector3D(BSPObject.getVertices().getElement(firstVertex+j).getVertex());
						if(!triangle[2].equals(triangle[0]) && !triangle[2].equals(triangle[1])) { // Make sure no point is equal to the third one
							// Make sure all three points are non collinear
							Vector3D cr=Vector3D.crossProduct(triangle[0].subtract(triangle[1]), triangle[0].subtract(triangle[2]));
							if(cr.length() > Window.getPrecision()) { // vector length is never negative.
								pointsWorked=true;
								break;
							}
						}
					}
				}
			} else { // If face information is not available, use the brush side's info instead
				int currentTextureIndex=currentSide.getTexture();
				if(currentTextureIndex>=0) {
					String mask=BSPObject.getTextures().getElement(currentTextureIndex).getMask();
					if(mask.equalsIgnoreCase("ignore") || mask.length()==0) {
						texture=BSPObject.getTextures().getElement(currentTextureIndex).getName();
					} else {
						texture=mask.substring(0,mask.length()-4); // Because mask includes file extensions
						masked=true;
					}
				} else { // If neither face or brush side has texture info, fall all the way back to brush. I don't know if this ever happens.
					if(brushTextureIndex>=0) { // If none of them have any info, noshader
						String mask=BSPObject.getTextures().getElement(brushTextureIndex).getMask();
						if(mask.equalsIgnoreCase("ignore") || mask.length()==0) {
							texture=BSPObject.getTextures().getElement(brushTextureIndex).getName();
						} else {
							texture=mask.substring(0,mask.length()-4); // Because mask includes file extensions
							masked=true;
						}
					}
				}
			}
			if(texture.equalsIgnoreCase("textures/common/vis")) {
				isVisBrush=true;
				break;
			}
			// Get the lengths of the axis vectors.
			// TODO: This information seems to be contained in Q3's vertex structure. But there doesn't seem
			// to be a way to directly link faces to brush sides.
			double UAxisLength=1;
			double VAxisLength=1;
			double texScaleS=1;
			double texScaleT=1;
			Vector3D[] textureAxes=GenericMethods.textureAxisFromPlane(currentPlane);
			double originShiftS=(textureAxes[0].getX()*origin[X])+(textureAxes[0].getY()*origin[Y])+(textureAxes[0].getZ()*origin[Z]);
			double originShiftT=(textureAxes[1].getX()*origin[X])+(textureAxes[1].getY()*origin[Y])+(textureAxes[1].getZ()*origin[Z]);
			double textureShiftS;
			double textureShiftT;
			if(firstVertex>=0) {
				textureShiftS=(double)BSPObject.getVertices().getElement(firstVertex).getTexCoordX()-originShiftS;
				textureShiftT=(double)BSPObject.getVertices().getElement(firstVertex).getTexCoordY()-originShiftT;
			} else {
				textureShiftS=0-originShiftS;
				textureShiftT=0-originShiftT;
			}
			float texRot=0;
			String material;
			if(masked) {
				material="wld_masked";
			} else {
				material="wld_lightmap";
			}
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
			if(pointsWorked) {
				newList[brushSides.length]=new MAPBrushSide(currentPlane, triangle, texture, textureAxes[0].getPoint(), textureShiftS, textureAxes[1].getPoint(), textureShiftT,
				                                            texRot, texScaleS, texScaleT, flags, material, lgtScale, lgtRot);
			} else {
				newList[brushSides.length]=new MAPBrushSide(currentPlane, texture, textureAxes[0].getPoint(), textureShiftS, textureAxes[1].getPoint(), textureShiftT,
				                                            texRot, texScaleS, texScaleT, flags, material, lgtScale, lgtRot);
			}
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
		
		if(isCoD && mapBrush.getNumSides()>6) {
			// Now we need to get rid of all the sides that aren't used. Get a list of
			// the useless sides from one brush, and delete those sides from all of them,
			// since they all have the same sides.
			if(!Window.dontCullIsSelected() && numSides>6) {
				int[] badSides=GenericMethods.findUnusedPlanes(mapBrush);
				// Need to iterate backward, since these lists go from low indices to high, and
				// the index of all subsequent items changes when something before it is removed.
				if(mapBrush.getNumSides()-badSides.length<4) {
					Window.println("WARNING: Plane cull returned less than 4 sides for entity "+currentEntity+" brush "+numBrshs,Window.VERBOSITY_WARNINGS);
				} else {
					for(int i=badSides.length-1;i>-1;i--) {
						mapBrush.delete(badSides[i]);
					}
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
