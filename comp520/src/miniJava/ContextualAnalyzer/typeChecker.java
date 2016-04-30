package miniJava.ContextualAnalyzer;
import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.TokenKind;


public class typeChecker implements Visitor<Object,Type> {

	private ErrorReporter reporter;

	private int numMainFunc;
	MethodDecl mainMethodDecl;
	MethodDecl printlnDecl; //added to support overloading of println by string and int
	MethodDecl printlnStringDecl;//added to support overloading of println by string and int
	private void checkForMain(MethodDecl md,Type retType){

		ParameterDecl pd;
		ArrayType arType;
		ClassType cType;
		if(!md.isPrivate && md.isStatic && md.name.equals("main") && md.parameterDeclList.size()==1 && retType.typeKind==TypeKind.VOID){	

			pd = md.parameterDeclList.get(0);

			if(pd.type instanceof ArrayType){
				arType = (ArrayType) pd.type;


				if(arType.eltType instanceof ClassType){

					cType = (ClassType)	arType.eltType ;

					if(cType.className.spelling.equals("String")){
						mainMethodDecl =md;
						numMainFunc++;
					}
				}


			}


		}
	}



	private typeEquality isEqual(Type el, Type er){



		if(el.typeKind == TypeKind.UNSUPPORTED || er.typeKind ==TypeKind.UNSUPPORTED)
			return typeEquality.UNSUPPORTED;
		else if (el.typeKind == TypeKind.ERROR )
			return typeEquality.ERROR;
		else if (er.typeKind == TypeKind.ERROR )
			return typeEquality.ERROR;
		else if((el.typeKind==TypeKind.NULL && er.typeKind!=TypeKind.VOID) || (er.typeKind==TypeKind.NULL  && el.typeKind!=TypeKind.VOID))
			return typeEquality.EQUAL;

		else if(er.typeKind ==el.typeKind){

			if(er.typeKind==TypeKind.CLASS ){

				ClassType ctEr,ctEl;

				ctEr = (ClassType) er;
				ctEl = (ClassType) el;

				if(!ctEr.className.spelling.equals(ctEl.className.spelling)){
					//return typeEquality.UNEQUAL;
					//added to support inheritance
				    Declaration dRight =	ctEr.className.getDecl();
					if(dRight instanceof ClassDecl){
						ClassDecl cdRight = (ClassDecl) dRight;
						
						if(!cdRight.isBaseClass)
					return 	 isEqual( el,cdRight.parentClassDecl.type );
						else
							return typeEquality.UNEQUAL;
					}
					else{
						System.out.println("classtype doesnt have a classdecl");
						 System.exit(-1); 
					}
				
				
				}
					
			}
			else if (er.typeKind==TypeKind.ARRAY ){

				ArrayType atEr,atEl;

				atEr = (ArrayType) er;
				atEl = (ArrayType) el;

				/*dont need to take care of the case when etlType are array because 
				 * this case will cause identification error,
				 *   int [] a;
				 *     a [] b;
				 * in this case we  wont find a*/
				if(atEr.eltType.typeKind==TypeKind.UNSUPPORTED && atEl.eltType.typeKind==TypeKind.UNSUPPORTED ) //added for case when both arrays are  UNSUPPORTED
					return typeEquality.EQUAL;
				else
					return isEqual(atEl.eltType, atEr.eltType); 
			}

			return typeEquality.EQUAL; 

		}
		else
			return typeEquality.UNEQUAL;





	}

	private Type getTypeOfExpr(Type t, Operator o){ //for unary operators



		if(t.typeKind==TypeKind.INT && o.spelling.equals("-"))
			return  new BaseType(TypeKind.INT, o.posn) ;//added position of output expr as position of operator

		else if(t.typeKind==TypeKind.BOOLEAN && o.spelling.equals("!"))
			return  new BaseType(TypeKind.BOOLEAN, o.posn) ;//added position of output expr as position of operator
		else{
			typeCheckFatalError("Shouldnt reach here, invalid type of unary operator");
			return new BaseType(TypeKind.ERROR, null);

		}




	}



