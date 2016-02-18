/**
 * Parser
 *
 * Grammar:
 *   S ::= E '$'
 *   E ::= T (oper T)*     
 *   T ::= num | '(' E ')'
 */
package miniJava.SyntacticAnalyzer;

import miniJava.SyntacticAnalyzer.Scanner;
import miniJava.SyntacticAnalyzer.TokenKind;

 

import miniJava.ErrorReporter;
import miniJava.AbstractSyntaxTrees.*;
public class Parser {

	private Scanner scanner;
	private ErrorReporter reporter;
	private Token token;
	private boolean trace = true;
	int i;
	public Parser(Scanner scanner, ErrorReporter reporter) {
		this.scanner = scanner;
		this.reporter = reporter;
	}

//comment
	/**
	 * SyntaxError is used to unwind parse stack when parse fails
	 *
	 */
	class SyntaxError extends Error {
		private static final long serialVersionUID = 1L;	
	}

	/**
	 * start parse
	 */
	public AST parse() {
		token = scanner.scan();
		try {
			return parseP();
		}
		catch (SyntaxError e) {return null; }
	}

	//    P ::= (CD)*$
	private miniJava.AbstractSyntaxTrees.Package parseP() throws SyntaxError {
		 
		ClassDeclList	cdl = new ClassDeclList();
		
		 while(token.kind == TokenKind.KEYWORD && token.spelling.equals("class"))
			cdl.add(parseCD());
		accept(TokenKind.EOT);
		return new miniJava.AbstractSyntaxTrees.Package( cdl, null);
	}


	 

	//   T ::= num | "(" E ")"
 

	/**
	 * accept current token and advance to next token
	 */
	private Token acceptIt() throws SyntaxError {
		Token t = token;
		accept(token.kind);
		return t;
	}

	/**
	 * verify that current token in input matches expected token and advance to next token
	 * @param expectedToken
	 * @throws SyntaxError  if match fails
	 */
	private void accept(TokenKind expectedTokenKind) throws SyntaxError {
		if (token.kind == expectedTokenKind) {
			if (trace)
				pTrace();
			token = scanner.scan();
		}
		else
			parseError("expecting '" + expectedTokenKind +
					"' but found '" + token.kind + "'");
	}

	/**
	 * report parse error and unwind call stack to start of parse
	 * @param e  string with error detail
	 * @throws SyntaxError
	 */
	private void parseError(String e) throws SyntaxError {
		reporter.reportError("Parse error: " + e);
		throw new SyntaxError();
	}

	// show parse stack whenever terminal is  accepted
	private void pTrace() {
		StackTraceElement [] stl = Thread.currentThread().getStackTrace();
		for (int i = stl.length - 1; i > 0 ; i--) {
			if(stl[i].toString().contains("parse"))
				System.out.println(stl[i]);
		}
		System.out.println("accepting: " + token.kind + " (\"" + token.spelling + "\")");
		System.out.println();
	}

	
	
	
/*//  AR ::= id [E]
	private void parseAR() throws SyntaxError {
			
		if(token.kind ==TokenKind.ID){
			acceptIt();
			parseSpecificToken(TokenKind.BRACE,"[");
			 parseE();
			 parseSpecificToken(TokenKind.BRACE,"]");
		}
		else
			parseError("Invalid Term - expecting ID " + token.kind);
	}
	*/
	//parses braces and checks for brace type
	private Token parseSpecificToken(TokenKind tk, String tSpell) throws SyntaxError {
		 	
		
		Token t = new Token(TokenKind.ERROR,""); 
		
			if(token.kind ==tk && tSpell.equals(token.spelling)  ){
				System.out.print(token.kind + " ");
				System.out.println(token.spelling);
			t	= acceptIt();
				}
				else{
					if(tk == TokenKind.ID)
						parseError("Invalid Term - expecting "+ tk + " but found " + token.kind + " " + token.spelling);
					
					else
						parseError("Invalid Term - expecting "+ tk + " " + tSpell + " but found " + token.kind + " " + token.spelling);
				
				}
				
		 return t;
	}
	
	
	
