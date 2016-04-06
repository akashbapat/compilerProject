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
 

 


public class codeGenerator implements Visitor<String,Object> {
		private HashMap<String,Prim> opToPrimMap; 
		functionPatcher fp;
		ErrorReporter reporter;
		MethodDecl mainMethodDecl;
	    int displacement;
	    public boolean generate(AST ast){
	        System.out.println("======= Generating code =====================");
	    try{
	    	ast.visit(this, "");
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
		
		 
		private void encodeAssign(Declaration d ){
			
			 if(d.type instanceof BaseType && (d.type.typeKind==TypeKind.INT || d.type.typeKind==TypeKind.BOOLEAN) && d.getEntity()!=null){			 
				 Machine.emit(Op.STORE, 1,Reg.LB, d.getEntity().address) ;
					}
		}
		
		
		private void createEntity(Declaration d, int s){

			if(d.type instanceof BaseType && d.type.typeKind==TypeKind.INT || d.type.typeKind==TypeKind.BOOLEAN ){
						
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
			
	//		if(re instanceof UnknownValue){
	//		Machine.emit(Op.LOADI,  );
		//	}
			
		}
	    
		///////////////////////////////////////////////////////////////////////////////
		//
		// PACKAGE
		//
		/////////////////////////////////////////////////////////////////////////////// 

	    public Object visitPackage(Package prog, String arg){
	
			Machine.emit(Op.LOADL,0);            // array length 0
			Machine.emit(Prim.newarr);           // empty String array argument
			int patchAddr_Call_main = Machine.nextInstrAddr();  // record instr addr where
			            fp.addFunction(mainMethodDecl, patchAddr_Call_main);                                        // "main" is called
			Machine.emit(Op.CALL,Reg.CB,-1);     // static call main (address to be patched)
			Machine.emit(Op.HALT,0,0,0);         // end execution
	        ClassDeclList cl = prog.classDeclList;
	      
	        String pfx = arg + "  . "; 
	        for (ClassDecl c: prog.classDeclList){
	            c.visit(this, pfx);
	        }
		    fp.patchFunctions();
	        return null;
	    }
	    
	    
		///////////////////////////////////////////////////////////////////////////////
		//
		// DECLARATIONS
		//
		///////////////////////////////////////////////////////////////////////////////
	    
	    public Object visitClassDecl(ClassDecl clas, String arg){
	        
	       
	    
	        String pfx = arg + "  . "; 
	        for (FieldDecl f: clas.fieldDeclList)
	        	f.visit(this, pfx);
	       
	        for (MethodDecl m: clas.methodDeclList)
	        	m.visit(this, pfx);
	        return null;
	    }
	    
	    public Object visitFieldDecl(FieldDecl f, String arg){
	        
	    	f.type.visit(this, null);
	     
	        return null;
	    }
	    
	    public Object visitMethodDecl(MethodDecl m, String arg){
	    	int i = Machine.nextInstrAddr();
	    	createEntity(m, i);
	    	
	    	m.type.visit(this, null);
	    	 
	        ParameterDeclList pdl = m.parameterDeclList;
	       
	        String pfx = ((String) arg) + "  . ";
	        for (ParameterDecl pd: pdl) {
	            pd.visit(this, pfx);
	        }
	        StatementList sl = m.statementList;
	       
	        for (Statement s: sl) {
	            s.visit(this, pfx);
	        }
	        
	        Machine.emit(Op.RETURN,0,0,1);  
	        
	        return null;	        
	    }
	    
	    public Object visitParameterDecl(ParameterDecl pd, String arg){
	      
	        pd.type.visit(this, null);
	       
	        return null;
	    } 
	    
	    public Object visitVarDecl(VarDecl vd, String arg){
	    	createEntity(  vd, 1);
	        vd.type.visit(this, null);
	        
	        return null;
	    }
	 
		
		///////////////////////////////////////////////////////////////////////////////
		//
		// TYPES
		//
		///////////////////////////////////////////////////////////////////////////////
	    
	    public Object visitBaseType(BaseType type, String arg){
	   
	        return null;
	    }
	    
	    public Object visitClassType(ClassType type, String arg){
	       
	        return null;
	    }
	    
	    public Object visitArrayType(ArrayType type, String arg){
	         
	        type.eltType.visit(this, null);
	        return null;
	    }
	    
		
		///////////////////////////////////////////////////////////////////////////////
		//
		// STATEMENTS
		//
		///////////////////////////////////////////////////////////////////////////////

	    public Object visitBlockStmt(BlockStmt stmt, String arg){
	       
	        StatementList sl = stmt.sl;
	        
	        String pfx = arg + "  . ";
	        for (Statement s: sl) {
	        	s.visit(this, pfx);
	        }
	        return null;
	    }
	    
	    public Object visitVardeclStmt(VarDeclStmt stmt, String arg){
	     
	        stmt.varDecl.visit(this, null);	
	        stmt.initExp.visit(this, null);
	        return null;
	    }
	    
	    public Object visitAssignStmt(AssignStmt stmt, String arg){
	        
	    //    stmt.ref.visit(this, null);
	        stmt.val.visit(this, null);
	        
	        if(stmt.ref instanceof IdRef){
	        	IdRef idr = (IdRef) stmt.ref;
	        encodeAssign(idr.id.getDecl());
	        }
	        return null;
	    }
	    
	    public Object visitIxAssignStmt(IxAssignStmt stmt, String arg){
	        
	        stmt.ixRef.visit(this, null);
	        stmt.val.visit(this, null);
	        return null;
	    }
	    
	    public Object visitCallStmt(CallStmt stmt, String arg){
	      
	        stmt.methodRef.visit(this, null);
	        ExprList al = stmt.argList;
 
	        String pfx = arg + "  . ";
	        for (Expression e: al) {
	            e.visit(this, pfx);
	        }
	        return null;
	    }
	    
	    public Object visitReturnStmt(ReturnStmt stmt, String arg){
	       
	         if (stmt.returnExpr != null)
	            stmt.returnExpr.visit(this, null);
	        return null;
	    }
	    
	    public Object visitIfStmt(IfStmt stmt, String arg){
	       
	        stmt.cond.visit(this, null);
	        int i = Machine.nextInstrAddr();
	        Machine.emit(Op.JUMPIF, 0, Reg.CB, 0);
	        stmt.thenStmt.visit(this, null);
	        int j = Machine.nextInstrAddr();
	        Machine.emit(Op.JUMP, 0, Reg.CB, 0);
	        int g = Machine.nextInstrAddr();
	        Machine.patch(i, g); 
	        if(stmt.elseStmt != null)
	            stmt.elseStmt.visit(this, null);
	        int h = Machine.nextInstrAddr();
	        Machine.patch(j, h);
	        return null;
	    }
	    
	    public Object visitWhileStmt(WhileStmt stmt, String arg){
	        int j = Machine.nextInstrAddr();
	        Machine.emit(Op.JUMP, 0, Reg.CB, 0);
	        int g = Machine.nextInstrAddr();
	        stmt.body.visit(this, null);
	        int h = Machine.nextInstrAddr();
	        Machine.patch(j, h);
	        stmt.cond.visit(this, null);
	        Machine.emit(Op.JUMPIF, 1, Reg.CB, g);
	        return null;
	    }
	    

		///////////////////////////////////////////////////////////////////////////////
		//
		// EXPRESSIONS
		//
		///////////////////////////////////////////////////////////////////////////////

	    public Object visitUnaryExpr(UnaryExpr expr, String arg){
	        expr.expr.visit(this, null);
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
	        expr.operator.visit(this, null);
	        Machine.emit(p);
	        return null;
	    }
	    
	    public Object visitBinaryExpr(BinaryExpr expr, String arg){
	        expr.left.visit(this, null);
	        expr.right.visit(this, null);	         
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
	        expr.operator.visit(this, null);
	        Machine.emit(p);
	        return null;
	    }
	    
	    public Object visitRefExpr(RefExpr expr, String arg){
	       
	        expr.ref.visit(this, null);
	        return null;
	    }
	    
	    public Object visitCallExpr(CallExpr expr, String arg){
	        
	        expr.functionRef.visit(this, null);
	        ExprList al = expr.argList;
	        
	        String pfx = arg + "  . ";
	        for (Expression e: al) {
	            e.visit(this, pfx);
	        }
	        return null;
	    }
	    
	    public Object visitLiteralExpr(LiteralExpr expr, String arg){
	        
	        expr.lit.visit(this, null);
	        return null;
	    }
	 
	    public Object visitNewArrayExpr(NewArrayExpr expr, String arg){
	       
	        expr.eltType.visit(this, null);
	        expr.sizeExpr.visit(this, null);
	        return null;
	    }
	    
	    public Object visitNewObjectExpr(NewObjectExpr expr, String arg){
	        
	        expr.classtype.visit(this, null);
	        return null;
	    }
	    

		///////////////////////////////////////////////////////////////////////////////
		//
		// REFERENCES
		//
		///////////////////////////////////////////////////////////////////////////////
		
	    public Object visitQualifiedRef(QualifiedRef qr, String arg) {
	     
	    	qr.id.visit(this, null);
	    	qr.ref.visit(this, null);
		    return null;
	    }
	    
	    public Object visitIndexedRef(IndexedRef ir, String arg) {
	     
	    	ir.indexExpr.visit(this, null);
	    	ir.idRef.visit(this, null);
	    	return null;
	    }
	    
	    public Object visitIdRef(IdRef ref, String arg) {
	    
	    	ref.id.visit(this, null);
	    	return null;
	    }
	   
	    public Object visitThisRef(ThisRef ref, String arg) {
	    	 
	    	return null;
	    }
	    
	    
		///////////////////////////////////////////////////////////////////////////////
		//
		// TERMINALS
		//
		///////////////////////////////////////////////////////////////////////////////
	    
	    public Object visitIdentifier(Identifier id, String arg){
	    encodeFetch(id);
	        return null;
	    }
	    
	    public Object visitOperator(Operator op, String arg){
	        return null;
	    }
	    
	    public Object visitIntLiteral(IntLiteral num, String arg){
	    	int numInt = Integer.parseInt(num.spelling);
	    	Machine.emit(Op.LOADL, numInt);
	        return null;
	    }
	    
	    public Object visitBooleanLiteral(BooleanLiteral bool, String arg){
	    	if(bool.spelling.equals("true")){
	    		Machine.emit(Op.LOADL, 0);
	    	}
	    	else{
	    		Machine.emit(Op.LOADL, 1);
	    	}
	        return null;
	    }
 

		public Object visitNullDecl(NullDecl decl, String arg) {
			// TODO Auto-generated method stub
			return null;
		}





		public Object visitNullRef(NullRef ref, String arg) {
			// TODO Auto-generated method stub
			return null;
		}
	
 


private  class functionPatcher{
	
	private   MethodDeclList mdl;
	private  ArrayList<Integer> patchAddressCallList;
	
	
	public functionPatcher(){
		mdl = new MethodDeclList();
		patchAddressCallList = new ArrayList<Integer> ();
		
	}
	
	public void addFunction(MethodDecl md, int  patchAddCall){
		mdl.add(md);
		patchAddressCallList.add(patchAddCall);
	}
	
	public void patchFunctions(){
		
	if(patchAddressCallList.size() == mdl.size()){
		
		for (int i=0;i<mdl.size();i++){
			
			Machine.patch(patchAddressCallList.get(i), mdl.get(i).getEntity().address);
			
		}
		
		
	}
	else
		System.out.println("Size of function list and patch address is not same");
		
	}
	
}

 


}
	
	
 
