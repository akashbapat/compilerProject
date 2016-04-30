package miniJava.AbstractSyntaxTrees;

import java.io.UnsupportedEncodingException;

import miniJava.SyntacticAnalyzer.Token;
 

public class StringLiteral extends Terminal {
 

  public StringLiteral(Token t) {
    super(t);
    
	 
	 
	 
	 
  }
 
  public <A,R> R visit(Visitor<A,R> v, A o) {
      return v.visitStringLiteral(this, o);
  }
 
  
}