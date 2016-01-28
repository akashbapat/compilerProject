/**
 *  Scan the first line of an input stream
 *
 *  Grammar:
 *   num ::= digit digit*
 *   digit ::= '0' | ... | '9'
 *   oper ::= '+' | '*'
 *   
 *   whitespace is the space character
 */
package miniJava.SyntacticAnalyzer;

import java.io.*;
import miniJava.ErrorReporter;
import java.util.HashMap;
import java.lang.String;

public class Scanner{

	private InputStream inputStream;
	private ErrorReporter reporter;
	private HashMap<String,Integer> keywordHmap; 
	private char currentChar;
	private TokenKind prevToken;
	private StringBuilder currentSpelling;
	
	private boolean eot = false; 

	public Scanner(InputStream inputStream, ErrorReporter reporter) {
		this.inputStream = inputStream;
		this.reporter = reporter;
		this.keywordHmap = new HashMap<String, Integer>();
		// initialize scanner state
		readChar();
	}

	/**
	 * skip whitespace and scan next token
	 * @return token
	 */
	public Token scan() {

		// skip whitespace
		while (!eot && currentChar == ' ')
			skipIt();

		// collect spelling and identify token kind
		currentSpelling = new StringBuilder();
		TokenKind kind = scanToken();

		// return new token
		return new Token(kind, currentSpelling.toString());
	}


	public TokenKind scanToken() {
		
		if (eot)
			return(TokenKind.EOT); 
		
		else if(isBrace())
			return (TokenKind.BRACE);
		
		else	if(isBinOp())
			return (TokenKind.BINOP);
		
		
		else if(isBrace())
			return (TokenKind.BRACE);

		// scan Token
<<<<<<< HEAD

		switch (currentChar) {
		case '/':
			takeIt();
			if(currentChar=='*' || currentChar=='/')
				ignoreComments();
			else
				return(TokenKind.BINOP);

		case '!':
			takeIt();
			
			if(currentChar=='=')
				return (TokenKind.BINOP);
			else
			return(TokenKind.UNOP);

		 
		case '-':
			takeIt();
			if(prevToken==TokenKind.NUM || prevToken==TokenKind.ID)
				return (TokenKind.BINOP);
			else
				return (TokenKind.UNOP);
			
			
		case '=':
			takeIt();
			if(currentChar=='=')
				return(TokenKind.BINOP);
			else
				return (TokenKind.EQUAL);
		case '.':
			takeIt();
			return (TokenKind.DOT);
			
			
		case ',':
			takeIt();
			return (TokenKind.COMMA);
		default:
			scanError("Unrecognized character '" + currentChar + "' in input");
			return(TokenKind.ERROR);

		if (isAlphanumeric(currentChar))
		{
			while (isAlphanumeric(currentChar))
				takeIt();
			if(isKeyword(currentSpelling.toString()))
			{
				return(TokenKind.KEYWORD);
			}
			else
			{
				prevToken = TokenKind.ID;
				return(TokenKind.ID);
			}

=======
		if (isAlphanumeric(currentChar))
		{
			while (isAlphanumeric(currentChar))
				takeIt();
			if(isKeyword(currentSpelling.toString()))
			{
				return(TokenKind.KEYWORD);
			}
			else
			{
				prevToken = TokenKind.ID;
				return(TokenKind.ID);
			}
>>>>>>> refs/remotes/origin/master
		}
		else
		{
			switch (currentChar) {
			
			case '0': case '1': case '2': case '3': case '4':
			case '5': case '6': case '7': case '8': case '9':
				while (isDigit(currentChar))
					takeIt();
				prevToken = TokenKind.NUM;
				return(TokenKind.NUM);

			default:
				scanError("Unrecognized character '" + currentChar + "' in input");
				return(TokenKind.ERROR);
			}
			
		}
		

	}

	private void takeIt() {
		currentSpelling.append(currentChar);
		nextChar();
	}

	
	
	private void skipIt() {
		nextChar();
	}

	private boolean isDigit(char c) {
		return (c >= '0') && (c <= '9');
	}
	
	private boolean isAlphanumeric(char c) {
		boolean isCapAlpha = (c >= 'A') && (c <= 'Z');
		boolean isSmallAlpha = (c >= 'a') && (c <= 'z');
		return isCapAlpha || isSmallAlpha || isDigit(c);
	}
	
	private void initializeHMap()
	{
		keywordHmap.put("public",1);
		keywordHmap.put("private",1);
		keywordHmap.put("class",1);
		keywordHmap.put("void",1);
		keywordHmap.put("static",1);
		keywordHmap.put("this",1);
		keywordHmap.put("return",1);
		keywordHmap.put("if",1);
		keywordHmap.put("else",1);
		keywordHmap.put("while",1);
		keywordHmap.put("new",1);
		keywordHmap.put("true",1);
		keywordHmap.put("false",1);
	}
	private boolean isKeyword(String s){
		return this.keywordHmap.containsKey(s);
	}
	private void scanError(String m) {
		reporter.reportError("Scan Error:  " + m);
	}


	private final static char eolUnix = '\n';
	private final static char eolWindows = '\r';

	/**
	 * advance to next char in inputstream
	 * detect end of file or end of line as end of input
	 */
	private void nextChar() {
		if (!eot)
			readChar();
	}

	private void readChar() {
		try {
			int c = inputStream.read();
			currentChar = (char) c;
			if (c == -1 || currentChar == eolUnix || currentChar == eolWindows) {
				eot = true;
			}
		} catch (IOException e) {
			scanError("I/O Exception!");
			eot = true;
		}
	}



	
	private boolean ignoreComments(){
		char prevChar;
		if(currentChar== '*'){ //big comment
			prevChar=currentChar;
			nextChar();
			while(currentChar!='/' && prevChar !='*' ){
				prevChar=currentChar;
				nextChar();
				if(eot)
					return false;
			}
			
		}
		else {  // single line comment
			
			while(currentChar!='\n'){
				nextChar();
			}
			
				
		}
		
		return true;
	}
	
	private boolean isBrace(){
		
		if(currentChar == '{' || currentChar == '}' || currentChar == '('|| currentChar == ')' || currentChar == '[' || currentChar == ']'){
			takeIt();
			return true;
			}
		else 
			return false;
	}
	
	
	private boolean isBinOp(){
		
		switch (currentChar) {
		//TO DO : take care for /
		case '+':  case '*': 
			takeIt();	
			return true;
		case '|':
			takeIt();
			if(currentChar=='|'){
				takeIt();
				return true;
			}
			else return false;
		case '>': case '<':
			
			takeIt();
			if(currentChar=='=') {
				takeIt();
				return true;
			}
			else if(isWhitespace())			
				return true;
			
				
			else 
				return false;
		  

		default:
			 return false;
		} 
	
		
	
	}
	
	
	private boolean isWhitespace() {
		
		if(currentChar== '\n'|| currentChar== ' '|| currentChar== '\r'|| currentChar== '\t')
			return true;
		
		return false;
	}
	
}
