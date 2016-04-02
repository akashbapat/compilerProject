
package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class NullDecl extends Declaration {
	
	public NullDecl( SourcePosition posn){
		super("null",new BaseType(TypeKind.NULL, posn),posn);
	}

	@Override
	public <A, R> R visit(Visitor<A, R> v, A o) {
		return v.visitNullDecl(this, o);
	}
}
