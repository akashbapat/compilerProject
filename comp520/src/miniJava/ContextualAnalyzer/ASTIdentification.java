package miniJava.ContextualAnalyzer;
import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.TokenKind;
import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.AbstractSyntaxTrees.Package;
 

/**
 * SyntaxError is used to unwind parse stack when parse fails
 *
 */


public class ASTIdentification implements Visitor<idTable,idTable>{

	private ErrorReporter reporter;
	private boolean isFuncStatic;

	class IdentificationError extends Error {
		private static final long serialVersionUID = 1L;	
	}



	public ASTIdentification(ErrorReporter er){
		isFuncStatic =false;
		reporter =er;

	}



	private void identificationError(String e) throws IdentificationError {
		reporter.reportError("*** Identification error: " + e);
		throw new IdentificationError();

	}

	public boolean showTree(AST ast){
		System.out.println("======= AST Identify =========================");
		idTable idTab = new idTable();



		try {
			ast.visit(this, idTab);
		}
		catch (IdentificationError ie) {
			System.out.println("Identification error occurred");
			return false;
		}

		System.out.println("Identification successfully completed");
		System.out.println("==============================================");
		return true;
	}


	///////////////////////////////////////////////////////////////////////////////
	//
	// PACKAGE
	//
	/////////////////////////////////////////////////////////////////////////////// 

	public idTable visitPackage(Package prog, idTable idTab){
	//	ClassDeclList cl = prog.classDeclList;
		
		idTab.openScope();
		for (ClassDecl c: prog.classDeclList){
			
			c.type = new ClassType(new Identifier(new Token(TokenKind.ID, c.name)),null   );
			int res = idTab.addDecl(c,idLevel.CLASS_LEVEL,idLevel.CLASS_LEVEL);
			if(res != -1)
			{
				identificationError("Class declaration failed - " + c.name + " already declared at level "+res );
			}
		}
		for (ClassDecl c: prog.classDeclList){
			
			
			idTab = c.visit(this, idTab);
		}
		idTab.printLevel(1);
		idTab.closeScope();
		return idTab;
	}

	///////////////////////////////////////////////////////////////////////////////
	//
	// DECLARATIONS
	//
	///////////////////////////////////////////////////////////////////////////////

	public idTable visitClassDecl(ClassDecl clas, idTable idTab){
		idTab.openScope();
		VarDecl thisVarDecl ;
		 thisVarDecl	= new VarDecl( new ClassType( new Identifier(new Token( TokenKind.ID, clas.name )) ,null), "this", null);
		 
	//	 ClassType ct = (ClassType) thisVarDecl.type; //this is borderline ridiculous
		 
		// ct.className.setDecl(thisVarDecl);//this is borderline ridiculous
		 
		 idTab.addDecl( thisVarDecl, idLevel.MEMBER_LEVEL );
		
		
		for (FieldDecl f: clas.fieldDeclList)
		{
			idTab = f.visit(this, idTab);
		}

		for (MethodDecl m: clas.methodDeclList){
			
			isFuncStatic =m.isStatic;
			idTab = m.visit(this, idTab);
		}

		idTab.printLevel(2);
		idTab.closeScope();
		return idTab;
	}

	public idTable visitFieldDecl(FieldDecl f, idTable idTab){
		idTab = f.type.visit(this, idTab);
		 
		
		int res = idTab.addDecl(f,idLevel.MEMBER_LEVEL,idLevel.MEMBER_LEVEL);
		if(res != -1)
		{
			identificationError("Member declaration failed - " + f.name + " already declared at level " +res);
		}
		return idTab;
	}

	public idTable visitMethodDecl(MethodDecl m, idTable idTab){
		idTab.addDecl(m,idLevel.MEMBER_LEVEL,idLevel.MEMBER_LEVEL);
		idTab.openScope();
		ParameterDeclList pdl = m.parameterDeclList;
		idTab = m.type.visit(this, idTab);
		for (ParameterDecl pd: pdl) {
			idTab = pd.visit(this, idTab);
		}
		StatementList sl = m.statementList;
		idTab.openScope();
		for (Statement s: sl) {
			idTab = s.visit(this, idTab);
		}
		idTab.printLevel(4);
		idTab.closeScope();
		idTab.printLevel(3);
		idTab.closeScope();
		return idTab;
	}

