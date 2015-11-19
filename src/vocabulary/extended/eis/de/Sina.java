package vocabulary.extended.eis.de;

public class Sina implements QAComponent {
	public Sina() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String process(Object inputString) {
		// TODO Auto-generated method stub
		System.out.println(inputString);
		if(inputString instanceof String)
			return simulateOutput((String)inputString);
		else 
			return null;
	}

	private String simulateOutput(String val){
		
			return "string out";
}



}
