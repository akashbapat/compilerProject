/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */
package miniJava.AbstractSyntaxTrees;

import java.util.HashMap;

import  miniJava.SyntacticAnalyzer.SourcePosition;

public class ClassDecl extends Declaration {

  public ClassDecl(String cn, FieldDeclList fdl, MethodDeclList mdl, SourcePosition posn) {
	  super(cn, null, posn);
	  fieldDeclList = fdl;
	  methodDeclList = mdl;
	  isBaseClass = false;
	  parentClassName ="";
	  classSize = 0;
	  numNonStaticMethods = 0;
	//  parentClassList = new HashMap<String, Boolean>();
  }
  
  public <A,R> R visit(Visitor<A, R> v, A o) {
      return v.visitClassDecl(this, o);
  }
      
  public FieldDeclList fieldDeclList;
  public MethodDeclList methodDeclList;
  public boolean  isBaseClass;
  public String parentClassName;
  public ClassDecl parentClassDecl;
  public int classSize;
  public int numNonStaticMethods;
 
  
}
