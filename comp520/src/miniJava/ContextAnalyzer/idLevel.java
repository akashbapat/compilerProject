package miniJava.ContextAnalyzer;

public enum idLevel {

	PREDEF_LEVEL(0), CLASS_LEVEL(1),MEMBER_LEVEL(2), PARAM_LEVEL(3), LOCAL_LEVEL(4) ;
	
	private int value;
	
	
	private idLevel(int val){
	this.value =val;
	}
	
	public int getValue(){
		return this.value;
	}
	
	
};