	public idTable visitParameterDecl(ParameterDecl pd, idTable idTab){
		idTab = pd.type.visit(this, idTab);
		int res = idTab.addDecl(pd,idLevel.PARAM_LEVEL,idLevel.PARAM_LEVEL);
		if(res != -1)
		{
			identificationError("Member declaration failed - " + pd.name + " already declared at level " +res );
		}
		return idTab;
	} 

	public idTable visitVarDecl(VarDecl vd, idTable idTab){

		return idTab;


	}

	///////////////////////////////////////////////////////////////////////////////
	//
	// TYPES
	//
	///////////////////////////////////////////////////////////////////////////////

	public idTable visitBaseType(BaseType type, idTable idTab){
		return idTab;
	}

	public idTable visitClassType(ClassType type, idTable idTab){
		Declaration d = idTab.getClass(type.className.spelling);
		if(d == null)
		{
			identificationError("Class "+type.className.spelling +" not found" );
		}
		type.className.setDecl(d);
		type.typeKind =type.className.getDecl().type.typeKind;    // added to add support for unsupported string

		return idTab;
	}

	public idTable visitArrayType(ArrayType type, idTable idTab){
		idTab = type.eltType.visit(this, idTab);
		return idTab;
	}


	///////////////////////////////////////////////////////////////////////////////
	//
	// STATEMENTS
	//
	///////////////////////////////////////////////////////////////////////////////

	public idTable visitBlockStmt(BlockStmt stmt, idTable idTab){
		idTab.openScope();
		StatementList sl = stmt.sl;
		for (Statement s: sl) {
			idTab = s.visit(this, idTab);
		}
		idTab.printRestLevel();
		idTab.closeScope();
		return idTab;
	}

	public idTable visitVardeclStmt(VarDeclStmt stmt, idTable idTab){

	//	ClassType ct;

		idTab = stmt.varDecl.visit(this, idTab);	
		
		
		idTab = stmt.initExp.visit(this, idTab);

		idTab = stmt.varDecl.type.visit(this, idTab);
		
	//	if(stmt.varDecl.type instanceof ClassType){
	//		ct =(ClassType) stmt.varDecl.type;
	//		stmt.varDecl.type.typeKind = ct.className.getDecl().type.typeKind; ///to support UNSUPPORTED
	//	}
		
		
		
		int res = idTab.addDecl(stmt.varDecl,idLevel.PARAM_LEVEL);
		if(res != -1)
		{
			identificationError("Local declaration failed - " + stmt.varDecl.name + " already declared at level " + res );
		}

		return idTab;
	}

	public idTable visitAssignStmt(AssignStmt stmt, idTable idTab){
		idTab = stmt.ref.visit(this, idTab);
		idTab = stmt.val.visit(this, idTab);
		return idTab;
	}

	public idTable visitIxAssignStmt(IxAssignStmt stmt, idTable idTab){
		idTab = stmt.ixRef.visit(this, idTab);
		idTab = stmt.val.visit(this, idTab);
		return idTab;
	}

	public idTable visitCallStmt(CallStmt stmt, idTable idTab){


		idTab=  stmt.methodRef.visit(this, idTab );
		ExprList al = stmt.argList;


		for (Expression e: al) {
			idTab=        e.visit(this, idTab);
		}
		return idTab;

	}

	public idTable visitReturnStmt(ReturnStmt stmt, idTable idTab){
		if (stmt.returnExpr != null)
			idTab = stmt.returnExpr.visit(this, idTab);
		return idTab;
	}

