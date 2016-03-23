package miniJava.SyntacticAnalyzer;

public class SourcePosition {

	 public int posn;
	 public int endPosn;
	 public int linePosn;
	 public int line;
	 public SourcePosition(int posn,int endPosn,int linePosn, int line){
		 this.posn = posn;
		 this.endPosn = endPosn;
		 this.linePosn = linePosn;
		 this.line = line;
	 }
}
