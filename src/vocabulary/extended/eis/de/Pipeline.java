package vocabulary.extended.eis.de;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class Pipeline {

	private static String endpoint="http://admin:admin@localhost:5820/question/query";
	
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
	
	
	
	public void loadTripleStore(String content)
	{
		UpdateRequest request = UpdateFactory.create(content) ;
		UpdateProcessor proc = UpdateExecutionFactory.createRemote(request, endpoint);
	    proc.execute() ;
	}
	
	public void writeFile(String fileName,String content)
	{
		try{
		BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
		bw.write(content);
		bw.close();
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	
	
	//Object getOutput(QAComponent comp, Object input){
		//return comp.process(input);
	//}
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
		String path = "/Users/kulsingh/Documents/workspace/Pipeline/src/vocabulary/extended/eis/de/Question";// path to the file
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
	public void initTripleStore()
	{
				String q="CLEAR ALL";
				UpdateRequest request = UpdateFactory.create(q) ;
			    UpdateProcessor proc = UpdateExecutionFactory.createRemote(request, endpoint);
			    proc.execute() ;
			    
			    //Load the Open Annotation Ontology
			    q="LOAD <http://www.openannotation.org/spec/core/20130208/oa.owl>";
				request = UpdateFactory.create(q) ;
			    proc = UpdateExecutionFactory.createRemote(request, endpoint);
			    proc.execute() ;
				
				//Load our ontology
				q="LOAD <http://localhost:8080/QAOntology_raw.ttl>";
				request = UpdateFactory.create(q) ;
			    proc = UpdateExecutionFactory.createRemote(request, endpoint);
			    proc.execute() ;
			    
			    //Prepare the question, answer and dataset objects
			    q="PREFIX qa: <http://www.wdaqua.eu/qa#>"+
			      "INSERT DATA {<http://localhost:8080/Question> a qa:Question}";
			    request = UpdateFactory.create(q) ;
			    proc = UpdateExecutionFactory.createRemote(request, endpoint);
			    proc.execute();
			    
			    q="PREFIX qa: <http://www.wdaqua.eu/qa#>"+
		  	      "INSERT DATA {<http://localhost:8080/Answer> a qa:Answer}";
		  	    request = UpdateFactory.create(q) ;
		  	    proc = UpdateExecutionFactory.createRemote(request, endpoint);
		  	    proc.execute();
			  	
			  	  q="PREFIX qa: <http://www.wdaqua.eu/qa#>"+
		  		     "INSERT DATA {<http://localhost:8080/Dataset> a qa:Dataset}";
			    request = UpdateFactory.create(q) ;
			    proc = UpdateExecutionFactory.createRemote(request, endpoint);
			    proc.execute();
	}
	
	public static void main(String[] args) throws InterruptedException, IOException {
		
		//PREPARE THE TRIPLESOTRE
		
		//Set up a Web Server that exposes the QAOntology
		try {
            new WebServer();
        }
        catch( IOException ioe ) {
            System.err.println( "Couldn't start server:\n" + ioe );
        }
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		//Clear the database
		String questionstring = "In which city did John F. Kennedy die?";
		//PipelineTests ptest = new PipelineTests();
		//ptest.executeTest(questionstring);
		
		Pipeline pline = new Pipeline();
		pline.replaceQuestion(questionstring);
		pline.initTripleStore();
	    
		String questionuri = "PREFIX qa: <http://www.wdaqua.eu/qa#>";
		
		String sparqlQuery = "PREFIX qa:<http://www.wdaqua.eu/qa#>SELECT ?questionuri WHERE {?questionuri a qa:Question}";
		
		
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qExe = QueryExecutionFactory.sparqlService( endpoint, query );
		ResultSet result=qExe.execSelect();
		
		ArrayList<String> list = new ArrayList<String>();
		while (result.hasNext()){
			list.add(result.next().getResource("questionuri").toString());
			System.out.println(list.get(list.size()-1));
		}
		
		// this part consume python service from our REST client and print output.
		RESTClient rstclnt = new RESTClient();
		String add = "http://localhost:8099/";
		
		//String address="";// assign the url for the service to the address string
		
		for(String address: list)
		{
			String qstn = rstclnt.getResults(address);
			
			String qns[] = qstn.split(" ");
			String append = add+String.join("%20", qns);
			System.out.println("The Url is= "+append);
			
			
			
			URL url = new URL(append);
			URLConnection urlConnection = url.openConnection();
			HttpURLConnection connection = null;
			if (urlConnection instanceof HttpURLConnection) {
				connection = (HttpURLConnection) urlConnection;
			} else {
				System.out.println("Please enter an HTTP URL.");
				System.exit(0);
			}
			BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			String urlString = "";
			String current;
			
			while ((current = in.readLine()) != null) {
				urlString += current;
				System.out.println(current);
			}
			pline.writeFile("/Users/kulsingh/Documents/workspace/Pipeline/src/vocabulary/extended/eis/de/DBpediaOutput.ttl", urlString);
			pline.loadTripleStore("LOAD </Users/kulsingh/Documents/workspace/Pipeline/src/vocabulary/extended/eis/de/DBpediaOutput.ttl> INTO GRAPH <temp>");
			System.out.println("The aNS IS: "+urlString);
			
			//System.out.println("The data received from service is: "+rstclnt.getResults(add+qstn));
		}
//		//Pipeline p = new Pipeline();
//		//String out= p.queryAnswer("ask question ....");
//		System.out.println(new Pipeline().queryAnswer("ask question ......"));
	}

}
