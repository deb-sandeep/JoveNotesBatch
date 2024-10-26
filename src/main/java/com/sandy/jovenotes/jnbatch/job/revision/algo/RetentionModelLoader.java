package com.sandy.jovenotes.jnbatch.job.revision.algo;

import com.google.gson.*;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class RetentionModelLoader {
    
    private static final Logger log = Logger.getLogger( RetentionModelLoader.class ) ;
    
    private static final String[] CARD_TYPES = {
        "fib",
        "question_answer",
        "true_false",
        "matching",
        "multi_choice"
    } ;

    Map<String, RetentionModel> loadRetentionModels()
        throws Exception {
        
        Map<String, RetentionModel> models = new HashMap<>() ;
        JsonObject root = getConfigJson() ;
        
        for( String cardType : CARD_TYPES ) {
            JsonObject cardRoot = root.getAsJsonObject( cardType ) ;
            if( cardRoot != null ) {
                RetentionModel model = parseRetentionModel( cardRoot, cardType ) ;
                models.put( cardType, model ) ;
            }
        }
        return models ;
    }
    
    private JsonObject getConfigJson() throws Exception {
        
        URL cfgUrl = this.getClass().getResource( "/retention-model.json" ) ;
        byte[] rawContents = IOUtils.toByteArray( Objects.requireNonNull( cfgUrl ) ) ;
        
        JsonParser parser = new JsonParser() ;
        return parser.parse( new String( rawContents ) ).getAsJsonObject();
    }
    
    private RetentionModel parseRetentionModel( JsonObject root, String cardType ) {
        
        RetentionModel model = new RetentionModel( cardType ) ;
        
        JsonArray coeffArray = root.getAsJsonArray( "coefficients" ) ;
        
        model.coeffSubjectNum      = coeffArray.get( 0 ).getAsFloat() ;
        model.coeffDifficultyLevel = coeffArray.get( 1 ).getAsFloat() ;
        model.coeffTimeSpent       = coeffArray.get( 2 ).getAsFloat() ;
        model.coeffAttemptNum      = coeffArray.get( 3 ).getAsFloat() ;
        model.coeffGapDuration     = coeffArray.get( 4 ).getAsFloat() ;
        model.coeffLearningEff     = coeffArray.get( 5 ).getAsFloat() ;
        
        model.intercept = root.getAsJsonPrimitive( "intercept" ).getAsFloat() ;
        model.threshold = root.getAsJsonPrimitive( "threshold" ).getAsFloat() ;
        
        return model ;
    }
    
    public static void main( String[] args ) throws Exception {
        RetentionModelLoader loader = new RetentionModelLoader() ;
        loader.loadRetentionModels() ;
    }
}
