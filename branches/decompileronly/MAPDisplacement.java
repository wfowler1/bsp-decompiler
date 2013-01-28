// MAPDisplacement class
// Holds the information for a displacement, ideally for a VMF file.

public class MAPDisplacement {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	private int power;
	private Vector3D start;
	private Vector3D[][] normals;
	private float[][] distances;
	private float[][] alphas;
	private int[] allowedVerts;
	
	// CONSTRUCTORS
	
	public MAPDisplacement(SourceDispInfo disp, SourceDispVertex[] vertices) {
		power=disp.getPower();
		start=disp.getStartPosition();
		int numVertsInRow=0;
		switch(power) {
			case 2:
				numVertsInRow=5;
				break;
			case 3:
				numVertsInRow=9;
				break;
			case 4:
				numVertsInRow=17;
				break;
		}
		normals=new Vector3D[numVertsInRow][numVertsInRow];
		distances=new float[numVertsInRow][numVertsInRow];
		alphas=new float[numVertsInRow][numVertsInRow];
		for(int i=0;i<numVertsInRow;i++) {
			for(int j=0;j<numVertsInRow;j++) {
				normals[i][j]=vertices[(int)(i*(Math.pow(2, power)+1))+j].getNormal();
				distances[i][j]=vertices[(int)(i*(Math.pow(2, power)+1))+j].getDist();
				alphas[i][j]=vertices[(int)(i*(Math.pow(2, power)+1))+j].getAlpha();
			}
		}
		allowedVerts=disp.getAllowedVerts();
	}
	
	// METHODS
	
	// ACCESSORS/MUTATORS
	
	public int getPower() {
		return power;
	}
	
	public Vector3D getStart() {
		return start;
	}
	
	public Vector3D getNormal(int row, int column) {
		return normals[row][column];
	}
	
	public float getDist(int row, int column) {
		return distances[row][column];
	}
	
	public float getAlpha(int row, int column) {
		return alphas[row][column];
	}
	
	public int[] getAllowedVerts() {
		return allowedVerts;
	}
}