package vocabulary.extended.eis.de;

import java.io.IOException;
import java.util.ArrayList;
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
	
	//Object getOutput(QAComponent comp, Object input){
		//return comp.process(input);
	//}
	public static void main(String[] args) throws InterruptedException {
		
		//PREPARE THE TRIPLESOTRE
		
		//Set up a Web Server that exposes the QAOntology
		try {
            new WebServer();
        }
        catch( IOException ioe ) {
            System.err.println( "Couldn't start server:\n" + ioe );
        }
		
		//Clear the database
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
	    
	 
	
		
		/*
		Query query = QueryFactory.create(q);
		QueryExecution qExe = QueryExecutionFactory.sparqlService( endpoint, query );
		
		ResultSet result=qExe.execSelect();
		ArrayList<String> list = new ArrayList<String>();
		while (result.hasNext()){
			list.add(result.next().getResource("s").toString());
			System.out.println(result.next().getResource("s").toString());
		}
		*/
	    
		//Pipeline p = new Pipeline();
		//String out= p.queryAnswer("ask question ....");
		System.out.println(new Pipeline().queryAnswer("ask question ......"));
	}

}