	private Type getTypeOfExpr(Type l,Type r, Operator o){ //for binary operators

		typeEquality	typeEq = isEqual(l,r);


		switch(typeEq){

		case ERROR:


			return   operatorAgreement(l,r,o);
		case UNEQUAL:
			typeCheckError("In binary expression,  LHS " + l  +" of typeKind " + l.typeKind + " and  RHS " + r +" of typeKind " + r.typeKind + " doesnt match with operator " + o + " of spelling  " + o.spelling);

			return new BaseType(TypeKind.ERROR, o.posn);//added position of output expr as position of operator

		case UNSUPPORTED:
			typeCheckFatalError("Type check fatal error: In binary expression unsupported type occured,  LHS " + l  +" of typeKind " + l.typeKind + " and  RHS " + r +" of typeKind " + r.typeKind + " doesnt match with operator " + o + " of spelling  " + o.spelling);

			return new BaseType(TypeKind.UNSUPPORTED, o.posn);//added position of output expr as position of operator

		case EQUAL:
			return   operatorAgreement(l,r,o);
		default:
			typeCheckFatalError("shouldnt reach here, left and right types are neither equal/unequal/error/unsupported");
			return new BaseType(TypeKind.ERROR, null);


		}


	}


	Type operatorAgreement(Type l,Type r, Operator o){
		// if you enter this function, it means l/r are either equal or have error



		if(o.spelling.equals("+") && (l.typeKind==TypeKind.INT || l.typeKind==TypeKind.ERROR))
			return new BaseType(TypeKind.INT, o.posn);//added position of output expr as position of operator

		else if(o.spelling.equals("-") && (l.typeKind==TypeKind.INT || l.typeKind==TypeKind.ERROR))
			return new BaseType(TypeKind.INT,  o.posn);//added position of output expr as position of operator

		else if(o.spelling.equals("*") && (l.typeKind==TypeKind.INT || l.typeKind==TypeKind.ERROR))
			return new BaseType(TypeKind.INT,  o.posn);//added position of output expr as position of operator

		else if(o.spelling.equals("/") && (l.typeKind==TypeKind.INT || l.typeKind==TypeKind.ERROR))
			return new BaseType(TypeKind.INT,  o.posn);//added position of output expr as position of operator

		else if(o.spelling.equals("<=") && (l.typeKind==TypeKind.INT || l.typeKind==TypeKind.ERROR))
			return new BaseType(TypeKind.BOOLEAN,  o.posn);//added position of output expr as position of operator

		else if(o.spelling.equals("<") && (l.typeKind==TypeKind.INT || l.typeKind==TypeKind.ERROR))
			return new BaseType(TypeKind.BOOLEAN,  o.posn);//added position of output expr as position of operator

		else if(o.spelling.equals(">") && (l.typeKind==TypeKind.INT || l.typeKind==TypeKind.ERROR))
			return new BaseType(TypeKind.BOOLEAN,  o.posn);//added position of output expr as position of operator

		else if(o.spelling.equals(">=") && (l.typeKind==TypeKind.INT || l.typeKind==TypeKind.ERROR))
			return new BaseType(TypeKind.BOOLEAN,  o.posn);//added position of output expr as position of operator

		else if(o.spelling.equals("==") && (l.typeKind==TypeKind.INT || l.typeKind==TypeKind.ERROR || l.typeKind==TypeKind.BOOLEAN || l.typeKind==TypeKind.NULL || r.typeKind==TypeKind.NULL))
			return new BaseType(TypeKind.BOOLEAN,  o.posn);//added position of output expr as position of operator

		else if(o.spelling.equals("!=") && (l.typeKind==TypeKind.INT || l.typeKind==TypeKind.ERROR || l.typeKind==TypeKind.BOOLEAN || l.typeKind==TypeKind.NULL || r.typeKind==TypeKind.NULL))
			return new BaseType(TypeKind.BOOLEAN,  o.posn);//added position of output expr as position of operator

		else if(o.spelling.equals("&&") && (l.typeKind==TypeKind.ERROR || l.typeKind==TypeKind.BOOLEAN))
			return new BaseType(TypeKind.BOOLEAN,  o.posn);//added position of output expr as position of operator

		else if(o.spelling.equals("||") && (l.typeKind==TypeKind.ERROR || l.typeKind==TypeKind.BOOLEAN))
			return new BaseType(TypeKind.BOOLEAN,  o.posn);//added position of output expr as position of operator

		else{
			typeCheckFatalError("shouldnt reach here, left type is " + l +" of typekind " + l.typeKind + " right type is "+ r +" of typekind " + r.typeKind + " and operator is " + o + " of spelling "  + o.spelling );
			return new BaseType(TypeKind.ERROR, null);//added position of output expr as position of operator
		}
	}


