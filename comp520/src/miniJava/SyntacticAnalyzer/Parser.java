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
	public void parse() {
		token = scanner.scan();
		try {
			parseP();
		}
		catch (SyntaxError e) { }
	}

	//    P ::= (CD)*$
	private void parseP() throws SyntaxError {
		System.out.print(token.kind + " " + token.spelling + "*");
		 while(token.kind == TokenKind.KEYWORD && token.spelling.equals("class"))
			 parseCD();
		accept(TokenKind.EOT);
	}


	 

	//   T ::= num | "(" E ")"
 

	/**
	 * accept current token and advance to next token
	 */
	private void acceptIt() throws SyntaxError {
		accept(token.kind);
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

	
	
	
//  AR ::= id [E]
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
	
	//parses braces and checks for brace type
	private void parseSpecificToken(TokenKind tk, String tSpell) throws SyntaxError {
		 	
			if(token.kind ==tk && tSpell.equals(token.spelling)  ){
				System.out.print(token.kind + " ");
				System.out.println(token.spelling);
				acceptIt();
				}
				else{
					if(tk == TokenKind.ID)
						parseError("Invalid Term - expecting "+ tk + " but found " + token.kind + " " + token.spelling);
					
					else
						parseError("Invalid Term - expecting "+ tk + " " + tSpell + " but found " + token.kind + " " + token.spelling);
				
				}
				
		 
	}
	
	
	
	//R ::=(this|id)(.id)*
	private void parseR() throws SyntaxError {
		
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
	
	
	
	// T::= boolean | int(e|[]) | id (e|[])
private void parseT() throws SyntaxError {
		
	switch (token.kind) {

	case ID:
		acceptIt();
		 if(token.kind==TokenKind.BRACE && token.spelling.equals("[")){
			 acceptIt();
			 parseSpecificToken(TokenKind.BRACE,"]");
		 }
		
		 break;
	case KEYWORD:
		if(token.spelling.equals("boolean"))
			acceptIt();
		
		else if (token.spelling.equals("int")){
			acceptIt();
			 if(token.kind==TokenKind.BRACE && token.spelling.equals("[")){
				 acceptIt();
				 parseSpecificToken(TokenKind.BRACE,"]");
			 }
		}
		else 
			parseError("Invalid Term - expecting int or boolean but found " + token.spelling);
		 
		break;
	default:
		parseError("Invalid Term - expecting ID or KEYWORD but found " + token.kind);
	}
		 
 }


//A ::=static?
	private void parseA() throws SyntaxError {
		
		if(token.kind ==TokenKind.KEYWORD && token.spelling.equals("static"))
			 acceptIt();
		 
	}
	
	
	
	//V ::=(public|private)?
		private void parseV() throws SyntaxError {
			
			if(token.kind ==TokenKind.KEYWORD && ( token.spelling.equals("public") || token.spelling.equals("private")))
				 acceptIt();
			 
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
		private void parseCD() throws SyntaxError {

			parseSpecificToken(TokenKind.KEYWORD,"class");
			
			parseSpecificToken(TokenKind.ID, token.spelling);
			
			parseSpecificToken(TokenKind.BRACE, "{");
			
				
			while(!token.spelling.equals("}")){
			
			parseV();
			parseA();
			
			if(token.kind ==TokenKind.KEYWORD &&  token.spelling.equals("void") ){
				acceptIt();
				parseSpecificToken(TokenKind.ID, token.spelling);
				parseSpecificToken(TokenKind.BRACE, "(");
				if(token.spelling.equals("boolean") || token.spelling.equals("int") || token.kind ==TokenKind.ID)
					 parsePL();
				
				parseSpecificToken(TokenKind.BRACE, ")");
				
				parseSpecificToken(TokenKind.BRACE, "{");
				
				while(!token.spelling.equals("}"))
					parseS();
				
				parseSpecificToken(TokenKind.BRACE, "}");
				
				
			}
			else{
				parseT();
				parseSpecificToken(TokenKind.ID, token.spelling);
				
				if(token.spelling.equals(";") && token.kind == TokenKind.SEMICOLON){
					acceptIt();
				//System.out.println(token.kind + " " + token.spelling);
				
				}
				else{	
					parseSpecificToken(TokenKind.BRACE, "(");
						 if(token.spelling.equals("boolean") || token.spelling.equals("int")  || token.kind ==TokenKind.ID)
							 parsePL();
						 
						parseSpecificToken(TokenKind.BRACE, ")");
						 
						parseSpecificToken(TokenKind.BRACE, "{");
						
						while(!token.spelling.equals("}"))
							parseS();
						
						parseSpecificToken(TokenKind.BRACE, "}");
				}
			}
		}
			
	 parseSpecificToken(TokenKind.BRACE, "}");
				
	}	
		
		
		
		//PL ::= T id ( , T id)*
				private void parsePL() throws SyntaxError {
					
					 
					parseT();
					parseSpecificToken(TokenKind.ID, token.spelling);
					
					while(token.kind==TokenKind.COMMA){
						acceptIt();
						parseT();
						parseSpecificToken(TokenKind.ID, token.spelling);
						
					}
						
						
				}
				
				

	//AL ::= E ( , E)*
	private void parseAL() throws SyntaxError {
		
		parseE();
		 		
		while(token.kind==TokenKind.COMMA){
			acceptIt();
			parseE();
		}
								
	}
	
	
	//S ::= 
		private void parseS() throws SyntaxError {
			
		System.out.println("In S");	 
			switch (token.kind) {

			case BRACE:
				parseSpecificToken(TokenKind.BRACE, "{");
				while(!token.spelling.equals("}"))
					parseS();
				
				parseSpecificToken(TokenKind.BRACE, "}");
				
				 
				break;
			case KEYWORD:
				
					 if(token.spelling.equals("boolean") ){
						 acceptIt();
						 parseSpecificToken(TokenKind.ID, token.spelling);
						 parseSpecificToken(TokenKind.EQUAL, "=");
						 parseE();
						 parseSpecificToken(TokenKind.SEMICOLON, ";");
					 }
					 
					 else  if(  token.spelling.equals("int") ){
						 acceptIt();
						 if(token.kind==TokenKind.BRACE && token.spelling.equals("[")){
							 acceptIt();
							 parseSpecificToken(TokenKind.BRACE,"]");
						 }
						 
						 
						 parseSpecificToken(TokenKind.ID, token.spelling);
						 parseSpecificToken(TokenKind.EQUAL,  "=");
						 parseE();
						 parseSpecificToken(TokenKind.SEMICOLON, ";");
					 }
					 
					 
					 else  if(token.spelling.equals("return")){
						 acceptIt();
						 
						 while(token.kind!=TokenKind.SEMICOLON)
							 parseE();
						 
						 parseSpecificToken(TokenKind.SEMICOLON,";");
						   
					 }
					 
					 else  if(token.spelling.equals("if")){
						 acceptIt();
						 
						 parseSpecificToken(TokenKind.BRACE, "(");
						 parseE();
						 parseSpecificToken(TokenKind.BRACE, ")");
						 
						 parseS();
						 
						 if(token.spelling.equals("else") && token.kind==TokenKind.KEYWORD){
							 acceptIt();
							 parseS();
						 }
						 
						   
					 }
					 
					 
					 else  if(token.spelling.equals( "while")){
						 acceptIt();
						 
						 parseSpecificToken(TokenKind.BRACE, "(");
						 parseE();
						 parseSpecificToken(TokenKind.BRACE, ")");
						 parseS();
						  
						 }
					 
					 else  if(token.spelling.equals("this")){
						 acceptIt();
						 
						 while(token.kind == TokenKind.DOT){
							 acceptIt();
							 parseSpecificToken(TokenKind.ID, token.spelling);
							 }
							 
								 if(token.kind == TokenKind.EQUAL){
									 acceptIt();
									 parseE();
																	
								 }
								 else if(token.kind == TokenKind.BRACE   && token.spelling.equals("(")) {
									 	
									 acceptIt();
									 	if ( !token.spelling.equals(")"))
									 		parseAL();
									 	
									 	parseSpecificToken(TokenKind.BRACE, ")");
								 }
								 
								 else
									 parseError("Invalid Term - expecting   = or ( but found" + token.spelling); 
					
								 parseSpecificToken(TokenKind.SEMICOLON, ";");
					 }
					 else
						 parseError("Invalid Term - expecting  this/while/return/boolean/int/if but found " + token.spelling); 
						 
						  
			
					 break;
			case ID:
				 parseSpecificToken(TokenKind.ID, token.spelling);
				 
				 if(token.kind == TokenKind.DOT){
					 
					 
					 while(token.kind == TokenKind.DOT){
						 acceptIt();
						 parseSpecificToken(TokenKind.ID, token.spelling);
						 }
						 
							 if(token.kind == TokenKind.EQUAL){
								 acceptIt();
								 parseE();
																
							 }
							 
							 
							 else if(token.kind == TokenKind.BRACE && token.spelling.equals("(")){
								acceptIt();
								
								if(!token.spelling.equals(")"))
										parseAL();
								
								 parseSpecificToken(TokenKind.BRACE,")"); 
								 
							 } 	
							 parseSpecificToken(TokenKind.SEMICOLON,";"); 
				 }
				 
				 else if (token.kind == TokenKind.BRACE && token.spelling.equals("[")){
					 acceptIt();
					 
					 if(token.kind == TokenKind.BRACE && token.spelling.equals("]")){
						 acceptIt();
						 parseSpecificToken(TokenKind.ID, token.spelling);
						 parseSpecificToken(TokenKind.EQUAL,  "=");
						 parseE();
						 parseSpecificToken(TokenKind.SEMICOLON, ";");
					 }
					 else{
						 parseE();
						 parseSpecificToken(TokenKind.BRACE, "]");
						 parseSpecificToken(TokenKind.EQUAL,  "=");
						 parseE();
						 parseSpecificToken(TokenKind.SEMICOLON, ";");
					 }
				 }
				 
				 else if(token.kind == TokenKind.EQUAL){
					 acceptIt();
					 	parseE();
					 	parseSpecificToken(TokenKind.SEMICOLON, ";");
				 }
				 
				 else if(token.kind == TokenKind.BRACE && token.spelling.equals("(")){
					 acceptIt();

						if(!token.spelling.equals(")"))
								parseAL();
						
						 parseSpecificToken(TokenKind.BRACE,")"); 
						 parseSpecificToken(TokenKind.SEMICOLON, ";");
					 
				 }
				 
				 else if(token.kind == TokenKind.ID){
					 acceptIt();
					 
					 parseSpecificToken(TokenKind.EQUAL, "=");
					 parseE();
					 parseSpecificToken(TokenKind.SEMICOLON, ";");
				 }
				 
			  else
				 	 parseError("Invalid Term - expecting . or ID or = or ( or [ but found " + token.spelling); 
				 
				 break; 
			default:
				parseError("Invalid Term - expecting ID or KEYWORD but found " + token.kind);
			}
			
			
									
		}
 
		//E ::= Rest (binop E)*
		private void parseE() throws SyntaxError {
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
			
				
		}			
		
}