	/*//R ::=(this|id)(.id)*
	private void  parseR() {
		
		if(token.kind ==TokenKind.KEYWORD && token.spelling.equals("this"))
			 acceptIt();
			 
			else if (token.kind ==TokenKind.ID)
				acceptIt();
		else
			parseError("Invalid Term - expecting ID" + token.kind);
		
		while(token.kind==TokenKind.DOT){
			acceptIt();
			parseSpecificToken(TokenKind.ID, token.spelling);
			
		}
		 
	}
	*/
	
	
	
	// T::= boolean | int(e|[]) | id (e|[])
private Type parseT() throws SyntaxError {
	Token t;
	switch (token.kind) {
	case ID:
		 t = acceptIt();
		 if(token.kind==TokenKind.BRACE && token.spelling.equals("[")){
			 acceptIt();
			 parseSpecificToken(TokenKind.BRACE,"]");
			 return new ArrayType(new ClassType(new Identifier(t),null),null);
		 }
		 return new ClassType(new Identifier(t),null);
	case KEYWORD:
		if(token.spelling.equals("boolean")){
			t = acceptIt();
			return new BaseType(TypeKind.BOOLEAN,null);
		}
		else if (token.spelling.equals("int")){
			t = acceptIt();
			 if(token.kind==TokenKind.BRACE && token.spelling.equals("[")){
				 acceptIt();
				 parseSpecificToken(TokenKind.BRACE,"]");
				 return new ArrayType(new BaseType(TypeKind.INT,null),null);
			 }
			 return new BaseType(TypeKind.INT,null);
		}
		else 
		{
			parseError("Invalid Term - expecting int or boolean but found " + token.spelling);
		 	return new BaseType(TypeKind.UNSUPPORTED,null);
		}
	default:
		parseError("Invalid Term - expecting ID or KEYWORD but found " + token.kind);
			return new BaseType(TypeKind.ERROR,null);
	}
	 
 }


//A ::=static?
	private String parseA() throws SyntaxError {
		
		String a="";
		if(token.kind ==TokenKind.KEYWORD && token.spelling.equals("static")){
			a=token.spelling;
			 acceptIt();
		}
			
		return a; 
	}
	
	
	
	//V ::=(public|private)?
		private String parseV() throws SyntaxError {
			String v="";
			if(token.kind ==TokenKind.KEYWORD && ( token.spelling.equals("public") || token.spelling.equals("private"))){
				v=token.spelling;
				acceptIt();
				
			}
			return v;
		}
	