	private Type typeCheckAssignment(Type l,Type r){
		boolean errorFlag =false; 

		typeEquality tEq;


		tEq = isEqual(l,r);

		switch(tEq){
		case UNEQUAL:
			typeCheckError("In assignment, argument type of LHS " + l  +" of typeKind " + l.typeKind + " doesnt match  argument type of RHS " + r +" of typeKind " + r.typeKind);
			errorFlag=true ;
			break;

		case EQUAL:


			break;

		case UNSUPPORTED:
			typeCheckError("In  assignment, argument type of LHS " + l +" of typeKind " + l.typeKind + " or  argument type of RHS " + r + " of typeKind " + r.typeKind +" is unsupported");

			break;

		case ERROR:
			errorFlag=true;

			break;


		default:
			typeCheckFatalError("Shouldnt reach here");
			break;

		}

		if(errorFlag)
			return new BaseType(TypeKind.ERROR, null);
		else
			return new BaseType(TypeKind.VOID,null) ;

	}

	public typeChecker(ErrorReporter er,MethodDecl _printlnDecl,MethodDecl _printlnStringDecl){
		numMainFunc =0;
		reporter =er;
		printlnDecl =_printlnDecl;
		printlnStringDecl = _printlnStringDecl;
	}



	class TypeCheckError extends Error {
		private static final long serialVersionUID = 1L;	
	}







	private void typeCheckError(String e)  {
		reporter.reportError("*** Type check error: " + e);
		//throw new TypeCheckError();

	}


	private void typeCheckFatalError(String e) throws TypeCheckError {
		reporter.reportError("*** Type check Fatal error, stopping: " + e);
		throw new TypeCheckError();

	}



	public MethodDecl typeCheckAST(AST ast){
		System.out.println("======= AST  Type Checker ====================");
		Type astType =null;



		try {
			astType = 	ast.visit(this, null);
		}
		catch (TypeCheckError ie) {
			System.out.println("Type check error occurred");
			return null;
		}
		if(astType!=null){
			if(astType.typeKind==TypeKind.VOID && numMainFunc==1){
				System.out.println("Type checking successfully completed");			
				return mainMethodDecl;
			}
			else 
				return null;

		}
		else
			return null;

	}



	///////////////////////////////////////////////////////////////////////////////
	//
	// PACKAGE
	//
	/////////////////////////////////////////////////////////////////////////////// 

	public Type visitPackage(miniJava.AbstractSyntaxTrees.Package prog, Object arg){
		boolean errorFlag = false;
		Type cType;
		ClassDeclList cl = prog.classDeclList;


		for (ClassDecl c: prog.classDeclList){
			cType	= c.visit(this, null);
			if(cType.typeKind==TypeKind.ERROR)
				errorFlag =true;
		}
		if(errorFlag)
			return new BaseType(TypeKind.ERROR,null);
		else
			return new BaseType(TypeKind.VOID, null);
	}


	///////////////////////////////////////////////////////////////////////////////
	//
	// DECLARATIONS -done 
	//
	///////////////////////////////////////////////////////////////////////////////

	public Type visitClassDecl(ClassDecl clas, Object arg){
		boolean errorFlag = false;
		Type t;

		for (FieldDecl f: clas.fieldDeclList){
			t=	f.visit(this, null);
			if(t.typeKind==TypeKind.ERROR)
				errorFlag =errorFlag| true;
		}


		for (MethodDecl m: clas.methodDeclList){
			t= 	m.visit(this, null);
			if(t.typeKind==TypeKind.ERROR)
				errorFlag =errorFlag| true;
		}

		if(errorFlag)
			return new BaseType(TypeKind.ERROR,null);
		else
			return new BaseType(TypeKind.VOID, null);


	}

	public Type visitFieldDecl(FieldDecl f, Object arg){

		return	f.type.visit(this, null);


	}

