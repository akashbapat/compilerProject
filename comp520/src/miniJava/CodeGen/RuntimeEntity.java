package miniJava.CodeGen;

import mJAM.Machine.Reg;
 

public abstract class RuntimeEntity { 
	
	public int size; 
	public int address;
	public Reg base;
	public int methodIndex;
	public int stackAddress;
	public RuntimeEntity ( ) {
		   size=0;
		   address=0;
		   base = Reg.LB;
		   methodIndex = -1;
		   stackAddress = -1;
		  }
	 
	 public RuntimeEntity (int s,int add,Reg bas) {
		   size=s;
		   address=add;
		   base = base;
		   methodIndex = -1;
		   stackAddress = -1;
		  }
	 public RuntimeEntity (int s,int add) {
		   size=s;
		   address=add;
		   base = Reg.LB;
		   methodIndex = -1;
		   stackAddress = -1;
		  }
	 public RuntimeEntity (int s,int add,int methodInd) {
		   size=s;
		   address=add;
		   base = Reg.LB;
		   methodIndex = methodInd;
		   stackAddress = -1;
		  }

	
}



