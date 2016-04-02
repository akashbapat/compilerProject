package miniJava.CodeGen;

import mJAM.*;
import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.AbstractSyntaxTrees.Package;
 
 


public class codeGenerator implements Visitor<String,Object> {
	
		
		ErrorReporter reporter;
	   
	   
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
	    
	   



		public codeGenerator(ErrorReporter er){
		 
			reporter =er;
			 
			
			Machine.initCodeGen();

		}

		 class CodeGenError extends Error {
				private static final long serialVersionUID = 1L;	
			}

		private void codeGenError(String e) throws CodeGenError {
			reporter.reportError("*** CodeGenError error: " + e);
			throw new CodeGenError();

		}
	      
	    
		///////////////////////////////////////////////////////////////////////////////
		//
		// PACKAGE
		//
		/////////////////////////////////////////////////////////////////////////////// 

	    public Object visitPackage(Package prog, String arg){
	      
	        ClassDeclList cl = prog.classDeclList;
	      
	        String pfx = arg + "  . "; 
	        for (ClassDecl c: prog.classDeclList){
	            c.visit(this, pfx);
	        }
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
	        return null;
	    }
	    
	    public Object visitParameterDecl(ParameterDecl pd, String arg){
	      
	        pd.type.visit(this, null);
	       
	        return null;
	    } 
	    
	    public Object visitVarDecl(VarDecl vd, String arg){
	      
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
	        
	        stmt.ref.visit(this, null);
	        stmt.val.visit(this, null);
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
	        stmt.thenStmt.visit(this, null);
	        if (stmt.elseStmt != null)
	            stmt.elseStmt.visit(this, null);
	        return null;
	    }
	    
	    public Object visitWhileStmt(WhileStmt stmt, String arg){
	        
	        stmt.cond.visit(this, null);
	        stmt.body.visit(this, null);
	        return null;
	    }
	    

		///////////////////////////////////////////////////////////////////////////////
		//
		// EXPRESSIONS
		//
		///////////////////////////////////////////////////////////////////////////////

	    public Object visitUnaryExpr(UnaryExpr expr, String arg){
	        
	        expr.operator.visit(this, null);
	        expr.expr.visit(this, null);
	        return null;
	    }
	    
	    public Object visitBinaryExpr(BinaryExpr expr, String arg){
	         
	        expr.operator.visit(this, null);
	        expr.left.visit(this, null);
	        expr.right.visit(this, null);
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
	        
	        return null;
	    }
	    
	    public Object visitOperator(Operator op, String arg){
	      
	        return null;
	    }
	    
	    public Object visitIntLiteral(IntLiteral num, String arg){
	       
	        return null;
	    }
	    
	    public Object visitBooleanLiteral(BooleanLiteral bool, String arg){
	    
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
	}
	
	
 
