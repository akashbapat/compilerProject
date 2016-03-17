/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.Token;

public class Identifier extends Terminal {
 private Declaration decl;
  public Identifier (Token t) {
    super (t);
  }
  //added by akash
  public Declaration  getDecl(){
	  
	  if(decl==null)
		  System.out.println("Error: declaration accessed before setting");
	  return decl;
  }

//added by akash
  public void setDecl(Declaration d){
	  decl=d;
  }
  
  
  public <A,R> R visit(Visitor<A,R> v, A o) {
      return v.visitIdentifier(this, o);
  }

}
