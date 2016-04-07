package miniJava.CodeGen;

import mJAM.*;
import mJAM.Machine.Op;
import mJAM.Machine.Prim;
import mJAM.Machine.Reg;
import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.AbstractSyntaxTrees.Package;

import java.util.ArrayList;
import java.util.HashMap;
 

 


public class codeGenerator implements Visitor<Boolean,Object> {
		private HashMap<String,Prim> opToPrimMap; 
		functionPatcher fp;
		ErrorReporter reporter;
		MethodDecl mainMethodDecl;
	    int displacement;
	    int heap_displacement;
	    public boolean generate(AST ast){
	        System.out.println("======= Generating code =====================");
	    try{
	    	ast.visit(this, false);
	    }
	    catch (CodeGenError cge) {
			System.out.println("Code generator error occurred");
			return false;
		}
	    
	        System.out.println("=============================================");
	        return true;
	    }   
	    
	    
	   

		public codeGenerator(ErrorReporter er,MethodDecl md){
			fp = new functionPatcher();
			reporter =er;
			opToPrimMap = new HashMap<String, Machine.Prim>(); 
			initializeHMap();
			Machine.initCodeGen();
			mainMethodDecl=md;
			displacement=3;
			int heap_displacement = 0;
		}

		 class CodeGenError extends Error {
				private static final long serialVersionUID = 1L;	
			}

		private void codeGenError(String e) throws CodeGenError {
			reporter.reportError("*** CodeGenError error: " + e);
			throw new CodeGenError();

		}
	    
		
		
		
		
		
		private void initializeHMap()
		{
			opToPrimMap.put("!",Prim.not);
			opToPrimMap.put("&&",Prim.and);
			opToPrimMap.put("||",Prim.or);
			opToPrimMap.put("+",Prim.add);
			opToPrimMap.put("*",Prim.mult);
			opToPrimMap.put("/",Prim.div);
			opToPrimMap.put("==",Prim.eq);
			opToPrimMap.put("<=",Prim.le);
			opToPrimMap.put(">=",Prim.ge);
			opToPrimMap.put("!=",Prim.ne);
			opToPrimMap.put("<",Prim.gt);
			opToPrimMap.put(">",Prim.lt);
		}
		//private void encodeAssign(Declaration d1,Declaration d2)
		//{
			
		//}
		
		private void allocateOnHeap(ClassDecl d){
			
		//	RuntimeEntity re = d.getEntity();
			 Machine.emit(Op.LOADL,-1 );  // inheritance flag indicating no superclass		 	
			 Machine.emit(Op.LOADL,d.fieldDeclList.size() );	
			 Machine.emit(Prim.newobj);
			 heap_displacement += d.fieldDeclList.size();   
	
		}
		
		
		
		
		private void encodeAssign(Declaration d ){
			
			 if((d.type instanceof ClassType) || (d.type instanceof BaseType && (d.type.typeKind==TypeKind.INT || d.type.typeKind==TypeKind.BOOLEAN)) && d.getEntity()!=null){			 
				 Machine.emit(Op.STORE, 1,Reg.LB, d.getEntity().address) ;
					}
			 
				 
		}
		
		
		private void createEntity(Declaration d, int s){

			if(d instanceof VarDecl ){ /*d.type instanceof BaseType && d.type.typeKind==TypeKind.INT || d.type.typeKind==TypeKind.BOOLEAN*/ 
						
		RuntimeEntity re =	 new UnknownValue(s, displacement);
		displacement +=s;
		d.setEntity(re);
	//	Machine.emit(Op.PUSH, 1);
			}
			else if(d instanceof MethodDecl ){
				displacement = 3;
				RuntimeEntity re =	 new KnownAddress(1, s);
				
				d.setEntity(re);
			//	Machine.emit(Op.PUSH, 1);
					}
			else if(d.type instanceof ClassType && d instanceof FieldDecl ){
				 ClassType ct = (ClassType) (d.type);
				 ClassDecl cd =  (ClassDecl)ct.className.getDecl();
				 int sA = cd.fieldDeclList.size();
				
				 RuntimeEntity re =	 new UnkownAddress(sA );
				 d.setEntity(re);
		 
					}
			
			
		}
		
		
		
		private void encodeFetch( Reference r){
			
			Declaration d = r.getDecl();
			RuntimeEntity re = d.getEntity();
			
				Machine.emit(Op.LOAD, Reg.LB, re.address);
			
			
			
	//		if(re instanceof UnknownValue){
	//		Machine.emit(Op.LOADI,  );
		//	}
			
		}
		
private void encodeFetch( Identifier id){
			
			Declaration d = id.getDecl();
			RuntimeEntity re = d.getEntity();
			
				Machine.emit(Op.LOAD, Reg.LB, re.address);
			
			
	 
			
		}
	    
