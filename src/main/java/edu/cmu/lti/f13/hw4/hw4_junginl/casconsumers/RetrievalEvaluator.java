package edu.cmu.lti.f13.hw4.hw4_junginl.casconsumers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.uima.cas.CAS;
import org.apache.uima.cas.CASException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.collection.CasConsumer_ImplBase;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.ResourceProcessException;
import org.apache.uima.util.ProcessTrace;
import org.uimafit.util.JCasUtil;

import edu.cmu.lti.f13.hw4.hw4_junginl.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_junginl.typesystems.Token;
import edu.cmu.lti.f13.hw4.hw4_junginl.utils.Utils;


public class RetrievalEvaluator extends CasConsumer_ImplBase {

	/** query id number **/
	public ArrayList<Integer> qIdList;

	/** query and text relevant values **/
	public ArrayList<Integer> relList;
	
	public ArrayList<String> textList;

		
	public void initialize() throws ResourceInitializationException {

		qIdList = new ArrayList<Integer>();

		relList = new ArrayList<Integer>();
		
		textList = new ArrayList<String>();

	}

	/**
	 * TODO :: 1. construct the global word dictionary 2. keep the word
	 * frequency for each sentence
	 */
	@Override
	public void processCas(CAS aCas) throws ResourceProcessException {

		JCas jcas;
		try {
			jcas =aCas.getJCas();
		} catch (CASException e) {
			throw new ResourceProcessException(e);
		}

		FSIterator it = jcas.getAnnotationIndex(Document.type).iterator();
	
		if (it.hasNext()) {
			Document doc = (Document) it.next();

			//Make sure that your previous annotators have populated this in CAS
			FSList fsTokenList = doc.getTokenList();
			ArrayList<Token> tokenList = Utils.fromFSListToCollection(fsTokenList, Token.class);

			qIdList.add(doc.getQueryID());
			relList.add(doc.getRelevanceValue());
			
			//Do something useful here
			textList.add(doc.getText());
		}

	}

	/**
	 * TODO 1. Compute Cosine Similarity and rank the retrieved sentences 2.
	 * Compute the MRR metric
	 */
	@Override
	public void collectionProcessComplete(ProcessTrace arg0)
			throws ResourceProcessException, IOException {

		super.collectionProcessComplete(arg0);

		// TODO :: compute the cosine similarity measure
		//Extract question sentences, tokenize, compute term-freq, put them into HashMap
		ArrayList<String> questionText = new ArrayList<String>();
    List<Map> questionV = new ArrayList<Map>();
    for (int i=0; i<relList.size(); i++) {
      if (relList.get(i)==99) {
        questionText.add(textList.get(i));
        System.out.println(questionText);
        Map<String,Integer> qVector = new HashMap<String, Integer>();
        List<String> tokensQ = new ArrayList<String>();
        for (String e: textList.get(i).split(" ")) {
          tokensQ.add(e);
        }
        Set<String> unique = new HashSet<String>(tokensQ);
        for (String key : unique) {
          qVector.put(key,Collections.frequency(tokensQ,key));
        }
        questionV.add(qVector);
      }
      else {
        continue;
      }
    }
		
		//Compute cosine similarity
    ArrayList<Double> scoreL = new ArrayList<Double>();
    int qIdnum = Collections.max(qIdList);
    int ind = 1;
    Map<String, Integer> queryVector = new HashMap<String, Integer>();
    for (int id=1; id<qIdnum+1; id++) {
      for (int i=0; i<textList.size(); i++) {
        if (qIdList.get(i)==id) {
          //HashMap of the question
          queryVector = questionV.get(id-1);
          
          //Creating HashMaps for answer sentences
          //Tokenize, Term-freq
          List<String> tokensA = new ArrayList<String>();
          Map<String, Integer> mapA = new HashMap<String, Integer>();
          for (String e: textList.get(i).split(" ")) {
            tokensA.add(e);
          }
          Set<String> unique = new HashSet<String>(tokensA);
          for (String key : unique) {
            mapA.put(key,Collections.frequency(tokensA,key));
          }
          //put into HashMap=docVector
          Map<String,Integer> docVector = mapA;
        
          //compute the cosine similarity
          double score = computeCosineSimilarity(queryVector, docVector);
          scoreL.add(score);
        }
      }
    }
		
		// TODO :: compute the rank of retrieved sentences
		
		
		
		// TODO :: compute the metric:: mean reciprocal rank
		double metric_mrr = compute_mrr();
		System.out.println(" (MRR) Mean Reciprocal Rank ::" + metric_mrr);
	}

	/**
	 * 
	 * @return cosine_similarity
	 */
	
	private double computeCosineSimilarity(Map<String, Integer> queryVector,
			Map<String, Integer> docVector) {
		double cosine_similarity=0.0;

		// TODO :: compute cosine similarity between two sentences
		//Compute the dot product
		double dotproduct = 0.0;
		dotproduct = dot(queryVector, docVector);
		
		//Compute the cosine similarity
		cosine_similarity = dotproduct / (Math.sqrt(dot(queryVector,queryVector)))*(Math.sqrt(dot(docVector,docVector))); 
		        
		return cosine_similarity;
	}

	/**
	 * @return dotproduct
	 */
	public static double dot(Map<String, Integer> v1, Map<String, Integer> v2) {
    double dotproduct = 0.0;
    Set<String> v1set = v1.keySet();
    Set<String> v2set = v2.keySet();
    Set<String> dict = new HashSet<String>(v1set);
    dict.addAll(v2set);
   
    // Compute the dot product
    Iterator<String> iter = v1set.iterator();
    String e;
    double v1Freq = 0.0;
    double v2Freq = 0.0;
    
    while (iter.hasNext()) {
      e = iter.next();
      v1Freq = v1.get(e);
      if (v2.containsKey(e)) {
        v2Freq = v2.get(e);
      }
      else {
        v2Freq = 0.0;
      }
      dotproduct = dotproduct + v1Freq * v2Freq;
    }
    return dotproduct;
  }
	
	/**
	 * 
	 * @return mrr
	 */
	private double compute_mrr() {
		double metric_mrr=0.0;

		// TODO :: compute Mean Reciprocal Rank (MRR) of the text collection
		
		return metric_mrr;
	}

}
