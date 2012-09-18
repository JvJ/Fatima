package FAtiMA.NormativeComponent;

public class NormativeGoal {
	
	private String _name;
	
	public NormativeGoal(String name){
		_name = name;
	}
	
	public String getName(){
		return _name;
	}
	
	public String toString(){
		return this.getName();
	}

}
