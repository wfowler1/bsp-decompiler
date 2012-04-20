// v38NodeStack class

// Contains a "stack" of v38Nodes. This class aids greatly in the
// traversal of a BSP tree without use of recursion.

public class v38NodeStack {
	
	// INITIAL DATA DECLARATION AND DEFINITION OF CONSTANTS
	
	v38Node[] stack;
	
	// CONSTRUCTORS
	
	public v38NodeStack() {
		stack=new v38Node[0];
	}
	
	// METHODS
	
	public void push(v38Node in) {
		v38Node[] newStack = new v38Node[stack.length+1];
		for(int i=0;i<stack.length;i++) {
			newStack[i]=stack[i];
		}
		newStack[newStack.length-1]=in;
		stack=newStack;
	}
	
	public v38Node pop() {
		v38Node returnme=stack[stack.length-1];
		v38Node[] newStack=new v38Node[stack.length-1];
		for(int i=0;i<stack.length-1;i++) {
			newStack[i]=stack[i];
		}
		stack=newStack;
		return returnme;
	}
	
	public v38Node read() {
		return stack[stack.length-1];
	}
	
	// ACCESSORS AND MUTATORS
	
	public boolean isEmpty() {
		return stack.length==0;
	}
	
	public int getSize() {
		return stack.length;
	}
}