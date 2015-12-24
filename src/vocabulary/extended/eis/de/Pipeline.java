package vocabulary.extended.eis.de;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.update.UpdateExecutionFactory;
import com.hp.hpl.jena.update.UpdateFactory;
import com.hp.hpl.jena.update.UpdateProcessor;
import com.hp.hpl.jena.update.UpdateRequest;
import org.apache.lucene.queryparser.classic.ParseException;

import Main.main; //This is the SINA import
import model.*;

import patty_wrapper.Indexer;

public class Pipeline {

	//Endpoint of Stardog
	private static String endpointStardog="http://admin:admin@localhost:5820/question/query";
	
	
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
	public void loadTripleStore(String sparqlQuery, String endpoint){
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
	public void initTripleStore(String endpoint, String namedGraph)
	{
				//Clear all tables of the database
				String q="CLEAR ALL";
				loadTripleStore(q, endpoint);
				
			    //Load the Open Annotation Ontology
			    q="LOAD <http://www.openannotation.org/spec/core/20130208/oa.owl> INTO GRAPH "+ namedGraph;
			    loadTripleStore(q, endpoint);
				
				//Load our ontology
				q="LOAD <http://localhost:8080/QAOntology_raw.ttl> INTO GRAPH"+ namedGraph;
				loadTripleStore(q, endpoint);
				
			    //Prepare the question, answer and dataset objects
			    q="PREFIX qa: <http://www.wdaqua.eu/qa#>"+
			      "INSERT DATA {GRAPH "+namedGraph+"{ <http://localhost:8080/Question> a qa:Question}}";
			    loadTripleStore(q, endpoint);
			   
			    q="PREFIX qa: <http://www.wdaqua.eu/qa#>"+
		  	      "INSERT DATA {GRAPH "+namedGraph+"{<http://localhost:8080/Answer> a qa:Answer}}";
			    loadTripleStore(q, endpoint);
		  	    
			  	q="PREFIX qa: <http://www.wdaqua.eu/qa#>"+
		  		  "INSERT DATA {GRAPH "+namedGraph+"{<http://localhost:8080/Dataset> a qa:Dataset}}";
			  	loadTripleStore(q, endpoint);
			  	
			  	//Make the first two annotations
			  	q="PREFIX oa: <http://www.w3.org/ns/openannotation/core/> "
			  	 +"PREFIX qa: <http://www.wdaqua.eu/qa#> "
			  	 +"INSERT DATA { "
			  	 +"GRAPH "+namedGraph+"{ "
			  	 +"<anno1> a  oa:AnnotationOfQuestion; "
				 +"   oa:hasTarget <URIQuestion> ;"
				 +"   oa:hasBody   <URIAnswer>   ."
				 +"<anno2> a  oa:AnnotationOfQuestion;"
				 +"   oa:hasTarget <URIQuestion> ;"
				 +"   oa:hasBody   <URIDataset> "
				 +"}}";  
				loadTripleStore(q, endpoint);
			   
	}
	
	public static void main(String[] args) throws InterruptedException, IOException, ParseException {
		
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
		pline.initTripleStore(endpointStardog, "<http://wdaqua.eu/namedGraph>");
		//The first component annotates the question with NE
	    pline.firstComponent(endpointStardog, "<http://wdaqua.eu/namedGraph>");
	    //The second component annotates the question with relations
		pline.secondComponent(endpointStardog,"<http://wdaqua.eu/namedGraph>");
		//The third component retrives the named entities and tris to build a SPARQL query
		pline.thirdComponent(endpointStardog,"<http://wdaqua.eu/namedGraph>");
	}
	
	
	public void firstComponent(String endpoint, String namedGraph) throws UnsupportedEncodingException{
		Pipeline pline = new Pipeline();
		//Execute a SPARQL query to retrieve the URI where the question is exposed
		String sparqlQuery = "PREFIX qa:<http://www.wdaqua.eu/qa#> SELECT ?questionuri FROM "+namedGraph+" WHERE {?questionuri a qa:Question}";
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
		//System.out.println(qstn);
		
		//Write into an empty file the result of the DBpedia wrapper to expose the result as a URI
		pline.writeFile("/Users/kulsingh/Documents/workspace/Pipe/src/vocabulary/extended/eis/de/DBpediaOutput.ttl", qstn);
		
		//The exposed result is loaded into a temporary graph
		pline.loadTripleStore("LOAD <http://localhost:8080/DBpediaOutput.ttl> INTO GRAPH <http://www.wdaqua.eu/qa#tmp>", endpoint);
		
		//The binding is applied
		sparqlQuery="prefix itsrdf: <http://www.w3.org/2005/11/its/rdf#> "
			 +"prefix nif: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> "
			 +"prefix qa: <http://www.wdaqua.eu/qa#> "
			 +"prefix oa: <http://www.w3.org/ns/openannotation/core/> "
			 +""
			 +"INSERT { "
			 +   "GRAPH "+namedGraph+" { "
			 +"  ?s a oa:TextPositionSelector . "
			 +"  ?s oa:start ?begin . "
			 +"  ?s oa:end ?end . "
			 +" "
			 +"  ?a a qa:AnnotationOfEntity . "
			 +"  ?a oa:hasBody ?NE . "
			 +"  ?a oa:hasTarget [ "
			 +"           a    oa:SpecificResource; "
             +"           oa:hasSource    <URIQuestion>; "
             +"           oa:hasSelector  ?s "
             +"  ] . "
             +"  ?a qa:score ?conf . "
             +"  ?a oa:annotatedBy 'DBpedia Spotlight wrapper' . "
             +"  ?a oa:annotatedAt ?time "
			 +"}} " 
			 +"WHERE { "
			 +"  SELECT ?a ?s ?NE ?begin ?end ?conf "
			 +"  WHERE { " 
			 +"     graph <http://www.wdaqua.eu/qa#tmp> { "
			 +"          ?s itsrdf:taIdentRef ?NE . " 
			 +"          ?s nif:beginIndex ?begin . "
			 +"          ?s nif:endIndex ?end . "
			 +"          OPTIONAL {?s nif:confidence ?conf} . " 
			 +"          BIND (IRI(CONCAT(str(?s),'#',str(RAND()))) AS ?a) . "
			 +"          BIND(now() as ?time) . "
			 +"      } " 
			 +"   }"
			 +"}";
		pline.loadTripleStore(sparqlQuery, endpoint);
		
		//All triples of the graph <http://www.wdaqua.eu/qa#tmp> are moved to the default graph 
		sparqlQuery="ADD <http://www.wdaqua.eu/qa#tmp> to "+namedGraph;
		pline.loadTripleStore(sparqlQuery, endpoint);
		
		//The temporary graph is dropped
		sparqlQuery="DROP GRAPH <http://www.wdaqua.eu/qa#tmp>";
		pline.loadTripleStore(sparqlQuery, endpoint);
		
	}
	
	public void secondComponent(String endpoint, String namedGraph) throws IOException, ParseException{
		//Execute a SPARQL query to retrieve the URI where the question is exposed
		String sparqlQuery = "PREFIX qa:<http://www.wdaqua.eu/qa#> "
							+"SELECT ?questionuri FROM "+namedGraph+" WHERE {?questionuri a qa:Question}";
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qExe = QueryExecutionFactory.sparqlService(endpoint, query );
		ResultSet result=qExe.execSelect();
		String uriQuestion =result.next().getResource("questionuri").toString();
		//Retrieve the question using an HTTP request 
		RESTClient rstclnt = new RESTClient();
		String question = rstclnt.getResults(uriQuestion);
		int length=question.length();
		question=question.replace("?", " ");
		
		sparqlQuery= "prefix itsrdf: <http://www.w3.org/2005/11/its/rdf> "
					+"prefix nif: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> "
					+"prefix qa: <http://www.wdaqua.eu/qa#> " 
					+"prefix oa: <http://www.w3.org/ns/openannotation/core/> "
					+""
					+"SELECT ?NE ?begin ?end "
					+"FROM "+ namedGraph + " "
					+"WHERE { "
					+"    ?a a qa:AnnotationOfEntity . "
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
		ArrayList<String> NE= new ArrayList<String>();
		ArrayList<Integer> begin= new ArrayList<Integer>();
		ArrayList<Integer> end= new ArrayList<Integer>();
		while (result.hasNext()){
			QuerySolution tmp = result.next();
			NE.add(tmp.getResource("NE").toString());
			begin.add(tmp.getLiteral("begin").getInt());
			end.add(tmp.getLiteral("end").getInt());
		}
		
		//The question will be taken and all parts that where not annotated before are dropped
		StringBuilder drop = new StringBuilder(question);
		//int k=0; //indicates the characters already dropped
		for (Integer i=0; i<begin.size(); i++){
			String replace="";
			for (Integer j=begin.get(i); j<end.get(i); j++){
				replace+=" ";
			}
			drop.replace(begin.get(i)-1, end.get(i), replace);
		}
		String search = drop.toString();
		//System.out.println(search);
		Indexer ind = new Indexer();
    	ind.index();
		Map<Integer,String> relations=ind.search("pattern", search);
    	
		Pipeline pline = new Pipeline();
		
		for (Map.Entry<Integer,String> entry : relations.entrySet())
    	{
			sparqlQuery="prefix itsrdf: <http://www.w3.org/2005/11/its/rdf#> "
					 +"prefix nif: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> "
					 +"prefix qa: <http://www.wdaqua.eu/qa#> "
					 +"prefix oa: <http://www.w3.org/ns/openannotation/core/> "
					 +"prefix dbo: <http://dbpedia.org/ontology/> "
					 +"prefix xsd: <http://www.w3.org/2001/XMLSchema#> "
					 +"INSERT { "
					 +"GRAPH "+namedGraph+" { "
					 +"  ?a a qa:AnnotationOfEntity . "
					 +"  ?a oa:hasBody dbo:"+entry.getValue()+" . "
					 +"  ?a oa:hasTarget [ "
					 +"           a    oa:SpecificResource; "
		             +"           oa:hasSource    <"+uriQuestion+">; "
		             +"           oa:hasSelector  [ "
		             +"                    a oa:TextPositionSelector ; "
		             +"                    oa:start \"0\"^^xsd:nonNegativeInteger ; "
		             +"                    oa:end  \""+length+"\"^^xsd:nonNegativeInteger  "
		             +"           ] "
		             +"  ] . "
		             +"  ?a qa:score "+entry.getKey()+" ; "
		             +"     oa:annotatedBy <http://wdaqua.example/Patty> ; "
		             +"	    oa:AnnotatedAt ?time  "
 					 +"}} "
					 +"WHERE { " 
					 +"BIND (IRI(str(RAND())) AS ?a) ."
					 +"BIND (now() as ?time) "
				     +"}"; 
			pline.loadTripleStore(sparqlQuery, endpoint);
		}
	}
	
	public void thirdComponent(String endpoint, String namedGraph){
		//SPARQL query to retrive the annotations of NE
		String sparqlQuery="prefix itsrdf: <http://www.w3.org/2005/11/its/rdf> "
			       +"prefix nif: <http://persistence.uni-leipzig.org/nlp2rdf/ontologies/nif-core#> " 
			       +"prefix qa: <http://www.wdaqua.eu/qa#> " 
			       +"prefix oa: <http://www.w3.org/ns/openannotation/core/> "
				   +"SELECT ?NE ?s "
				   +"FROM "+ namedGraph + " "
				   +"WHERE { "
				   +"    ?a a qa:AnnotationOfEntity . "
				   +"    ?a oa:hasBody ?NE . "
				   +"    ?a oa:hasTarget [ "
			       +"	       a             oa:SpecificResource; "
			       +"          oa:hasSource    ?r; "
			       +" ] . "
			       +"?a qa:score ?s "
			 	   +"}";
		Query query = QueryFactory.create(sparqlQuery);
		QueryExecution qExe = QueryExecutionFactory.sparqlService(endpoint, query );
		ResultSet result=qExe.execSelect();
		ArrayList<String> neList = new ArrayList<String>();
		ArrayList<String> sList = new ArrayList<String>();
		while (result.hasNext()){
			QuerySolution tmp = result.next();
			neList.add(tmp.getResource("NE").toString());
			//sList.add(tmp.getResource("s").toString());
			//System.out.println(tmp.getResource("NE").toString());
			//System.out.println(tmp.getLiteral("s").getInt());
		}
		
		List<String> sparqlQueries = Main.main.run(neList.get(1)+","+  neList.get(0));
		//Main.main.main(null);
		System.out.println(sparqlQueries);
		

	}
	
}