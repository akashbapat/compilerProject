/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public abstract class AST {

  public AST (SourcePosition posn) {
    this.posn = posn;
  }
  
  public String toString() {
      String fullClassName = this.getClass().getName();
      String cn = fullClassName.substring(1 + fullClassName.lastIndexOf('.'));
      if (ASTDisplay.showPosition){
    	 // cn = cn + " " + posn.toString();
    	 if(posn==null)
    		 System.out.println("Warning: no resolvable position, possibly a statement");
    	
    	 else
    		  cn = cn + " Line no: " + Integer.toString(posn.line) +" position: " + Integer.toString(posn.linePosn) ;
    		 
      }
      return cn;
  }

  public abstract <A,R> R visit(Visitor<A,R> v, A o);

  public SourcePosition posn;
}
