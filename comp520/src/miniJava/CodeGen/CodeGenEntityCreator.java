package miniJava.CodeGen;


import mJAM.*;
import mJAM.Machine.Op;
import mJAM.Machine.Prim;
import mJAM.Machine.Reg;
import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.AbstractSyntaxTrees.Package;
import miniJava.CodeGen.codeGenerator.CodeGenError;
//import miniJava.CodeGen.codeGenerator.functionPatcher;


public class CodeGenEntityCreator implements Visitor<Object,Object>{
		ErrorReporter reporter;
	    int displacement;
	    int stack_displacement;
	    int object_displacement;
	    public Boolean generate(AST ast){
	        System.out.println("======= Generating Enitities for Code and declaring static variables =====================");
	    try{
	    	 ast.visit(this, null);
	    }
	    catch (CodeGenError cge) {
			System.out.println("Code generator error occurred");
			return false;
		}
	    
	        System.out.println("=============================================");
	        return true;
	    }   
	    
		public CodeGenEntityCreator(ErrorReporter er){
			reporter = er;
			displacement=3;
			Machine.initCodeGen();
		}
		
		 class CodeGenError extends Error {
				private static final long serialVersionUID = 1L;	
			}

		private void codeGenError(String e) throws CodeGenError {
			reporter.reportError("*** CodeGenError error: " + e);
			throw new CodeGenError();

		}
		
		
		private void allocateOnStack(FieldDecl fd){
			if(fd.type instanceof ClassType){
				 ClassType ct = (ClassType) (fd.type);
				 ClassDecl cd =  (ClassDecl)ct.className.getDecl();
				 allocateOnHeap(cd);
				 //int n = getClassDeclSize(cd);
				 //Machine.emit(Op.PUSH, 1);
				}
				else if(fd.type instanceof BaseType || fd.type instanceof ArrayType){ //arraytype and basetype
					Machine.emit(Op.PUSH, 1);
				}
				else{
					System.out.println("allocateOnStack failed:FieldDecl is not of type class , or array, or base ");
				}
		}
		
		public int getClassDeclSize(ClassDecl cd){
			int size = 0;
			for (int i= 0; i< cd.fieldDeclList.size(); i++){
				FieldDecl fd = cd.fieldDeclList.get(i);
				if(!fd.isStatic){
					size++;
				}
			}
			return size;
		}
		
		private void allocateOnHeap(ClassDecl d){
			FieldDecl fd;
			 ClassDecl cdfd;
			 ClassType ct;
			 Declaration ctD;
			 
			int classSize =  getClassDeclSize(d) ;
			Machine.emit(Op.LOADL,-1 );  // inheritance flag indicating no superclass		 	
			Machine.emit(Op.LOADL,classSize ); 	
			Machine.emit(Prim.newobj);	
			
			for (int i=0;i<classSize;i++){ // this was for automatic child object creation
				fd =d.fieldDeclList.get(i);
				if(fd.type instanceof ClassType){
					
					
					
					ct = (ClassType) fd.type ;
					ctD = ct.className.getDecl();
					if(ctD instanceof ClassDecl){
						
						Machine.emit(Op.LOAD, Reg.ST, -1);
						Machine.emit(Op.LOADL, i);
				 
		  	allocateOnHeap( ( ClassDecl) ct.className.getDecl());
		  				Machine.emit(Prim.fieldupd); // patching pointers of child classes
		  	
		  	
		  	
					}
		  	
		  	
		  	
				
				}
				
				
				
				
			}
			
			
			
		}

		private void createEntity(Declaration d, int s,Reg base){

		if(d instanceof VarDecl ){ /*d.type instanceof BaseType && d.type.typeKind==TypeKind.INT || d.type.typeKind==TypeKind.BOOLEAN*/ 
						
		RuntimeEntity re =	 new UnknownValue(s, displacement);
		displacement +=s;
		d.setEntity(re);
			}
			else if(d instanceof MethodDecl ){
				displacement = 3;
				RuntimeEntity re =	 new KnownAddress(1, s);
				re.base = base;
				d.setEntity(re);
					}
			else if( d instanceof FieldDecl ){
				if(((FieldDecl) d).isStatic){
						if(d.type instanceof ClassType){
						 ClassType ct = (ClassType) (d.type);
						 ClassDecl cd =  (ClassDecl)ct.className.getDecl();
						 int n = getClassDeclSize(cd);
						
						 RuntimeEntity re =	 new KnownAddress(n,stack_displacement);
						 re.base = base; 
						 d.setEntity(re);
						 stack_displacement++;
						}
						else if(d.type instanceof BaseType || d.type instanceof ArrayType){ //arraytype and basetype
							
							
							 RuntimeEntity re =	 new KnownAddress(1 , stack_displacement);
							 re.base = base;
							 d.setEntity(re);
							 stack_displacement++;
							
						}
						else{
							System.out.println("Create Entity failed:FieldDecl is not of type class , or array, or base ");
						}
				}
				else{
					if(d.type instanceof ClassType){
						 ClassType ct = (ClassType) (d.type);
						 ClassDecl cd =  (ClassDecl)ct.className.getDecl();
						 int n = cd.fieldDeclList.size();
						
						 RuntimeEntity re =	 new KnownAddress(n,s);
						 re.base = base; 
						 d.setEntity(re);
						}
						else if(d.type instanceof BaseType || d.type instanceof ArrayType){ //arraytype and basetype
							
							
							 RuntimeEntity re =	 new KnownAddress(1 , s);
							 re.base = base;
							 d.setEntity(re);
							
						}
						else{
							System.out.println("Create Entity failed:FieldDecl is not of type class , or array, or base ");
						}
				}
				
		 
			}
			else if( d instanceof ParameterDecl ){
				
				 RuntimeEntity re =	 new KnownAddress(1,s );
				 re.base = base; 
				 d.setEntity(re);
				
			}
		}
		
		
		///////////////////////////////////////////////////////////////////////////////
		//
		// PACKAGE
		//
		/////////////////////////////////////////////////////////////////////////////// 

