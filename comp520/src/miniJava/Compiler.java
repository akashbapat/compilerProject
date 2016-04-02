/**
 *   COMP 520 
 *   Simple expression scanner and parser
 *     following package structure of a full compiler
 *
 *  Parser grammar:
 *   S ::= E '$'
 *   E ::= T (oper T)*     
 *   T ::= num | '(' E ')'
 *
 *  Scanner grammar:
 *   num ::= digit digit*
 *   digit ::= '0' | ... | '9'
 *   oper ::= '+' | '*'
 */

package miniJava;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import miniJava.SyntacticAnalyzer.Parser;
import miniJava.SyntacticAnalyzer.Scanner;
////Remove later
import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.TokenKind;
import miniJava.AbstractSyntaxTrees.*;
import miniJava.ContextualAnalyzer.ASTIdentification;
import miniJava.ContextualAnalyzer.typeChecker;
/**
 * Recognize whether input is an arithmetic expression as defined by
 * a simple context free grammar for expressions and a scanner grammar.
 * 
 */
public class Compiler {


	/**
	 * @param args  if no args provided parse from keyboard input
	 *              else args[0] is name of file containing input to be parsed  
	 */
	public static void main(String[] args) {

		InputStream inputStream = null;
		if (args.length == 0) {
			System.out.println("Enter Expression");
			inputStream = System.in;
		}
		else {
			try {
				inputStream = new FileInputStream(args[0]);
			} catch (FileNotFoundException e) {
				System.out.println("Input file " + args[0] + " not found");
				System.exit(1);
			}		
		}

		ErrorReporter reporter = new ErrorReporter();
		Scanner scanner = new Scanner(inputStream, reporter);
		Parser parser = new Parser(scanner, reporter);
		AST ast;
		int debug = 0;
		//Code for testing scanner
		if(debug == 1)
		{
			Token t;
			do
			{
				t = scanner.scan();
				System.out.print(t.kind);
				System.out.print(" ");
				System.out.println(t.spelling);
				
			}while (t.kind != TokenKind.EOT);

		}
		else
		{
			System.out.println("Syntactic analysis ... ");
			ast = parser.parse();
			System.out.print("Syntactic analysis complete: \n ");
			
	     
			if (reporter.hasErrors()) {
				System.out.println("INVALID arithmetic expression");
				System.exit(4);
			}
			else {
				System.out.println("valid arithmetic expression");
				ASTDisplay display = new ASTDisplay();
			
//				
//				
     			//display.showTree(ast);
 			ASTIdentification astIdentify = new ASTIdentification(reporter);
//				
//				 
 				if(	astIdentify.showTree(ast)){
 			 
 			 
 				 typeChecker typeCheckerObj = new typeChecker(reporter);
 	 		if( typeCheckerObj.typeCheckAST(ast))
 	 			System.exit(0);
 	 		else
 	 			System.exit(4);
 			}
 				else{
 					System.exit(4);
 				}
 				
 				
 				
				System.exit(0);
    
			}

		}
	}
}






