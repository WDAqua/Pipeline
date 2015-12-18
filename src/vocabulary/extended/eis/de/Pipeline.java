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
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class Pipeline {

	//Endpoint of Stardog
	private static String endpoint="http://admin:admin@localhost:5820/question/query";
	
	
	DBpediaSpotlight spotlight = new DBpediaSpotlight();
	PattyRelationIdentifier relationmatching = new PattyRelationIdentifier();
	
	Sina sinatext = new Sina();
	
	public String queryAnswer(String query){
		String spotlightoutput = spotlight.process(query);
		String pattyoutput = relationmatching.process(spotlightoutput);
		String answer = sinatext.process(pattyoutput);
		return answer;
	}
	
	//Function to perform update requests to the endpoint
	public void loadTripleStore(String sparqlQuery){
		UpdateRequest request = UpdateFactory.create(sparqlQuery) ;
		UpdateProcessor proc = UpdateExecutionFactory.createRemote(request, endpoint);
	    proc.execute() ;
	}
	
	//Function that writes to a File some String 
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
	
	
	//Function that initializes the triplestore with the WADM + the QA ontology + some inital structures
	public void initTripleStore()
	{
				//Clear all tables of the database
				String q="CLEAR ALL";
				loadTripleStore(q);
				
			    //Load the Open Annotation Ontology
			    q="LOAD <http://www.openannotation.org/spec/core/20130208/oa.owl>";
			    loadTripleStore(q);
				
				//Load our ontology
				q="LOAD <http://localhost:8080/QAOntology_raw.ttl>";
				loadTripleStore(q);
				
			    //Prepare the question, answer and dataset objects
			    q="PREFIX qa: <http://www.wdaqua.eu/qa#>"+
			      "INSERT DATA {<http://localhost:8080/Question> a qa:Question}";
			    loadTripleStore(q);
			   
			    q="PREFIX qa: <http://www.wdaqua.eu/qa#>"+
		  	      "INSERT DATA {<http://localhost:8080/Answer> a qa:Answer}";
			    loadTripleStore(q);
		  	    
			  	q="PREFIX qa: <http://www.wdaqua.eu/qa#>"+
		  		  "INSERT DATA {<http://localhost:8080/Dataset> a qa:Dataset}";
			  	loadTripleStore(q);
			  	
			  	//Make the first two annotations
			  	q="PREFIX oa: <http://www.w3.org/ns/openannotation/core/> "
			  	 +"PREFIX qa: <http://www.wdaqua.eu/qa#> "
			  	 +"INSERT DATA { "
			  	 +"<anno1> a  oa:AnnotationOfQuestion; "
				 +"   oa:hasTarget <URIQuestion> ;"
				 +"   oa:hasBody   <URIAnswer>   ."
				 +"<anno2> a  oa:AnnotationOfQuestion;"
				 +"   oa:hasTarget <URIQuestion> ;"
				 +"   oa:hasBody   <URIDataset> "
				 +"}";  
				loadTripleStore(q);
			   
	}
	
	public static void main(String[] args) throws InterruptedException, IOException {
		org.apache.log4j.Logger.getRootLogger().setLevel(org.apache.log4j.Level.OFF);
		//Set up a Web Server that exposes the QAOntology
		try {
            new WebServer();
        }
        catch( IOException ioe ) {
            System.err.println( "Couldn't start server:\n" + ioe );
        }
		
		Pipeline pline = new Pipeline();
		
		//The triplestore is initialized with the WADM, the QA ontology and some initial structures
		pline.initTripleStore();
	    
		//Execute a SPARQL query to retrieve the URI where the question is exposed
		String sparqlQuery = "PREFIX qa:<http://www.wdaqua.eu/qa#> SELECT ?questionuri WHERE {?questionuri a qa:Question}";
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qExe = QueryExecutionFactory.sparqlService(endpoint, query );
		ResultSet result=qExe.execSelect();
		String uriQuestion =result.next().getResource("questionuri").toString();
		
		//Retrieve the question using an HTTP request 
		RESTClient rstclnt = new RESTClient();
		String question = rstclnt.getResults(uriQuestion);
		
		//Send the question to the DBpedia service
		String a="http://localhost:8099/"+URLEncoder.encode(question, "UTF-8");
		String qstn = rstclnt.getResults(a);
		System.out.println(qstn);
		
		//Write into an empty file the result of the DBpedia wrapper to expose the result as a URI
		pline.writeFile("src/vocabulary/extended/eis/de/DBpediaOutput.ttl", qstn);
		
		//The exposed result is loaded into a temporary graph
		pline.loadTripleStore("LOAD <http://localhost:8080/DBpediaOutput.ttl> INTO GRAPH <http://www.wdaqua.eu/qa#tmp>");
		
		//The binding is applied
		sparqlQuery="prefix itsrdf: <http://www.w3.org/2005/11/its/rdf#> "
			 +"prefix nif: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> "
			 +"prefix qa: <http://www.wdaqua.eu/qa#> "
			 +"prefix oa: <http://www.w3.org/ns/openannotation/core/> "
			 +""
			 +"INSERT { "
			 +"  ?s a oa:TextPositionSelector . "
			 +"  ?s oa:start ?begin . "
			 +"  ?s oa:end ?end . "
			 +" "
			 +"  ?a a qa:AnnotationOfNE . "
			 +"  ?a oa:hasBody ?NE . "
			 +"  ?a oa:hasTarget [ "
			 +"           a    oa:SpecificResource; "
             +"           oa:hasSource    <URIQuestion>; "
             +"           oa:hasSelector  ?s "
             +"  ] . "
             +"  ?a qa:score ?conf . "
			 +"} " 
			 +"WHERE { "
			 +"  SELECT ?a ?s ?NE ?begin ?end ?conf "
			 +"  WHERE { " 
			 +"     graph <http://www.wdaqua.eu/qa#tmp> { "
			 +"          ?s itsrdf:taIdentRef ?NE . " 
			 +"          ?s nif:beginIndex ?begin . "
			 +"          ?s nif:endIndex ?end . "
			 +"          OPTIONAL {?s nif:confidence ?conf} . " 
			 +"          BIND (IRI(CONCAT(str(?s),'_',str(RAND()))) AS ?a) . " 
			 +"      } " 
			 +"   }"
			 +"}";
		pline.loadTripleStore(sparqlQuery);
		
		//All triples of the graph <http://www.wdaqua.eu/qa#tmp> are moved to the default graph 
		sparqlQuery="ADD <http://www.wdaqua.eu/qa#tmp> to  DEFAULT";
		pline.loadTripleStore(sparqlQuery);
		//The temporary graph is dropped
		sparqlQuery="DROP GRAPH <http://www.wdaqua.eu/qa#tmp>";
		pline.loadTripleStore(sparqlQuery);
		pline.secondComponent();
	}
	
	
	public void secondComponent(){
		//Execute a SPARQL query to retrieve the URI where the question is exposed
		String sparqlQuery = "PREFIX qa:<http://www.wdaqua.eu/qa#> "
							+"SELECT ?questionuri WHERE {?questionuri a qa:Question}";
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qExe = QueryExecutionFactory.sparqlService(endpoint, query );
		ResultSet result=qExe.execSelect();
		String uriQuestion =result.next().getResource("questionuri").toString();
		//Retrieve the question using an HTTP request 
		RESTClient rstclnt = new RESTClient();
		String question = rstclnt.getResults(uriQuestion);
		
		sparqlQuery= "prefix itsrdf: <http://www.w3.org/2005/11/its/rdf> "
					+"prefix nif: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> "
					+"prefix qa: <http://www.wdaqua.eu/qa#> " 
					+"prefix oa: <http://www.w3.org/ns/openannotation/core/> "
					+""
					+"SELECT ?NE ?begin ?end WHERE { "
					+"    ?a a qa:AnnotationOfNE . "
					+"    ?a oa:hasBody ?NE . "
					+"    ?a oa:hasTarget [ "
					+"        a    oa:SpecificResource; "
					+"        oa:hasSource    ?r; "
					+"        oa:hasSelector  [ "
					+"              a      oa:TextPositionSelector; "
                    +"              oa:start        ?begin ; "
                    +"              oa:end          ?end "
                    +"        ] " 
                    +"    ] . "
					+"} ";
		query = QueryFactory.create(sparqlQuery);
		qExe = QueryExecutionFactory.sparqlService(endpoint, query );
		result=qExe.execSelect();
		while (result.hasNext()){
			QuerySolution tmp = result.next();
			System.out.println(tmp.getLiteral("NE").toString());
			System.out.println(tmp.getLiteral("begin").getInt());
			System.out.println(tmp.getLiteral("end").getInt());
			
		}
	}
	
}