// WADDecompiler class

// Handles the actual decompilation.

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
	public Entities decompile() throws java.io.IOException, java.lang.InterruptedException {
		Window.println("Decompiling...",Window.VERBOSITY_ALWAYS);
		Window.println(doomMap.getMapName(),Window.VERBOSITY_ALWAYS);
		
		mapFile=new Entities();
		Entity world = new Entity("worldspawn");
		world.setAttribute("mapversion", "510");
		mapFile.add(world);
		
		String[] lowerWallTextures=new String[doomMap.getSidedefs().size()];
		String[] midWallTextures=new String[doomMap.getSidedefs().size()];
		String[] higherWallTextures=new String[doomMap.getSidedefs().size()];
		
		short[] sectorTag=new short[doomMap.getSectors().size()];
		String playerStartOrigin="";
		
		// Since Doom relied on sectors to define a cieling and floor height, and nothing else,
		// need to find the minimum and maximum used Z values. This is because the Doom engine
		// is only a pseudo-3D engine. For all it cares, the cieling and floor extend to their
		// respective infinities. For a GC/Hammer map, however, this cannot be the case.
		int ZMin=32767;  // Even though the values in the map will never exceed these, use ints here to avoid
		int ZMax=-32768; // overflows, in case the map DOES go within 32 units of these values.
		for(int i=0;i<doomMap.getSectors().size();i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while finding min and max Z values.");
			}
			DSector currentSector=doomMap.getSectors().getElement(i);
			sectorTag[i]=currentSector.getTag();
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
		int[] nodeparents = new int[doomMap.getNodes().size()];
		boolean[] nodeIsLeft = new boolean[doomMap.getNodes().size()];
		
		for(int i=0;i<doomMap.getNodes().size();i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while iterating through binary tree.");
			}
			nodeparents[i]=-1; // There should only be one node left with -1 as a parent. This SHOULD be the root.
			for(int j=0;j<doomMap.getNodes().size();j++) {
				if(doomMap.getNodes().getElement(j).getChild1() == i) {
					nodeparents[i]=j;
					break;
				} else {
					if(doomMap.getNodes().getElement(j).getChild2() == i) {
						nodeparents[i]=j;
						nodeIsLeft[i]=true;
						break;
					}
				}
			}
		}
		
		// Keep a list of what subsectors belong to which sector
		int[] subsectorSectors = new int[doomMap.getSubSectors().size()];
		// Keep a list of what sidedefs belong to what subsector as well
		int[][] subsectorSidedefs = new int[doomMap.getSubSectors().size()][];
		
		short[][] sideDefShifts=new short[2][doomMap.getSidedefs().size()];
		
		// Figure out what sector each subsector belongs to, and what node is its parent.
		// Depending on sector "tags" this will help greatly in creation of brushbased entities,
		// and also helps in finding subsector floor and cieling heights.
		int[] ssparents = new int[doomMap.getSubSectors().size()];
		boolean[] ssIsLeft = new boolean[doomMap.getSubSectors().size()];
		for(int i=0;i<doomMap.getSubSectors().size();i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while processing subsector "+i+".");
			}
			Window.println("Populating texture lists for subsector "+i,Window.VERBOSITY_BRUSHCREATION);
			// First, find the subsector's parent and whether it is the left or right child.
			ssparents[i]=-1; // No subsector should have a -1 in here
			for(int j=0;j<doomMap.getNodes().size();j++) {
				// When a node references a subsector, it is not referenced by negative
				// index, as future BSP versions do. The bits 0-14 ARE the index, and
				// bit 15 (which is the sign bit in two's compliment math) determines
				// whether or not it is a node or subsector. Therefore, we need to add
				// 2^15 to the number to produce the actual index.
				if(doomMap.getNodes().getElement(j).getChild1()+32768 == i) {
					ssparents[i]=j;
					break;
				} else {
					if(doomMap.getNodes().getElement(j).getChild2()+32768 == i) {
						ssparents[i]=j;
						ssIsLeft[i]=true;
						break;
					}
				}
			}
			
			// Second, figure out what sector a subsector belongs to, and the type of sector it is.
			subsectorSectors[i]=-1;
			Edge currentsubsector=doomMap.getSubSectors().getElement(i);
			subsectorSidedefs[i]=new int[currentsubsector.getNumSegs()];
			for(int j=0;j<currentsubsector.getNumSegs();j++) { // For each segment the subsector references
				DSegment currentsegment=doomMap.getSegments().getElement(currentsubsector.getFirstSeg()+j);
				DLinedef currentlinedef=doomMap.getLinedefs().getElement(currentsegment.getLinedef());
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
				DSidedef currentSidedef=doomMap.getSidedefs().getElement(currentsidedefIndex);
				if(currentlinedef.isOneSided()) {
					// A one-sided linedef should always be like this
					midWallTextures[currentsidedefIndex]=doomMap.getWadName()+"/"+currentSidedef.getMidTexture();
					higherWallTextures[currentsidedefIndex]="special/nodraw";
					lowerWallTextures[currentsidedefIndex]="special/nodraw";
					sideDefShifts[X][currentsidedefIndex]=currentSidedef.getOffsetX();
					sideDefShifts[Y][currentsidedefIndex]=currentSidedef.getOffsetY();
				} else {
					// I don't really get why I need to apply these textures to the other side. But if it works I won't argue...
					if(!currentSidedef.getMidTexture().equals("-")) {
						midWallTextures[othersideIndex]=doomMap.getWadName()+"/"+currentSidedef.getMidTexture();
					} else {
						midWallTextures[othersideIndex]="special/nodraw";
					}
					if(!currentSidedef.getHighTexture().equals("-")) {
						higherWallTextures[othersideIndex]=doomMap.getWadName()+"/"+currentSidedef.getHighTexture();
					} else {
						higherWallTextures[othersideIndex]="special/nodraw";
					}
					if(!currentSidedef.getLowTexture().equals("-")) {
						lowerWallTextures[othersideIndex]=doomMap.getWadName()+"/"+currentSidedef.getLowTexture();
					} else {
						lowerWallTextures[othersideIndex]="special/nodraw";
					}
					sideDefShifts[X][othersideIndex]=currentSidedef.getOffsetX();
					sideDefShifts[Y][othersideIndex]=currentSidedef.getOffsetY();
				}
				// Sometimes a subsector seems to belong to more than one sector. Only the reference in the first seg is true.
				if(j==0) {
					subsectorSectors[i]=currentSidedef.getSector();
				}
			}
		}
		boolean[] linedefFlagsDealtWith=new boolean[doomMap.getLinedefs().size()];
		boolean[] linedefSpecialsDealtWith=new boolean[doomMap.getLinedefs().size()];
		
		MAPBrush[][] sectorFloorBrushes=new MAPBrush[doomMap.getSectors().size()][0];
		MAPBrush[][] sectorCielingBrushes=new MAPBrush[doomMap.getSectors().size()][0];
		
		// For one-sided linedefs referenced by more than one subsector
		boolean[] outsideBrushAlreadyCreated=new boolean[doomMap.getLinedefs().size()];
		
		for(int i=0;i<doomMap.getSubSectors().size();i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while creating brushes for subsector "+i+".");
			}
			Window.println("Creating brushes for subsector "+i,Window.VERBOSITY_BRUSHCREATION);
			
			Edge currentsubsector=doomMap.getSubSectors().getElement(i);
			
			// Third, create a few brushes out of the geometry.
			MAPBrush cielingBrush=new MAPBrush(numBrshs++, 0, false);
			MAPBrush floorBrush=new MAPBrush(numBrshs++, 0, false);
			MAPBrush midBrush=new MAPBrush(numBrshs++, 0, false);
			MAPBrush damageBrush=new MAPBrush(numBrshs++, 0, false);
			DSector currentSector=doomMap.getSectors().getElement(subsectorSectors[i]);
			
			Vector3D[] roofPlane=new Vector3D[3];
			double[] roofTexS=new double[3];
			double[] roofTexT=new double[3];
			roofPlane[0]=new Vector3D(0, Window.getPlanePointCoef(), ZMax);
			roofPlane[1]=new Vector3D(Window.getPlanePointCoef(), Window.getPlanePointCoef(), ZMax);
			roofPlane[2]=new Vector3D(Window.getPlanePointCoef(), 0, ZMax);
			roofTexS[0]=1;
			roofTexT[1]=-1;
			
			Vector3D[] cileingPlane=new Vector3D[3];
			double[] cileingTexS=new double[3];
			double[] cileingTexT=new double[3];
			cileingPlane[0]=new Vector3D(0, 0, currentSector.getCielingHeight());
			cileingPlane[1]=new Vector3D(Window.getPlanePointCoef(), 0, currentSector.getCielingHeight());
			cileingPlane[2]=new Vector3D(Window.getPlanePointCoef(), Window.getPlanePointCoef(), currentSector.getCielingHeight());
			cileingTexS[0]=1;
			cileingTexT[1]=-1;
			
			Vector3D[] floorPlane=new Vector3D[3];
			double[] floorTexS=new double[3];
			double[] floorTexT=new double[3];
			floorPlane[0]=new Vector3D(0, Window.getPlanePointCoef(), currentSector.getFloorHeight());
			floorPlane[1]=new Vector3D(Window.getPlanePointCoef(), Window.getPlanePointCoef(), currentSector.getFloorHeight());
			floorPlane[2]=new Vector3D(Window.getPlanePointCoef(), 0, currentSector.getFloorHeight());
			floorTexS[0]=1;
			floorTexT[1]=-1;

			Vector3D[] foundationPlane=new Vector3D[3];
			double[] foundationTexS=new double[3];
			double[] foundationTexT=new double[3];
			foundationPlane[0]=new Vector3D(0, 0, ZMin);
			foundationPlane[1]=new Vector3D(Window.getPlanePointCoef(), 0, ZMin);
			foundationPlane[2]=new Vector3D(Window.getPlanePointCoef(), Window.getPlanePointCoef(), ZMin);
			foundationTexS[0]=1;
			foundationTexT[1]=-1;

			Vector3D[] topPlane=new Vector3D[3];
			double[] topTexS=new double[3];
			double[] topTexT=new double[3];
			topPlane[0]=new Vector3D(0, Window.getPlanePointCoef(), currentSector.getCielingHeight());
			topPlane[1]=new Vector3D(Window.getPlanePointCoef(), Window.getPlanePointCoef(), currentSector.getCielingHeight());
			topPlane[2]=new Vector3D(Window.getPlanePointCoef(), 0, currentSector.getCielingHeight());
			topTexS[0]=1;
			topTexT[1]=-1;
	
			Vector3D[] bottomPlane=new Vector3D[3];
			double[] bottomTexS=new double[3];
			double[] bottomTexT=new double[3];
			bottomPlane[0]=new Vector3D(0, 0, currentSector.getFloorHeight());
			bottomPlane[1]=new Vector3D(Window.getPlanePointCoef(), 0, currentSector.getFloorHeight());
			bottomPlane[2]=new Vector3D(Window.getPlanePointCoef(), Window.getPlanePointCoef(), currentSector.getFloorHeight());
			bottomTexS[0]=1;
			bottomTexT[1]=-1;

			int nextNode=ssparents[i];
			boolean leftSide=ssIsLeft[i];

			for(int j=0;j<currentsubsector.getNumSegs();j++) { // Iterate through the sidedefs defined by segments of this subsector
				DSegment currentseg=doomMap.getSegments().getElement(currentsubsector.getFirstSeg()+j);
				Vector3D start=doomMap.getVertices().getElement(currentseg.getStartVertex()).getVertex();
				Vector3D end=doomMap.getVertices().getElement(currentseg.getEndVertex()).getVertex();
				DLinedef currentLinedef=doomMap.getLinedefs().getElement((int)currentseg.getLinedef());
				
				Vector3D[] plane=new Vector3D[3];
				double[] texS=new double[3];
				double[] texT=new double[3];
				plane[0]=new Vector3D(start.getX(), start.getY(), ZMin);
				plane[1]=new Vector3D(end.getX(), end.getY(), ZMin);
				plane[2]=new Vector3D(end.getX(), end.getY(), ZMax);
				
				Vector3D linestart=new Vector3D(doomMap.getVertices().getElement(currentLinedef.getStart()).getVertex().getX(), doomMap.getVertices().getElement(currentLinedef.getStart()).getVertex().getY(), ZMin);
				Vector3D lineend=new Vector3D(doomMap.getVertices().getElement(currentLinedef.getEnd()).getVertex().getX(), doomMap.getVertices().getElement(currentLinedef.getEnd()).getVertex().getY(), ZMax);
				
				double sideLength=Math.sqrt(Math.pow(start.getX()-end.getX(), 2) + Math.pow(start.getY()-end.getY(),2));
				
				boolean upperUnpegged=!((currentLinedef.getFlags()[0] & ((byte)1 << 3)) == 0);
				boolean lowerUnpegged=!((currentLinedef.getFlags()[0] & ((byte)1 << 4)) == 0);
				
				texS[0]=(start.getX()-end.getX())/sideLength;
				texS[1]=(start.getY()-end.getY())/sideLength;
				texS[2]=0;
				texT[0]=0;
				texT[1]=0;
				texT[2]=-1;
				
				double SShift=sideDefShifts[X][subsectorSidedefs[i][j]]-(texS[0]*end.getX())-(texS[1]*end.getY());
				double lowTShift=0;
				double highTShift=0;
				if(!currentLinedef.isOneSided()) {
					DSector otherSideSector;
					if(doomMap.getSectors().getElement(doomMap.getSidedefs().getElement(currentLinedef.getLeft()).getSector())==currentSector) {
						otherSideSector=doomMap.getSectors().getElement(doomMap.getSidedefs().getElement(currentLinedef.getRight()).getSector());
					} else {
						otherSideSector=doomMap.getSectors().getElement(doomMap.getSidedefs().getElement(currentLinedef.getLeft()).getSector());
					}
					if(lowerUnpegged) {
						lowTShift=otherSideSector.getCielingHeight();
					} else {
						lowTShift=currentSector.getFloorHeight();
					}
					if(upperUnpegged) {
						highTShift=otherSideSector.getCielingHeight();
					} else {
						highTShift=currentSector.getCielingHeight();
					}
					lowTShift+=sideDefShifts[Y][subsectorSidedefs[i][j]];
					highTShift+=sideDefShifts[Y][subsectorSidedefs[i][j]];
				}
				MAPBrushSide low=new MAPBrushSide(plane, lowerWallTextures[subsectorSidedefs[i][j]], texS, SShift, texT, lowTShift, 0, 1, 1, 0, "wld_lightmap", 16, 0);
				MAPBrushSide high=new MAPBrushSide(plane, higherWallTextures[subsectorSidedefs[i][j]], texS, SShift, texT, highTShift, 0, 1, 1, 0, "wld_lightmap", 16, 0);
				MAPBrushSide mid;
				MAPBrushSide damage=new MAPBrushSide(plane, "special/trigger", texS, 0, texT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
				
				if(currentLinedef.isOneSided()) {
					if(!outsideBrushAlreadyCreated[currentseg.getLinedef()]) {
						outsideBrushAlreadyCreated[currentseg.getLinedef()]=true;
						double highestCieling=currentSector.getCielingHeight();
						double lowestFloor=currentSector.getFloorHeight();
						if(currentSector.getTag()!=0) {
							double temp=getHighestNeighborCielingHeight(subsectorSectors[i]);
							if(temp>highestCieling) {
								highestCieling=temp;
							}
							temp=getLowestNeighborFloorHeight(subsectorSectors[i]);
							if(temp<lowestFloor) {
								lowestFloor=temp;
							}
						}
						MAPBrush outsideBrush=null;
						if(lowestFloor<=highestCieling) {
							outsideBrush = MAPBrush.createFaceBrush(midWallTextures[subsectorSidedefs[i][j]], "special/nodraw", new Vector3D(linestart.getX(), linestart.getY(), ZMin), new Vector3D(lineend.getX(), lineend.getY(), ZMax), sideDefShifts[X][subsectorSidedefs[i][j]], sideDefShifts[Y][subsectorSidedefs[i][j]], lowerUnpegged, currentSector.getCielingHeight(), currentSector.getFloorHeight());
						} else {
							outsideBrush = MAPBrush.createFaceBrush(midWallTextures[subsectorSidedefs[i][j]], "special/nodraw", new Vector3D(linestart.getX(), linestart.getY(), lowestFloor), new Vector3D(lineend.getX(), lineend.getY(), highestCieling), sideDefShifts[X][subsectorSidedefs[i][j]], sideDefShifts[Y][subsectorSidedefs[i][j]], lowerUnpegged, currentSector.getCielingHeight(), currentSector.getFloorHeight());
						}
						world.addBrush(outsideBrush);
					}
					mid=new MAPBrushSide(plane, "special/nodraw", texS, 0, texT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
				} else {
					double midTShift=sideDefShifts[Y][subsectorSidedefs[i][j]];
					if(lowerUnpegged) {
						midTShift+=currentSector.getFloorHeight();
					} else {
						midTShift+=currentSector.getCielingHeight();
					}
					mid=new MAPBrushSide(plane, midWallTextures[subsectorSidedefs[i][j]], texS, SShift, texT, midTShift, 0, 1, 1, 0, "wld_lightmap", 16, 0);
					if(!linedefFlagsDealtWith[currentseg.getLinedef()]) {
						linedefFlagsDealtWith[currentseg.getLinedef()]=true;
						if(!((currentLinedef.getFlags()[0] & ((byte)1 << 0)) == 0)) { // Flag 0x0001 indicates "solid" but doesn't block bullets. It is assumed for all one-sided.
							MAPBrush solidBrush = MAPBrush.createFaceBrush("special/clip", "special/clip", linestart, lineend,0,0, false,0,0);
							world.addBrush(solidBrush);
						} else {
							if(!((currentLinedef.getFlags()[0] & ((byte)1 << 1)) == 0)) { // Flag 0x0002 indicates "monster clip".
								MAPBrush solidBrush = MAPBrush.createFaceBrush("special/enemyclip", "special/enemyclip", linestart, lineend,0,0, false,0,0);
								world.addBrush(solidBrush);
							}
						}
					}
					int othersideindex=-1;
					if(currentLinedef.getRight()==subsectorSidedefs[i][j]) {
						othersideindex=currentLinedef.getLeft();
					}
					if(currentLinedef.getLeft()==subsectorSidedefs[i][j]) {
						othersideindex=currentLinedef.getRight();
					}
					DSidedef otherside=doomMap.getSidedefs().getElement(othersideindex);
					if(currentLinedef.getAction()!=0 && !linedefSpecialsDealtWith[currentseg.getLinedef()]) {
						linedefSpecialsDealtWith[currentseg.getLinedef()]=true;
						Entity trigger=null;
						MAPBrush triggerBrush = MAPBrush.createFaceBrush("special/trigger", "special/trigger", linestart, lineend,0,0, false,0,0);
						if(doomMap.getVersion()==DoomMap.TYPE_HEXEN) {
							boolean[] bitset=new boolean[16];
							for(int k=0;k<8;k++) {
								bitset[k]=!((currentLinedef.getFlags()[0] & ((byte)k << 1)) == 0);
							}
							for(int k=0;k<8;k++) {
								bitset[k+8]=!((currentLinedef.getFlags()[1] & ((byte)k << 1)) == 0);
							}
							if(bitset[10] && bitset[11] && !bitset[12]) { // Triggered when "Used" by player
								trigger=new Entity("func_button");
								trigger.setAttribute("spawnflags", "1");
								if(bitset[9]) {
									trigger.setAttribute("wait", "1");
								} else {
									trigger.setAttribute("wait", "-1");
								}
							} else {
								if(bitset[9]) { // Can be activated more than once
									trigger=new Entity("trigger_multiple");
									trigger.setAttribute("wait", "1");
								} else {
									trigger=new Entity("trigger_once");
								}
							}
							switch(currentLinedef.getAction()) {
								case 21: // Floor lower to lowest surrounding floor
								case 22: // Floor lower to next lowest surrounding floor
									if(currentLinedef.getArguments()[0]!=0) {
										trigger.setAttribute("target", "sector"+currentLinedef.getArguments()[0]+"lowerfloor");
									} else {
										trigger.setAttribute("target", "sectornum"+otherside.getSector()+"lowerfloor");
									}
									break;
								case 24: // Floor raise to highest surrounding floor
								case 25: // Floor raise to next highest surrounding floor
									if(currentLinedef.getArguments()[0]!=0) {
										trigger.setAttribute("target", "sector"+currentLinedef.getArguments()[0]+"raisefloor");
									} else {
										trigger.setAttribute("target", "sectornum"+otherside.getSector()+"raisefloor");
									}
									break;
								case 70: // Teleport
									trigger=new Entity("trigger_teleport");
									if(currentLinedef.getArguments()[0]!=0) {
										trigger.setAttribute("target", "teledest"+currentLinedef.getArguments()[0]);
									} else {
										trigger.setAttribute("target", "sector"+currentLinedef.getTag()+"teledest");
									}
									break;
								case 80: // Exec script
									// This is a toughie. I can't write a script-to-entity converter.
									trigger.setAttribute("target", "script"+currentLinedef.getArguments()[0]);
									trigger.setAttribute("arg0", ""+currentLinedef.getArguments()[2]);
									trigger.setAttribute("arg1", ""+currentLinedef.getArguments()[3]);
									trigger.setAttribute("arg2", ""+currentLinedef.getArguments()[4]);
									break;
								case 181: // PLANE_ALIGN
									trigger=null;
									if(!leftSide) {
										DSidedef getsector=doomMap.getSidedefs().getElement(currentLinedef.getLeft());
										DSector copyheight=doomMap.getSectors().getElement(getsector.getSector());
										short newHeight=copyheight.getFloorHeight();
										//floorPlane[0]=new Vector3D(0, Window.getPlanePointCoef(), 2000);
										//floorPlane[1]=new Vector3D(Window.getPlanePointCoef(), Window.getPlanePointCoef(), currentSector.getFloorHeight());
										//floorPlane[2]=new Vector3D(Window.getPlanePointCoef(), 0, currentSector.getFloorHeight());
									} else {
										linedefSpecialsDealtWith[currentseg.getLinedef()]=false;
									}
									break;
								default:
									trigger=null;
									break;
							}
						} else {
							switch(currentLinedef.getAction()) {
								case 1: // Use Door. open, wait, close
								case 31: // Use Door. Open, stay.
									trigger=new Entity("func_button");
									trigger.setAttribute("wait", "1");
									if(currentLinedef.getAction()==31) {
										trigger.setAttribute("wait", "-1");
									}
									trigger.setAttribute("spawnflags", "1");
									if(doomMap.getSectors().getElement(otherside.getSector()).getTag()==0) {
										trigger.setAttribute("target", "sectornum"+otherside.getSector()+"door");
										if(currentLinedef.getAction()==1) {
											sectorTag[otherside.getSector()]=-1;
										}
										if(currentLinedef.getAction()==31) {
											sectorTag[otherside.getSector()]=-2;
										}
									} else {
										trigger.setAttribute("target", "sector"+doomMap.getSectors().getElement(otherside.getSector()).getTag()+"door");
									}
									break;
								case 36: // Floor lower to 8 above next lowest neighboring sector
								case 38: // Floor lower to next lowest neighboring sector
									trigger=new Entity("trigger_once");
									trigger.setAttribute("target", "sector"+currentLinedef.getTag()+"lowerfloor");
									break;
								case 62: // Floor lower to next lowest neighboring sector, wait 4s, goes back up
									trigger=new Entity("func_button");
									trigger.setAttribute("target", "sector"+currentLinedef.getTag()+"vator");
									trigger.setAttribute("wait", "1");
									trigger.setAttribute("spawnflags", "1");
									break;
								case 63: // Door with button, retriggerable
								case 103: // Push button, one-time door open stay open
									trigger=new Entity("func_button");
									trigger.setAttribute("target", "sector"+currentLinedef.getTag()+"door");
									trigger.setAttribute("wait", "1");
									if(currentLinedef.getAction()==103) {
										trigger.setAttribute("wait", "-1");
									}
									trigger.setAttribute("spawnflags", "1");
									break;
								case 88: // Walkover retriggerable elevator trigger
									trigger=new Entity("trigger_multiple");
									trigger.setAttribute("target", "sector"+currentLinedef.getTag()+"vator");
									break;
								case 97: // Walkover retriggerable Teleport
									trigger=new Entity("trigger_teleport");
									trigger.setAttribute("target", "sector"+currentLinedef.getTag()+"teledest");
									break;
								case 109: // Walkover one-time door open stay open
									trigger=new Entity("trigger_once");
									trigger.setAttribute("target", "sector"+currentLinedef.getTag()+"door");
									break;
							}
						}
						if(trigger!=null) {
							trigger.addBrush(triggerBrush);
							mapFile.add(trigger);
						}
					}
				}
				
				cielingBrush.add(high);
				midBrush.add(mid);
				floorBrush.add(low);
				damageBrush.add(damage);
			}
			
			MAPBrushSide roof=new MAPBrushSide(roofPlane, "special/nodraw", roofTexS, 0, roofTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			MAPBrushSide cieling=new MAPBrushSide(cileingPlane, doomMap.getWadName()+"/"+currentSector.getCielingTexture(), cileingTexS, 0, cileingTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			MAPBrushSide floor=new MAPBrushSide(floorPlane, doomMap.getWadName()+"/"+currentSector.getFloorTexture(), floorTexS, 0, floorTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			MAPBrushSide foundation=new MAPBrushSide(foundationPlane, "special/nodraw", foundationTexS, 0, foundationTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			MAPBrushSide top=new MAPBrushSide(topPlane, "special/nodraw", topTexS, 0, topTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			MAPBrushSide bottom=new MAPBrushSide(bottomPlane, "special/nodraw", bottomTexS, 0, bottomTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			MAPBrushSide invertedFloor=new MAPBrushSide(Plane.flip(floorPlane), "special/trigger", floorTexS, 0, floorTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			MAPBrushSide damageTop=new MAPBrushSide(new Vector3D[] { floorPlane[0].add(new Vector3D(0,0,1)), floorPlane[1].add(new Vector3D(0,0,1)), floorPlane[2].add(new Vector3D(0,0,1)) }, "special/trigger", floorTexS, 0, floorTexT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
			damageBrush.add(damageTop);
			damageBrush.add(invertedFloor);

			midBrush.add(top);
			midBrush.add(bottom);
			
			cielingBrush.add(cieling);
			cielingBrush.add(roof);
			
			floorBrush.add(floor);
			floorBrush.add(foundation);
			
			// Now need to add the data from node subdivisions. Neither segments nor nodes
			// will completely define a usable brush, but both of them together will.
			do {
				DNode currentNode=doomMap.getNodes().getElement(nextNode);
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
				MAPBrushSide low=new MAPBrushSide(plane, "special/nodraw", texS, 0, texT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
				MAPBrushSide high=new MAPBrushSide(plane, "special/nodraw", texS, 0, texT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
				MAPBrushSide mid=new MAPBrushSide(plane, "special/nodraw", texS, 0, texT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
				MAPBrushSide damage=new MAPBrushSide(plane, "special/trigger", texS, 0, texT, 0, 0, 1, 1, 0, "wld_lightmap", 16, 0);
				
				cielingBrush.add(high);
				midBrush.add(mid);
				floorBrush.add(low);
				damageBrush.add(damage);
				
				leftSide=nodeIsLeft[nextNode];
				nextNode=nodeparents[nextNode];
			} while(nextNode!=-1);
			// Now we need to get rid of all the sides that aren't used. Get a list of
			// the useless sides from one brush, and delete those sides from all of them,
			// since they all have the same sides.
			int[] badSides=new int[0];
			if(!Window.dontCullIsSelected()) {
				badSides=MAPBrush.findUnusedPlanes(cielingBrush);
				// Need to iterate backward, since these lists go from low indices to high, and
				// the index of all subsequent items changes when something before it is removed.
				if(cielingBrush.getNumSides()-badSides.length<4) {
					Window.println("WARNING: Plane cull returned less than 4 sides for subsector "+i,Window.VERBOSITY_WARNINGS);
					badSides=new int[0];
				} else {
					for(int j=badSides.length-1;j>-1;j--) {
						cielingBrush.delete(badSides[j]);
						floorBrush.delete(badSides[j]);
					}
				}
			}
			
			MAPBrush[] newFloorList=new MAPBrush[sectorFloorBrushes[subsectorSectors[i]].length+1];
			MAPBrush[] newCielingList=new MAPBrush[sectorCielingBrushes[subsectorSectors[i]].length+1];
			for(int j=0;j<sectorFloorBrushes[subsectorSectors[i]].length;j++) {
				newFloorList[j]=sectorFloorBrushes[subsectorSectors[i]][j];
				newCielingList[j]=sectorCielingBrushes[subsectorSectors[i]][j];
			}
			newFloorList[newFloorList.length-1]=floorBrush;
			newCielingList[newCielingList.length-1]=cielingBrush;
			sectorFloorBrushes[subsectorSectors[i]]=newFloorList;
			sectorCielingBrushes[subsectorSectors[i]]=newCielingList;
			
			boolean containsMiddle=false;
			for(int j=0;j<midBrush.getNumSides();j++) {
				if(!midBrush.getSide(j).getTexture().equalsIgnoreCase("special/nodraw")) {
					containsMiddle=true;
					break;
				}
			}
			if(containsMiddle && currentSector.getCielingHeight() > currentSector.getFloorHeight()) {
				Entity middleEnt=new Entity("func_illusionary");
				if(midBrush.getNumSides()-badSides.length>=4) {
					for(int j=badSides.length-1;j>-1;j--) {
						midBrush.delete(badSides[j]);
					}
				}
				
				middleEnt.addBrush(midBrush);
				mapFile.add(middleEnt);
			}
			Entity hurtMe=new Entity("trigger_hurt");
			switch(currentSector.getType()) {
				case 4:
				case 11:
				case 16:
					hurtMe.setAttribute("dmg", "40");
					if(damageBrush.getNumSides()-badSides.length>=4) {
						for(int j=badSides.length-1;j>-1;j--) {
							damageBrush.delete(badSides[j]);
						}
					}
					hurtMe.addBrush(damageBrush);
					mapFile.add(hurtMe);
					break;
				case 5:
					hurtMe.setAttribute("dmg", "20");
					if(damageBrush.getNumSides()-badSides.length>=4) {
						for(int j=badSides.length-1;j>-1;j--) {
							damageBrush.delete(badSides[j]);
						}
					}
					hurtMe.addBrush(damageBrush);
					mapFile.add(hurtMe);
					break;
				case 7:
					hurtMe.setAttribute("dmg", "10");
					if(damageBrush.getNumSides()-badSides.length>=4) {
						for(int j=badSides.length-1;j>-1;j--) {
							damageBrush.delete(badSides[j]);
						}
					}
					hurtMe.addBrush(damageBrush);
					mapFile.add(hurtMe);
					break;
			}
			Window.setProgress(jobnum, i+1, doomMap.getSubSectors().size(), "Decompiling...");
		}
		
		// Add the brushes to the map, as world by default, or entities if they are supported.
		for(int i=0;i<doomMap.getSectors().size();i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while adding brushes to world from sector "+i+".");
			}
			boolean[] floorsUsed=new boolean[sectorFloorBrushes[i].length];
			boolean[] cielingsUsed=new boolean[sectorCielingBrushes[i].length];
			if(sectorTag[i]==0) {
				for(int j=0;j<sectorFloorBrushes[i].length;j++) {
					world.addBrush(sectorFloorBrushes[i][j]);
					floorsUsed[j]=true;
					world.addBrush(sectorCielingBrushes[i][j]);
					cielingsUsed[j]=true;
				}
			} else {
				if(sectorTag[i]==-1 || sectorTag[i]==-2) { // I'm using this to mean a door with no tag number
					Entity newDoor=new Entity("func_door");
					newDoor.setAttribute("speed", "60");
					newDoor.setAttribute("angles", "270 0 0");
					newDoor.setAttribute("spawnflags", "256");
					newDoor.setAttribute("targetname", "sectornum"+i+"door");
					if(sectorTag[i]==-1) {
						newDoor.setAttribute("wait", "4");
					} else {
						if(sectorTag[i]==-2) {
							newDoor.setAttribute("wait", "-1");
						}
					}
					int lowestNeighborCielingHeight=getLowestNeighborCielingHeight(i);
					int lip=ZMax-lowestNeighborCielingHeight+4;
					newDoor.setAttribute("lip", ""+lip);
					for(int j=0;j<sectorFloorBrushes[i].length;j++) {
						cielingsUsed[j]=true;
						newDoor.addBrush(sectorCielingBrushes[i][j]);
					}
					mapFile.add(newDoor);
				} else {
					for(int j=0;j<doomMap.getLinedefs().size();j++) {
						DLinedef currentLinedef=doomMap.getLinedefs().getElement(j);
						int linedefTriggerType=currentLinedef.getAction();
						if(doomMap.getVersion()==doomMap.TYPE_HEXEN) {
							switch(linedefTriggerType) {
								case 21: // Floor lower to lowest neighbor
								case 22: // Floor lower to nearest lower neighbor
									// I don't know where retriggerability is determined, or whether or not it goes back up.
									if(currentLinedef.getArguments()[0]==sectorTag[i]) {
										Entity newFloor=new Entity("func_door");
										newFloor.setAttribute("angles", "90 0 0");
										newFloor.setAttribute("wait", "-1");
										newFloor.setAttribute("speed", ""+currentLinedef.getArguments()[1]);
										if(currentLinedef.getArguments()[0]==0) {
											newFloor.setAttribute("targetname", "sectornum"+i+"lowerfloor");
										} else {
											newFloor.setAttribute("targetname", "sector"+currentLinedef.getArguments()[0]+"lowerfloor");
										}
										int lowestNeighborFloorHeight;
										if(linedefTriggerType==21) {
											lowestNeighborFloorHeight=getLowestNeighborFloorHeight(i);
										} else {
											lowestNeighborFloorHeight=getNextLowestNeighborFloorHeight(i);
										}
										if(lowestNeighborFloorHeight==32768) {
											lowestNeighborFloorHeight=doomMap.getSectors().getElement(i).getFloorHeight();
										}
										int lip=ZMin-lowestNeighborFloorHeight;
										newFloor.setAttribute("lip", ""+Math.abs(lip));
										for(int k=0;k<sectorFloorBrushes[i].length;k++) {
											if(!floorsUsed[k]) {
												floorsUsed[k]=true;
												newFloor.addBrush(sectorFloorBrushes[i][k]);
											}
										}
										mapFile.add(newFloor);
									}
									break;
								case 24: // Floor raise to highest neighbor
								case 25: // Floor raise to nearest higher neighbor
									// I don't know where retriggerability is determined, or whether or not it goes back up.
									if(currentLinedef.getArguments()[0]==sectorTag[i]) {
										Entity newFloor=new Entity("func_door");
										newFloor.setAttribute("angles", "270 0 0");
										newFloor.setAttribute("wait", "-1");
										newFloor.setAttribute("speed", ""+currentLinedef.getArguments()[1]);
										if(currentLinedef.getArguments()[0]==0) {
											newFloor.setAttribute("targetname", "sectornum"+i+"raisefloor");
										} else {
											newFloor.setAttribute("targetname", "sector"+currentLinedef.getArguments()[0]+"raisefloor");
										}
										int highestNeighborFloorHeight;
										if(linedefTriggerType==24) {
											highestNeighborFloorHeight=getHighestNeighborFloorHeight(i);
										} else {
											highestNeighborFloorHeight=getNextHighestNeighborFloorHeight(i);
										}
										if(highestNeighborFloorHeight==-32768) {
											highestNeighborFloorHeight=doomMap.getSectors().getElement(i).getFloorHeight();
										}
										int lip=ZMin-highestNeighborFloorHeight;
										newFloor.setAttribute("lip", ""+Math.abs(lip));
										for(int k=0;k<sectorFloorBrushes[i].length;k++) {
											if(!floorsUsed[k]) {
												floorsUsed[k]=true;
												newFloor.addBrush(sectorFloorBrushes[i][k]);
											}
										}
										mapFile.add(newFloor);
									}
									break;
							}
						} else {
							if(currentLinedef.getTag()==sectorTag[i]) {
								switch(linedefTriggerType) {
									case 36: // Line crossed, floor lowers, stays 8 above next lowest
									case 38: // Line crossed, floor lowers, stays at next lowest
										Entity newFloor=new Entity("func_door");
										newFloor.setAttribute("speed", "120");
										newFloor.setAttribute("angles", "90 0 0");
										newFloor.setAttribute("targetname", "sector"+sectorTag[i]+"lowerfloor");
										newFloor.setAttribute("wait", "-1");
										int lowestNeighborFloorHeight=getLowestNeighborFloorHeight(i);
										int lip=ZMin-lowestNeighborFloorHeight;
										if(linedefTriggerType==36) {
											lip-=8;
										}
										newFloor.setAttribute("lip", ""+Math.abs(lip));
										for(int k=0;k<sectorFloorBrushes[i].length;k++) {
											if(!floorsUsed[k]) {
												floorsUsed[k]=true;
												newFloor.addBrush(sectorFloorBrushes[i][k]);
											}
										}
										mapFile.add(newFloor);
										break;
									case 63: // Push button, door opens, waits 4s, closes
									case 103: // Push button, door opens, stays
									case 109: // Cross line, door opens, stays
										Entity newDoor=new Entity("func_door");
										newDoor.setAttribute("speed", "60");
										newDoor.setAttribute("angles", "270 0 0");
										newDoor.setAttribute("targetname", "sector"+sectorTag[i]+"door");
										newDoor.setAttribute("wait", "-1");
										if(sectorTag[i]==63) {
											newDoor.setAttribute("wait", "4");
										}
										int lowestNeighborCielingHeight=getLowestNeighborCielingHeight(i);
										lip=ZMax-lowestNeighborCielingHeight+4;
										newDoor.setAttribute("lip", ""+lip);
										for(int k=0;k<sectorFloorBrushes[i].length;k++) {
											if(!cielingsUsed[k]) {
												cielingsUsed[k]=true;
												newDoor.addBrush(sectorCielingBrushes[i][k]);
											}
										}
										mapFile.add(newDoor);
										break;
									case 62: // Push button, Elevator goes down to lowest, wait 4s, goes up
									case 88: // Elevator goes down to lowest, wait 4s, goes up
										Entity newVator=new Entity("func_door");
										newVator.setAttribute("speed", "120");
										newVator.setAttribute("angles", "90 0 0");
										newVator.setAttribute("targetname", "sector"+sectorTag[i]+"vator");
										newVator.setAttribute("wait", "4");
										lowestNeighborFloorHeight=getLowestNeighborFloorHeight(i);
										lip=Math.abs(ZMin-lowestNeighborFloorHeight);
										newVator.setAttribute("lip", ""+lip);
										for(int k=0;k<sectorFloorBrushes[i].length;k++) {
											if(!floorsUsed[k]) {
												newVator.addBrush(sectorFloorBrushes[i][k]);
												floorsUsed[k]=true;
											}
										}
										mapFile.add(newVator);
										break;
									default: // I'd like to not use this evenutally, all the trigger types ought to be handled
										Window.println("WARNING: Unimplemented linedef trigger type "+linedefTriggerType+" for sector "+i+" tagged "+sectorTag[i],Window.VERBOSITY_WARNINGS);
										for(int k=0;k<sectorFloorBrushes[i].length;k++) {
											if(!floorsUsed[k]) {
												world.addBrush(sectorFloorBrushes[i][k]);
												floorsUsed[k]=true;
											}
											if(!cielingsUsed[k]) {
												world.addBrush(sectorCielingBrushes[i][k]);
												cielingsUsed[k]=true;
											}
										}
										break;
								}
							}
						}
					}
				}
			}
			for(int j=0;j<sectorFloorBrushes[i].length;j++) {
				if(!cielingsUsed[j]) {
					world.addBrush(sectorCielingBrushes[i][j]);
				}
				if(!floorsUsed[j]) {
					world.addBrush(sectorFloorBrushes[i][j]);
				}
			}
		}
		
		// Convert THINGS
		for(int i=0;i<doomMap.getThings().size();i++) {
			if(Thread.currentThread().interrupted()) {
				throw new java.lang.InterruptedException("while creating entity for thing "+i+".");
			}
			DThing currentThing=doomMap.getThings().getElement(i);
			// To find the true height of a thing, I need to iterate through nodes until I come to a subsector
			// definition. Then I need to use the floor height of the sector that subsector belongs to.
			Vector3D origin=currentThing.getOrigin();
			int subsectorIndex=doomMap.getNodes().size()-1;
			while(subsectorIndex>=0) { // Once child is negative, subsector is found
				DNode currentNode=doomMap.getNodes().getElement(subsectorIndex);
				Vector3D start=currentNode.getVecHead();
				Vector3D end=currentNode.getVecHead().add(currentNode.getVecTail());
				Plane currentPlane=new Plane(start, end, new Vector3D(start.getX(), start.getY(), 1));
				if(currentPlane.distance(origin)<0) {
					subsectorIndex=currentNode.getChild1();
				} else {
					subsectorIndex=currentNode.getChild2();
				}
			}
			subsectorIndex+=32768;
			int sectorIndex=subsectorSectors[subsectorIndex];
			DSector thingSector=doomMap.getSectors().getElement(sectorIndex);
			if(origin.getZ()==0) {
				origin.setZ(thingSector.getFloorHeight());
			}
			
			Entity thing=null;
			// Things from both Doom and Hexen here
			switch(currentThing.getClassNum()) {
				case 1: // Single player spawn
				case 2: // coop
				case 3: // coop
				case 4: // coop
					thing=new Entity("info_player_start");
					if(currentThing.getClassNum()>1) {
						thing.setAttribute("targetname", "coopspawn"+currentThing.getClassNum());
					}
					playerStartOrigin=origin.getX()+" "+origin.getY()+" "+(origin.getZ()+36);
					thing.setAttribute("origin", playerStartOrigin);
					thing.setAttribute("angles", "0 "+currentThing.getAngle()+" 0");
					break;
				case 11: // Deathmatch spawn
					thing=new Entity("info_player_deathmatch");
					thing.setAttribute("origin", origin.getX()+" "+origin.getY()+" "+(origin.getZ()+36));
					thing.setAttribute("angles", "0 "+currentThing.getAngle()+" 0");
					break;
				case 14: // Teleport destination
					thing=new Entity("info_teleport_destination");
					if(currentThing.getID()!=0) {
						thing.setAttribute("targetname", "teledest"+currentThing.getID());
					} else {
						thing.setAttribute("targetname", "sector"+thingSector.getTag()+"teledest");
					}
					thing.setAttribute("origin", origin.getX()+" "+origin.getY()+" "+(origin.getZ()+36));
					thing.setAttribute("angles", "0 "+currentThing.getAngle()+" 0");
					break;
				case 17: // Big cell pack
					thing=new Entity("ammo_bondmine");
					thing.setAttribute("origin", origin.getX()+" "+origin.getY()+" "+origin.getZ());
					thing.setAttribute("angles", "0 "+currentThing.getAngle()+" 0");
					break;
				case 82: // Super shotgun
					thing=new Entity("weapon_pdw90");
					thing.setAttribute("origin", origin.getX()+" "+origin.getY()+" "+origin.getZ());
					thing.setAttribute("angles", "0 "+currentThing.getAngle()+" 0");
					break;
				case 2001: // Shotgun
					thing=new Entity("weapon_frinesi");
					thing.setAttribute("origin", origin.getX()+" "+origin.getY()+" "+origin.getZ());
					thing.setAttribute("angles", "0 "+currentThing.getAngle()+" 0");
					break;
				case 2002: // Chaingun
					thing=new Entity("weapon_minigun");
					thing.setAttribute("origin", origin.getX()+" "+origin.getY()+" "+origin.getZ());
					thing.setAttribute("angles", "0 "+currentThing.getAngle()+" 0");
					break;
				case 2003: // Rocket launcher
					thing=new Entity("weapon_rocketlauncher");
					thing.setAttribute("origin", origin.getX()+" "+origin.getY()+" "+origin.getZ());
					thing.setAttribute("angles", "0 "+currentThing.getAngle()+" 0");
					break;
				case 2004: // Plasma gun
					thing=new Entity("weapon_grenadelauncher");
					thing.setAttribute("origin", origin.getX()+" "+origin.getY()+" "+origin.getZ());
					thing.setAttribute("angles", "0 "+currentThing.getAngle()+" 0");
					break;
				case 2005: // Chainsaw
					thing=new Entity("weapon_ronin");
					thing.setAttribute("origin", origin.getX()+" "+origin.getY()+" "+origin.getZ());
					thing.setAttribute("angles", "0 "+currentThing.getAngle()+" 0");
					break;
				case 2006: // BFG9000
					thing=new Entity("weapon_laserrifle");
					thing.setAttribute("origin", origin.getX()+" "+origin.getY()+" "+origin.getZ());
					thing.setAttribute("angles", "0 "+currentThing.getAngle()+" 0");
					break;
				case 2007: // Ammo clip
					thing=new Entity("ammo_pp9");
					thing.setAttribute("origin", origin.getX()+" "+origin.getY()+" "+origin.getZ());
					thing.setAttribute("angles", "0 "+currentThing.getAngle()+" 0");
					break;
				case 2008: // Shotgun shells
					thing=new Entity("ammo_mini");
					thing.setAttribute("origin", origin.getX()+" "+origin.getY()+" "+origin.getZ());
					thing.setAttribute("angles", "0 "+currentThing.getAngle()+" 0");
					break;
				case 2010: // Rocket
					thing=new Entity("ammo_darts");
					thing.setAttribute("origin", origin.getX()+" "+origin.getY()+" "+origin.getZ());
					thing.setAttribute("angles", "0 "+currentThing.getAngle()+" 0");
					break;
				case 2046: // Box of Rockets
					thing=new Entity("ammo_rocketlauncher");
					thing.setAttribute("origin", origin.getX()+" "+origin.getY()+" "+origin.getZ());
					thing.setAttribute("angles", "0 "+currentThing.getAngle()+" 0");
					break;
				case 2047: // Cell pack
					thing=new Entity("ammo_grenadelauncher");
					thing.setAttribute("origin", origin.getX()+" "+origin.getY()+" "+origin.getZ());
					thing.setAttribute("angles", "0 "+currentThing.getAngle()+" 0");
					break;
				case 2048: // Box of ammo
					thing=new Entity("ammo_mp9");
					thing.setAttribute("origin", origin.getX()+" "+origin.getY()+" "+origin.getZ());
					thing.setAttribute("angles", "0 "+currentThing.getAngle()+" 0");
					break;
				case 2049: // Box of shells
					thing=new Entity("ammo_shotgun");
					thing.setAttribute("origin", origin.getX()+" "+origin.getY()+" "+origin.getZ());
					thing.setAttribute("angles", "0 "+currentThing.getAngle()+" 0");
					break;
			}
			
			if(doomMap.getVersion()==DoomMap.TYPE_HEXEN) { // Hexen only
			
			} else { // Doom only
				
			}
			
			if(thing!=null) {
				mapFile.add(thing);
			}
		}
		
		Entity playerequip=new Entity("game_player_equip");
		playerequip.setAttribute("weapon_pp9", "1");
		playerequip.setAttribute("origin", playerStartOrigin);
		mapFile.add(playerequip);
		return mapFile;
	}
	
	private int getLowestNeighborCielingHeight(int sector) {
		int lowestNeighborCielingHeight=32768;
		for(int j=0;j<doomMap.getLinedefs().size();j++) {
			DLinedef currentLinedef=doomMap.getLinedefs().getElement(j);
			if(!currentLinedef.isOneSided()) {
				DSector neighbor=null;
				if(doomMap.getSidedefs().getElement(currentLinedef.getLeft()).getSector()==sector) {
					neighbor=doomMap.getSectors().getElement(doomMap.getSidedefs().getElement(currentLinedef.getRight()).getSector());
				} else {
					if(doomMap.getSidedefs().getElement(currentLinedef.getRight()).getSector()==sector) {
						neighbor=doomMap.getSectors().getElement(doomMap.getSidedefs().getElement(currentLinedef.getLeft()).getSector());
					}
				}
				if(neighbor!=null && neighbor.getCielingHeight()<lowestNeighborCielingHeight) {
					lowestNeighborCielingHeight=neighbor.getCielingHeight();
				}
			}
		}
		return lowestNeighborCielingHeight;
	}
	
	private int getLowestNeighborFloorHeight(int sector) {
		int lowestNeighborFloorHeight=32768;
		for(int k=0;k<doomMap.getLinedefs().size();k++) {
			DLinedef currentSearchLinedef=doomMap.getLinedefs().getElement(k);
			if(!currentSearchLinedef.isOneSided()) {
				DSector neighbor=null;
				if(doomMap.getSidedefs().getElement(currentSearchLinedef.getLeft()).getSector()==sector) {
					neighbor=doomMap.getSectors().getElement(doomMap.getSidedefs().getElement(currentSearchLinedef.getRight()).getSector());
				} else {
					if(doomMap.getSidedefs().getElement(currentSearchLinedef.getRight()).getSector()==sector) {
						neighbor=doomMap.getSectors().getElement(doomMap.getSidedefs().getElement(currentSearchLinedef.getLeft()).getSector());
					}
				}
				if(neighbor!=null && neighbor.getFloorHeight()<lowestNeighborFloorHeight) {
					lowestNeighborFloorHeight=neighbor.getFloorHeight();
				}
			}
		}
		return lowestNeighborFloorHeight;
	}
	
	private int getHighestNeighborCielingHeight(int sector) {
		int highestNeighborCielingHeight=-32768;
		for(int j=0;j<doomMap.getLinedefs().size();j++) {
			DLinedef currentLinedef=doomMap.getLinedefs().getElement(j);
			if(!currentLinedef.isOneSided()) {
				DSector neighbor=null;
				if(doomMap.getSidedefs().getElement(currentLinedef.getLeft()).getSector()==sector) {
					neighbor=doomMap.getSectors().getElement(doomMap.getSidedefs().getElement(currentLinedef.getRight()).getSector());
				} else {
					if(doomMap.getSidedefs().getElement(currentLinedef.getRight()).getSector()==sector) {
						neighbor=doomMap.getSectors().getElement(doomMap.getSidedefs().getElement(currentLinedef.getLeft()).getSector());
					}
				}
				if(neighbor!=null && neighbor.getCielingHeight()>highestNeighborCielingHeight) {
					highestNeighborCielingHeight=neighbor.getCielingHeight();
				}
			}
		}
		return highestNeighborCielingHeight;
	}
	
	private int getHighestNeighborFloorHeight(int sector) {
		int highestNeighborFloorHeight=-32768;
		for(int k=0;k<doomMap.getLinedefs().size();k++) {
			DLinedef currentSearchLinedef=doomMap.getLinedefs().getElement(k);
			if(!currentSearchLinedef.isOneSided()) {
				DSector neighbor=null;
				if(doomMap.getSidedefs().getElement(currentSearchLinedef.getLeft()).getSector()==sector) {
					neighbor=doomMap.getSectors().getElement(doomMap.getSidedefs().getElement(currentSearchLinedef.getRight()).getSector());
				} else {
					if(doomMap.getSidedefs().getElement(currentSearchLinedef.getRight()).getSector()==sector) {
						neighbor=doomMap.getSectors().getElement(doomMap.getSidedefs().getElement(currentSearchLinedef.getLeft()).getSector());
					}
				}
				if(neighbor!=null && neighbor.getFloorHeight()>highestNeighborFloorHeight) {
					highestNeighborFloorHeight=neighbor.getFloorHeight();
				}
			}
		}
		return highestNeighborFloorHeight;
	}
	
	private int getNextLowestNeighborCielingHeight(int sector) {
		int nextLowestNeighborCielingHeight=32768;
		int current=doomMap.getSectors().getElement(sector).getCielingHeight();
		for(int j=0;j<doomMap.getLinedefs().size();j++) {
			DLinedef currentLinedef=doomMap.getLinedefs().getElement(j);
			if(!currentLinedef.isOneSided()) {
				DSector neighbor=null;
				if(doomMap.getSidedefs().getElement(currentLinedef.getLeft()).getSector()==sector) {
					neighbor=doomMap.getSectors().getElement(doomMap.getSidedefs().getElement(currentLinedef.getRight()).getSector());
				} else {
					if(doomMap.getSidedefs().getElement(currentLinedef.getRight()).getSector()==sector) {
						neighbor=doomMap.getSectors().getElement(doomMap.getSidedefs().getElement(currentLinedef.getLeft()).getSector());
					}
				}
				if(neighbor!=null && neighbor.getCielingHeight()>nextLowestNeighborCielingHeight && neighbor.getCielingHeight()<current) {
					nextLowestNeighborCielingHeight=neighbor.getCielingHeight();
				}
			}
		}
		return nextLowestNeighborCielingHeight;
	}
	
	private int getNextLowestNeighborFloorHeight(int sector) {
		int nextLowestNeighborFloorHeight=32768;
		int current=doomMap.getSectors().getElement(sector).getFloorHeight();
		for(int k=0;k<doomMap.getLinedefs().size();k++) {
			DLinedef currentSearchLinedef=doomMap.getLinedefs().getElement(k);
			if(!currentSearchLinedef.isOneSided()) {
				DSector neighbor=null;
				if(doomMap.getSidedefs().getElement(currentSearchLinedef.getLeft()).getSector()==sector) {
					neighbor=doomMap.getSectors().getElement(doomMap.getSidedefs().getElement(currentSearchLinedef.getRight()).getSector());
				} else {
					if(doomMap.getSidedefs().getElement(currentSearchLinedef.getRight()).getSector()==sector) {
						neighbor=doomMap.getSectors().getElement(doomMap.getSidedefs().getElement(currentSearchLinedef.getLeft()).getSector());
					}
				}
				if(neighbor!=null && neighbor.getFloorHeight()>nextLowestNeighborFloorHeight && neighbor.getFloorHeight()<current) {
					nextLowestNeighborFloorHeight=neighbor.getFloorHeight();
				}
			}
		}
		return nextLowestNeighborFloorHeight;
	}
	
	private int getNextHighestNeighborCielingHeight(int sector) {
		int nextHighestNeighborCielingHeight=-32768;
		int current=doomMap.getSectors().getElement(sector).getCielingHeight();
		for(int j=0;j<doomMap.getLinedefs().size();j++) {
			DLinedef currentLinedef=doomMap.getLinedefs().getElement(j);
			if(!currentLinedef.isOneSided()) {
				DSector neighbor=null;
				if(doomMap.getSidedefs().getElement(currentLinedef.getLeft()).getSector()==sector) {
					neighbor=doomMap.getSectors().getElement(doomMap.getSidedefs().getElement(currentLinedef.getRight()).getSector());
				} else {
					if(doomMap.getSidedefs().getElement(currentLinedef.getRight()).getSector()==sector) {
						neighbor=doomMap.getSectors().getElement(doomMap.getSidedefs().getElement(currentLinedef.getLeft()).getSector());
					}
				}
				if(neighbor!=null && neighbor.getCielingHeight()<nextHighestNeighborCielingHeight && neighbor.getCielingHeight()>current) {
					nextHighestNeighborCielingHeight=neighbor.getCielingHeight();
				}
			}
		}
		return nextHighestNeighborCielingHeight;
	}
	
	private int getNextHighestNeighborFloorHeight(int sector) {
		int nextHighestNeighborFloorHeight=-32768;
		int current=doomMap.getSectors().getElement(sector).getFloorHeight();
		for(int k=0;k<doomMap.getLinedefs().size();k++) {
			DLinedef currentSearchLinedef=doomMap.getLinedefs().getElement(k);
			if(!currentSearchLinedef.isOneSided()) {
				DSector neighbor=null;
				if(doomMap.getSidedefs().getElement(currentSearchLinedef.getLeft()).getSector()==sector) {
					neighbor=doomMap.getSectors().getElement(doomMap.getSidedefs().getElement(currentSearchLinedef.getRight()).getSector());
				} else {
					if(doomMap.getSidedefs().getElement(currentSearchLinedef.getRight()).getSector()==sector) {
						neighbor=doomMap.getSectors().getElement(doomMap.getSidedefs().getElement(currentSearchLinedef.getLeft()).getSector());
					}
				}
				if(neighbor!=null && neighbor.getFloorHeight()<nextHighestNeighborFloorHeight && neighbor.getFloorHeight()>current) {
					nextHighestNeighborFloorHeight=neighbor.getFloorHeight();
				}
			}
		}
		return nextHighestNeighborFloorHeight;
	}
}
