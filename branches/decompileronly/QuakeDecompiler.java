// QuakeDecompiler
// Attempts to decompile a Quake BSP

import java.util.Date;

public class QuakeDecompiler {

	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	public static final int A = 0;
	public static final int B = 1;
	public static final int C = 2;
	
	public static final int X = 0;
	public static final int Y = 1;
	public static final int Z = 2;
	
	private int jobnum;
	
	private Entities mapFile; // Most MAP file formats are simply a bunch of nested entities
	private int numBrshs;
	private int numSimpleCorrects=0;
	private int numAdvancedCorrects=0;
	private int numGoodBrushes=0;
	
	private BSP BSPObject;
	
	// CONSTRUCTORS
	
	// This constructor sets everything according to specified settings.
	public QuakeDecompiler(BSP BSPObject, int jobnum) {
		// Set up global variables
		this.BSPObject=BSPObject;
		this.jobnum=jobnum;
	}

	// METHODS
	
	// -decompile()
	// Attempts to convert the Quake/Half-life BSP file back into a .MAP file.
	public void decompile() throws java.io.IOException, java.lang.InterruptedException {
		Window.println("Decompiling...",Window.VERBOSITY_ALWAYS);
		Date begin=new Date();
		// In the decompiler, it is not necessary to copy all entities to a new object, since
		// no writing is ever done back to the BSP file.
		mapFile=BSPObject.getEntities();
		int numTotalItems=0;
		// I need to go through each entity and see if it's brush-based.
		// Worldspawn is brush-based as well as any entity with model *#.
		for(int i=0;i<BSPObject.getEntities().length();i++) { // For each entity
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while processing entity "+i+".");
			}
			Window.println("Entity "+i+": "+mapFile.getElement(i).getAttribute("classname"),Window.VERBOSITY_ENTITIES);
			// getModelNumber() returns 0 for worldspawn, the *# for brush based entities, and -1 for everything else
			int currentModel=mapFile.getElement(i).getModelNumber();
			
			if(currentModel>-1) { // If this is still -1 then it's strictly a point-based entity. Move on to the next one.
				double[] origin=mapFile.getElement(i).getOrigin();
				/*Leaf[] leaves=BSPObject.getLeavesInModel(currentModel);
				int numLeaves=leaves.length;
				numBrshs=0; // Reset the brush count for each entity
				for(int j=0;j<numLeaves;j++) { // For each leaf in the bunch
					Leaf currentLeaf=leaves[j];
					int firstMarkFace=currentLeaf.getFirstMarkFace();
					int numMarkFaces=currentLeaf.getNumMarkFaces();
					// Since brush structures don't exist in Quake/GoldSrc BSPs, I have to try to reconstruct them from faces.
					if(numMarkFaces>0) {
						if(numMarkFaces<8) { // An ideal case. A brush needs at least four sides, so if there's less than eight, there's not enough for two brushes.
							MAPBrush newBrush=new MAPBrush(0, i, false);
							for(int k=0;k<numMarkFaces;k++) {
								Face currentFace=BSPObject.getFaces().getElement((int)BSPObject.getMarkSurfaces().getElement(firstMarkFace+k));
								Plane currentPlane=BSPObject.getPlanes().getElement(currentFace.getPlane());
								String texture="special/clip";
								double[] textureU=new double[3];
								double[] textureV=new double[3];
								double UShift=0;
								double VShift=0;
								double texScaleU=1;
								double texScaleV=1;
								Vector3D[] plane;
								if(currentFace.getSide()!=0) {
									plane=GenericMethods.extrapPlanePoints(currentPlane);
								} else {
									plane=GenericMethods.extrapPlanePoints(Plane.flip(currentPlane));
								}
								if(currentFace.getTexture()>-1) {
									TexInfo currentInfo=BSPObject.getTexInfo().getElement(currentFace.getTexture());
									Texture currentTexture=BSPObject.getTextures().getElement(currentInfo.getTexture());
									texture=currentTexture.getName();
									// Get the lengths of the axis vectors
									double SAxisLength=Math.sqrt(Math.pow((double)currentInfo.getSAxis().getX(),2)+Math.pow((double)currentInfo.getSAxis().getY(),2)+Math.pow((double)currentInfo.getSAxis().getZ(),2));
									double TAxisLength=Math.sqrt(Math.pow((double)currentInfo.getTAxis().getX(),2)+Math.pow((double)currentInfo.getTAxis().getY(),2)+Math.pow((double)currentInfo.getTAxis().getZ(),2));
									// In compiled maps, shorter vectors=longer textures and vice versa. This will convert their lengths back to 1. We'll use the actual scale values for length.
									texScaleU=(1/SAxisLength);// Let's use these values using the lengths of the U and V axes we found above.
									texScaleV=(1/TAxisLength);
									textureU[0]=((double)currentInfo.getSAxis().getX()/SAxisLength);
									textureU[1]=((double)currentInfo.getSAxis().getY()/SAxisLength);
									textureU[2]=((double)currentInfo.getSAxis().getZ()/SAxisLength);
									textureV[0]=((double)currentInfo.getTAxis().getX()/TAxisLength);
									textureV[1]=((double)currentInfo.getTAxis().getY()/TAxisLength);
									textureV[2]=((double)currentInfo.getTAxis().getZ()/TAxisLength);
									UShift=(double)currentInfo.getSShift();
									VShift=(double)currentInfo.getTShift();
								} else {
									Vector3D[] axes;
									if(currentFace.getSide()!=0) {
										axes=GenericMethods.textureAxisFromPlane(currentPlane);
									} else {
										axes=GenericMethods.textureAxisFromPlane(Plane.flip(currentPlane));
									}
									textureU=axes[0].getPoint();
									textureV=axes[1].getPoint();
								}
								double originShiftU=(textureU[0]*origin[X]+textureU[1]*origin[Y]+textureU[2]*origin[Z])/texScaleU;
								double textureShiftU=UShift-originShiftU;
								double originShiftV=(textureV[0]*origin[X]+textureV[1]*origin[Y]+textureV[2]*origin[Z])/texScaleV;
								double textureShiftV=VShift-originShiftV;
								float texRot=0; // In compiled maps this is calculated into the U and V axes, so set it to 0 until I can figure out a good way to determine a better value.
								int flags=0; // Set this to 0 until we can somehow associate faces with brushes
								String material="wld_lightmap"; // Since materials are a NightFire only thing, set this to a good default
								double lgtScale=16; // These values are impossible to get from a compiled map since they
								double lgtRot=0;    // are used by RAD for generating lightmaps, then are discarded, I believe.
								MAPBrushSide brushSide=new MAPBrushSide(plane, texture, textureU, textureShiftU, textureV, textureShiftV,
								                               texRot, texScaleU, texScaleV, flags, material, lgtScale, lgtRot);
								newBrush.add(brushSide);
							}
							mapFile.getElement(i).addBrush(newBrush);
						} else {
							Window.println("Leaf with "+numMarkFaces+" faces",Window.VERBOSITY_WARNINGS);
						}
					}
				}*/
				Model currentModelObject=BSPObject.getModels().getElement(currentModel);
				int firstFace=currentModelObject.getFirstFace();
				int numFaces=currentModelObject.getNumFaces();
				for(int j=0;j<numFaces;j++) {
					Face face=BSPObject.getFaces().getElement(firstFace+j);
					// Turn vertices and edges into arrays of vectors
					Vector3D[] froms=new Vector3D[face.getNumEdges()];
					Vector3D[] tos=new Vector3D[face.getNumEdges()];
					for(int k=0;k<face.getNumEdges();k++) {
						if(BSPObject.getSurfEdges().getElement(face.getFirstEdge()+k) > 0) {
							froms[k]=BSPObject.getVertices().getElement(BSPObject.getEdges().getElement((int)BSPObject.getSurfEdges().getElement(face.getFirstEdge()+k)).getFirstVertex()).getVertex();
							tos[k]=BSPObject.getVertices().getElement(BSPObject.getEdges().getElement((int)BSPObject.getSurfEdges().getElement(face.getFirstEdge()+k)).getSecondVertex()).getVertex();
						} else {
							tos[k]=BSPObject.getVertices().getElement(BSPObject.getEdges().getElement((int)BSPObject.getSurfEdges().getElement(face.getFirstEdge()+k) * -1).getFirstVertex()).getVertex();
							froms[k]=BSPObject.getVertices().getElement(BSPObject.getEdges().getElement((int)BSPObject.getSurfEdges().getElement(face.getFirstEdge()+k) * -1).getSecondVertex()).getVertex();
						}
					}
					
					TexInfo currentTexInfo=BSPObject.getTexInfo().getElement(face.getTexture());
					Texture currentTexture=BSPObject.getTextures().getElement(currentTexInfo.getTexture());
					String texture=currentTexture.getName();
					
					MAPBrush faceBrush = GenericMethods.createBrushFromWind(froms, tos, texture, "special/nodraw", currentTexInfo);
					mapFile.getElement(i).addBrush(faceBrush);
				}
			}
			numTotalItems++;
			Window.setProgress(jobnum, numTotalItems, BSPObject.getEntities().length(), "Decompiling...");
		}
		Window.setProgress(jobnum, numTotalItems, BSPObject.getEntities().length(), "Saving...");
		MAPMaker.outputMaps(mapFile, BSPObject.getMapNameNoExtension(), BSPObject.getFolder(), BSPObject.getVersion());
		/*if(!Window.skipFlipIsSelected()) {
			Window.println("Num simple corrected brushes: "+numSimpleCorrects,Window.VERBOSITY_MAPSTATS); 
			Window.println("Num advanced corrected brushes: "+numAdvancedCorrects,Window.VERBOSITY_MAPSTATS); 
			Window.println("Num good brushes: "+numGoodBrushes,Window.VERBOSITY_MAPSTATS); 
		}*/
		Date end=new Date();
		Window.println("Time taken: "+(end.getTime()-begin.getTime())+"ms"+(char)0x0D+(char)0x0A,Window.VERBOSITY_ALWAYS);
	}
}