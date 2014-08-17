package cz.cuni.mff.ufal.textan.textpro;

// *************** INTERFACE*******************

import cz.cuni.mff.ufal.textan.commons.utils.Pair;
import cz.cuni.mff.ufal.textan.textpro.data.Entity;

import java.util.List;
import java.util.Map;

/*
 * TextPro: Text Processing
 * Assign entity to potential objects in database (automatically)
 */

public interface ITextPro {
   
    
    //  Output a list of token after processing
    /*
    * Convert the document to a list of tokens, 
    * This method should inherit from other part
    */
    public List<String> TokenizeDoc(String document);
    
    
    // The main class of TextPro
    // The result of double ranking is a map from entity to the id value of 
    
    /*
    * Develop the heuristic function to assign a list of entity to a list of list of objects :)
    * Difficult to understand, huh?
    * Each entity is process to get a list of objects which somehow match with the entity
    * A list of entity will return a map from each entity to a list of objects along with their score
    */
    public Map<Entity, List<Pair<Long, Double>>> HeuristicRanking(String document, List<Entity> eList, int topK);
    
    /*
    * Basically the same purpose as HeuristicRanking, but use machine learning method
    */
    public Map<Entity, List<Pair<Long, Double>>> MachineLearning(String document, List<Entity> eList, int topK);
    
    // Learn the model
    /*
    * Machine Learning method, to learn the model
    * Current model is designed with weka
    */
    public void learn();    
    
}