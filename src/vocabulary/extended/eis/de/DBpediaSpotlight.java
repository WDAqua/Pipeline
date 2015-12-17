package vocabulary.extended.eis.de;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class DBpediaSpotlight implements QAComponent {

	public DBpediaSpotlight() {
		// TODO Auto-generated constructor stub
	}

	public String getQuestion()
	{
		String path="/Users/kulsingh/Documents/workspace/Pipeline/src/vocabulary/extended/eis/de/Question";// path to the file
		String question = "";
		
		try{
			
			BufferedReader bReader = new BufferedReader(new FileReader(path));
			question = bReader.readLine();
			
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return question;
	}
	public static void replaceQuestion(String question)
	{
		String path = "src/vocabulary/extended/eis/de/Question";// path to the file
		try{
			
			BufferedWriter bWriter = new BufferedWriter(new FileWriter(path,false));
			bWriter.write(question);
			bWriter.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
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
