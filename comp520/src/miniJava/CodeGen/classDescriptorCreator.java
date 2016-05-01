package miniJava.CodeGen;

import java.util.ArrayList;
import java.util.HashMap;

import mJAM.Machine;
import mJAM.Machine.Op;
import mJAM.Machine.Reg;
import miniJava.AbstractSyntaxTrees.ClassDecl;

 public class classDescriptorCreator{
	
	private HashMap<String, Integer> classDesc;
	private  ArrayList<Integer> classDescPatcherClassAddr;
	private ArrayList<String> classDescPatcherClassName;
	private int stackDisplacement;
	public classDescriptorCreator(){
		classDesc = new HashMap<String, Integer>();
		classDescPatcherClassName = new ArrayList<String>();
		classDescPatcherClassAddr = new ArrayList<Integer>();
		stackDisplacement =0;
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
	 
  
classDesc.put(cd.name, stackDisplacement);

Machine.emit(Op.LOADL, cd.numNonStaticMethods); // puts number of non static methods in the first field  
if(!cd.isBaseClass){
	
	classDescPatcherClassName.add(cd.parentClassName);
	classDescPatcherClassAddr.add(stackDisplacement +1);
}
Machine.emit(Op.LOADL, -1);
Machine.emit(Op.PUSH, cd.numNonStaticMethods);
	
stackDisplacement = stackDisplacement + 2 +  cd.numNonStaticMethods;

 }
 
 
 public void addFunction(String classname, int delta, int FuncAddr){
	 
	int classDelta = classDesc.get(classname);
	 
	//classDelta +2+delta [SB] <- address
	
 int 	stackaddr = classDelta +2+delta ;
 
 Machine.emit(Op.LOADL, FuncAddr);
 
	Machine.emit(Op.STORE, 1,Reg.SB, stackaddr) ;
	
 }
 
 
 
 public void patchParentClasses(){
	 // here all the empty descriptors have been allocated
	 
	 for(int i=0;i<classDescPatcherClassName.size();i++){
		 
		int  stackAddr = classDescPatcherClassAddr.get(i);
		int parentDelta = classDesc.get(classDescPatcherClassName.get(i));
		
		 Machine.emit(Op.LOADL, parentDelta);
		 
			Machine.emit(Op.STORE, 1,Reg.SB, stackAddr) ;
		 
	 }
	 
	 
 }
 
 public void setStackDisplacement(int delta){
	 stackDisplacement =delta;
 }
	
}