	    public Object visitPackage(Package prog,Object obj){
	
	        for (ClassDecl c: prog.classDeclList){
	            c.visit(this, null);
	        }
	        return prog;
	    }
	    
		///////////////////////////////////////////////////////////////////////////////
		//
		// DECLARATIONS
		//
		///////////////////////////////////////////////////////////////////////////////
	    
	    public Object visitClassDecl(ClassDecl clas, Object obj){
	    	int k = 0;
	        for (int i= 0; i< clas.fieldDeclList.size(); i++){
	        	FieldDecl f = clas.fieldDeclList.get(i);
	        	if(f.isStatic)
	        	{
	        		createEntity(f,-1,Reg.SB);
	        		allocateOnStack(f);
	        		
	        	}
	        	else
	        	{
	        		createEntity(f,k,Reg.OB);
	        		k++;
	        	}
	        	f.visit(this, null);
	        	
	        }
	        for (MethodDecl m: clas.methodDeclList)
	        	m.visit(this, null);
	        
	        return clas;
	    }
	    
	    public Object visitFieldDecl(FieldDecl f, Object obj){
	    	
	    	f.type.visit(this, false);
	     
	        return null;
	    }
	    
	    public Object visitMethodDecl(MethodDecl m, Object obj){
	    	ParameterDecl pd;
	    	createEntity(m, -1,Reg.CB); //creating entity
	    	Statement s;
	     	m.type.visit(this, false);
	     	
	        ParameterDeclList pdl = m.parameterDeclList;
	       
	       
	        for (int i=0; i< pdl.size();i++) {
	        	pd=pdl.get(i);
	        	createEntity(pd, i - pdl.size() ,Reg.LB);
	            pd.visit(this, false);
	        }
	        StatementList sl = m.statementList;
	       
	        for (int i = 0;i<sl.size(); i++) {
	        		s=sl.get(i);
	        		        	
	            s.visit(this, null);
	            
	            
	        }	        
	        return null;	        
	    }
	    
	    public Object visitParameterDecl(ParameterDecl pd, Object obj){
	      
	        pd.type.visit(this, null);
	       
	        return null;
	    } 
	    
	    public Object visitVarDecl(VarDecl vd, Object obj){
	    	createEntity(  vd, 1,Reg.LB);
	        vd.type.visit(this, null);
	        return null;
	    }
	    
		
		///////////////////////////////////////////////////////////////////////////////
		//
		// TYPES
		//
		///////////////////////////////////////////////////////////////////////////////
	    
	    public Object visitBaseType(BaseType type, Object obj){
	   
	        return null;
	    }
	    
	    public Object visitClassType(ClassType type, Object obj){
	       
	        return null;
	    }
	    
	    public Object visitArrayType(ArrayType type, Object obj){
	         
	        type.eltType.visit(this, false);
	        return null;
	    }
	    
		///////////////////////////////////////////////////////////////////////////////
		//
		// STATEMENTS
		//
		///////////////////////////////////////////////////////////////////////////////

	    public Object visitBlockStmt(BlockStmt stmt, Object obj){
	       
	        StatementList sl = stmt.sl;
	        
	     
	        for (Statement s: sl) {
	        	s.visit(this, null);
	        }
	        return null;
	    }
	    
	    public Object visitVardeclStmt(VarDeclStmt stmt, Object obj){
		        stmt.varDecl.visit(this, false);	
		        stmt.initExp.visit(this, false);
	        return null;
	    }
	    
	    public Object visitAssignStmt(AssignStmt stmt, Object obj){
	        
	    	stmt.ref.visit(this, true);
	        stmt.val.visit(this, false);
            return null;
	    }
	    
