package vocabulary.extended.eis.de;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;
import fi.iki.elonen.NanoHTTPD.Response.Status;

public class WebServer extends NanoHTTPD {
	
	public WebServer() throws IOException {
        super(8080);
        start();
        System.out.println( "\nRunning! Point your browers to http://localhost:8080/ \n" );
    }

    @Override
    public Response serve(String uri, Method method,
        Map<String, String> header, Map<String, String> parameters,
        Map<String, String> files) {
    	
    	//System.out.println(uri);
	    if (uri.equals("/QAOntology_raw.ttl")){
	    	String answer = "";
		    FileInputStream fis = null;
		    try {
		        fis = new FileInputStream("/Users/kulsingh/Documents/workspace/Pipe/src/vocabulary/extended/eis/de/QAOntology_raw.ttl");
		    } catch (FileNotFoundException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }
		    return new NanoHTTPD.Response(Status.OK, "text/turtle", fis);
	    }
	    
	    if (uri.equals("/DBpediaOutput.ttl")){
	    	String answer = "";
		    FileInputStream fis = null;
		    try {
		        fis = new FileInputStream("/Users/kulsingh/Documents/workspace/Pipe/src/vocabulary/extended/eis/de/DBpediaOutput.ttl");
		    } catch (FileNotFoundException e) {
		        // TODO Auto-generated catch block
		    	System.out.println(uri+" caught in Exeption");
		    	
		        e.printStackTrace();
		    }
		    return new NanoHTTPD.Response(Status.OK, "text/turtle", fis);
	    }
	    
	    if (uri.equals("/Question")){
	    	String answer = "";
		    FileInputStream fis = null;
		    try {
		        fis = new FileInputStream("/Users/kulsingh/Documents/workspace/Pipe/src/vocabulary/extended/eis/de/Question");
		    } catch (FileNotFoundException e) {
		        // TODO Auto-generated catch block
		        e.printStackTrace();
		    }
		    return new NanoHTTPD.Response(Status.OK, "text/plain", fis);
	    }
	    String msg = "<html><body><h1>Hello server</h1>\n";
        return new NanoHTTPD.Response( msg + "</body></html>\n" );
  }
}
