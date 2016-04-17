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
import java.util.Stack;
 
public class codeGenerator implements Visitor<Boolean,Object> {
	private HashMap<String,Prim> opToPrimMap; 
	functionPatcher fp;
	ErrorReporter reporter;
	MethodDecl mainMethodDecl;
	CodeGenEntityCreator cgec;
	int displacement;
	
	
	   boolean inSystem,inOut,inPrintln, inArray;
	
	
	
	
	public boolean generate(AST ast){
		System.out.println("======= Generating code =====================");
		try{
			cgec.generate(ast);
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
		//Machine.initCodeGen();
		mainMethodDecl=md;
		displacement=3;
		cgec = new CodeGenEntityCreator(er);
		//Initializing flags for println
		    inSystem = false;
		    inOut = false;
		    inPrintln = false;
		    inArray = false;
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
		opToPrimMap.put("<",Prim.lt);
		opToPrimMap.put(">",Prim.gt);
	}
	//private void encodeAssign(Declaration d1,Declaration d2)
	//{

	//}

	private void allocateOnHeap(ClassDecl d){
		FieldDecl fd;
		 ClassDecl cdfd;
		 ClassType ct;
		 Declaration ctD;
		 
		int classSize =  cgec.getClassDeclSize(d) ;
		Machine.emit(Op.LOADL,-1 );  // inheritance flag indicating no superclass		 	
		Machine.emit(Op.LOADL,classSize ); 	
		Machine.emit(Prim.newobj);	
		
	/*	for (int i=0;i<classSize;i++){ // this was for automatic child object creation
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
			
			
			
			
		}*/
		
		
		
	}




	private void encodeAssign(Declaration d ){

	/*	if((d.type instanceof ClassType) || (d.type instanceof BaseType && (d.type.typeKind==TypeKind.INT || d.type.typeKind==TypeKind.BOOLEAN)) && d.getEntity()!=null){			 
			if(d instanceof FieldDecl){
				if(((FieldDecl) d).isStatic)
				{
					Machine.emit(Op.STORE, 1,Reg.SB, d.getEntity().address) ;
				}
				else
				{
					}
			}
			else{
				Machine.emit(Op.STORE, 1,Reg.LB, d.getEntity().address) ;
			}
		}*/
		
		
		 
		
		
		
		//valid   access
		
		FieldDecl fd;
		
	//	if(d instanceof ClassDecl){
		
	//	}
	 	
		
		if((d.type instanceof ClassType) || (d.type instanceof BaseType && (d.type.typeKind==TypeKind.INT || d.type.typeKind==TypeKind.BOOLEAN)) && d.getEntity()!=null){			 
		
			if(d instanceof  LocalDecl)
			Machine.emit(Op.STORE, 1,Reg.LB, d.getEntity().address) ;
			
			else if (d instanceof  FieldDecl){
				fd = (FieldDecl) d;
				if(fd.isStatic)
				Machine.emit(Op.STORE, 1,Reg.SB, d.getEntity().address) ;
				else
				Machine.emit(Op.STORE, 1,Reg.OB, d.getEntity().address) ;
			}
			
			
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
		else if( d instanceof FieldDecl ){

			if(d.type instanceof ClassType){
				ClassType ct = (ClassType) (d.type);
				ClassDecl cd =  (ClassDecl)ct.className.getDecl();
				int n = cd.fieldDeclList.size();

				RuntimeEntity re =	 new KnownAddress(n,s);
				d.setEntity(re);
			}
			else if(d.type instanceof BaseType || d.type instanceof ArrayType){ //arraytype and basetype


				RuntimeEntity re =	 new KnownAddress(1 , s);
				d.setEntity(re);

			}
			else{
				System.out.println("Create Entity failed:FieldDecl is not of type class , or array, or base ");
			}

		}



	}



	private void encodeFetch( Declaration d){


		RuntimeEntity re = d.getEntity();


		if( d instanceof FieldDecl ){
			if(((FieldDecl) d).isStatic){
				Machine.emit(Op.LOADL, re.address);
			}
			else{
				Machine.emit(Op.LOADL, re.address);
			}
		}
		else if(d instanceof VarDecl){
			Machine.emit(Op.LOAD, Reg.LB, re.address);
		}
		else if(d instanceof ClassDecl){
			Machine.emit(Op.LOADL, 0);
		}
		else if(d instanceof MethodDecl){
			int address = Machine.nextInstrAddr();
			Machine.emit(Op.CALLI,Reg.CB,-1);
			fp.addFunction((MethodDecl)d, address);
		}
		else if(d instanceof ParameterDecl){
			Machine.emit(Op.LOAD, Reg.LB,re.address);
		}
		else{
			System.out.println("Failed to encode");
		}



	}

	  private Boolean checkQRForPrintln(QualifiedRef qr){
          Stack<Identifier> unRolledRef = new Stack<Identifier>();
          Reference ref = qr;
          do{
              QualifiedRef qqr = (QualifiedRef)ref;
              unRolledRef.push(qqr.id);
              ref = qqr.ref;
          }while (ref instanceof QualifiedRef);
          IdRef idr = (IdRef)ref;
          unRolledRef.push(idr.id);
          
          if(unRolledRef.size() == 3){
              Identifier id = unRolledRef.pop();
              Declaration d = id.getDecl();
              if(d instanceof ClassDecl){
                  ClassDecl cd = (ClassDecl)d;
                  if(cd.name.equals("System")){
                      id = unRolledRef.pop();
                      d = id.getDecl();
                      FieldDecl fd = (FieldDecl)d;
                      if (fd.type.typeKind == TypeKind.CLASS){
                          ClassType ct = (ClassType)fd.type;
                          if(fd.name.equals("out") && ct.className.spelling.equals("_PrintStream") ){
                              id = unRolledRef.pop();
                              d = id.getDecl();
                              if(d instanceof MethodDecl){
                                  MethodDecl md = (MethodDecl) d;
                                  if(md.name.equals("println") && md.parameterDeclList.size() == 1){
                                  //    md.parameterDeclList.get(0).visit(this, false);
                                	  
                                	  
                                	  
                                      Machine.emit(Prim.putintnl);
                                      return true;
                                  }
                                  else if(md.name.equals("Println") && md.parameterDeclList.size() != 1 ){
                                      codeGenError("Incorrect number of arguments in println at line: "+md.posn.line);
                                      return false;
                                  }
                                  else{
                                      return false;
                                  }
                              }
                              else{
                                  return false;
                              }
                              
                          }
                          else{
                              return false;
                          }
                      }
                      else{
                          return false;
                      }
                  }
                  else{
                      return false;
                  }
          }
          else{
              return false;
          }
                  
          }
          else
          {
              return false;
          }
      }
	
	
	private Boolean isQRlength(QualifiedRef qr){
		Stack<Identifier> unRolledRef = new Stack<Identifier>();
		Reference ref = qr; 
		do{
			QualifiedRef qqr = (QualifiedRef)ref;
			unRolledRef.push(qqr.id);
			ref = qqr.ref;
		}while (ref instanceof QualifiedRef);
		IdRef idr = (IdRef)ref;
		unRolledRef.push(idr.id);
		if(unRolledRef.size() == 2){
			Identifier idArr = unRolledRef.pop();
			Declaration d = idArr.getDecl();
			if(d instanceof VarDecl){
				VarDecl vd = (VarDecl)d;
				if(vd.type.typeKind == TypeKind.ARRAY){
					Identifier id = unRolledRef.pop();
					d = id.getDecl();
					if(id.spelling.equals("length")){
						idArr.visit(this, false);
						Machine.emit(Prim.arraylen);
						return true;
					}
					else
					{
						return false;
					}
				}
				else{
					return false;
				}
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
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
			//createEntity(f,i);
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
				Machine.emit(Op.RETURN,1,Reg.LB,m.parameterDeclList.size());  
			}
			else if (s instanceof ReturnStmt && retType.typeKind==TypeKind.VOID){
				Machine.emit(Op.RETURN,0,Reg.LB,m.parameterDeclList.size());  
				if(i==sl.size()-1 ){
					voidLastReturn = true; // if void function has last statement as return, turn on the flag
				}
			}


		}
		if(!voidLastReturn &&  retType.typeKind==TypeKind.VOID )
			Machine.emit(Op.RETURN,0,Reg.LB,m.parameterDeclList.size()); // if   void function DOES NOT have last statement as return

		return null;	        
	}

	public Object visitParameterDecl(ParameterDecl pd, Boolean isLHS){

		pd.type.visit(this, false);

		return null;
	} 

	public Object visitVarDecl(VarDecl vd, Boolean isLHS){
		createEntity(  vd, 1);
		// vd.type.visit(this, false);

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
		IdRef idr;
		stmt.ref.visit(this, true);
		stmt.val.visit(this, false);
		if(stmt.ref instanceof QualifiedRef){
			Machine.emit(Prim.fieldupd);
		}
		else if(stmt.ref instanceof IdRef) {
			idr = (IdRef) stmt.ref ;
			encodeAssign(idr.id.getDecl() );
		}
		else
		encodeAssign(stmt.ref.getDecl() );
		
		return null;
	}

	public Object visitIxAssignStmt(IxAssignStmt stmt, Boolean isLHS){

		stmt.ixRef.visit(this, false);
		stmt.val.visit(this, false);
		return null;
	}
	// This is a comment
	public Object visitCallStmt(CallStmt stmt, Boolean isLHS){

		ExprList al = stmt.argList;


		for (Expression e: al) {
			e.visit(this, false);
		}
		
		
		stmt.methodRef.visit(this, false);
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

		
		ExprList al = expr.argList;


		for (Expression e: al) {
			e.visit(this, false);
		}
		
		
		expr.functionRef.visit(this, false);
		 
		
		return null;
	}

	public Object visitLiteralExpr(LiteralExpr expr, Boolean isLHS){

		expr.lit.visit(this, false);
		return null;
	}

	 public Object visitNewArrayExpr(NewArrayExpr expr, Boolean isLHS){
	    	expr.sizeExpr.visit(this, false);
	    	Machine.emit(Prim.newarr);
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
		if(!checkQRForPrintln(qr) && !(isQRlength(qr))){
 		qr.ref.visit(this, false); //even if qr is in lhs, we need to load addresses
		 
		qr.id.visit(this, false);
	 	
		if(!isLHS && !( qr.id.getDecl() instanceof MethodDecl))
		  Machine.emit(Prim.fieldref); //last call should not happen
  
	  

		}
 			 
	 
		return null;
	}

	public Object visitIndexedRef(IndexedRef ir, Boolean isLHS) {

		ir.indexExpr.visit(this, false);
		ir.idRef.visit(this, false);
		return null;
	}

	public Object visitIdRef(IdRef ref, Boolean isLHS) {

		ref.id.visit(this, isLHS);
		return null;
	}

	public Object visitThisRef(ThisRef ref, Boolean isLHS) {
		Machine.emit(Op.LOAD,Machine.Reg.OB,0);	    	

		return null;
	}


	///////////////////////////////////////////////////////////////////////////////
	//
	// TERMINALS
	//
	///////////////////////////////////////////////////////////////////////////////

	public Object visitIdentifier(Identifier id, Boolean isLHS){
		 
		if(!isLHS)
			encodeFetch(id.getDecl());
		 
			
		 
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

	//done
	public Object visitNullDecl(NullDecl decl, Boolean isLHS) {
		// TODO Auto-generated method stub
		return null;
	}




	//done
	public Object visitNullRef(NullRef ref, Boolean isLHS) {
		Machine.emit(Op.LOADL, Machine.nullRep); 
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



