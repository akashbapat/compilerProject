package miniJava.CodeGen;

import mJAM.Machine.Reg;;
 

public abstract class RuntimeEntity { 
	
	public int size; 
	public int address;
	public Reg base;
	 public RuntimeEntity ( ) {
		   size=0;
		   address=0;
		   base = Reg.LB;
		  }
	 
	 public RuntimeEntity (int s,int add,Reg bas) {
		   size=s;
		   address=add;
		   base = base;
		  }
	 public RuntimeEntity (int s,int add) {
		   size=s;
		   address=add;
		   base = Reg.LB;
		  }
	
}



