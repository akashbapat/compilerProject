package miniJava.AbstractSyntaxTrees;

import miniJava.SyntacticAnalyzer.SourcePosition;

public class ForStmt extends Statement
{
    public ForStmt(Statement i, Expression c,Statement inc, Statement b, SourcePosition posn){
        super(posn);
        init = i;
        cond = c;
        increment =inc;
        body = b;
        
    }
        
    public <A,R> R visit(Visitor<A,R> v, A o) {
        return v.visitForStmt(this, o); //for for.visit always check for null for init, cond and increment
    }
    public Statement init;
    public Expression cond;
    public Statement increment;
    public Statement body;
}
