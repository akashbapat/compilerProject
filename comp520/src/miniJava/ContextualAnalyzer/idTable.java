package miniJava.ContextualAnalyzer;
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
import miniJava.AbstractSyntaxTrees.VarDecl;
import miniJava.SyntacticAnalyzer.SourcePosition;
import miniJava.SyntacticAnalyzer.Token;
import miniJava.SyntacticAnalyzer.TokenKind;


public class idTable {
	private ArrayList <HashMap<String,Declaration> > levelList;
	private ClassDecl currentClass; //added to support calling of static functions from static functions in same class without using class name 
	private MethodDecl printlnStringMd;
	public void setCurrentClass(ClassDecl cd){ //added to support calling of static functions from static functions in same class without using class name 
		currentClass =cd;
	}
	
	public ClassDecl getCurrentClass(){ //added to support calling of static functions from static functions in same class without using class name 
	
		if(currentClass==null)
			System.out.println("Current class is null, accessed before setting ");
		
		return 	currentClass ;
	}
	 
	
	public idTable( ){
		
		ClassDecl cdSys,cdString,cd_printStream;
		levelList = new ArrayList <HashMap<String,Declaration> > ();


		HashMap<String,Declaration>	level0 = new HashMap<String,Declaration>() ;

		FieldDeclList   sysFDList = new FieldDeclList();

		sysFDList.add(new FieldDecl(false, true, new ClassType(new Identifier(new Token( TokenKind.ID ,"_PrintStream")),new SourcePosition()), "out" , new SourcePosition()));

		//sysMethodList.add( MethodDecl( new MemberDecl(false,   true , new Type(TypeKind.CLASS, null), "_PrintStream", null), ParameterDeclList pl, StatementList sl, SourcePosition posn));

		cdSys =new ClassDecl("System", sysFDList, null , new SourcePosition()) ;
		cdSys.type = new ClassType(new Identifier(new Token(TokenKind.ID, "System")), new SourcePosition());
		level0.put("System", cdSys);

		cdString = new ClassDecl("String", null, null , new SourcePosition());
		cdString.type = new ClassType(new Identifier(new Token(TokenKind.STRING, "String")), new SourcePosition());
		cdString.type.typeKind = TypeKind.CLASS; //changed from unsupported to string to support string as basetype
		
		level0.put("String", cdString );
		


		ParameterDeclList pdlPrintStream = new ParameterDeclList();
		
		
		pdlPrintStream.add( new ParameterDecl( new BaseType(TypeKind.INT,new SourcePosition()), "n", new SourcePosition()));
		
		
		MethodDeclList mdPrintStream = new MethodDeclList();
		mdPrintStream.add( new MethodDecl( new FieldDecl(false, false, new BaseType(TypeKind.VOID,new SourcePosition()) , "println", new SourcePosition()) , pdlPrintStream, null, new SourcePosition()));
		
		cd_printStream =new ClassDecl("_PrintStream", null,   mdPrintStream, new SourcePosition()) ;
		cd_printStream.type= new ClassType(new Identifier(new Token(TokenKind.ID, "_PrintStream")), new SourcePosition());
		
		level0.put("_PrintStream",cd_printStream  );


		levelList.add(level0);

		
		
		ParameterDeclList pdlPrintStreamString = new ParameterDeclList();
		pdlPrintStreamString.add( new ParameterDecl( new ClassType(new Identifier(new Token(TokenKind.STRING,"String", new SourcePosition())),new SourcePosition()), "s", new SourcePosition()));		
		printlnStringMd = new MethodDecl( new FieldDecl(false, false, new BaseType(TypeKind.VOID,new SourcePosition()) , "printlnString", new SourcePosition()) , pdlPrintStreamString, null, new SourcePosition());
		
		
		
		
		
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


	public Declaration getIdentifier(String name , boolean isFuncStatic,boolean isCall ){ //names of fields and methods 

		Declaration d;
		MemberDecl md;
		for (int i=  levelList.size() -1; i >= idLevel.MEMBER_LEVEL.getValue() ;i-- ){

			d = levelList.get(i ).get( name) ;

			if(d != null  ){
				
			if(i == idLevel.MEMBER_LEVEL.getValue()){
				
				 
				  if(isFuncStatic){  //function is   static
					 if(isCall){  // call inside   static func 
						 if(d instanceof MemberDecl && ((MemberDecl) d).isStatic)
						 return d;
						 else 
							 return null;
						 
					 }
					 else{	 // field inside   static func 
						 if(d instanceof FieldDecl &&  ((FieldDecl) d).isStatic)
							 return d;
							 else 
								 return null;
					 }
					 
				 }
				 else{//function is   NOT static
					 
					 
						if (d instanceof VarDecl && name.equals("this"))
							return d;
					 
					 
						else if(isCall){   // call inside non static func 
					 		 if(d instanceof MemberDecl)
								 return d;
								 else 
									 return null;
					 }
					 else{// field inside non static func 
						 if(d instanceof FieldDecl)
							 return d;
							 else 
								 return null;
					 }
					 
					 
					 
					 
				 }
				
			}
			else{ // its not a member decl, is its either paramdecl or local decl
				return d;
			}
				

			/*	if(isFuncStatic && i == idLevel.MEMBER_LEVEL.getValue()){
					if((d instanceof FieldDecl ) || (d instanceof MethodDecl  && isCall)){
						md = (MemberDecl) d;
					 

					if(md.isStatic)
						return d;
					else 
						return null;
					
					}
					 
				}
				else
					return d  ; */
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

	/*public Declaration getStaticFunctionSameClass(String name) {
		
		
		Declaration d;

		for ( MethodDecl fd : currentClass.methodDeclList){ //check in member level for function
			 

			if( fd.name.equals(name))
				return fd;
		}


		return null;
		
		
		
	}
	*/
	
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


public MethodDecl getPrintlnDecl(){
	
	Declaration printStreamClass = levelList.get(0).get("_PrintStream") ;
	ClassDecl printStreamCd ;
	MethodDecl printlnDecl =null;
	if(printStreamClass instanceof ClassDecl){
		printStreamCd = (ClassDecl) printStreamClass;
		
		printlnDecl =	printStreamCd.methodDeclList.get(0); //hardcoded
	}
	else{
	System.out.print("***Fatal idtable error: _PrintStream is not a type of class");
	System.exit(-1);
	}
return	printlnDecl	;
}

public MethodDecl getPrintlnStringDecl(){
	
	 
return	printlnStringMd	;
}

}