		///////////////////////////////////////////////////////////////////////////////
		//
		// PACKAGE
		//
		/////////////////////////////////////////////////////////////////////////////// 

	    public Object visitPackage(Package prog, Boolean isLHS){
	
			Machine.emit(Op.LOADL,0);            // array length 0
			Machine.emit(Prim.newarr);           // empty String array argument
			int patchAddr_Call_main = Machine.nextInstrAddr();  // record instr addr where
			            fp.addFunction(mainMethodDecl, patchAddr_Call_main);                                        // "main" is called
			Machine.emit(Op.CALL,Reg.CB,-1);     // static call main (address to be patched)
			Machine.emit(Op.HALT,0,0,0);         // end execution
	        ClassDeclList cl = prog.classDeclList;
	      
	        
	        for (ClassDecl c: prog.classDeclList){
	            c.visit(this, false);
	        }
		    fp.patchMembers();
	        return null;
	    }
	    
	    
		///////////////////////////////////////////////////////////////////////////////
		//
		// DECLARATIONS
		//
		///////////////////////////////////////////////////////////////////////////////
	    
	    public Object visitClassDecl(ClassDecl clas, Boolean isLHS){
	        
	    
	        for (int i= 0; i< clas.fieldDeclList.size(); i++){
	        	FieldDecl f = clas.fieldDeclList.get(i);
	        	createEntity(f,i);
	        	f.visit(this, false);
	        	
	        }
	        for (MethodDecl m: clas.methodDeclList)
	        	m.visit(this, false);
	        return null;
	    }
	    
	    public Object visitFieldDecl(FieldDecl f, Boolean isLHS){
	    	
	    	f.type.visit(this, false);
	     
	        return null;
	    }
	    
	    public Object visitMethodDecl(MethodDecl m, Boolean isLHS){
	    	int address = Machine.nextInstrAddr();
	    	createEntity(m, address);
	    	Statement s;
	     	m.type.visit(this, false);
	     	Type retType =m.type;
	     	boolean voidLastReturn =false;
	     	 
	     	
	        ParameterDeclList pdl = m.parameterDeclList;
	       
	       
	        for (ParameterDecl pd: pdl) {
	            pd.visit(this, false);
	        }
	        StatementList sl = m.statementList;
	       
	        for (int i = 0;i<sl.size(); i++) {
	        		s=sl.get(i);
	        		        	
	            s.visit(this, false);
	            
	            if(s instanceof ReturnStmt && retType.typeKind!=TypeKind.VOID ){
	            	Machine.emit(Op.RETURN,1,0,m.parameterDeclList.size());  
	        	}
	            else if (s instanceof ReturnStmt && retType.typeKind==TypeKind.VOID){
	            	Machine.emit(Op.RETURN,0,0,m.parameterDeclList.size());  
	            	if(i==sl.size()-1 ){
	            		voidLastReturn = true; // if void function has last statement as return, turn on the flag
	            	}
	        	}
	            
	            
	        }
	        if(!voidLastReturn &&  retType.typeKind==TypeKind.VOID )
	        Machine.emit(Op.RETURN,0,0,m.parameterDeclList.size()); // if   void function DOES NOT have last statement as return
	        
	        return null;	        
	    }
	    
	    public Object visitParameterDecl(ParameterDecl pd, Boolean isLHS){
	      
	        pd.type.visit(this, false);
	       
	        return null;
	    } 
	    
	    public Object visitVarDecl(VarDecl vd, Boolean isLHS){
	    	createEntity(  vd, 1);
	        vd.type.visit(this, false);
	        
	        return null;
	    }
	 
		
		///////////////////////////////////////////////////////////////////////////////
		//
		// TYPES
		//
		///////////////////////////////////////////////////////////////////////////////
	    
	    public Object visitBaseType(BaseType type, Boolean isLHS){
	   
	        return null;
	    }
	    
	    public Object visitClassType(ClassType type, Boolean isLHS){
	       
	        return null;
	    }
	    
	    public Object visitArrayType(ArrayType type, Boolean isLHS){
	         
	        type.eltType.visit(this, false);
	        return null;
	    }
	    
		
		///////////////////////////////////////////////////////////////////////////////
		//
		// STATEMENTS
		//
		///////////////////////////////////////////////////////////////////////////////