	    public Object visitIxAssignStmt(IxAssignStmt stmt, Object obj){
	        
	        stmt.ixRef.visit(this, false);
	        stmt.val.visit(this, false);
	        return null;
	    }
	    
	    public Object visitCallStmt(CallStmt stmt, Object obj){
	      
	        stmt.methodRef.visit(this, null);
	        ExprList al = stmt.argList;
 
	       
	        for (Expression e: al) {
	            e.visit(this, null);
	        }
	        return null;
	    }
	    
	    public Object visitReturnStmt(ReturnStmt stmt, Object obj){
	       
	         if (stmt.returnExpr != null)
	            stmt.returnExpr.visit(this, null);
	        return null;
	    }
	    
	    public Object visitIfStmt(IfStmt stmt, Object obj){
	       
	        stmt.cond.visit(this, null);
	        stmt.thenStmt.visit(this, null);
	        if(stmt.elseStmt != null)
	            stmt.elseStmt.visit(this, null);
	        return null;
	    }
	    
	    public Object visitWhileStmt(WhileStmt stmt, Object obj){
	        stmt.body.visit(this, false);
	        stmt.cond.visit(this, false);
	        return null;
	    }
	    
	    public Object visitForStmt(ForStmt stmt, Object obj){
	    	//always check for null in for stmt for init,cond and inc
		 
			if(stmt.init!=null)
		 stmt.init.visit(this, null);
			
			if(stmt.cond!=null)
				 stmt.cond.visit(this, null);
			
			if(stmt.increment!=null)
				  stmt.increment.visit(this, null);
			
			stmt.body.visit(this,null);
			 
		 	 
	        return null;
	    }
	    
		///////////////////////////////////////////////////////////////////////////////
		//
		// EXPRESSIONS
		//
		///////////////////////////////////////////////////////////////////////////////

	    public Object visitUnaryExpr(UnaryExpr expr, Object obj){
	        expr.expr.visit(this, null);
	        return null;
	    }
	    
	    public Object visitBinaryExpr(BinaryExpr expr, Object obj){
	        expr.left.visit(this, null);
	        expr.right.visit(this, null);	         
	        return null;
	    }
	    
	    public Object visitRefExpr(RefExpr expr, Object obj){
	       
	        expr.ref.visit(this, null);
	        return null;
	    }
	    
	    public Object visitCallExpr(CallExpr expr, Object obj){
	        
	        expr.functionRef.visit(this, null);
	        ExprList al = expr.argList;
	        
	        
	        for (Expression e: al) {
	            e.visit(this, null);
	        }
	        return null;
	    }
	    
	    public Object visitLiteralExpr(LiteralExpr expr, Object obj){
	        
	        expr.lit.visit(this, null);
	        return null;
	    }
	 
	    public Object visitNewArrayExpr(NewArrayExpr expr, Object obj){
	       
	        expr.eltType.visit(this, null);
	        expr.sizeExpr.visit(this, null);
	        return null;
	    }
	    
	    public Object visitNewObjectExpr(NewObjectExpr expr, Object obj){
	        expr.classtype.visit(this, null);
	  
	        return null;
	    }
	    

		///////////////////////////////////////////////////////////////////////////////
		//
		// REFERENCES
		//
		///////////////////////////////////////////////////////////////////////////////
		
	    public Object visitQualifiedRef(QualifiedRef qr, Object obj) {
	    	qr.ref.visit(this, null);
    		qr.id.visit(this, null);
	    	return null;
	    }
	    
	    public Object visitIndexedRef(IndexedRef ir, Object obj) {
	    	ir.indexExpr.visit(this, false);
	    	ir.idRef.visit(this, false);
	    	return null;
	    }
	    
	    public Object visitIdRef(IdRef ref, Object obj) {
	    	ref.id.visit(this, null);
	    	return null;
	    }
	   
	    public Object visitThisRef(ThisRef ref, Object obj) {
	    		return null;
	    }
	    
	    
		///////////////////////////////////////////////////////////////////////////////
		//
		// TERMINALS
		//
		///////////////////////////////////////////////////////////////////////////////
	    
	    public Object visitIdentifier(Identifier id, Object obj){
	        return null;
	    }
	    
	    public Object visitOperator(Operator op, Object obj){
	        return null;
	    }
	    
	    public Object visitIntLiteral(IntLiteral num, Object obj){
	        return null;
	    }
	    
	    public Object visitBooleanLiteral(BooleanLiteral bool, Object obj){
	        return null;
	    }
 

		public Object visitNullDecl(NullDecl decl, Object obj) {
			// TODO Auto-generated method stub
			return null;
		}


		public Object visitNullRef(NullRef ref, Object obj) {
			// TODO Auto-generated method stub
			return null;
		}


	
}


 