	public idTable visitIfStmt(IfStmt stmt, idTable idTab){
		idTab = stmt.cond.visit(this, idTab);


		if(stmt.thenStmt.isVarDecl){
			identificationError("Cannot declare solitary variables in if statement: failed to declare " );
		}
		else{
			idTab = stmt.thenStmt.visit(this, idTab);
		}  

 

		if (stmt.elseStmt != null){

			if(stmt.elseStmt.isVarDecl ){
				identificationError("Cannot declare solitary variables in else statement: failed to declare " );
			}
			else
				idTab = stmt.elseStmt.visit(this, idTab);

		}

		return idTab;
	}

	public idTable visitWhileStmt(WhileStmt stmt, idTable idTab){
		idTab = stmt.cond.visit(this, idTab);

		if(stmt.body.isVarDecl){
			identificationError("Cannot declare solitary variables in while statement: failed to declare " );
		}
		else
			idTab = stmt.body.visit(this, idTab);


		return idTab;
	}


	///////////////////////////////////////////////////////////////////////////////
	//
	// EXPRESSIONS - no change, done
	//
	///////////////////////////////////////////////////////////////////////////////

	public idTable visitUnaryExpr(UnaryExpr expr, idTable idTab){
		idTab = expr.operator.visit(this, idTab);
		idTab = expr.expr.visit(this, idTab);
		return idTab;
	}

	public idTable visitBinaryExpr(BinaryExpr expr, idTable idTab){
		idTab = expr.operator.visit(this, idTab);
		idTab = expr.left.visit(this, idTab);
		idTab = expr.right.visit(this, idTab);
		return idTab;
	}

	public idTable visitRefExpr(RefExpr expr, idTable idTab){
		idTab = expr.ref.visit(this, idTab);
		return idTab;
	}

	public idTable visitCallExpr(CallExpr expr, idTable idTab){
		idTab = expr.functionRef.visit(this, idTab);
		ExprList al = expr.argList;
		for (Expression e: al) {
			idTab = e.visit(this, idTab);
		}
		return idTab;
	}

	public idTable visitLiteralExpr(LiteralExpr expr, idTable idTab){
		expr.lit.visit(this, idTab);
		return idTab;
	}

	public idTable visitNewArrayExpr(NewArrayExpr expr, idTable idTab){
		expr.eltType.visit(this, idTab);
		expr.sizeExpr.visit(this, idTab);
		return idTab;
	}

	public idTable visitNewObjectExpr(NewObjectExpr expr, idTable  idTab){
		expr.classtype.visit(this, idTab);
		return idTab;
	}


	///////////////////////////////////////////////////////////////////////////////
	//
	// REFERENCES
	//
	///////////////////////////////////////////////////////////////////////////////

	public idTable visitQualifiedRef(QualifiedRef qr, idTable idTab) {
 
		ClassDecl cd ;
		Declaration  childD;
		FieldDecl fd;
		ClassType ct;
		MethodDecl md;

		idTab  = qr.id.visit(this, idTab);
		idTab  = qr.ref.visit(this, idTab);
  
		
		if(qr.ref.getDecl() instanceof ClassDecl){
					cd = (ClassDecl) qr.ref.getDecl();		
					
		 
			for(int i=0;i<cd.fieldDeclList.size();i++){
				fd =     cd.fieldDeclList.get(i);

				if(    (!fd.isPrivate  || fd.isPrivate  && (qr.ref instanceof ThisRef) )      && qr.id.spelling.equals(fd.name) &&  (!qr.ref.isStatic || qr.ref.isStatic && fd.isStatic) ){

					childD =fd;
					qr.id.setDecl(childD);
					
					if(childD.type.typeKind==TypeKind.CLASS){
						ct = (ClassType) childD.type;
					qr.setDecl(idTab.getClass(ct.className.spelling));
					}
					else{
						qr.setDecl(childD);
					}
					
					break;
				}
			}
			
			
			for(int i=0;i<cd.methodDeclList.size();i++){
				md =     cd.methodDeclList.get(i);

				if( (!md.isPrivate  || md.isPrivate  && (qr.ref instanceof ThisRef) )   && qr.id.spelling.equals(md.name) &&  (!qr.ref.isStatic || qr.ref.isStatic && md.isStatic) ){

					childD =md;
					qr.id.setDecl(childD);
					
					if(childD.type.typeKind==TypeKind.CLASS){
						ct = (ClassType) childD.type;
					qr.setDecl(idTab.getClass(ct.className.spelling));
					
					}
					else{
						qr.setDecl(childD);
					}
					
					break;
				}
			}
			
			

			if(qr.id.getDecl()==null){
				identificationError(qr.id +"  " + qr.id.spelling + " not found in class " + qr.ref.getDecl().name);
			}

//		}

			
		
		}
		
		else{
			identificationError("qualified reference is not applicable on non-class variables " );
		 
		}
		
		return idTab;

	}