	public Type visitMethodDecl(MethodDecl m, Object arg){



		boolean errorFlag=false;	
		boolean hasRetStmt=false;
		typeEquality tEq;

		Type methodRetType = 	m.type.visit(this, null);
		Type stmtType;	 
		ParameterDeclList pdl = m.parameterDeclList;
		Statement s;
		checkForMain(  m,  methodRetType);


		for (ParameterDecl pd: pdl) {
			pd.visit(this, null);
		}
		StatementList sl = m.statementList;

		for (int i=0; i<sl.size();i++) {
			s =sl.get(i);
			stmtType	= s.visit(this, null);

			if(stmtType.typeKind==TypeKind.ERROR)
				errorFlag =   true;

			if( methodRetType.typeKind!=TypeKind.VOID && i==sl.size()-1 && !(s instanceof ReturnStmt)) // asserts that nonvoid functions have last line as return statement
				errorFlag= true;

			if(s instanceof ReturnStmt){
				hasRetStmt=true;

				tEq = isEqual(stmtType,methodRetType);

				switch(tEq){
				case UNEQUAL:
					typeCheckError("In method declaration "+m.name + " return type " +methodRetType+" of typekind " + methodRetType.typeKind + "  doesnt match return statement type " + stmtType + " of typekind " + stmtType.typeKind);
					errorFlag=true | errorFlag;
					break;

				case EQUAL:
					break;

				case UNSUPPORTED:
					typeCheckError("In method declaration "+m.name + " return type " +methodRetType +" of typekind " + methodRetType.typeKind +"  or return statement type " + stmtType + " of typekind " + stmtType.typeKind + " is unsupported");


					break;

				case ERROR:
					errorFlag =errorFlag|true;
					break;
				default:
					typeCheckFatalError("Shouldnt reach here");
					break;

				}


			}
		}


		if(errorFlag)
			return new BaseType(TypeKind.ERROR,null);
		else if(!hasRetStmt && methodRetType.typeKind!=TypeKind.VOID  ){

			typeCheckError("In method declaration "+m.name + " return type is " +methodRetType+" but method doesnt contain return statement");
			return new BaseType(TypeKind.ERROR,null);

		}

		else
			return new BaseType(TypeKind.VOID, null);



	}

	public Type visitParameterDecl(ParameterDecl pd, Object arg){

		return pd.type.visit(this, null);


	} 

	public Type visitVarDecl(VarDecl vd, Object arg){

		return vd.type.visit(this, null);


	}


	public Type visitNullDecl(NullDecl decl, Object arg) {
		typeCheckFatalError("Shouldnt reach here : visiting   null decl");
		return null;
	}


	///////////////////////////////////////////////////////////////////////////////
	//
	// TYPES -done ? confirm visitArrayType return statement, do i need to check for unsupported?
	//
	///////////////////////////////////////////////////////////////////////////////

	public Type visitBaseType(BaseType type, Object arg){

		return type;
	}

	public Type visitClassType(ClassType type, Object arg){

		return type;
	}

	public Type visitArrayType(ArrayType type, Object arg){

		type.eltType.visit(this, null);
		return type; // confirm confirmed once, confirmed
	}


	///////////////////////////////////////////////////////////////////////////////
	//
	// STATEMENTS - done
	//
	///////////////////////////////////////////////////////////////////////////////

	public Type visitBlockStmt(BlockStmt stmt, Object arg){
		boolean errorFlag=false;
		StatementList sl = stmt.sl;
		Type stmtType=null; 

		for (Statement s: sl) {
			stmtType =	s.visit(this, null);
			if(stmtType.typeKind==TypeKind.ERROR)
				errorFlag =errorFlag| true;
		}

		if(errorFlag)
			return new BaseType(TypeKind.ERROR,null);
		else
			return new BaseType(TypeKind.VOID,null);
	}

	public Type visitVardeclStmt(VarDeclStmt stmt, Object arg){

		Type	varLHS =	stmt.varDecl.visit(this, null);	
		Type expRHS =	stmt.initExp.visit(this, null);


		return typeCheckAssignment(varLHS,expRHS);
	}

	public Type visitAssignStmt(AssignStmt stmt, Object arg){


		Type	refRHS =	stmt.val.visit(this, null);
		Type	refLHS= stmt.ref.visit(this, null);


		return typeCheckAssignment(refLHS,refRHS);



	}

