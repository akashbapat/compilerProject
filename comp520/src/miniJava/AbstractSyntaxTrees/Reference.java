/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public abstract class Reference extends AST
{
	 private Declaration decl;
	 public boolean isStatic = false;
	 
	public Reference(SourcePosition posn){
		 
		super(posn);
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
	  
	
	

}
