package miniJava.CodeGen;

import java.util.HashMap;

import mJAM.Machine;
import mJAM.Machine.Op;
import miniJava.AbstractSyntaxTrees.ClassDecl;

 public class classDescriptorCreator{
	
	private HashMap<String, Integer> classDesc;
	private  HashMap<String, Integer> classDescPatcher;
	
	public classDescriptorCreator(){
		classDesc = new HashMap<String, Integer>();
		classDescPatcher = new HashMap<String, Integer>();
	}
	
 public int	getDescDisplacement(String classname){

		
	  
	 if( classDesc.containsKey(classname)){
		return classDesc.get(classname);
	 }
	 else{
		 System.out.println("No such class is created by class descriptor");
		 return -1;
	 }
			
	}
 
 public void allocate(ClassDecl  cd){
	 
 


classDesc.put(cd.name, dCd);

Machine.emit(Op.LOADL, cd.numNonStaticMethods); // puts number of non static methods in the first field  

int	 parentClassDesc = Machine.nextInstrAddr();
Machine.emit(Op.LOADL, -1);
Machine.emit(Op.PUSH, cd.numNonStaticMethods);
	 
 }
 
 
 public void addFunction(String classname, int delta, int address){
	 
 }
	
}