	public Type visitIxAssignStmt(IxAssignStmt stmt, Object arg){


		Type	refLHS = stmt.ixRef.visit(this, null);
		Type	refRHS = stmt.val.visit(this, null);

		return typeCheckAssignment(refLHS,refRHS);

	}

	public Type visitCallStmt(CallStmt stmt, Object arg){
		boolean errorFlag =false; 
		Type retTypeOfFunc = stmt.methodRef.visit(this, null);
		typeEquality tEq;
		MethodDecl md;
		ClassType ct;
		QualifiedRef qr;
		md = (MethodDecl) stmt.methodRef.getDecl();

		ExprList al = stmt.argList;
		Type expType;	 

		if(al.size()!=md.parameterDeclList.size())
			typeCheckFatalError("Number of arguments for declared function " + md + " of name " + md.name + " is " + md.parameterDeclList.size() + " but you provided " +al.size() + " arguments at " + stmt);


		for (int i =0;i<al.size();i++) {

			Expression e = al.get(i);
			expType =	e.visit(this, null);



			tEq = isEqual(expType, md.parameterDeclList.get(i).type);

			if(md == printlnDecl && expType instanceof ClassType ){ //override the result of isEqual to simulate overloading of println
				ct = (ClassType) expType;
				if(ct.className.spelling.equals("String")){	
					tEq = typeEquality.EQUAL;
					stmt.methodRef.setDecl(printlnStringDecl);
					if(stmt.methodRef instanceof QualifiedRef){
						qr =  (QualifiedRef) stmt.methodRef;

						qr.id.setDecl(printlnStringDecl);
					}
				}
			}



			switch(tEq){
			case UNEQUAL:
				typeCheckError("Argument " + i +" type " + expType + " doesnt match method's argument type " + md.parameterDeclList.get(i).type);
				errorFlag=true | errorFlag;
				break;

			case EQUAL:
				break;

			case UNSUPPORTED:
				typeCheckFatalError("Argument " + i +" type " + expType + " or  " + md.parameterDeclList.get(i).type +" is unsupported");
				break;

			case ERROR:
				errorFlag = true;
				break;
			default:
				typeCheckFatalError("Shouldnt reach here");
				break;

			}


		}

		if(errorFlag)
			return new BaseType(TypeKind.ERROR, null);
		else
			return retTypeOfFunc ;
	}

	public Type visitReturnStmt(ReturnStmt stmt, Object arg){
		Type retType=null;	 
		if (stmt.returnExpr != null)
			retType = stmt.returnExpr.visit(this, null);
		else 
			retType= new  BaseType(TypeKind.VOID,stmt.posn);



		return retType;
	}

	public Type visitIfStmt(IfStmt stmt, Object arg){

		Type condType =		stmt.cond.visit(this, null);
		Type thenType =		stmt.thenStmt.visit(this, null);
		Type elseType = null;


		if (stmt.elseStmt != null)
			elseType =	stmt.elseStmt.visit(this, null);


		if(condType.typeKind == TypeKind.ERROR || thenType.typeKind == TypeKind.ERROR )
			return new BaseType(TypeKind.ERROR,null);

		else if(elseType!=null){
			if(elseType.typeKind==TypeKind.ERROR)
				return new BaseType(TypeKind.ERROR,null);
		}
		else if(condType.typeKind != TypeKind.BOOLEAN ){
			typeCheckError("In if statement, conditon  "+stmt.cond + " doesn not evaluate to boolean but to "+ condType.typeKind);
			return new BaseType(TypeKind.ERROR,null) ;

		}


		return new BaseType(TypeKind.VOID,null) ;

	}

	public Type visitWhileStmt(WhileStmt stmt, Object arg){

		Type condType =	stmt.cond.visit(this, null);
		Type bodyType =	stmt.body.visit(this, null);


		if(condType.typeKind == TypeKind.ERROR || bodyType.typeKind == TypeKind.ERROR )
			return new BaseType(TypeKind.ERROR,null);

		else if(condType.typeKind == TypeKind.BOOLEAN )
			return new BaseType(TypeKind.VOID,null) ;

		else 
			return new BaseType(TypeKind.ERROR,null) ;
	}


