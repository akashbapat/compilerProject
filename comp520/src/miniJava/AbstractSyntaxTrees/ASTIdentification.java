package miniJava.AbstractSyntaxTrees;
import miniJava.ContextAnalyzer.idTable;;
public class ASTIdentification implements Visitor<idTable,idTable>{

	public void showTree(AST ast){
	    System.out.println("======= AST Identify =========================");
	    idTable idTab = new idTable();
	    ast.visit(this, idTab);
	    System.out.println("=============================================");
	}
	

	///////////////////////////////////////////////////////////////////////////////
	//
	// PACKAGE
	//
	/////////////////////////////////////////////////////////////////////////////// 

    public idTable visitPackage(Package prog, idTable idTab){
    	ClassDeclList cl = prog.classDeclList;
    	idTab.openScope();
        for (ClassDecl c: prog.classDeclList){
        	idTab.addDecl(c);
        }
        for (ClassDecl c: prog.classDeclList){
        	c.visit(this, idTab);
        }
        idTab.printLevel(1);
        idTab.closeScope();
        return idTab;
    }
    
	///////////////////////////////////////////////////////////////////////////////
	//
	// DECLARATIONS
	//
	///////////////////////////////////////////////////////////////////////////////
    
    public idTable visitClassDecl(ClassDecl clas, idTable idTab){
    	idTab.openScope();
        for (FieldDecl f: clas.fieldDeclList)
        {
        	idTab = f.visit(this, idTab);
        }
        	
        for (MethodDecl m: clas.methodDeclList){
        	idTab = m.visit(this, idTab);
        }
        
        idTab.printLevel(2);
        idTab.closeScope();
        return idTab;
    }
    
    public idTable visitFieldDecl(FieldDecl f, idTable idTab){
    	idTab.addDecl(f);
    	return idTab;
    }
    
    public idTable visitMethodDecl(MethodDecl m, idTable idTab){
    	idTab.openScope();
        ParameterDeclList pdl = m.parameterDeclList;
        for (ParameterDecl pd: pdl) {
        	idTab = pd.visit(this, idTab);
        }
        StatementList sl = m.statementList;
        idTab.openScope();
        for (Statement s: sl) {
            idTab = s.visit(this, idTab);
        
        }
        idTab.printLevel(4);
        idTab.closeScope();
        idTab.printLevel(3);
        idTab.closeScope();
        return idTab;
    }
    
    public idTable visitParameterDecl(ParameterDecl pd, idTable idTab){
    	idTab.addDecl(pd);
        return idTab;
    } 
    
