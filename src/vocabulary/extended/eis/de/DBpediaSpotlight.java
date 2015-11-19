package vocabulary.extended.eis.de;

public class DBpediaSpotlight implements QAComponent {

	public DBpediaSpotlight() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public String process(Object inputString){
		// TODO Auto-generated method stub
		
		System.out.println(inputString);
		if(inputString instanceof String)
			return simulateOutput((String)inputString);
		else 
			return null;
	}

	private String simulateOutput(String val){
		
			return "Json Object";
}

}
