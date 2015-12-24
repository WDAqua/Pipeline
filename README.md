# Pipeline

To the the Project Pipeline, here is the detailed description:

Qanary-An Extensible Vocabulary for Open Question Answering Systems contains three component. In our Project Pipeline, these three components are DBpediaSpotlight, Patty Relation Identifier, and SINA.

To run the Poroject, please follow following Steps.
(Note that the Project Pipeline is Maven build)

{
Requirements:
a. Please install python 3.
b. Please install Stardog 4.0.2 or later from http://stardog.com/#download
After Installing Stardog,
c. Please run the stardog from stardog-4.0.2/bin folder using the command ./stardog-admin server start
d. create a local triplestore named question in Stardog following its instruction on the above mentioned website (http://stardog.com/)
}


(1) Download the Folder Pipeline from the Github repository.

(2) Go to the Folder /Pipeline/src/dbpediaspotlight2nif and run the following command 

python3 dbpediaspotlight_client_service.py --example "example5.ttl" --port 8099 

(4) Now go to the Pipeline/src/vocabulary/extended/eis/de/Pipeline.java file, and in line number 163 of the code, changed the path of the file and point it to your local.

(5) Go to the Pipeline/src/patty_wrapper/Indexer.java, and in line 74, change the path to your local.
(6) Go to Pipeline/src/vocabulary/extended/eis/de/WebServer.java, change path to your local in line 29,41, and 55.

Save the changes.

(6) Now to build the Mavan Pipeline project, follow following steps in your IDE -->>
(6.1) mvn clean
(6.2) mvn install
(6.3) mvn build

(7) Step 6 will create a Pipeline-0.0.1-SNAPSHOT.jar in the /Pipeline/target folder.

(8) Now run the .jar created in step 6 by running following command in your terminal
java -jar Pipeline-0.0.1-SNAPSHOT.jar

Step 8 will display the SPARQL query generated out of Question string fed into first component (i.e. DBpediaSpotlight) on your terminal