	    public Object visitBlockStmt(BlockStmt stmt, Boolean isLHS){
	       
	        StatementList sl = stmt.sl;
	        
	     
	        for (Statement s: sl) {
	        	s.visit(this, false);
	        }
	        return null;
	    }
	    
	    public Object visitVardeclStmt(VarDeclStmt stmt, Boolean isLHS){
	    	     
	        stmt.varDecl.visit(this, false);	
	        stmt.initExp.visit(this, false);
	       
	       
	        return null;
	    }
	    
	    public Object visitAssignStmt(AssignStmt stmt, Boolean isLHS){
	        
	    	 
	        stmt.val.visit(this, false);
	        stmt.ref.visit(this, true);
            return null;
	    }
	    
	    public Object visitIxAssignStmt(IxAssignStmt stmt, Boolean isLHS){
	        
	        stmt.ixRef.visit(this, false);
	        stmt.val.visit(this, false);
	        return null;
	    }
	    
	    public Object visitCallStmt(CallStmt stmt, Boolean isLHS){
	      
	        stmt.methodRef.visit(this, false);
	        ExprList al = stmt.argList;
 
	       
	        for (Expression e: al) {
	            e.visit(this, false);
	        }
	        return null;
	    }
	    
	    public Object visitReturnStmt(ReturnStmt stmt, Boolean isLHS){
	       
	         if (stmt.returnExpr != null)
	            stmt.returnExpr.visit(this, false);
	        return null;
	    }
	    
	    public Object visitIfStmt(IfStmt stmt, Boolean isLHS){
	       
	        stmt.cond.visit(this, false);
	        int i = Machine.nextInstrAddr();
	        Machine.emit(Op.JUMPIF, 0, Reg.CB, 0);
	        stmt.thenStmt.visit(this, false);
	        int j = Machine.nextInstrAddr();
	        Machine.emit(Op.JUMP, 0, Reg.CB, 0);
	        int g = Machine.nextInstrAddr();
	        Machine.patch(i, g); 
	        if(stmt.elseStmt != null)
	            stmt.elseStmt.visit(this, false);
	        int h = Machine.nextInstrAddr();
	        Machine.patch(j, h);
	        return null;
	    }
	    
	    public Object visitWhileStmt(WhileStmt stmt, Boolean isLHS){
	        int j = Machine.nextInstrAddr();
	        Machine.emit(Op.JUMP, 0, Reg.CB, 0);
	        int g = Machine.nextInstrAddr();
	        stmt.body.visit(this, false);
	        int h = Machine.nextInstrAddr();
	        Machine.patch(j, h);
	        stmt.cond.visit(this, false);
	        Machine.emit(Op.JUMPIF, 1, Reg.CB, g);
	        return null;
	    }
	    

		///////////////////////////////////////////////////////////////////////////////
		//
		// EXPRESSIONS
		//
		///////////////////////////////////////////////////////////////////////////////

	    public Object visitUnaryExpr(UnaryExpr expr, Boolean isLHS){
	        expr.expr.visit(this, false);
	    	Prim p =null;
	    	String spelling = expr.operator.spelling;
	    	if(opToPrimMap.containsKey(spelling))
	    	{
	    		p = opToPrimMap.get(spelling);
	    	}
	    	else if(spelling.equals("-"))
	    	{
	    		p = Prim.neg;
	    	}
	        expr.operator.visit(this, false);
	        Machine.emit(p);
	        return null;
	    }
	    
	    public Object visitBinaryExpr(BinaryExpr expr, Boolean isLHS){
	        expr.left.visit(this, false);
	        expr.right.visit(this, false);	         
	    	Prim p = null;
	    	String spelling = expr.operator.spelling;
	    	if(opToPrimMap.containsKey(spelling))
	    	{
	    		p = opToPrimMap.get(spelling);
	    	}
	    	else if(spelling.equals("-"))
	    	{
	    		p = Prim.sub;
	    	}	    	
	        expr.operator.visit(this, false);
	        Machine.emit(p);
	        return null;
	    }
	    
	    public Object visitRefExpr(RefExpr expr, Boolean isLHS){
	       
	        expr.ref.visit(this, false);
	        return null;
	    }
	    
	    public Object visitCallExpr(CallExpr expr, Boolean isLHS){
	        
	        expr.functionRef.visit(this, false);
	        ExprList al = expr.argList;
	        
	        
	        for (Expression e: al) {
	            e.visit(this, false);
	        }
	        return null;
	    }
	    
	    public Object visitLiteralExpr(LiteralExpr expr, Boolean isLHS){
	        
	        expr.lit.visit(this, false);
	        return null;
	    }
	 
