package miniJava.CodeGen;

import java.util.ArrayList;
import java.util.HashMap;

import mJAM.Machine;
import mJAM.Machine.Op;
import mJAM.Machine.Reg;
import miniJava.AbstractSyntaxTrees.ClassDecl;
import miniJava.AbstractSyntaxTrees.ClassDeclList;
import miniJava.AbstractSyntaxTrees.MethodDecl;

 public class classDescriptorCreator{
	
	private HashMap<String, Integer> classDesc;
	private  ArrayList<Integer> classDescPatcherClassAddr;
	private ArrayList<String> classDescPatcherClassName;
	private int stackDisplacement;
	
	
	private ArrayList<Integer> childVirtualFuncAddr;
	private ArrayList<Integer> parentOverriddenFuncDelta;
	private ClassDeclList cdl;
	private ArrayList<Integer> deltaChildClass;
	private ArrayList<Integer> ownFuncCodeAddr;
	private ArrayList< Integer> ownFuncStackWriteAddr;
	public classDescriptorCreator(){
		 
		classDesc = new HashMap<String, Integer>();
		classDescPatcherClassName = new ArrayList<String>();
		classDescPatcherClassAddr = new ArrayList<Integer>();
		
		ownFuncCodeAddr = new ArrayList<Integer>();
		ownFuncStackWriteAddr = new ArrayList<Integer>();
		
		parentOverriddenFuncDelta = new ArrayList<Integer>();
		childVirtualFuncAddr = new ArrayList<Integer>();
		deltaChildClass = new ArrayList<Integer>();
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
 
 
 public void addClassDeclList(ClassDeclList _cdl){
	 cdl =_cdl;
 }
 
 
 
 
 public void allocate(ClassDecl  cd){
	 /*
  
classDesc.put(cd.name, stackDisplacement);

Machine.emit(Op.LOADL, cd.numNonStaticMethods); // puts number of non static methods in the first field  
if(!cd.isBaseClass){
	
	classDescPatcherClassName.add(cd.parentClassName);
	classDescPatcherClassAddr.add(stackDisplacement +1);
}
Machine.emit(Op.LOADL, -1);
Machine.emit(Op.PUSH, cd.numNonStaticMethods);//creates  empty slots for func code addr
	
stackDisplacement = stackDisplacement + 2 +  cd.numNonStaticMethods;
*/
	 
	 
	 classDesc.put(cd.name, stackDisplacement);	 
	 if(!cd.isBaseClass){
	 	
	 	classDescPatcherClassName.add(cd.parentClassName);
	 	classDescPatcherClassAddr.add(stackDisplacement);
	 }
	 
	 
	 Machine.emit(Op.LOADL, -1);//creates dA
	 Machine.emit(Op.LOADL, cd.numNonStaticMethods); // puts number of non static methods in the first field  
	 Machine.emit(Op.PUSH, cd.numNonStaticMethods); //creates  empty slots for func code addr
	 
	 stackDisplacement = stackDisplacement + 2 +  cd.numNonStaticMethods;
	 
	 
	 
 }
 
 
 public void addFunction(ClassDecl cd,MethodDecl funcMd, int delta, int FuncAddr){
	 
	int classDelta = classDesc.get(cd.name);
	 
	//classDelta +2+delta [SB] <- address
	
 int 	stackAddr = classDelta +2+delta ;
 
// Machine.emit(Op.LOADL, FuncAddr);
 
	//Machine.emit(Op.STORE, 1,Reg.SB, stackAddr) ;
	
 
 ownFuncCodeAddr.add(FuncAddr);
 ownFuncStackWriteAddr.add(stackAddr);
 
	funcMd.getEntity().stackAddress = stackAddr;
	
	
	
	
	addToVirtualPatcher( cd, funcMd, stackAddr,classDelta);
	
 }
 
 public void ownFuncPatcher(){
	 int FuncAddr,stackAddr;
	  for(int i=0;i<ownFuncCodeAddr.size();i++){
		  FuncAddr = ownFuncCodeAddr.get(i);
		  stackAddr = ownFuncStackWriteAddr.get(i);
		  
		 Machine.emit(Op.LOADL, FuncAddr);
		  
			 Machine.emit(Op.STORE, 1,Reg.SB, stackAddr) ;
		  
	  }
 }
 
 
 
 private void addToVirtualPatcher(ClassDecl cd,MethodDecl funcMd, int stackAddr,int classDelta){
	 
	 ClassDecl parentClassDecl ;
	 MethodDecl md;
	  
	 
	 if(!cd.isBaseClass){
	 	 
		 parentClassDecl = cd.parentClassDecl;
		 
		 addToVirtualPatcher(  parentClassDecl,  funcMd,  stackAddr,classDelta); //necessary to pass whatever is received by outside call
		 
		 for(int i =0;i<parentClassDecl.methodDeclList.size(); i++){
			 md = parentClassDecl.methodDeclList.get(i);
			 
			 if(md.name.equals(funcMd.name)){
		 		 
				 childVirtualFuncAddr.add(stackAddr);
				 parentOverriddenFuncDelta.add(md.getEntity().methodIndex);
				 deltaChildClass.add(classDelta);
			 }
			 
		 }
		 
	 }
	 
	 
 }
 
 
 public void virtualPatcher(){
	 
	 int overriddenFuncDelta;
	 int overWriteAddr;
	 for(int i=0;i<childVirtualFuncAddr.size();i++){
	//	 Machine.emit(Op.LOADL, childVirtualFuncAddr.get(i)  );
		 Machine.emit(Op.LOAD, 1, Reg.SB,childVirtualFuncAddr.get(i)); 
		 
		 overriddenFuncDelta = parentOverriddenFuncDelta.get(i);
		 overriddenFuncDelta = overriddenFuncDelta +2;
		 overWriteAddr = overriddenFuncDelta + deltaChildClass.get(i);

			Machine.emit(Op.STORE, 1,Reg.SB, overWriteAddr) ;
		 
	 }
	 
	
	 
	 
 }
 
 
 public void parentMethodCopy(){
	 
	 for(ClassDecl c : cdl){
		 parentMethodCopyInChild(  c);
	 }
	 
 }
 
 
 private void parentMethodCopyInChild(ClassDecl c){
	 
	 ClassDecl parentClassDecl;
	 int parentDeltaStack ;
	 int ownDeltaStack;
	 if(!c.isBaseClass){
		 
		 parentMethodCopyInChild(c.parentClassDecl);
		 
		 
		 parentClassDecl = c.parentClassDecl;
		 parentDeltaStack = classDesc.get(parentClassDecl.name);
		 ownDeltaStack = classDesc.get(c.name);
		 parentDeltaStack=parentDeltaStack+2;
		 ownDeltaStack = ownDeltaStack+2;
		 
		 
		 
		 for(int i=0;i<parentClassDecl.numNonStaticMethods; i++){
			
			 Machine.emit(Op.LOAD, Reg.SB, parentDeltaStack);
			 Machine.emit(Op.STORE,1, Reg.SB, ownDeltaStack);
			 ownDeltaStack++;
			 parentDeltaStack++;
		 }
		 
		 
	 }
	  
 }
 
 
 public void patchParentClassesD(){
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

