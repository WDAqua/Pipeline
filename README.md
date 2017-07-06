
Dear Viewers, the Project now (as of June 2017) has evolved a lot since this publication in ESWC 2016. Our Current working Git link is: https://github.com/WDAqua/Qanary.
Also, the permanent email of the authors of this paper are: Kuldeep Singh- Kskuldeepvit@gmail.com, Andreas Both: andreas.both.de@gmail.com. For any question reach out to us. 

For understanding the implementation point of view of ESWC research article, please read the following:
# Pipeline

To the the Project Pipeline, here is the detailed description:

Qanary-An Extensible Vocabulary for Open Question Answering Systems contains three component. In our Project Pipeline, these three components are DBpediaSpotlight, Patty Relation Identifier, and SINA.

To run the Project, please follow following Steps.
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


Please note that to run the first component, we have used an example Turtle file. This is because DBpedia Spotlight service was down and still face some problems. As our main goal in the project is to show the interoperability using QANARY vocabulary instead the actual working of the Question Answering System, therefore we have taken an example to display output of DBpedia Spotlight and then bind it using QANARY vocabulary to push into the Triplestore.

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

Step 8 will display the SPARQL query generated out of Question string fed into first component (i.e. DBpediaSpotlight) on your terminal. This step display the final output and evaulate our claim that interoperability using QANARY vocabulary can be possible. We have taken three different components, bind it to our vocabulary to demostrate the examplary working or a message driven approach of QA system.