	public idTable visitIndexedRef(IndexedRef ir, idTable idTab) {

		ir.indexExpr.visit(this, idTab);

	/*	 = idTab.getIdentifier(ir.idRef.id.spelling, isFuncStatic);

		
	*/
		idTab =	ir.idRef.visit(this, idTab);
		
		Declaration d = ir.idRef.id.getDecl();
		if(d==null){
			identificationError(" Indexed identifier " +ir.idRef.id.spelling+" not found ");
		}
		else if (d.type.typeKind == TypeKind.ARRAY){
		
			ir.setDecl(d);
			ir.idRef.setDecl(d);
		}
		else
			identificationError(" Indexed identifier " +ir.idRef.id.spelling+" is not declared as a array ");
		
		
		return idTab;

	}

	public idTable visitIdRef(IdRef ref, idTable idTab) {

		
		
			
		
		
		Declaration d = idTab.getIdentifier(ref.id.spelling, isFuncStatic); //for static member access inside static functions
		
		if(ref.id.spelling.equals("null"))
			d = new VarDecl( new BaseType(TypeKind.NULL,null),"null",null);
		
		ClassType ct;
		if(d==null){

	 		d = idTab.getStaticIdentifier(ref.id.spelling); // A.x , static access

		 if(d==null)
				identificationError(" Identifier of type class "+ ref.id + " named " + ref.id.spelling + " not declared  or is not a static declaration");
		 	else			 
			 	ref.isStatic = true;

		}
		//ref.isStatic = isFuncStatic;
		ref.id.setDecl(d);
		
		
		if(ref.isStatic){
			ref.setDecl(d);
		}
		else if(d.type.typeKind==TypeKind.CLASS){
			ct = (ClassType) d.type;
		ref.setDecl(idTab.getClass(ct.className.spelling));
		}
		else if(d instanceof MethodDecl)  //weird logic, used as workaround
			ref.setDecl(d);


		idTab = ref.id.visit(this, idTab);
		return idTab;
	}

	public idTable visitThisRef(ThisRef ref, idTable idTab) {
	 	ClassType ct;
		Declaration d = idTab.getIdentifier("this",false);
		if(d==null)
			identificationError(" Identifier 'this' not found");
		//else{			 
		
		  if(d.type.typeKind==TypeKind.CLASS){
		 		ct = (ClassType) d.type;
		 	ref.setDecl(idTab.getClass(ct.className.spelling));
			
		 	}

			
			
			 else
			 identificationError(" 'this' doesnt point to a class");
	  	
		//	ref.setDecl(d);
		//}
		return idTab;
	}


	///////////////////////////////////////////////////////////////////////////////
	//
	// TERMINALS
	//
	///////////////////////////////////////////////////////////////////////////////
	//Modify This 
	public idTable visitIdentifier(Identifier id, idTable idTab){
 
		return idTab;
	}

	public idTable visitOperator(Operator op, idTable idTab){
		return idTab;
	}

	public idTable visitIntLiteral(IntLiteral num, idTable idTab){
		return idTab;
	}

	public idTable visitBooleanLiteral(BooleanLiteral bool, idTable idTab){
		return idTab;
	}
}
