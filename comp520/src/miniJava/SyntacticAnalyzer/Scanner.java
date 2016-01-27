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
 
public class Scanner{

	private InputStream inputStream;
	private ErrorReporter reporter;

	private char currentChar;
	private TokenKind prevToken;
	private StringBuilder currentSpelling;
	
	private boolean eot = false; 

	public Scanner(InputStream inputStream, ErrorReporter reporter) {
		this.inputStream = inputStream;
		this.reporter = reporter;

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

		// scan Token
		switch (currentChar) {
		case '+':
			takeIt();
			return(TokenKind.PLUS);

		case '*':
			takeIt();
			return(TokenKind.TIMES);

		case '(': 
			takeIt();
			return(TokenKind.LPAREN);

		case ')':
			takeIt();
			return(TokenKind.RPAREN);

		case '0': case '1': case '2': case '3': case '4':
		case '5': case '6': case '7': case '8': case '9':
			while (isDigit(currentChar))
				takeIt();
			return(TokenKind.NUM);

		default:
			scanError("Unrecognized character '" + currentChar + "' in input");
			return(TokenKind.ERROR);
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



	
	private boolean ignoreComments(char starOrSlash){
		char prevChar;
		if(starOrSlash== '*'){ //big comment
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
		
		if(currentChar == '{' || currentChar == '}' || currentChar == '('|| currentChar == ')' || currentChar == '[' || currentChar == ']')
			takeIt();
			return true;
		else 
			return false;
	}
	
	
	private boolean isBinOp(){
		
		switch (currentChar) {
		//TO DO : take care for /
		case '+': case '-': case '*': case '3': case '/':
		case '+':
			takeIt();
		 

		default:
			 return false;
		} 
	
		
		return true;
	}
	
}
