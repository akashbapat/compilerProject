package miniJava.ContextAnalyzer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import miniJava.AbstractSyntaxTrees.BaseType;
import miniJava.AbstractSyntaxTrees.ClassDecl;
import miniJava.AbstractSyntaxTrees.ClassType;
import miniJava.AbstractSyntaxTrees.Declaration;
import miniJava.AbstractSyntaxTrees.FieldDecl;
import miniJava.AbstractSyntaxTrees.FieldDeclList;
import miniJava.AbstractSyntaxTrees.Identifier;
import miniJava.AbstractSyntaxTrees.MemberDecl;
import miniJava.AbstractSyntaxTrees.MethodDecl;
import miniJava.AbstractSyntaxTrees.MethodDeclList;
import miniJava.AbstractSyntaxTrees.ParameterDecl;
import miniJava.AbstractSyntaxTrees.ParameterDeclList;


import miniJava.AbstractSyntaxTrees.TypeKind;

import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.TokenKind;


public class idTable {
	private ArrayList <HashMap<String,Declaration> > levelList;


	public idTable( ){
		
		ClassDecl cdSys,cdString,cd_printStream;
		levelList = new ArrayList <HashMap<String,Declaration> > ();


		HashMap<String,Declaration>	level0 = new HashMap<String,Declaration>() ;

		FieldDeclList   sysFDList = new FieldDeclList();

		sysFDList.add(new FieldDecl(false, true, new ClassType(new Identifier(new Token( TokenKind.ID ,"_PrintStream")),null), "out" , null));

		//sysMethodList.add( MethodDecl( new MemberDecl(false,   true , new Type(TypeKind.CLASS, null), "_PrintStream", null), ParameterDeclList pl, StatementList sl, SourcePosition posn));

		cdSys =new ClassDecl("System", sysFDList, null , null) ;
		cdSys.type = new ClassType(new Identifier(new Token(TokenKind.ID, "System")), null);
		level0.put("System", cdSys);

		cdString = new ClassDecl("String", null, null , null);
		cdString.type = new ClassType(new Identifier(new Token(TokenKind.ID, "String")), null);
		cdString.type.typeKind = TypeKind.UNSUPPORTED;
		
		level0.put("String", cdString );
		


		ParameterDeclList pdlPrintStream = new ParameterDeclList();

		pdlPrintStream.add( new ParameterDecl( new BaseType(TypeKind.INT,null), "n", null));
		MethodDeclList mdPrintStream = new MethodDeclList();
		mdPrintStream.add( new MethodDecl( new FieldDecl(false, false, new BaseType(TypeKind.VOID,null) , "println", null) , pdlPrintStream, null, null));

		
		cd_printStream =new ClassDecl("_printStream", null,   mdPrintStream, null) ;
		cd_printStream.type= new ClassType(new Identifier(new Token(TokenKind.ID, "_printStream")), null);
		
		level0.put("_printStream",cd_printStream  );


		levelList.add(level0);

	}

	public void openScope(){

		HashMap<String,Declaration>	newlevel = new HashMap<String,Declaration>() ;

		levelList.add(newlevel);

	}


	public void closeScope(){

		int n = levelList.size()-1;

		if(n<=0)
			System.out.println("deleting level 0 or invalid levels");

		levelList.remove( n );

	}

	public int  addDecl(Declaration inDecl , idLevel startLevel  , idLevel stopLevel) {


		for (int i = startLevel.getValue(); i <= stopLevel.getValue(); i++){


			if( levelList.get(i ).containsKey(inDecl.name))
				return i;

		}

		levelList.get(levelList.size()-1 ).put(inDecl.name,inDecl);

		return -1;


	}

	public int  addDecl(Declaration inDecl , idLevel startLevel  ) {


		for (int i = startLevel.getValue(); i< levelList.size(); i++){


			if( levelList.get(i ).containsKey(inDecl.name))
				return i;

		}

		levelList.get(levelList.size()-1 ).put(inDecl.name,inDecl);

		return -1;


	}


	public Declaration getIdentifier(String name , boolean isFuncStatic ){ //names of fields and methods 

		Declaration d;
		MemberDecl md;
		for (int i=  levelList.size() -1; i >= idLevel.MEMBER_LEVEL.getValue() ;i-- ){

			d = levelList.get(i ).get( name) ;

			if(d != null  ){

				if(isFuncStatic && i == idLevel.MEMBER_LEVEL.getValue()){
					if(d instanceof MemberDecl){
						md = (MemberDecl) d;
					 

					if(md.isStatic)
						return d;
					else 
						return null;
					
					}
					 
				}
				else
					return d  ;
			}

		}

		return null;

	}


	public Declaration getStaticIdentifier(String name  ){ //names of  classes for static access

		Declaration d;




		for (int i= idLevel.CLASS_LEVEL.getValue(); i >= idLevel.PREDEF_LEVEL.getValue() ;i-- ){

			d = levelList.get(i ).get( name) ;

			if(d != null)
				return d  ;

		}

		return null;

	}





	public Declaration getClass(String name ){

		Declaration d;

		for (int i=idLevel.CLASS_LEVEL.getValue();i >= idLevel.PREDEF_LEVEL.getValue();i--){
			d=levelList.get(i).get(name) ;

			if( d!=null)
				return d;
		}


		return null;
	}

	public  void printLevel(int n){



		if(n>levelList.size()-1){
			System.out.println("error") ;
		}
		else{
			Set<String> keys = levelList.get(n).keySet();



			System.out.print( n);
			System.out.println (keys);



		}

	}

	public void printRestLevel(   ){



		for (int i=5 ;i<levelList.size();i++){




			Set<String> keys = levelList.get(i).keySet();



			System.out.print(i);
			System.out.println (keys);



		}

	}



}
