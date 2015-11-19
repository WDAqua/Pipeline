package vocabulary.extended.eis.de;

public class Pipeline {

	DBpediaSpotlight spotlight = new DBpediaSpotlight();
	PattyRelationIdentifier relationmatching = new PattyRelationIdentifier();
	
	Sina sinatext = new Sina();

	public Pipeline() {
		
	}
	
	public void init(){
		
	}
	
	public String queryAnswer(String query){
		
		String spotlightoutput = spotlight.process(query);
		String pattyoutput = relationmatching.process(spotlightoutput);
		//String QGOutput = queryGeneration.process(QAOutput + ESOutput);
		//String QEOutput = queryExecution.process(QGOutput);
		String answer = sinatext.process(pattyoutput);
		
		//getOutput(spotlight, query);
		
		return answer;
		
	}
	
	//Object getOutput(QAComponent comp, Object input){
		//return comp.process(input);
	//}
	public static void main(String[] args) {
		//Pipeline p = new Pipeline();
		//String out= p.queryAnswer("ask question ....");
		System.out.println(new Pipeline().queryAnswer("ask question ......"));
	}

}