	//dont need MD and FD as they dont occur in simplified grammar	
	/*	//FD ::=VAT id
		private void parseFD() throws SyntaxError {
			
			parseV();
			parseA();
			parseT();
			
			parseSpecificToken(TokenKind.ID, token.spelling);
				
		}
	*/
		
		
	/*CD ::= class id {
						VA (   void id (PL?) {S*}
						 	  |  T id (e| (PL?) {S*} )			 	 			
				     	 ) 
					  }
	 */
		private ClassDecl parseCD() throws SyntaxError {
			
			 String cn ;
			 FieldDeclList fdList = new FieldDeclList();
			 MethodDeclList mdList =new   MethodDeclList();
			 String visiblityStr,accessStr;
			 boolean isPrivate, isStatic;
			 Token idToken;
				
				
			parseSpecificToken(TokenKind.KEYWORD,"class");
			
			cn=token.spelling;
			parseSpecificToken(TokenKind.ID, token.spelling);
			
			parseSpecificToken(TokenKind.BRACE, "{");
			
				
			while(!token.spelling.equals("}")){
			
				visiblityStr =	parseV();
				accessStr =	parseA();
				
				if(visiblityStr.equals("private"))
					isPrivate =true;
				else 
					isPrivate =false;
				
				if(accessStr.equals("static"))
					isStatic =true;
				else 
					isStatic =false;
				
				
			
			if(token.kind ==TokenKind.KEYWORD &&  token.spelling.equals("void") ){
			 	StatementList sList =new StatementList();
				 ParameterDeclList pdl = new ParameterDeclList();
				 
				acceptIt();
				idToken= 	parseSpecificToken(TokenKind.ID, token.spelling);
				parseSpecificToken(TokenKind.BRACE, "(");
				if(token.spelling.equals("boolean") || token.spelling.equals("int") || token.kind ==TokenKind.ID)
				pdl=	 parsePL();
				
				parseSpecificToken(TokenKind.BRACE, ")");
				
				parseSpecificToken(TokenKind.BRACE, "{");
				
				while(!token.spelling.equals("}")){
				 
				sList.add(parseS());
				
				}
				parseSpecificToken(TokenKind.BRACE, "}");
				
			 
				MethodDecl	md = new MethodDecl(new FieldDecl(isPrivate, isStatic, new BaseType(TypeKind.VOID,null), idToken.spelling, null), pdl, sList,null);
			
				mdList.add( md  );
				
				
			}
			else{
				Type tType = parseT();
			idToken=	parseSpecificToken(TokenKind.ID, token.spelling);
				
				if(token.spelling.equals(";") && token.kind == TokenKind.SEMICOLON){
					acceptIt();
				//System.out.println(token.kind + " " + token.spelling);
				 fdList.add( new FieldDecl(isPrivate, isStatic, tType, idToken.spelling, null));
				}
				else{	
					StatementList sList =new StatementList();
					 ParameterDeclList pdl = new ParameterDeclList();
					
					
					parseSpecificToken(TokenKind.BRACE, "(");
						 if(token.spelling.equals("boolean") || token.spelling.equals("int")  || token.kind ==TokenKind.ID)
							 pdl = parsePL();
						 
						parseSpecificToken(TokenKind.BRACE, ")");
						 
						parseSpecificToken(TokenKind.BRACE, "{");
						
						while(!token.spelling.equals("}"))
							sList.add(parseS());
						
						parseSpecificToken(TokenKind.BRACE, "}");
						
						MethodDecl	md = new MethodDecl(new FieldDecl(isPrivate, isStatic, tType, idToken.spelling, null), pdl, sList,null);
						
						mdList.add( md  );
						
						
				}
			}
			
			
		}
			
	 parseSpecificToken(TokenKind.BRACE, "}");
		
	 return new ClassDecl(cn, fdList, mdList, null);
	}	
		
		
		
		//PL ::= T id ( , T id)*
				private ParameterDeclList parsePL() throws SyntaxError {
					
					ParameterDeclList retPL =new ParameterDeclList();	  
					Type t =	parseT();
				
				Token id=	parseSpecificToken(TokenKind.ID, token.spelling);
					
				retPL.add(new ParameterDecl(t, id.spelling, null));
			
					while(token.kind==TokenKind.COMMA){
						acceptIt();
					t=	parseT();
					id	= parseSpecificToken(TokenKind.ID, token.spelling);
					retPL.add(new ParameterDecl(t, id.spelling, null));
					}
						
			return retPL;			
				}
				
				

