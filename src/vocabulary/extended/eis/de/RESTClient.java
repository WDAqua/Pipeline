package vocabulary.extended.eis.de;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.impl.client.HttpClientBuilder;

public class RESTClient {

	public String getResults(String urlOfWebService) {
		String result = "";
		try {

			// create HTTP Client
			HttpClient httpClient = HttpClientBuilder.create().build();

			// Create new getRequest with below mentioned URL
			HttpGet getRequest = new HttpGet(urlOfWebService);

			// Add additional header to getRequest which accepts application/xml
			// data
			getRequest.addHeader("accept", "application/xml");

			// Execute your request and catch response
			HttpResponse response = httpClient.execute(getRequest);

			// Check for HTTP response code: 200 = success
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			}

			// Get-Capture Complete application/xml body response
			BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
			String output="";
			

			// Simply iterate through XML response and show on console.
			while ((output = br.readLine()) != null) {
				result+=output+"\n";
				//System.out.println(output);
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();

		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	/*public static void main(String[] args) {

		// Example of how to call 
		RESTClient rstclnt = new RESTClient();
		String address="localhost:8888";// assign the url for the service to the address string
		System.out.println("The data received from service is: "+rstclnt.getResults(address));
	}*/

}
