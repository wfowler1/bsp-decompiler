// WADDecompiler class

// Handles the actual decompilation.

import java.util.Date;

public class WADDecompiler {

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
	
	private DoomMap doomMap;
	
	// CONSTRUCTORS
	
	// This constructor sets up everything to convert a Doom map into brushes compatible with modern map editors.
	// I don't know if this is decompiling, per se. I don't know if Doom maps were ever compiled or if they just had nodes built.
	public WADDecompiler(DoomMap doomMap, int jobnum) {
		this.doomMap=doomMap;
		this.jobnum=jobnum;
	}
	
	// METHODS
	
	// +decompile()
	// Attempts to convert a map in a Doom WAD into a usable .MAP file. This has many
	// challenges, not the least of which is the fact that the Doom engine didn't use
	// brushes (at least, not in any sane way).
	public void decompile() throws java.io.IOException {
		Date begin=new Date();
		Window.println(doomMap.getMapName(),Window.VERBOSITY_ALWAYS);
		
		mapFile=new Entities();
		Entity world = new Entity("worldspawn");
		world.setAttribute("mapversion", "510");
		mapFile.add(world);
		
		String[] lowerWallTextures=new String[doomMap.getSidedefs().getNumElements()];
		String[] midWallTextures=new String[doomMap.getSidedefs().getNumElements()];
		String[] higherWallTextures=new String[doomMap.getSidedefs().getNumElements()];
		
		short[] sectorType=new short[doomMap.getSectors().getNumElements()];
		
		// Since Doom relied on sectors to define a cieling and floor height, and nothing else,
		// need to find the minimum and maximum used Z values. This is because the Doom engine
		// is only a pseudo-3D engine. For all it cares, the cieling and floor extend to their
		// respective infinities. For a GC/Hammer map, however, this cannot be the case.
		int ZMin=32767;  // Even though the values in the map will never exceed these, use ints here to avoid
		int ZMax=-32768; // overflows, in case the map DOES go within 32 units of these values.
		for(int i=0;i<doomMap.getSectors().getNumElements();i++) {
			DSector currentSector=doomMap.getSectors().getSector(i);
			if(currentSector.getFloorHeight()<ZMin+32) {
				ZMin=currentSector.getFloorHeight()-32; // Can't use the actual value, because that IS the floor
			} else {
				if(currentSector.getCielingHeight()>ZMax-32) {
					ZMax=currentSector.getCielingHeight()+32; // or the cieling. Subtract or add a sane value to it.
				}
			}
		}
		
		// I need to analyze the binary tree and get more information, particularly the
		// parent nodes of each subsector and node, as well as whether it's the right or
		// left child of that node. These are extremely important, as the parent defines
		// boundaries for the children, as well as inheriting further boundaries from its
		// parents. These boundaries are invaluable for forming brushes.
		int[] nodeparents = new int[doomMap.getNodes().getNumElements()];
		boolean[] nodeIsLeft = new boolean[doomMap.getNodes().getNumElements()];
		
		for(int i=0;i<doomMap.getNodes().getNumElements();i++) {
			nodeparents[i]=-1; // There should only be one node left with -1 as a parent. This SHOULD be the root.
			for(int j=0;j<doomMap.getNodes().getNumElements();j++) {
				if(doomMap.getNodes().getNode(j).getChild1() == i) {
					nodeparents[i]=j;
					break;
				} else {
					if(doomMap.getNodes().getNode(j).getChild2() == i) {
						nodeparents[i]=j;
						nodeIsLeft[i]=true;
						break;
					}
				}
			}
		}
		
		int[] subsectorSectors = new int[doomMap.getSubSectors().getNumElements()];
		// Keep a list of what sidedefs belong to what subsector as well
		int[][] subsectorSidedefs = new int[doomMap.getSubSectors().getNumElements()][];
		
		// Figure out what sector each subsector belongs to, and what node is its parent.
		// Depending on sector "tags" this will help greatly in creation of brushbased entities,
		// and also helps in finding subsector floor and cieling heights.
		int[] ssparents = new int[doomMap.getSubSectors().getNumElements()];
		boolean[] ssIsLeft = new boolean[doomMap.getSubSectors().getNumElements()];
		for(int i=0;i<doomMap.getSubSectors().getNumElements();i++) {
			Window.println("Creating brushes for subsector "+i,Window.VERBOSITY_BRUSHCREATION);
			// First, find the subsector's parent and whether it is the left or right child.
			ssparents[i]=-1; // No subsector should have a -1 in here
			for(int j=0;j<doomMap.getNodes().getNumElements();j++) {
				// When a node references a subsector, it is not referenced by negative
				// index, as future BSP versions do. The bits 0-14 ARE the index, and
				// bit 15 (which is the sign bit in two's compliment math) determines
				// whether or not it is a node or subsector. Therefore, we need to add
				// 2^15 to the number to produce the actual index.
				if(doomMap.getNodes().getNode(j).getChild1()+32768 == i) {
					ssparents[i]=j;
					break;
				} else {
					if(doomMap.getNodes().getNode(j).getChild2()+32768 == i) {
						ssparents[i]=j;
						ssIsLeft[i]=true;
						break;
					}
				}
			}
			
			// Second, figure out what sector a subsector belongs to, and the type of sector it is.
			subsectorSectors[i]=-1;
			DSubSector currentsubsector=doomMap.getSubSectors().getSubSector(i);
			subsectorSidedefs[i]=new int[currentsubsector.getNumSegs()];
			for(int j=0;j<currentsubsector.getNumSegs();j++) { // For each segment the subsector references
				DSegment currentsegment=doomMap.getSegments().getSegment(currentsubsector.getFirstSeg()+j);
				DLinedef currentlinedef=doomMap.getLinedefs().getLinedef(currentsegment.getLinedef());
				int currentsidedefIndex;
				int othersideIndex;
				if(currentsegment.getDirection()==0) {
					currentsidedefIndex=currentlinedef.getRight();
					othersideIndex=currentlinedef.getLeft();
				} else {
					currentsidedefIndex=currentlinedef.getLeft();
					othersideIndex=currentlinedef.getRight();
				}
				subsectorSidedefs[i][j]=currentsidedefIndex;
				DSidedef currentSidedef=doomMap.getSidedefs().getSide(currentsidedefIndex);
				if(currentlinedef.getType()!=0 && othersideIndex!=-1) { // If this is a triggering linedef
					DSidedef otherSidedef=doomMap.getSidedefs().getSide(othersideIndex);
					if(currentlinedef.getTag()!=0) { // If the target is not 0
						for(int k=0;k<doomMap.getSectors().getNumElements();k++) {
							DSector taggedsector=doomMap.getSectors().getSector(k);
							if(taggedsector.getTag()==currentlinedef.getTag()) {
								sectorType[k]=currentlinedef.getType();
							}
						}
					} else {
						sectorType[currentSidedef.getSector()]=currentlinedef.getType();
					}
				}
				if(!currentSidedef.getMidTexture().equals("-")) {
					midWallTextures[currentsidedefIndex]=doomMap.getWadName()+"/"+currentSidedef.getMidTexture();
				} else {
					midWallTextures[currentsidedefIndex]="special/nodraw";
				}
				if(!currentSidedef.getHighTexture().equals("-")) {
					higherWallTextures[currentsidedefIndex]=doomMap.getWadName()+"/"+currentSidedef.getHighTexture();
				} else {
					higherWallTextures[currentsidedefIndex]="special/nodraw";
				}
				if(!currentSidedef.getLowTexture().equals("-")) {
					lowerWallTextures[currentsidedefIndex]=doomMap.getWadName()+"/"+currentSidedef.getLowTexture();
				} else {
					lowerWallTextures[currentsidedefIndex]="special/nodraw";
				}
				// Sometimes a subsector seems to belong to more than one sector. I don't know why.
				if(subsectorSectors[i]!=-1 && currentSidedef.getSector()!=subsectorSectors[i]) {
					Window.println("WARNING: Subsector "+i+" has sides defining different sectors!",Window.VERBOSITY_WARNINGS);
					Window.println("This is probably nothing to worry about, but something might be wrong (wrong floor/cieling height)",Window.VERBOSITY_WARNINGS);
				} else {
					subsectorSectors[i]=currentSidedef.getSector();
				}
			}
			
			// Third, create a few brushes out of the geometry.
			MAPBrush cielingBrush=new MAPBrush(numBrshs++, 0, false);
			MAPBrush floorBrush=new MAPBrush(numBrshs++, 0, false);
			MAPBrush midBrush=new MAPBrush(numBrshs++, 0, false);
			DSector currentSector=doomMap.getSectors().getSector(subsectorSectors[i]);
			
			Vector3D[] roofPlane=new Vector3D[3];
			double[] roofTexS=new double[3];
			double[] roofTexT=new double[3];
			roofPlane[0]=new Vector3D(0, Window.getPlanePointCoef(), ZMax);
			roofPlane[1]=new Vector3D(Window.getPlanePointCoef(), Window.getPlanePointCoef(), ZMax);
			roofPlane[2]=new Vector3D(Window.getPlanePointCoef(), 0, ZMax);
			roofTexS[0]=1;
			roofTexT[1]=-1;
			MAPBrushSide roof=new MAPBrushSide(roofPlane, doomMap.getWadName()+"/"+currentSector.getCielingTexture(), roofTexS, 0, roofTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			
			Vector3D[] cileingPlane=new Vector3D[3];
			double[] cileingTexS=new double[3];
			double[] cileingTexT=new double[3];
			cileingPlane[0]=new Vector3D(0, 0, currentSector.getCielingHeight());
			cileingPlane[1]=new Vector3D(Window.getPlanePointCoef(), 0, currentSector.getCielingHeight());
			cileingPlane[2]=new Vector3D(Window.getPlanePointCoef(), Window.getPlanePointCoef(), currentSector.getCielingHeight());
			cileingTexS[0]=1;
			cileingTexT[1]=-1;
			MAPBrushSide cieling=new MAPBrushSide(cileingPlane, doomMap.getWadName()+"/"+currentSector.getCielingTexture(), cileingTexS, 0, cileingTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			
			Vector3D[] floorPlane=new Vector3D[3];
			double[] floorTexS=new double[3];
			double[] floorTexT=new double[3];
			floorPlane[0]=new Vector3D(0, Window.getPlanePointCoef(), currentSector.getFloorHeight());
			floorPlane[1]=new Vector3D(Window.getPlanePointCoef(), Window.getPlanePointCoef(), currentSector.getFloorHeight());
			floorPlane[2]=new Vector3D(Window.getPlanePointCoef(), 0, currentSector.getFloorHeight());
			floorTexS[0]=1;
			floorTexT[1]=-1;
			MAPBrushSide floor=new MAPBrushSide(floorPlane, doomMap.getWadName()+"/"+currentSector.getFloorTexture(), floorTexS, 0, floorTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);

			Vector3D[] foundationPlane=new Vector3D[3];
			double[] foundationTexS=new double[3];
			double[] foundationTexT=new double[3];
			foundationPlane[0]=new Vector3D(0, 0, ZMin);
			foundationPlane[1]=new Vector3D(Window.getPlanePointCoef(), 0, ZMin);
			foundationPlane[2]=new Vector3D(Window.getPlanePointCoef(), Window.getPlanePointCoef(), ZMin);
			foundationTexS[0]=1;
			foundationTexT[1]=-1;
			MAPBrushSide foundation=new MAPBrushSide(foundationPlane, doomMap.getWadName()+"/"+currentSector.getFloorTexture(), foundationTexS, 0, foundationTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			
			cielingBrush.add(cieling);
			cielingBrush.add(roof);
			
			floorBrush.add(floor);
			floorBrush.add(foundation);

			int nextNode=ssparents[i];
			boolean leftSide=ssIsLeft[i];

			for(int j=0;j<subsectorSidedefs[i].length;j++) { // Iterate through the sidedefs defined by segments of this subsector
				DSegment currentseg=doomMap.getSegments().getSegment(currentsubsector.getFirstSeg()+j);
				Vector3D start=doomMap.getVertices().getVertex(currentseg.getStartVertex());
				Vector3D end=doomMap.getVertices().getVertex(currentseg.getEndVertex());
				DLinedef currentLinedef=doomMap.getLinedefs().getLinedef(currentseg.getLinedef());
				
				Vector3D[] plane=new Vector3D[3];
				double[] texS=new double[3];
				double[] texT=new double[3];
				plane[0]=new Vector3D(start.getX(), start.getY(), ZMin);
				plane[1]=new Vector3D(end.getX(), end.getY(), ZMin);
				plane[2]=new Vector3D(end.getX(), end.getY(), ZMax);
				
				double sideLength=Math.sqrt(Math.pow(start.getX()-end.getX(), 2) + Math.pow(start.getY()-end.getY(),2));
				
				texS[0]=(start.getX()-end.getX())/sideLength;
				texS[1]=(start.getY()-end.getY())/sideLength;
				texS[2]=0;
				texT[0]=0;
				texT[1]=0;
				texT[2]=1;
				MAPBrushSide low=new MAPBrushSide(plane, lowerWallTextures[subsectorSidedefs[i][j]], texS, 0, texT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
				MAPBrushSide high=new MAPBrushSide(plane, higherWallTextures[subsectorSidedefs[i][j]], texS, 0, texT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
				MAPBrushSide mid;
				
				if(currentLinedef.isOneSided()) {
					MAPBrush outsideBrush=null;
					if(midWallTextures[subsectorSidedefs[i][j]]!="special/nodraw") {
						outsideBrush = createFaceBrush(midWallTextures[subsectorSidedefs[i][j]], plane[0], plane[2]);
					} else { // If the outside sidedef uses no texture
						for(int k=0;k<subsectorSidedefs[i].length;k++) { // That is BULL SHIT! Find a side of the subsector that uses one
							if(midWallTextures[subsectorSidedefs[i][k]]!="special/nodraw") {
								outsideBrush = createFaceBrush(midWallTextures[subsectorSidedefs[i][k]], plane[0], plane[2]);
							}
						}
						if(outsideBrush==null) { // If no side of the subsector uses one, then fuck.
							outsideBrush = createFaceBrush("special/nodraw", plane[0], plane[2]);
						}
					}
					world.addBrush(outsideBrush);
					mid=new MAPBrushSide(plane, "special/nodraw", texS, 0, texT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
				} else {
					mid=new MAPBrushSide(plane, midWallTextures[subsectorSidedefs[i][j]], texS, 0, texT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
				}
				
				cielingBrush.add(high);
				midBrush.add(mid);
				floorBrush.add(low);
			}
			// Now need to add the data from node subdivisions. Neither segments nor nodes
			// will completely define a usable brush, but both of them together will.
			do {
				DNode currentNode=doomMap.getNodes().getNode(nextNode);
				Vector3D start;
				Vector3D end;
				if(leftSide) {
					start=currentNode.getVecHead().add(currentNode.getVecTail());
					end=currentNode.getVecHead();
				} else {
					start=currentNode.getVecHead();
					end=currentNode.getVecHead().add(currentNode.getVecTail());
				}
				
				Vector3D[] plane=new Vector3D[3];
				double[] texS=new double[3];
				double[] texT=new double[3];
				// This is somehow always correct. And I'm okay with that.
				plane[0]=new Vector3D(start.getX(), start.getY(), ZMin);
				plane[1]=new Vector3D(end.getX(), end.getY(), ZMin);
				plane[2]=new Vector3D(start.getX(), start.getY(), ZMax);
				
				double sideLength=Math.sqrt(Math.pow(start.getX()-end.getX(), 2) + Math.pow(start.getY()-end.getY(),2));
				
				texS[0]=(start.getX()-end.getX())/sideLength;
				texS[1]=(start.getY()-end.getY())/sideLength;
				texS[2]=0;
				texT[0]=0;
				texT[1]=0;
				texT[2]=1;
				MAPBrushSide low=new MAPBrushSide(plane, lowerWallTextures[subsectorSidedefs[i][0]], texS, 0, texT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
				MAPBrushSide high=new MAPBrushSide(plane, higherWallTextures[subsectorSidedefs[i][0]], texS, 0, texT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
				MAPBrushSide mid=new MAPBrushSide(plane, midWallTextures[subsectorSidedefs[i][0]], texS, 0, texT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
				
				cielingBrush.add(high);
				midBrush.add(mid);
				floorBrush.add(low);
				
				leftSide=nodeIsLeft[nextNode];
				nextNode=nodeparents[nextNode];
			} while(nextNode!=-1);
			// Now we need to get rid of all the sides that aren't used. Get a list of
			// the useless sides from one brush, and delete those sides from all of them,
			// since they all have the same sides.
			if(!Window.dontCullIsSelected()) {
				int[] badSides=GenericMethods.findUnusedPlanes(cielingBrush);
				// Need to iterate backward, since these lists go from low indices to high, and
				// the index of all subsequent items changes when something before it is removed.
				if(cielingBrush.getNumSides()-badSides.length<4) {
					Window.println("WARNING: Plane cull returned less than 4 sides for subsector "+i,Window.VERBOSITY_WARNINGS);
				} else {
					for(int j=badSides.length-1;j>-1;j--) {
						cielingBrush.delete(badSides[j]);
						floorBrush.delete(badSides[j]);
					}
				}
			}
			world.addBrush(floorBrush);
			world.addBrush(cielingBrush);
			/*boolean containsMiddle=false; // Need to figure out how to determine this. As it is, no middle sides will come out.
			if(containsMiddle && currentSector.getCielingHeight() > currentSector.getFloorHeight()) {
				Entity middleEnt=new Entity("func_illusionary");
				Vector3D[] topPlane=new Vector3D[3];
				double[] topTexS=new double[3];
				double[] topTexT=new double[3];
				topPlane[0]=new Vector3D(0, Window.getPlanePointCoef(), currentSector.getCielingHeight());
				topPlane[1]=new Vector3D(Window.getPlanePointCoef(), Window.getPlanePointCoef(), currentSector.getCielingHeight());
				topPlane[2]=new Vector3D(Window.getPlanePointCoef(), 0, currentSector.getCielingHeight());
				topTexS[0]=1;
				topTexT[1]=-1;
				MAPBrushSide top=new MAPBrushSide(topPlane, doomMap.getWadName()+"/"+currentSector.getFloorTexture(), topTexS, 0, topTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
	
				Vector3D[] bottomPlane=new Vector3D[3];
				double[] bottomTexS=new double[3];
				double[] bottomTexT=new double[3];
				bottomPlane[0]=new Vector3D(0, 0, currentSector.getFloorHeight());
				bottomPlane[1]=new Vector3D(Window.getPlanePointCoef(), 0, currentSector.getFloorHeight());
				bottomPlane[2]=new Vector3D(Window.getPlanePointCoef(), Window.getPlanePointCoef(), currentSector.getFloorHeight());
				bottomTexS[0]=1;
				bottomTexT[1]=-1;
				MAPBrushSide bottom=new MAPBrushSide(bottomPlane, doomMap.getWadName()+"/"+currentSector.getFloorTexture(), bottomTexS, 0, bottomTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);

				midBrush.add(top);
				midBrush.add(bottom);
				midBrush.add(outsideLeft);
				midBrush.add(outsideRight);
				midBrush.add(outsideNear);
				midBrush.add(outsideFar);
				midBrush=GenericMethods.cullUnusedPlanes(midBrush);
				
				middleEnt.addBrush(midBrush);
				mapFile.add(middleEnt);
			}*/
			Window.setProgress(jobnum, i+1, doomMap.getSubSectors().getNumElements(), "Decompiling...");
		}
		
		Window.setProgress(jobnum, 1, 1, "Saving...");
		MAPMaker.outputMaps(mapFile, doomMap.getMapName(), doomMap.getFolder()+doomMap.getWadName(), doomMap.VERSION);
		Date end=new Date();
		Window.println("Time taken: "+(end.getTime()-begin.getTime())+"ms"+(char)0x0D+(char)0x0A,Window.VERBOSITY_ALWAYS);
	}
	
	// createFaceBrush(String, Vector3D, Vector3D)
	// This creates a rectangular brush. The String is assumed to be a texture for a face, and
	// the two vectors are a bounding box to create a plane with (mins-maxs). This can be expanded
	// later to include passing of texture scaling and positioning vectors as well, but this is
	// all I need right now.
	public MAPBrush createFaceBrush(String texture, Vector3D mins, Vector3D maxs) {
		Window.println("Creating brush for face with "+texture,Window.VERBOSITY_BRUSHCREATION);
		MAPBrush newBrush = new MAPBrush(numBrshs++, 0, false);
		numBrshs++;
		Vector3D[][] planes=new Vector3D[6][3]; // Six planes for a cube brush, three vertices for each plane
		double[][] texS=new double[6][3];
		double[][] texT=new double[6][3];
		
		double sideLengthXY=Math.sqrt(Math.pow(mins.getX()-maxs.getX(), 2) + Math.pow(mins.getY()-maxs.getY(),2));
		Vector3D diffVec1 = new Vector3D(mins.getX(), mins.getY(), maxs.getZ()).subtract(mins);
		Vector3D diffVec2 = new Vector3D(maxs.getX(), maxs.getY(), mins.getZ()).subtract(mins);
		Vector3D cross = Vector3D.crossProduct(diffVec2, diffVec1);
		cross.normalize();
		
		// Face
		planes[0][0]=new Vector3D(mins.getX(), mins.getY(), maxs.getZ());
		planes[0][1]=new Vector3D(maxs.getX(), maxs.getY(), mins.getZ());
		planes[0][2]=mins;
		texS[0][0]=(mins.getX()-maxs.getX())/sideLengthXY;
		texS[0][1]=(mins.getY()-maxs.getY())/sideLengthXY;
		texT[0][2]=-1;
		// Far
		planes[1][0]=new Vector3D(mins.getX(), mins.getY(), maxs.getZ()).subtract(cross);
		planes[1][1]=mins.subtract(cross);
		planes[1][2]=new Vector3D(maxs.getX(), maxs.getY(), mins.getZ()).subtract(cross);
		texS[1][0]=texS[0][0];
		texS[1][1]=texS[0][1];
		texT[1][2]=-1;
		// Top
		planes[2][0]=new Vector3D(mins.getX(), mins.getY(), maxs.getZ());
		planes[2][1]=new Vector3D(mins.getX(), mins.getY(), maxs.getZ()).subtract(cross);
		planes[2][2]=maxs;
		texS[2][0]=1;
		texT[2][1]=1;
		// Bottom
		planes[3][0]=mins;
		planes[3][1]=new Vector3D(maxs.getX(), maxs.getY(), mins.getZ());
		planes[3][2]=new Vector3D(maxs.getX(), maxs.getY(), mins.getZ()).subtract(cross);
		texS[3][0]=1;
		texT[3][1]=1;
		// Left
		planes[4][0]=mins;
		planes[4][1]=mins.subtract(cross);
		planes[4][2]=new Vector3D(mins.getX(), mins.getY(), maxs.getZ());
		texS[4][0]=texS[0][1];
		texS[4][1]=texS[0][0];
		texT[4][2]=1;
		// Right
		planes[5][0]=maxs;
		planes[5][1]=maxs.subtract(cross);
		planes[5][2]=new Vector3D(maxs.getX(), maxs.getY(), mins.getZ());
		texS[5][0]=texS[0][1];
		texS[5][1]=texS[0][0];
		texT[5][2]=1;

		MAPBrushSide front=new MAPBrushSide(planes[0], texture, texS[0], 0, texT[0], 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
		newBrush.add(front);
		for(int i=1;i<6;i++) {
			newBrush.add(new MAPBrushSide(planes[i], "special/nodraw", texS[i], 0, texT[i], 0, 0, 1, 1, 32, "wld_lightmap", 16, 0));
		}
		
		return newBrush;
	}
}
