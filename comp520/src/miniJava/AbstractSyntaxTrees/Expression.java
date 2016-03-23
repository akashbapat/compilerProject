/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import  miniJava.SyntacticAnalyzer.SourcePosition;

public abstract class Expression extends AST {

  public Expression(SourcePosition posn) {
    super (posn);
  }
  
  /*private Type t;
  
  public Type getType(){
	  return t;
  }
  
  public void setType(Type inType){
	  t=inType;
  }
   commented by akash, edits removed from log*/
}
