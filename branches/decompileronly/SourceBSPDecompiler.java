// SourceBSPDecompiler class
// Decompile BSP v38

public class SourceBSPDecompiler {

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
	
	private BSP BSPObject;
	
	// CONSTRUCTORS

	// This constructor sets everything according to specified settings.
	public SourceBSPDecompiler(BSP BSPObject, int jobnum) {
		// Set up global variables
		this.BSPObject=BSPObject;
		this.jobnum=jobnum;
	}
	
	// METHODS

	// Attempt to turn the BSP into a .MAP file
	public Entities decompile() throws java.io.IOException, java.lang.InterruptedException {
		Window.println("Decompiling...",Window.VERBOSITY_ALWAYS);
		// In the decompiler, it is not necessary to copy all entities to a new object, since
		// no writing is ever done back to the BSP file.
		mapFile=BSPObject.getEntities();
		//int numAreaPortals=0;
		int numTotalItems=0;
		int originalNumEntities=BSPObject.getEntities().length(); // Need to keep track of this in this algorithm, since I create more entities on the fly
		for(int i=0;i<originalNumEntities;i++) { // For each entity
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while processing entity "+i+".");
			}
			Window.println("Entity "+i+": "+mapFile.getElement(i).getAttribute("classname"),Window.VERBOSITY_ENTITIES);
			// getModelNumber() returns 0 for worldspawn, the *# for brush based entities, and -1 for everything else
			int currentModel=mapFile.getElement(i).getModelNumber();
			if(currentModel>-1) { // If this is still -1 then it's strictly a point-based entity. Move on to the next one.
				double[] origin=mapFile.getElement(i).getOrigin();
				Leaf[] leaves=BSPObject.getLeavesInModel(currentModel);
				int numLeaves=leaves.length;
				boolean[] brushesUsed=new boolean[BSPObject.getBrushes().length()]; // Keep a list of brushes already in the model, since sometimes the leaves lump references one brush several times
				numBrshs=0; // Reset the brush count for each entity
				for(int j=0;j<numLeaves;j++) { // For each leaf in the bunch
					Leaf currentLeaf=leaves[j];
					int firstMarkBrushIndex=currentLeaf.getFirstMarkBrush();
					int numBrushIndices=currentLeaf.getNumMarkBrushes();
					if(numBrushIndices>0) { // A lot of leaves reference no brushes. If this is one, this iteration of the j loop is finished
						for(int k=0;k<numBrushIndices;k++) { // For each brush referenced
							if(Thread.currentThread().interrupted()) {
								throw new java.lang.InterruptedException("while processing entity "+i+" brush "+numBrshs+".");
							}
							long currentBrushIndex=BSPObject.getMarkBrushes().getElement(firstMarkBrushIndex+k);
							if(!brushesUsed[(int)currentBrushIndex]) { // If the current brush has NOT been used in this entity
								Window.print("Brush "+numBrshs,Window.VERBOSITY_BRUSHCREATION);
								brushesUsed[(int)currentBrushIndex]=true;
								Brush brush=BSPObject.getBrushes().getElement((int)currentBrushIndex);
								try {
									decompileBrush(brush, i); // Decompile the brush
								} catch(java.lang.InterruptedException e) {
									throw new java.lang.InterruptedException("while processing entity "+i+" brush "+numBrshs+" "+e.toString().substring(32));
								}
								numBrshs++;
								numTotalItems++;
								Window.setProgress(jobnum, numTotalItems, BSPObject.getBrushes().length()+originalNumEntities, "Decompiling...");
							}
						}
					}
				}
			}
			numTotalItems++; // This entity
			Window.setProgress(jobnum, numTotalItems, BSPObject.getBrushes().length()+originalNumEntities, "Decompiling...");
		}
		// Find displacement faces and generate brushes for them
		for(int i=0;i<BSPObject.getFaces().length();i++) {
			Face face=BSPObject.getFaces().getElement(i);
			if(face.getDisplacement()>-1) {
				SourceDispInfo disp=BSPObject.getDispInfos().getElement(face.getDisplacement());
				TexInfo currentTexInfo;
				if(face.getTexture()>-1) {
					currentTexInfo=BSPObject.getTexInfo().getElement(face.getTexture());
				} else {
					Vector3D[] axes=GenericMethods.textureAxisFromPlane(BSPObject.getPlanes().getElement(face.getPlane()));
					currentTexInfo=new TexInfo(axes[0], 0, axes[1], 0, 0, BSPObject.findTexDataWithTexture("tools/toolsclip"));
				}
				SourceTexData currentTexData=BSPObject.getTexDatas().getElement(currentTexInfo.getTexture());
				String texture=BSPObject.getTextures().getTextureAtOffset((int)BSPObject.getTexTable().getElement(currentTexData.getStringTableIndex()));
				double[] textureU=new double[3];
				double[] textureV=new double[3];
				// Get the lengths of the axis vectors
				double SAxisLength=Math.sqrt(Math.pow((double)currentTexInfo.getSAxis().getX(),2)+Math.pow((double)currentTexInfo.getSAxis().getY(),2)+Math.pow((double)currentTexInfo.getSAxis().getZ(),2));
				double TAxisLength=Math.sqrt(Math.pow((double)currentTexInfo.getTAxis().getX(),2)+Math.pow((double)currentTexInfo.getTAxis().getY(),2)+Math.pow((double)currentTexInfo.getTAxis().getZ(),2));
				// In compiled maps, shorter vectors=longer textures and vice versa. This will convert their lengths back to 1. We'll use the actual scale values for length.
				double texScaleU=(1/SAxisLength);// Let's use these values using the lengths of the U and V axes we found above.
				double texScaleV=(1/TAxisLength);
				textureU[0]=((double)currentTexInfo.getSAxis().getX()/SAxisLength);
				textureU[1]=((double)currentTexInfo.getSAxis().getY()/SAxisLength);
				textureU[2]=((double)currentTexInfo.getSAxis().getZ()/SAxisLength);
				double textureShiftU=(double)currentTexInfo.getSShift();
				textureV[0]=((double)currentTexInfo.getTAxis().getX()/TAxisLength);
				textureV[1]=((double)currentTexInfo.getTAxis().getY()/TAxisLength);
				textureV[2]=((double)currentTexInfo.getTAxis().getZ()/TAxisLength);
				double textureShiftV=(double)currentTexInfo.getTShift();
				
				if(face.getNumEdges()!=4) {
					Window.println("Displacement face with "+face.getNumEdges()+" edges!",Window.VERBOSITY_WARNINGS);
				}
				
				// Turn vertices and edges into arrays of vectors
				Vector3D[] froms=new Vector3D[face.getNumEdges()];
				Vector3D[] tos=new Vector3D[face.getNumEdges()];
				for(int j=0;j<face.getNumEdges();j++) {
					if(BSPObject.getSurfEdges().getElement(face.getFirstEdge()+j) > 0) {
						froms[j]=BSPObject.getVertices().getElement(BSPObject.getEdges().getElement((int)BSPObject.getSurfEdges().getElement(face.getFirstEdge()+j)).getFirstVertex()).getVertex();
						tos[j]=BSPObject.getVertices().getElement(BSPObject.getEdges().getElement((int)BSPObject.getSurfEdges().getElement(face.getFirstEdge()+j)).getSecondVertex()).getVertex();
					} else {
						tos[j]=BSPObject.getVertices().getElement(BSPObject.getEdges().getElement((int)BSPObject.getSurfEdges().getElement(face.getFirstEdge()+j) * -1).getFirstVertex()).getVertex();
						froms[j]=BSPObject.getVertices().getElement(BSPObject.getEdges().getElement((int)BSPObject.getSurfEdges().getElement(face.getFirstEdge()+j) * -1).getSecondVertex()).getVertex();
					}
				}
				
				MAPBrush displacementBrush = GenericMethods.createBrushFromWind(froms, tos, texture, "TOOLS/TOOLSNODRAW", currentTexInfo);
				
				MAPDisplacement mapdisp=new MAPDisplacement(disp, BSPObject.getDispVerts().getVertsInDisp(disp.getDispVertStart(), disp.getPower()));
				displacementBrush.getSide(0).setDisplacement(mapdisp);
				mapFile.getElement(0).addBrush(displacementBrush);
			}
		}
		for(int i=0;i<BSPObject.getStaticProps().length();i++) {
			Entity newStaticProp=new Entity("prop_static");
			SourceStaticProp currentProp=BSPObject.getStaticProps().getElement(i);
			newStaticProp.setAttribute("model", BSPObject.getStaticProps().getDictionary()[currentProp.getDictionaryEntry()]);
			newStaticProp.setAttribute("skin", currentProp.getSkin()+"");
			newStaticProp.setAttribute("origin", currentProp.getOrigin().getX()+" "+currentProp.getOrigin().getY()+" "+currentProp.getOrigin().getZ());
			newStaticProp.setAttribute("angles", currentProp.getAngles().getX()+" "+currentProp.getAngles().getY()+" "+currentProp.getAngles().getZ());
			newStaticProp.setAttribute("solid", currentProp.getSolidity()+"");
			newStaticProp.setAttribute("fademindist", currentProp.getMinFadeDist()+"");
			newStaticProp.setAttribute("fademaxdist", currentProp.getMaxFadeDist()+"");
			newStaticProp.setAttribute("fadescale", currentProp.getForcedFadeScale()+"");
			if(currentProp.getTargetname()!=null) {
				newStaticProp.setAttribute("targetname", currentProp.getTargetname());
			}
			mapFile.add(newStaticProp);
		}
		for(int i=0;i<BSPObject.getCubemaps().length();i++) {
			Entity newCubemap=new Entity("env_cubemap");
			SourceCubemap currentCube=BSPObject.getCubemaps().getElement(i);
			newCubemap.setAttribute("origin", currentCube.getOrigin().getX()+" "+currentCube.getOrigin().getY()+" "+currentCube.getOrigin().getZ());
			newCubemap.setAttribute("cubemapsize", currentCube.getSize()+"");
			mapFile.add(newCubemap);
		}
		if(!Window.skipFlipIsSelected()) {
			Window.println("Num simple corrected brushes: "+numSimpleCorrects,Window.VERBOSITY_MAPSTATS); 
			Window.println("Num advanced corrected brushes: "+numAdvancedCorrects,Window.VERBOSITY_MAPSTATS); 
			Window.println("Num good brushes: "+numGoodBrushes,Window.VERBOSITY_MAPSTATS); 
		}
		return mapFile;
	}

	// -decompileBrush38(Brush, int, boolean)
	// Decompiles the Brush and adds it to entitiy #currentEntity as .MAP data.
	private void decompileBrush(Brush brush, int currentEntity) throws java.lang.InterruptedException {
		double[] origin=mapFile.getElement(currentEntity).getOrigin();
		int firstSide=brush.getFirstSide();
		int numSides=brush.getNumSides();
		MAPBrushSide[] brushSides=new MAPBrushSide[numSides];
		boolean isDetail=false;
		if (currentEntity==0 && !Window.noDetailIsSelected() && (brush.getContents()[3] & ((byte)1 << 3)) != 0) {
			isDetail=true;
		}
		MAPBrush mapBrush = new MAPBrush(numBrshs, currentEntity, isDetail);
		if (currentEntity==0 && !Window.noWaterIsSelected() && (brush.getContents()[0] & ((byte)1 << 5)) != 0) {
			mapBrush.setWater(true);
		}
		Window.println(": "+numSides+" sides, detail: "+isDetail,Window.VERBOSITY_BRUSHCREATION);
		for(int i=0;i<numSides;i++) { // For each side of the brush
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("side "+i+".");
			}
			BrushSide currentSide=BSPObject.getBrushSides().getElement(firstSide+i);
			if(currentSide.isBevel()==0) { // Bevel sides are evil
				Vector3D[] plane=new Vector3D[3]; // Three points define a plane. All I have to do is find three points on that plane.
				Plane currentPlane=BSPObject.getPlanes().getElement(currentSide.getPlane()); // To find those three points, I must extrapolate from planes until I find a way to associate faces with brushes
				boolean isDuplicate=false;/* TODO: We sure don't want duplicate planes (though this is already handled by the MAPBrush class). Make sure neither checked side is bevel.
				for(int j=i+1;j<numSides;j++) { // For each subsequent side of the brush
					if(currentPlane.equals(BSPObject.getPlanes().getPlane(BSPObject.getBrushSides().getElement(firstSide+j).getPlane()))) {
						Window.println("WARNING: Duplicate planes in a brush, sides "+i+" and "+j,Window.VERBOSITY_WARNINGS);
						isDuplicate=true;
					}
				}*/
				if(!isDuplicate) {
					TexInfo currentTexInfo=null;
					String texture="tools/toolsclip";
					if(currentSide.getTexture()>-1) {
						currentTexInfo=BSPObject.getTexInfo().getElement(currentSide.getTexture());
					} else {
						int dataIndex=BSPObject.findTexDataWithTexture("tools/toolsclip");
						if(dataIndex>=0) {
							currentTexInfo=new TexInfo(new Vector3D(0,0,0), 0, new Vector3D(0,0,0), 0, 0, dataIndex);
						}
					}
					if(currentTexInfo!=null) {
						SourceTexData currentTexData;
						if(currentTexInfo.getTexture()>=0) { // I've only found one case where this is a problem: c2a3a in HL Source. Don't know why.
							currentTexData=BSPObject.getTexDatas().getElement(currentTexInfo.getTexture());
							texture=BSPObject.getTextures().getTextureAtOffset((int)BSPObject.getTexTable().getElement(currentTexData.getStringTableIndex()));
						} else {
							texture="tools/toolsskip";
						}
					}
					double[] textureU=new double[3];
					double[] textureV=new double[3];
					double textureShiftU=0;
					double textureShiftV=0;
					double texScaleU=1;
					double texScaleV=1;
					// Get the lengths of the axis vectors
					if((texture.length()>6 && texture.substring(0,6).equalsIgnoreCase("tools/")) || currentTexInfo==null) {
						// Tools textured faces do not maintain their own texture axes. Therefore, an arbitrary axis is
						// used in the compiled map. When decompiled, these axes might smear the texture on the face. Fix that.
						Vector3D[] axes=GenericMethods.textureAxisFromPlane(currentPlane);
						textureU=axes[0].getPoint();
						textureV=axes[1].getPoint();
					} else {
						double SAxisLength=Math.sqrt(Math.pow((double)currentTexInfo.getSAxis().getX(),2)+Math.pow((double)currentTexInfo.getSAxis().getY(),2)+Math.pow((double)currentTexInfo.getSAxis().getZ(),2));
						double TAxisLength=Math.sqrt(Math.pow((double)currentTexInfo.getTAxis().getX(),2)+Math.pow((double)currentTexInfo.getTAxis().getY(),2)+Math.pow((double)currentTexInfo.getTAxis().getZ(),2));
						// In compiled maps, shorter vectors=longer textures and vice versa. This will convert their lengths back to 1. We'll use the actual scale values for length.
						texScaleU=(1/SAxisLength);// Let's use these values using the lengths of the U and V axes we found above.
						texScaleV=(1/TAxisLength);
						textureU[0]=((double)currentTexInfo.getSAxis().getX()/SAxisLength);
						textureU[1]=((double)currentTexInfo.getSAxis().getY()/SAxisLength);
						textureU[2]=((double)currentTexInfo.getSAxis().getZ()/SAxisLength);
						double originShiftU=(((double)currentTexInfo.getSAxis().getX()/SAxisLength)*origin[X]+((double)currentTexInfo.getSAxis().getY()/SAxisLength)*origin[Y]+((double)currentTexInfo.getSAxis().getZ()/SAxisLength)*origin[Z])/texScaleU;
						textureShiftU=(double)currentTexInfo.getSShift()-originShiftU;
						textureV[0]=((double)currentTexInfo.getTAxis().getX()/TAxisLength);
						textureV[1]=((double)currentTexInfo.getTAxis().getY()/TAxisLength);
						textureV[2]=((double)currentTexInfo.getTAxis().getZ()/TAxisLength);
						double originShiftV=(((double)currentTexInfo.getTAxis().getX()/TAxisLength)*origin[X]+((double)currentTexInfo.getTAxis().getY()/TAxisLength)*origin[Y]+((double)currentTexInfo.getTAxis().getZ()/TAxisLength)*origin[Z])/texScaleV;
						textureShiftV=(double)currentTexInfo.getTShift()-originShiftV;
					}
					float texRot=0; // In compiled maps this is calculated into the U and V axes, so set it to 0 until I can figure out a good way to determine a better value.
					int flags=0; // Set this to 0 until we can somehow associate faces with brushes
					String material="wld_lightmap"; // Since materials are a NightFire only thing, set this to a good default
					double lgtScale=16; // These values are impossible to get from a compiled map since they
					double lgtRot=0;    // are used by RAD for generating lightmaps, then are discarded, I believe.
					brushSides[i]=new MAPBrushSide(currentPlane, texture, textureU, textureShiftU, textureV, textureShiftV,
					                               texRot, texScaleU, texScaleV, flags, material, lgtScale, lgtRot);
					mapBrush.add(brushSides[i]);
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
		if(Window.brushesToWorldIsSelected()) {
			mapBrush.setWater(false);
			mapFile.getElement(0).addBrush(mapBrush);
		} else {
			mapFile.getElement(currentEntity).addBrush(mapBrush);
		}
	}
	
	public TexInfo createPerpTexInfo(Plane in) {
		Vector3D[] axes=GenericMethods.textureAxisFromPlane(in);
		return new TexInfo(axes[0], 0, axes[1], 0, 0, BSPObject.findTexDataWithTexture("tools/toolsclip"));
	} 
}