	    public Object visitNewArrayExpr(NewArrayExpr expr, Boolean isLHS){
	       
	        expr.eltType.visit(this, false);
	        expr.sizeExpr.visit(this, false);
	        return null;
	    }
	    
	    public Object visitNewObjectExpr(NewObjectExpr expr, Boolean isLHS){
	        expr.classtype.visit(this, false);
	        
	       if( expr.classtype.className.getDecl() instanceof ClassDecl){
	    	   ClassDecl cd=  (ClassDecl) expr.classtype.className.getDecl() ;
	    	   allocateOnHeap(cd);
	       }
	       else
	    	   System.out.println("New obj in visitNewObjectExpr is not classDecl ");
	    	   
	        	 
	        
	        return null;
	    }
	    

		///////////////////////////////////////////////////////////////////////////////
		//
		// REFERENCES
		//
		///////////////////////////////////////////////////////////////////////////////
		
	    public Object visitQualifiedRef(QualifiedRef qr, Boolean isLHS) {
	     
	    	qr.id.visit(this, false);
	    	int addr = Machine.nextInstrAddr();
	    	Machine.emit(Op.LOADL,-1);
	    	if(qr.id.getDecl() instanceof FieldDecl){
	    		FieldDecl fd = (FieldDecl) qr.id.getDecl();
	    		fp.addField(fd,addr);	
	    	}
	    	else
	    	{
	    		System.out.println("Identifier of qualified reference is not FieldDecl");
	    	}
	    	qr.ref.visit(this, false);
		    return null;
	    }
	    
	    public Object visitIndexedRef(IndexedRef ir, Boolean isLHS) {
	     
	    	ir.indexExpr.visit(this, false);
	    	ir.idRef.visit(this, false);
	    	return null;
	    }
	    
	    public Object visitIdRef(IdRef ref, Boolean isLHS) {
	    
	    	ref.id.visit(this, false);
	    	return null;
	    }
	   
	    public Object visitThisRef(ThisRef ref, Boolean isLHS) {
	    	 
	    	if(isLHS == null ){
	    		
	    	}
	    	else if(isLHS ==true)  {
	    		
	    	}
	    	else
	    		System.out.println("In visitThisRef, isLHS is false, shouldnt happen. isLHS=true/null");
	    		
	    	
	    	return null;
	    }
	    
	    
		///////////////////////////////////////////////////////////////////////////////
		//
		// TERMINALS
		//
		///////////////////////////////////////////////////////////////////////////////
	    
	    public Object visitIdentifier(Identifier id, Boolean isLHS){
	    	if(isLHS){
	    		encodeAssign(id.getDecl());
	    	}
	    	else{
	    encodeFetch(id);
	    	}
	        return null;
	    }
	    
	    public Object visitOperator(Operator op, Boolean isLHS){
	        return null;
	    }
	    
	    public Object visitIntLiteral(IntLiteral num, Boolean isLHS){
	    	int numInt = Integer.parseInt(num.spelling);
	    	Machine.emit(Op.LOADL, numInt);
	        return null;
	    }
	    
	    public Object visitBooleanLiteral(BooleanLiteral bool, Boolean isLHS){
	    	if(bool.spelling.equals("true")){
	    		Machine.emit(Op.LOADL, 0);
	    	}
	    	else{
	    		Machine.emit(Op.LOADL, 1);
	    	}
	        return null;
	    }
 

		public Object visitNullDecl(NullDecl decl, Boolean isLHS) {
			// TODO Auto-generated method stub
			return null;
		}





		public Object visitNullRef(NullRef ref, Boolean isLHS) {
			// TODO Auto-generated method stub
			return null;
		}
	
 


private  class functionPatcher{
	
	private   ArrayList< MemberDecl> memdl;
	private  ArrayList<Integer> patchAddressCallList;
	
	
	public functionPatcher(){
		memdl = new  ArrayList< MemberDecl>() ;
		patchAddressCallList = new ArrayList<Integer> ();
		
	}
	
	public void addFunction(MethodDecl md, int  patchAddCall){
		memdl.add(md);
		patchAddressCallList.add(patchAddCall);
	}
	
	public void addField(FieldDecl fd, int  patchAddCall){
		memdl.add(fd);
		patchAddressCallList.add(patchAddCall);
	}
	
	public void patchMembers(){
		
	if(patchAddressCallList.size() == memdl.size()){
		
		for (int i=0;i<memdl.size();i++){
			
			Machine.patch(patchAddressCallList.get(i), memdl.get(i).getEntity().address);
			
		}
		
		
	}
	else
		System.out.println("Size of function list and patch address is not same");
		
	}
	
}

 


}
	
	
 
