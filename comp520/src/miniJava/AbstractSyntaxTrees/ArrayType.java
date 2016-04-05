/**
 * miniJava Abstract Syntax Tree classes
 * @author prins
 * @version COMP 520 (v2.2)
 */

package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class ArrayType extends Type {
	private Expression e;//added to maintain length of array

	public void setLengthExpression(Expression inE){ //added to maintain length of array
		e = inE;
	}

	public Expression getLengthExpression(){//added to maintain length of array
		
		if(e==null)
			System.out.println("Expression is null");
		return e;
	}

	public ArrayType(Type eltType, SourcePosition posn){
		super(TypeKind.ARRAY, posn);
		this.eltType = eltType;
	}

	public <A,R> R visit(Visitor<A,R> v, A o) {
		return v.visitArrayType(this, o);
	}

	public Type eltType;
}