    public idTable visitVarDecl(VarDecl vd, idTable idTab){
        idTab.addDecl(vd);
        return idTab;


}
    
///////////////////////////////////////////////////////////////////////////////
//
// TYPES
//
///////////////////////////////////////////////////////////////////////////////

public idTable visitBaseType(BaseType type, idTable idTab){
    return idTab;
}

public idTable visitClassType(ClassType type, idTable idTab){
    return idTab;
}

public idTable visitArrayType(ArrayType type, idTable idTab){
    return idTab;
}


///////////////////////////////////////////////////////////////////////////////
//
// STATEMENTS
//
///////////////////////////////////////////////////////////////////////////////

public idTable visitBlockStmt(BlockStmt stmt, idTable idTab){
	idTab.openScope();
	StatementList sl = stmt.sl;
    for (Statement s: sl) {
    	idTab = s.visit(this, idTab);
    }
    idTab.printRestLevel();
	idTab.closeScope();
    return idTab;
}

public idTable visitVardeclStmt(VarDeclStmt stmt, idTable idTab){
    idTab = stmt.varDecl.visit(this, idTab);	
    idTab = stmt.initExp.visit(this, idTab);
    return idTab;
}

public idTable visitAssignStmt(AssignStmt stmt, idTable idTab){
    idTab = stmt.ref.visit(this, idTab);
    idTab = stmt.val.visit(this, idTab);
    return idTab;
}

public idTable visitIxAssignStmt(IxAssignStmt stmt, idTable idTab){
    idTab = stmt.ixRef.visit(this, idTab);
    idTab = stmt.val.visit(this, idTab);
    return idTab;
}

public idTable visitCallStmt(CallStmt stmt, idTable idTab){
 
    
    idTab=  stmt.methodRef.visit(this, idTab );
    ExprList al = stmt.argList;
    
     
    for (Expression e: al) {
    	idTab=        e.visit(this, idTab);
    }
    return null;
    
}

public idTable visitReturnStmt(ReturnStmt stmt, idTable idTab){
     if (stmt.returnExpr != null)
        idTab = stmt.returnExpr.visit(this, idTab);
    return idTab;
}

public idTable visitIfStmt(IfStmt stmt, idTable idTab){
    idTab = stmt.cond.visit(this, idTab);
    idTab = stmt.thenStmt.visit(this, idTab);
    if (stmt.elseStmt != null)
        idTab = stmt.elseStmt.visit(this, idTab);
    return idTab;
}

public idTable visitWhileStmt(WhileStmt stmt, idTable idTab){
    idTab = stmt.cond.visit(this, idTab);
    idTab = stmt.body.visit(this, idTab);
    return idTab;
}


///////////////////////////////////////////////////////////////////////////////
//
// EXPRESSIONS
//
///////////////////////////////////////////////////////////////////////////////

public idTable visitUnaryExpr(UnaryExpr expr, idTable idTab){
    idTab = expr.operator.visit(this, idTab);
    idTab = expr.expr.visit(this, idTab);
    return idTab;
}

public idTable visitBinaryExpr(BinaryExpr expr, idTable idTab){
    idTab = expr.operator.visit(this, idTab);
    idTab = expr.left.visit(this, idTab);
    idTab = expr.right.visit(this, idTab);
    return idTab;
}

public idTable visitRefExpr(RefExpr expr, idTable idTab){
    idTab = expr.ref.visit(this, idTab);
    return idTab;
}

public idTable visitCallExpr(CallExpr expr, idTable idTab){
    idTab = expr.functionRef.visit(this, idTab);
    ExprList al = expr.argList;
    for (Expression e: al) {
        idTab = e.visit(this, idTab);
    }
    return idTab;
}

public idTable visitLiteralExpr(LiteralExpr expr, idTable idTab){
    expr.lit.visit(this, idTab);
    return idTab;
}

public idTable visitNewArrayExpr(NewArrayExpr expr, idTable idTab){
    expr.eltType.visit(this, idTab);
    expr.sizeExpr.visit(this, idTab);
    return idTab;
}

public idTable visitNewObjectExpr(NewObjectExpr expr, idTable  idTab){
    expr.classtype.visit(this, idTab);
    return idTab;
}


///////////////////////////////////////////////////////////////////////////////
//
// REFERENCES
//
///////////////////////////////////////////////////////////////////////////////

public idTable visitQualifiedRef(QualifiedRef qr, idTable idTab) {
	qr.id.visit(this, idTab);
	qr.ref.visit(this, idTab);
    return idTab;
}

public idTable visitIndexedRef(IndexedRef ir, idTable idTab) {
			
	ir.indexExpr.visit(this, idTab);
	
	   Declaration d = idTab.getIdentifer(ir.idRef.id.spelling);
		
	   	if(d==null){
	   			identificationError(" Indexed identifier " +ir.idRef.id.spelling+" not found ");
	   	}
	   	else
	   		ir.idRef.id.setDecl(d);
	
	ir.idRef.visit(this, idTab);
	return idTab;
	
}

public idTable visitIdRef(IdRef ref, idTable idTab) {
	   Declaration d = idTab.getIdentifer(ref.id.spelling);
	
	   	if(d==null){
	   			identificationError("*** Refernce identifier " + ref.id.spelling +" not found ");
	   	}
	   	else
	   		ref.id.setDecl(d);
	   	
	   	
	   	
	idTab = ref.id.visit(this, idTab);
	return idTab;
}

public idTable visitThisRef(ThisRef ref, idTable idTab) {
	return idTab;
}


///////////////////////////////////////////////////////////////////////////////
//
// TERMINALS
//
///////////////////////////////////////////////////////////////////////////////
//Modify This 
public idTable visitIdentifier(Identifier id, idTable idTab){
 //   Declaration d = idTab.getIdentifer(id.spelling);
   
   // id.setDecl(d);
 //   System.out.println(id.spelling + " " + d.name + " " + d.type.typeKind);
    return idTab;
}

public idTable visitOperator(Operator op, idTable idTab){
    return idTab;
}

public idTable visitIntLiteral(IntLiteral num, idTable idTab){
    return idTab;
}

public idTable visitBooleanLiteral(BooleanLiteral bool, idTable idTab){
    return idTab;
}
}