	//AL ::= E ( , E)*
	private ExprList parseAL() throws SyntaxError {
		ExprList retEList = new ExprList();
		retEList.add(	parseE() );
		 		
		while(token.kind==TokenKind.COMMA){
			acceptIt();
			retEList.add(parseE());
		}
		return retEList;						
	}
	
	
	//S ::= 
	private Statement parseS() throws SyntaxError {
		 
		System.out.println("In S");	 
			switch (token.kind) {
			
			case BRACE:
				StatementList bsSL = new StatementList();
				parseSpecificToken(TokenKind.BRACE, "{");
				
				while(!token.spelling.equals("}"))
					bsSL.add(parseS());
				parseSpecificToken(TokenKind.BRACE, "}");
				return new BlockStmt(bsSL,null);
				 
				
			case KEYWORD:
					
					 if(token.spelling.equals("boolean") ){
						 Token id = new Token(null,"");
						 acceptIt();
						 id = token;
						 Expression e = parseE();
						 parseSpecificToken(TokenKind.SEMICOLON, ";");
						 VarDecl vd = new VarDecl(new BaseType(TypeKind.BOOLEAN,null),id.spelling,null);
						 return new  VarDeclStmt(vd,e,null);
					 }
					 
					 else  if(  token.spelling.equals("int") ){
						 boolean isArray = false; 
						 Token id = new Token(null,"");
						 acceptIt();
						 if(token.kind==TokenKind.BRACE && token.spelling.equals("[")){
							 isArray = true;
							 acceptIt();
							 parseSpecificToken(TokenKind.BRACE,"]");
						 }
						 
						 id = parseSpecificToken(TokenKind.ID, token.spelling);
						 parseSpecificToken(TokenKind.EQUAL,  "=");
						 Expression e = parseE();
						 parseSpecificToken(TokenKind.SEMICOLON, ";");
						 if(isArray)
						 {
							 VarDecl vd = new VarDecl(new ArrayType(new BaseType(TypeKind.INT,null),null),id.spelling,null);
							 return new  VarDeclStmt(vd,e,null); 
						 }
						 else
						 {
							 VarDecl vd = new VarDecl(new BaseType(TypeKind.INT,null),id.spelling,null);
							 return new  VarDeclStmt(vd,e,null); 
						 }
					 }
					 
					 
					 else  if(token.spelling.equals("return")){
						 ReturnStmt rs = new ReturnStmt(null,null);
						 acceptIt();
						 if(token.kind!=TokenKind.SEMICOLON){
							Expression e = parseE();
							rs = new ReturnStmt(e,null);
						 }
						 else{
							rs = new ReturnStmt(null,null);
						 }
						 parseSpecificToken(TokenKind.SEMICOLON,";");
						 return rs;
					 }
					 else  if(token.spelling.equals("if")){
						 acceptIt();
						 
						 parseSpecificToken(TokenKind.BRACE, "(");
						 Expression ex = parseE();
						 parseSpecificToken(TokenKind.BRACE, ")");
						 
						 Statement t = parseS();
						 
						 if(token.spelling.equals("else") && token.kind==TokenKind.KEYWORD){
							 acceptIt();
							 Statement e = parseS();
							 return new IfStmt(ex,  t, e, null);
						 }
						 else
						 {
							 return new IfStmt(ex,  t, null, null);
						 }
						   
					 }
					 
					 
					 else  if(token.spelling.equals( "while")){
						 acceptIt();
						 
						 parseSpecificToken(TokenKind.BRACE, "(");
						 Expression b =parseE();
						 parseSpecificToken(TokenKind.BRACE, ")");
						 Statement s =parseS();
						 return new WhileStmt( b, s,null); 
						 }
					 
					 else  if(token.spelling.equals("this")){
						 acceptIt();
						 Token id;
						 Reference qr = new ThisRef(null);
						 while(token.kind == TokenKind.DOT){
							 acceptIt();
							 id = parseSpecificToken(TokenKind.ID, token.spelling);
							 qr = new QualifiedRef(qr,new Identifier (id),null);
							 }
							 
								 if(token.kind == TokenKind.EQUAL){
									 acceptIt();
									 Expression e = parseE();
									 parseSpecificToken(TokenKind.SEMICOLON, ";");
									 return new AssignStmt(qr, e,null);								
								 }
								 else if(token.kind == TokenKind.BRACE   && token.spelling.equals("(")) {
									 ExprList el = new ExprList();	
									 acceptIt();
									 	if ( !token.spelling.equals(")"))
									 		 el=parseAL();
									 	
									 	parseSpecificToken(TokenKind.BRACE, ")");
									 	parseSpecificToken(TokenKind.SEMICOLON, ";");
									 	return new CallStmt(new ThisRef(null), el, null);
								 }
								 
								 else
									 parseError("Invalid Term - expecting   = or ( but found" + token.spelling); 
					
								 	
								 	return null;
					 }
					 else
						 parseError("Invalid Term - expecting  this/while/return/boolean/int/if but found " + token.spelling); 
						 return null;
						  
			case ID:
				 Token id = new Token(null,"");
				 id = parseSpecificToken(TokenKind.ID, token.spelling);
				 
				 if(token.kind == TokenKind.DOT){
					 Reference qr = new IdRef(new Identifier (id), null);
					 while(token.kind == TokenKind.DOT){
						 Token id2 = new Token(null,"");
						 acceptIt();
						 id2 = parseSpecificToken(TokenKind.ID, token.spelling);
						 qr = new QualifiedRef(qr,new Identifier (id2),null);
						 }
							 if(token.kind == TokenKind.EQUAL){
								 acceptIt();
								 Expression e = parseE();
								 parseSpecificToken(TokenKind.SEMICOLON,";");
								 return new AssignStmt(qr, e, null);
							 }
							 
							 
							 else if(token.kind == TokenKind.BRACE && token.spelling.equals("(")){
								acceptIt();
								ExprList el = new ExprList();	
								if(!token.spelling.equals(")"))
										el = parseAL();
								
								 parseSpecificToken(TokenKind.BRACE,")"); 
								 parseSpecificToken(TokenKind.SEMICOLON,";");
								 return new CallStmt(qr, el, null);
							 } 	
			 
							 
				 }
				 
				 else if (token.kind == TokenKind.BRACE && token.spelling.equals("[")){
					 acceptIt();
					 
					 if(token.kind == TokenKind.BRACE && token.spelling.equals("]")){
						 //Type id = Expression;
						 //id[] id2 = Expression;
						 Token id2 = new Token(null,"");
						 acceptIt();
						 id2 = parseSpecificToken(TokenKind.ID, token.spelling);
						 parseSpecificToken(TokenKind.EQUAL,  "=");
						 Expression e = parseE();
						 parseSpecificToken(TokenKind.SEMICOLON, ";");
						 VarDecl vd = new VarDecl(new ClassType(new Identifier(id),null), id2.spelling, null);
						 return new VarDeclStmt(vd, e, null);
					 }
					 else{
						 //Array Reference = Expression;
						 Expression e1 = parseE();
						 parseSpecificToken(TokenKind.BRACE, "]");
						 parseSpecificToken(TokenKind.EQUAL,  "=");
						 Expression e2 = parseE();
						 parseSpecificToken(TokenKind.SEMICOLON, ";");
						 IdRef idr = new IdRef(new Identifier(id), null);
						 IndexedRef idxref = new IndexedRef(idr, e1, null);
						 return new IxAssignStmt (idxref,e2,null);
					 }
				 }
				 
				 else if(token.kind == TokenKind.EQUAL){
					//Reference = expression;
					 acceptIt();
					 	Expression e = parseE();
					 	parseSpecificToken(TokenKind.SEMICOLON, ";");
					 	IdRef ir = new IdRef(new Identifier(id), null);
					 	return new AssignStmt(ir, e , null);
					 	
				 }
				 
				 else if(token.kind == TokenKind.BRACE && token.spelling.equals("(")){
					 	//Reference (al?);
					 acceptIt();
					 	ExprList el = new ExprList();
						if(!token.spelling.equals(")"))
								el = parseAL();
						
						 parseSpecificToken(TokenKind.BRACE,")"); 
						 parseSpecificToken(TokenKind.SEMICOLON, ";");
						 IdRef ir = new IdRef(new Identifier(id), null);
						 return new CallStmt(ir, el, null);
				 }
				 
				 else if(token.kind == TokenKind.ID){
					 //Type id = Expr;
					 //id id = Expr;
					 Token id2 = new Token(null,"");
					 acceptIt();
					 
					 parseSpecificToken(TokenKind.EQUAL, "=");
					 Expression e = parseE();
					 parseSpecificToken(TokenKind.SEMICOLON, ";");
					 VarDecl vd = new VarDecl(new ClassType(new Identifier(id),null), id2.spelling, null);
					 return new VarDeclStmt(vd,e, null);
				 }
				 
			  else
				 	 parseError("Invalid Term - expecting . or ID or = or ( or [ but found " + token.spelling); 
				 	 return null;
			default:
				parseError("Invalid Term - expecting ID or KEYWORD but found " + token.kind);
				return null;
			}
			
			
									
		}
 
