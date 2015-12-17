package vocabulary.extended.eis.de;
import static org.junit.Assert.*;

import org.junit.Test;

import vocabulary.extended.eis.de.*;

public class PipelineTests {
	
	String questionstring = "In which city did John F. Kennedy die?";
	
	@Test
    public void executeTest(String questionstring){
        Pipeline myPipeline = new Pipeline();
        
        DBpediaSpotlight.replaceQuestion(questionstring);
        assertEquals(this.questionstring, myPipeline.spotlight.getQuestion()); // myPipeline.execute() needs to return a boolean
    }

}