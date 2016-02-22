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
	private char previousChar; //Used to maintain line number
	private TokenKind prevToken;
	private StringBuilder currentSpelling;
	private int currPosn;
	private int currLinePosn;
	private int currLine;
	private boolean eot = false; 

	public Scanner(InputStream inputStream, ErrorReporter reporter) {
		this.inputStream = inputStream;
		this.reporter = reporter;
		this.keywordHmap = new HashMap<String, Integer>();
		initializeHMap();
		currPosn = 0;
		currLinePosn = 0;
		currLine = 0;
		// initialize scanner state
		readChar();
	}

	/**
	 * skip whitespace and scan next token
	 * @return token
	 */
	public Token scan() {



		// collect spelling and identify token kind
		currentSpelling = new StringBuilder();
		TokenKind kind = scanToken();
		// return new token
		return new Token(kind, currentSpelling.toString());
	}


	public TokenKind scanToken() 
	{

		while (!eot && (currentChar == ' ' ||  currentChar == '\r' || currentChar == '\n' || currentChar == '\t') )
			skipIt();
		if (eot){
			prevToken = TokenKind.EOT;
			return (TokenKind.EOT);
		}
		else if (isBrace()){
			prevToken = TokenKind.BRACE;
			return (TokenKind.BRACE);
		}
		else if (isBinOp()){
			prevToken = TokenKind.BINOP;
			return (TokenKind.BINOP);
		}
		else 
		{
			switch (currentChar) {
			case 'a':  case 'b':  case 'c':  case 'd':  case 'e':
		    case 'f':  case 'g':  case 'h':  case 'i':  case 'j':
		    case 'k':  case 'l':  case 'm':  case 'n':  case 'o':
		    case 'p':  case 'q':  case 'r':  case 's':  case 't':
		    case 'u':  case 'v':  case 'w':  case 'x':  case 'y':
		    case 'z':
		    case 'A':  case 'B':  case 'C':  case 'D':  case 'E':
		    case 'F':  case 'G':  case 'H':  case 'I':  case 'J':
		    case 'K':  case 'L':  case 'M':  case 'N':  case 'O':
		    case 'P':  case 'Q':  case 'R':  case 'S':  case 'T':
		    case 'U':  case 'V':  case 'W':  case 'X':  case 'Y':
		    case 'Z':
		    	while (isAlphabet(currentChar) || isDigit(currentChar) || currentChar=='_')
					takeIt();
		    	if (isKeyword(currentSpelling.toString())) {
					prevToken = TokenKind.KEYWORD;
					return (TokenKind.KEYWORD);
				} 
				else 
				{
					prevToken = TokenKind.ID;
					return (TokenKind.ID);
				}	
				
		    case '0':			case '1':			case '2':			case '3':			case '4':
		    case '5':			case '6':			case '7':			case '8':			case '9':
				while (isDigit(currentChar))
					takeIt();
				prevToken = TokenKind.NUM;
				return (TokenKind.NUM);	
			
				
		    case '|':
				takeIt();
				if(currentChar=='|'){
					takeIt();
					prevToken = TokenKind.BINOP;
					return (TokenKind.BINOP);
				}
				else {
					scanError("Unrecognized character '" + '|' + "' in input");
					prevToken = TokenKind.ERROR;
					takeIt();
					return (TokenKind.ERROR);
				}
				
			case '&':
				takeIt();
				if(currentChar=='&'){
					takeIt();
					prevToken = TokenKind.BINOP;
					return (TokenKind.BINOP);
				}
				else {
					scanError("Unrecognized character '" + '&' + "' in input");
					skipIt();
					prevToken = TokenKind.ERROR;
					return (TokenKind.ERROR);
				}	
			case '/':
				takeIt();
				if (currentChar == '*' || currentChar == '/'){
					if(ignoreComments()){
						return scanToken();
					}
					else{
						scanError("Unterminated multi-line comment");
						prevToken = TokenKind.ERROR;
						return (TokenKind.ERROR);
					}
				}
				else{
					prevToken = TokenKind.BINOP;
					return (TokenKind.BINOP);
				}
			case '!':
				takeIt();
				if (currentChar == '='){
					takeIt();
					prevToken = TokenKind.BINOP;
					return (TokenKind.BINOP);
				}
				else{
					prevToken = TokenKind.UNOP;
					return (TokenKind.UNOP);
				}
			case '-':
				takeIt();
				if(currentChar != '-')
				{
					if (prevToken == TokenKind.NUM || prevToken == TokenKind.ID){
						prevToken = TokenKind.BINOP;
						return (TokenKind.BINOP);
					}
					else{
						prevToken = TokenKind.UNOP;
						return (TokenKind.UNOP);
					}
				}
				else
				{
					scanError("Unrecognized character '" + " -- " + "' in input");
					prevToken = TokenKind.ERROR;
					skipIt();
					return (TokenKind.ERROR);
				}
				
			case '=':
				takeIt();
				if (currentChar == '='){
					takeIt();
					prevToken = TokenKind.BINOP;
					return (TokenKind.BINOP);
				}
				else{
					prevToken = TokenKind.EQUAL;
					return (TokenKind.EQUAL);
					}
			case '.':
				takeIt();
				prevToken = TokenKind.DOT;
				return (TokenKind.DOT);

			case ',':
				takeIt();
				prevToken = TokenKind.COMMA;
				return (TokenKind.COMMA);

			case ';':
				takeIt();
				prevToken = TokenKind.SEMICOLON;
				return (TokenKind.SEMICOLON);
			
			default:
				scanError("Unrecognized character '" + currentChar + "' in input");
				prevToken = TokenKind.ERROR;
				takeIt();
				return (TokenKind.ERROR);
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
	
	private boolean isAlphabet(char c) {
		boolean isCapAlpha = (c >= 'A') && (c <= 'Z');
		boolean isSmallAlpha = (c >= 'a') && (c <= 'z');
		return isCapAlpha || isSmallAlpha;
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
		keywordHmap.put("int",1);
		keywordHmap.put("boolean",1);
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
		{
			readChar();
			currPosn++;
			currLinePosn++;
			if(currentChar == '\n')
			{
				currLinePosn = 0;
				currLine++;
			}
		}
			
	}

	private void readChar() {
		try {
			int c = inputStream.read();
			currentChar = (char) c;
			if (c == -1 ) {
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
			do{
				prevChar=currentChar;
				nextChar();
				if(eot)
					return false;
			}while(!(currentChar=='/' && prevChar =='*') );
			
			nextChar();
			currentSpelling.setLength(0);
		}
		else {  // single line comment
			
			while(! (currentChar=='\n' || currentChar=='\r' || eot)){
				nextChar();
				 
			}
			nextChar();
			if(currentChar=='\n')
				skipIt();
			currentSpelling.setLength(0);
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
		
		case '>': case '<':
			
			takeIt();
			if(currentChar=='=') {
				takeIt();
				return true;
			}
			else 
				return true;
			
	

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