		//E ::= Rest (binop E)*
		/*private void parseE() throws SyntaxError {
			System.out.println("In E");	 
			 
			switch(token.kind){
			
			case BRACE:
				parseSpecificToken(TokenKind.BRACE, "(");
				 
					parseE();
				
				parseSpecificToken(TokenKind.BRACE, ")");
				
				break;
			case ID:
				acceptIt();
				if(token.spelling.equals("[") && token.kind == TokenKind.BRACE ){
					acceptIt();
					parseE();
					parseSpecificToken(TokenKind.BRACE, "]");
				}
				else if(token.spelling.equals(".") && token.kind == TokenKind.DOT ){
					
					while(token.kind==TokenKind.DOT){
						acceptIt();
						parseSpecificToken(TokenKind.ID, token.spelling);
						}
					
					if(token.spelling.equals("(") && token.kind == TokenKind.BRACE){
						acceptIt();
						 if(!token.spelling.equals(")") )
							 parseAL();
						 parseSpecificToken(TokenKind.BRACE, ")");
					}
					
				}
				else if(token.spelling.equals("(") && token.kind == TokenKind.BRACE){
					acceptIt();
					 if(!token.spelling.equals(")") )
						 parseAL();
					 parseSpecificToken(TokenKind.BRACE, ")");
				}
				
				// else is not there as it has epsilon in it.
				
				break;
			case KEYWORD:
				
				if(token.spelling.equals("true") || token.spelling.equals("false") )
					acceptIt();
				
				else if(token.spelling.equals("new") ){
					acceptIt();
					
					if(token.spelling.equals("int") && token.kind == TokenKind.KEYWORD ){
						acceptIt();
						parseSpecificToken(TokenKind.BRACE, "[");
						parseE();
						parseSpecificToken(TokenKind.BRACE, "]");
					}
					else if(token.kind == TokenKind.ID){
						acceptIt();
						
						if(token.spelling.equals("(") && token.kind == TokenKind.BRACE){
							acceptIt();
							parseSpecificToken(TokenKind.BRACE, ")");
						}
						else if(token.spelling.equals("[") && token.kind == TokenKind.BRACE){
							acceptIt();
							parseE();
							parseSpecificToken(TokenKind.BRACE, "]");
						}
						else
							parseError("Invalid Term - expecting Brace [ or ( but found " + token.kind + token.spelling);
					}
					else
						parseError("Invalid Term - expecting ID or new/int but found " + token.kind + token.spelling);
				}
				
				else if(token.spelling.equals("this")){
					acceptIt();
					while(token.kind == TokenKind.DOT ){
						acceptIt();
						parseSpecificToken(TokenKind.ID, token.spelling);
					}
					
					if(token.spelling.equals("(") && token.kind == TokenKind.BRACE){
						acceptIt();
						 if(!token.spelling.equals(")") )
							 parseAL();
						 parseSpecificToken(TokenKind.BRACE, ")");
					}
				}
					
					
				else
					parseError("Invalid Term - expecting Keyword true/false/new/this but found " + token.spelling);
				
				break;
			case NUM :
					acceptIt();
					break;	
			case UNOP:
				acceptIt();
				parseE();
				break;
			default:
				parseError("Invalid Term - expecting ID or KEYWORD but found " + token.kind);
			}
			
			
			
			while(token.kind==TokenKind.BINOP){
				acceptIt();
				parseE();
			}
			
				
		}*/		
		
		
		//E ::= OR (|| OR)*
		private Expression parseE() throws SyntaxError { //parseE 's name can be kept as parseOR
			Expression e1,e2;
			Token opToken;
			Operator op;
			
			e1 = parseAND();
			
			while(token.kind==TokenKind.BINOP && token.spelling.equals("||")){
				opToken= acceptIt();
				 op = new Operator(opToken);
			e2=	parseAND();
			e1= new BinaryExpr(op,e1,e2,null);
			}
			return e1;		
		}
		
		
		//AND ::= equality (&& equality)*
		private Expression parseAND() throws SyntaxError {
			Expression e1,e2;
			Token opToken;
			Operator op;
			
		e1 =	parseEquality();
			
			while(token.kind==TokenKind.BINOP && token.spelling.equals("&&")  ){
				opToken= acceptIt();
				 op = new Operator(opToken);
			e2 = 	parseEquality();
			e1= new BinaryExpr(op,e1,e2,null);
			}
			return e1;		
		}
		
		
		
		
		//equality ::= relational ((== | != ) relational)*
				private Expression parseEquality() throws SyntaxError {
					Expression e1,e2;
					Token opToken;
					Operator op;
					
					e1 = parseRelational();
					
					while(token.kind==TokenKind.BINOP &&( token.spelling.equals("==") || token.spelling.equals("!="))){
						opToken= acceptIt();
						 op = new Operator(opToken);
					e2 = 	parseRelational();
					e1= new BinaryExpr(op,e1,e2,null);
					}
					return e1;		
				}
		