	public Type visitForStmt(ForStmt stmt, Object arg){
		//always check for null in for stmt for init,cond and inc
		Type initT=null;
		Type condT=null;
		Type incT=null;
		boolean errorFlag =false;

		if(stmt.init!=null)
			initT  = stmt.init.visit(this, null);

		if(stmt.cond!=null)
			condT  = stmt.cond.visit(this, null);

		if(stmt.increment!=null)
			incT	  = stmt.increment.visit(this, null);



		Type bodyT =	stmt.body.visit(this,null);



		if(initT!=null && initT.typeKind == TypeKind.ERROR )//the first exp should be !=null, taking advantage of short-circuiting
			errorFlag =true;

		else if(condT!=null && condT.typeKind != TypeKind.BOOLEAN) //the first exp should be !=null, taking advantage of short-circuiting
			errorFlag =true;

		else if(incT!=null && incT.typeKind == TypeKind.ERROR) //the first exp should be !=null, taking advantage of short-circuiting
			errorFlag =true;

		else if(bodyT!=null && bodyT.typeKind == TypeKind.ERROR) //the first exp should be !=null, taking advantage of short-circuiting
			errorFlag =true;

		if(errorFlag)
			return    new BaseType(TypeKind.ERROR,null) ;
		else
			return new BaseType(TypeKind.VOID,null) ;



	}




	///////////////////////////////////////////////////////////////////////////////
	//
	// EXPRESSIONS - done
	//
	///////////////////////////////////////////////////////////////////////////////

	public Type visitUnaryExpr(UnaryExpr expr, Object arg){

		expr.operator.visit(this, null);
		Type eType =	expr.expr.visit(this, null);

		Type retType = getTypeOfExpr(eType, expr.operator);

		if(retType.typeKind==TypeKind.ERROR)
			typeCheckFatalError("Unsupported type encountered in unary expression");
		else if(retType.typeKind==TypeKind.UNSUPPORTED)
			typeCheckFatalError("Unsupported type encountered in unary expression");

		return retType;
	}

	public Type visitBinaryExpr(BinaryExpr expr, Object arg){

		expr.operator.visit(this, null);
		Type leType = expr.left.visit(this, null);
		Type reType = 	expr.right.visit(this, null);

		Type retType = getTypeOfExpr(leType,reType, expr.operator);

		return retType;

	}

	public Type visitRefExpr(RefExpr expr, Object arg){

		return	expr.ref.visit(this, null);

	}

	public Type visitCallExpr(CallExpr expr, Object arg){

		boolean errorFlag =false;
		Type funcRetType =	expr.functionRef.visit(this, null);
		ExprList al = expr.argList;
		Type argType,argDefType;
		typeEquality tEq;
		MethodDecl md;
		Expression e;
		Declaration d =expr.functionRef.getDecl();


		if(d instanceof MethodDecl){
			md = (MethodDecl) d;

			if(al.size() != md.parameterDeclList.size())
				typeCheckFatalError("Number of arguments for declared function " + md + " of name " + md.name + " is " + md.parameterDeclList.size() + " but you provided " +al.size() + " arguments at function call in expression " + expr);



			for (int i=0;i<al.size();i++ ) {
				e = al.get(i);
				argType =	e.visit(this, null);
				argDefType =md.parameterDeclList.get(i).type;

				tEq = isEqual(argType,argDefType);

				switch(tEq){

				case ERROR:
					errorFlag =true;
					break;
				case UNEQUAL:
					typeCheckError("Argument type in function definition " + md.name + " is " + argDefType + " of typekind " + argDefType.typeKind +" and argument input type is " +argType + " of typekind " + argType.typeKind + " they dont match");
					errorFlag =true;
					break;

				case UNSUPPORTED:
					typeCheckFatalError("Argument type in function definition " + md.name + " is " +  argDefType  + " of typekind " + argDefType.typeKind +" and argument input type is " + argType  + " of typekind " + argType.typeKind   + " either of them is unsupported");
					return new BaseType(TypeKind.UNSUPPORTED, null);


				case EQUAL:
					break;
				default:
					typeCheckFatalError("shouldnt reach here, left and right types are neither equal/unequal/error/unsupported");
					return new BaseType(TypeKind.ERROR, null);

				}


			}
		}
		else{
			typeCheckFatalError("Shouldnt reach here : reference to function doesnt have a declaration of type methodDecl");
		}

		if(errorFlag)
			return new BaseType(TypeKind.ERROR,null);
		else
			return funcRetType;


	}