				//relational = additive ((< | >| <=| >= ) additive)*
				private Expression parseRelational() throws SyntaxError {
					Expression e1,e2;
					Token opToken;
					Operator op;
					
					e1 = parseAdditive();
					
					while(token.kind==TokenKind.BINOP &&(token.spelling.equals(">")  || token.spelling.equals("<") || token.spelling.equals("<=") || token.spelling.equals(">="))){
						opToken= acceptIt();
						 op = new Operator(opToken);
						e2 = parseAdditive();
						e1= new BinaryExpr(op,e1,e2,null);
					}
					return e1;			
				}
		
				//additive = multiplicative ((+ | - ) multiplicative)*
				private Expression parseAdditive() throws SyntaxError {
					Expression e1,e2;
					Token opToken;
					Operator op;
					
					
					e1 = parseMultiplicative();
					
					while(token.kind==TokenKind.BINOP &&(  token.spelling.equals("+") || token.spelling.equals("-"))){
						opToken= acceptIt();
						 op = new Operator(opToken);
						e2 = parseMultiplicative();
						
						e1= new BinaryExpr(op,e1,e2,null);
					}
					return e1;	
				}	
				
				
				
				//multiplicative = unary ((*| / ) unary)*
				private Expression parseMultiplicative() throws SyntaxError {
					Expression e1,e2;
					Token opToken;
					Operator op;
					
				e1=	parseUnary();
					
					while(token.kind==TokenKind.BINOP &&(  token.spelling.equals("*") || token.spelling.equals("/"))){
						opToken= acceptIt();
						 op= new Operator(opToken);
					e2=	parseUnary();
					e1= new BinaryExpr(op,e1,e2,null);
					
					}
					
					return e1;
				}	
				

				
				//unary =   (-| !  )  unary |rest
				private Expression parseUnary() throws SyntaxError {
					Expression e;
					Token opToken;
					Operator op;
					 if(token.kind == TokenKind.UNOP){
						 opToken= acceptIt();
						 op= new Operator(opToken);
					e=	 parseUnary();
					return new UnaryExpr(op,e, null);
					 }
					 else{
						 e	= parseRest();
						return e; 
					 }
				}
				 
				
				//rest  
				private Expression parseRest() throws SyntaxError {
					
					Expression	e;
					Token idToken;
					ExprList alExpList;
					alExpList =new ExprList();
					
					switch(token.kind){
					
					case BRACE:
						parseSpecificToken(TokenKind.BRACE, "(");
						 
					e = parseE();
						
						parseSpecificToken(TokenKind.BRACE, ")");
						
						  return e;
					case ID:
						  idToken = acceptIt();
						Identifier  idIdentifier = new Identifier( idToken);
						Reference idRef = new IdRef(idIdentifier,null);
						
						if(token.spelling.equals("[") && token.kind == TokenKind.BRACE ){
							acceptIt();
						  e =	parseE();
							parseSpecificToken(TokenKind.BRACE, "]");
							
							return new RefExpr(new  IndexedRef(new IdRef(idIdentifier,null), e, null) , null);
						}
						else if(token.spelling.equals(".") && token.kind == TokenKind.DOT ){
							
							while(token.kind==TokenKind.DOT){
								acceptIt();
							Token	qualIdToken= parseSpecificToken(TokenKind.ID, token.spelling);
								
								idRef= new QualifiedRef(idRef,new Identifier(qualIdToken),null);
								}
							
							if(token.spelling.equals("(") && token.kind == TokenKind.BRACE){
								
								acceptIt();
								 if(!token.spelling.equals(")") ){
									 alExpList	= parseAL();
								 }
								 parseSpecificToken(TokenKind.BRACE, ")");
								 
								 
								 return new CallExpr(idRef,alExpList,null);
							  
							}
							return new RefExpr(idRef,null);	
						}
						else if(token.spelling.equals("(") && token.kind == TokenKind.BRACE){
							acceptIt();
							 if(!token.spelling.equals(")") ){
								 alExpList	=  parseAL();
							 }
							 parseSpecificToken(TokenKind.BRACE, ")");
							 return new CallExpr(idRef,alExpList,null);
						}
						
						// else is not there as it has epsilon in it.
						
						 return  new RefExpr(idRef, null);
						 
					case KEYWORD:
						
						if(token.spelling.equals("true") || token.spelling.equals("false") ){
						Token booleanToken = 	acceptIt();
							return new LiteralExpr(new BooleanLiteral(booleanToken), null);
						}
						else if(token.spelling.equals("new") ){
							 acceptIt();
							
							if(token.spelling.equals("int") && token.kind == TokenKind.KEYWORD ){
								acceptIt();
								parseSpecificToken(TokenKind.BRACE, "[");
							e=	parseE();
								parseSpecificToken(TokenKind.BRACE, "]");
								
								return new NewArrayExpr(new BaseType(TypeKind.INT,null), e, null);
							}
							else if(token.kind == TokenKind.ID){
								
						 	idToken=	acceptIt();
								
								if(token.spelling.equals("(") && token.kind == TokenKind.BRACE){
									acceptIt();
									parseSpecificToken(TokenKind.BRACE, ")");
									return new NewObjectExpr(new ClassType(new Identifier(idToken),null), null);
								}
								else if(token.spelling.equals("[") && token.kind == TokenKind.BRACE){
									acceptIt();
								e=	parseE();
									parseSpecificToken(TokenKind.BRACE, "]");
									
									return new NewArrayExpr(new ClassType(new Identifier(idToken),null), e, null);
								}
								else{
									parseError("Invalid Term - expecting Brace [ or ( but found " + token.kind + token.spelling);
								return null;
								}
							
							}
							else{
								parseError("Invalid Term - expecting ID or new/int but found " + token.kind + token.spelling);
							return null;
							}
						
						
						
						}
						
						else if(token.spelling.equals("this")){
						 	acceptIt();
						Reference thisReference= 	new ThisRef(null);
						
							while(token.kind == TokenKind.DOT ){
								acceptIt();
							  idToken=	parseSpecificToken(TokenKind.ID, token.spelling);
							
							thisReference= new QualifiedRef(thisReference,new Identifier(idToken), null);
							}
							
							if(token.spelling.equals("(") && token.kind == TokenKind.BRACE){
								acceptIt();
							 
								 if(!token.spelling.equals(")") ){
									 
									   alExpList  =	 parseAL();
								 }
								 parseSpecificToken(TokenKind.BRACE, ")");
								 
						 	 return new CallExpr(thisReference,alExpList,null);
							}
							
							return new RefExpr(thisReference,null);
						}
							
							
						else{
							parseError("Invalid Term - expecting Keyword true/false/new/this but found " + token.spelling);
						return null;
						}
						 
					case NUM :
						Token numToken=	acceptIt();
						
					 
						return new LiteralExpr(new IntLiteral(numToken),null); 
					default:
						parseError("Invalid Term - expecting ID or KEYWORD but found " + token.kind);
						
					}
					
				 	
					return null;
				}		
}