	public Type visitLiteralExpr(LiteralExpr expr, Object arg){

		return expr.lit.visit(this, null);

	}

	public Type visitNewArrayExpr(NewArrayExpr expr, Object arg){
		ArrayType arrType;
		boolean errorFlag =false; 
		Type eltType = expr.eltType.visit(this, null);
		Type exprType = expr.sizeExpr.visit(this, null);

		BaseType exprBT;
		if(exprType instanceof  BaseType){
			exprBT = (BaseType) exprType;
			if(exprBT.typeKind!=TypeKind.INT){
				typeCheckError("size expression did not evaluate to integer");
				errorFlag =true;
			}
		}
		else{
			typeCheckError("size expression did not evaluate to basetype");
			errorFlag =true;
		}



		if(	errorFlag)
			return new BaseType(TypeKind.ERROR,null);
		else{
			arrType = new ArrayType(eltType,expr.posn);

			arrType.setLengthExpression( expr.sizeExpr);//added to maintain length

			return arrType;
		}
	}

	public Type visitNewObjectExpr(NewObjectExpr expr, Object arg){

		expr.classtype.visit(this, null);
		return expr.classtype;
	}


	///////////////////////////////////////////////////////////////////////////////
	//
	// REFERENCES - done?, check visitQualifiedRef
	//
	///////////////////////////////////////////////////////////////////////////////

	public Type visitQualifiedRef(QualifiedRef qr, Object arg) {
		boolean errorFlag=false;
		Type idT =	qr.id.visit(this, null);
		Type idRef =	qr.ref.visit(this, null);

		//do I nedd to assert that idT is the same as the field type of idref ?

		return idT;
	}

	public Type visitIndexedRef(IndexedRef ir, Object arg) {
		boolean errorFlag=false; 
		Type	exprType=	ir.indexExpr.visit(this, null);
		BaseType exprBT;
		Type idRefType;
		ArrayType arrType;
		if(exprType instanceof  BaseType){
			exprBT = (BaseType) exprType;
			if(exprBT.typeKind!=TypeKind.INT){
				typeCheckError("Indexed expression did not evaluate to integer");
				errorFlag =true;
			}
		}
		else{
			typeCheckError("Indexed expression did not evaluate to basetype");
			errorFlag =true;
		}



		if(	errorFlag)
			return new BaseType(TypeKind.ERROR,null);
		else{

			idRefType = 	ir.idRef.visit(this, null);   
			if(idRefType instanceof ArrayType){
				arrType= (ArrayType) idRefType;

				if(arrType.eltType.typeKind == TypeKind.UNSUPPORTED)
					typeCheckFatalError("IndexedRef's  " + ir.idRef + " is of unsupported type");
				return arrType.eltType;
			}
			else{
				typeCheckFatalError("IndexedRef's  " + ir.idRef + " is not of type array");
				return new BaseType(TypeKind.ERROR,null);


			}
		}
	}

	public Type visitIdRef(IdRef ref, Object arg) {

		return 	ref.id.visit(this, null);

	}

	public Type visitThisRef(ThisRef ref, Object arg) {

		return ref.getDecl().type;
	}


	public Type visitNullRef(NullRef ref, Object arg) {

		return ref.getDecl().type;
	}


	///////////////////////////////////////////////////////////////////////////////
	//
	// TERMINALS -done
	//
	///////////////////////////////////////////////////////////////////////////////

	public Type visitIdentifier(Identifier id, Object arg){

		Type idType =id.getDecl().type;
		if(idType.typeKind == TypeKind.UNSUPPORTED)
			typeCheckFatalError("Encountered UNSUPPORTED type for " + id + " of name " + id.spelling);
		return id.getDecl().type;
	}

	public Type visitOperator(Operator op, Object arg){

		return null;
	}

	public Type visitIntLiteral(IntLiteral num, Object arg){

		return new  BaseType(TypeKind.INT,num.posn);
	}

	public Type visitBooleanLiteral(BooleanLiteral bool, Object arg){
		return new  BaseType(TypeKind.BOOLEAN,bool.posn);
	}

	public Type visitStringLiteral(StringLiteral s, Object arg){

		return    new ClassType(new Identifier(new Token(TokenKind.STRING,"String",s.posn)),s.posn); //added to support string as classtype

	}


}


