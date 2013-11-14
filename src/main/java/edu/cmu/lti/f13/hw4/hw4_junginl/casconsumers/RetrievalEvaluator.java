package edu.cmu.lti.f13.hw4.hw4_junginl.casconsumers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
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
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Word;
//import edu.stanford.nlp.objectbank.TokenizerFactory;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.PTBTokenizer.PTBTokenizerFactory;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.util.CoreMap;


public class RetrievalEvaluator extends CasConsumer_ImplBase {

	/** query id number **/
	public ArrayList<Integer> qIdList;

	/** query and text relevant values **/
	public ArrayList<Integer> relList;
	
	public ArrayList<String> textList;

	public ArrayList<Integer> rankL;
	
	public ArrayList<Double> rankScoreL;
	
	public ArrayList<Integer> sentenceL;
		
	public void initialize() throws ResourceInitializationException {

		qIdList = new ArrayList<Integer>();

		relList = new ArrayList<Integer>();
		
		textList = new ArrayList<String>();
		
		rankL = new ArrayList<Integer>();
		
		rankScoreL = new ArrayList<Double>();
		
		sentenceL = new ArrayList<Integer>();
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
		
//		
//		String[] textAll = new String[textList.size()];
//		for (int i=1; i<textList.size(); i++) {
//		  textAll[i] = textList.get(i);
//		}
//	  //import Stanford CoreNLP tool for Lemma
//		ArrayList<ArrayList<String>> tokenAll = new ArrayList<ArrayList<String>>();
//    ArrayList<ArrayList<String>> lemmaAll = new ArrayList<ArrayList<String>>();
//    for (int i = 0; i < textAll.length; i++) {
//      tokenAll.add(new ArrayList<String>());
//      lemmaAll.add(new ArrayList<String>());
//      String text = textAll[i];
//      Properties props = new Properties();
//      props.put("annotators", "tokenize, ssplit, pos, lemma");
//      StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
//      // create an empty Annotation just with the given text
//      Annotation document1 = new Annotation(text);
//      // run all Annotators on this text
//      pipeline.annotate(document1);
//      List<CoreMap> sentences = document1.get(SentencesAnnotation.class);
//      for (CoreMap sentence : sentences) {
//        // traversing the words in the current sentence
//        // a CoreLabel is a CoreMap with additional token-specific methods
//        for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
//          // this is the text of the token
//          String word = token.get(TextAnnotation.class);
//          // this is the POS tag of the token
//          String lemma = token.get(LemmaAnnotation.class);
//          // Output the result
//          String tokenn = token.toString();
//          tokenAll.get(i).add(tokenn);
//          lemmaAll.get(i).add(lemma);
//        }
//      }
//    }
//		System.out.println(lemmaAll);
		

		// TODO :: compute the cosine similarity measure
		//Extract question sentences, tokenize, compute term-freq, put them into HashMap
		//questionText = list of question sentences (text-only) 
		ArrayList<String> questionText = new ArrayList<String>();
		//questionV = list of maps(qVector) for all question sentences
    List<Map> questionV = new ArrayList<Map>();
    for (int i=0; i<relList.size(); i++) {
      if (relList.get(i)==99) {
        questionText.add(textList.get(i));
        //qVector = for each question sentence, map of token and freq
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
          double scoretemp = computeCosineSimilarity(queryVector, queryVector);
//          System.out.println(queryVector.keySet());
//          System.out.println("scoretemp=" + scoretemp);
          scoreL.add(score);
        }
      }
    }
		
		// TODO :: compute the rank of retrieved sentences
    for (int i = 0; i < qIdList.size(); i++) {
      if (relList.get(i) != 1)
        continue;
      int qId = qIdList.get(i);
      rankScoreL.add(scoreL.get(i));
      int rank = 1;
      int sent = 0;
      for (int j = 0; j < qIdList.size(); j++) {
        if (qIdList.get(j) != qIdList.get(i))
          continue;
        if (relList.get(j) == 99)
          continue;
        sent++;
        if (relList.get(j) == 1) {
          sentenceL.add(sent);
          continue;
        }
        if (scoreL.get(j) > scoreL.get(i))
          rank++;
      }
      rankL.add(rank);
    }
    
    for (int i = 0; i < rankScoreL.size(); i++) {
      int qid = i + 1;
      System.out.println("Score:" + rankScoreL.get(i) + "\t" + "rank=" + rankL.get(i) + "\t" + "rel=1" + "\t" + "qid="
              + qid + "\t" + "sent" + sentenceL.get(i));
    }
		
		// TODO :: compute the metric:: mean reciprocal rank
		double mrr = compute_mrr();
		System.out.println(" (MRR) Mean Reciprocal Rank ::" + mrr);
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
		double denom = 0.0;
		denom = (Math.sqrt(dot(queryVector,queryVector)))*(Math.sqrt(dot(docVector,docVector)));
		cosine_similarity = dotproduct / denom;
//		cosine_similarity = dotproduct / (Math.sqrt(dot(queryVector,queryVector)))*(Math.sqrt(dot(docVector,docVector))); 
		        
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
	 * @param queryVector
	 * @param docVector
	 * @return jaccard
	 */
	private double computeJaccard(Map<String, Integer> queryVector,
	        Map<String, Integer> docVector) {
	  //Compute jaccard coefficient between two sentences
	  double jaccard=0.0;
	  double jaccQ=0.0;
	  double jaccA=0.0;
	  double jaccInters=0.0;
	  Map<String, Integer> intersect = new HashMap<String, Integer>(queryVector);
	  intersect.keySet().retainAll(docVector.keySet());
	  
	  jaccQ = queryVector.size() - intersect.size();
	  jaccA = docVector.size() - intersect.size();
	  jaccInters = intersect.size();
	  jaccard = jaccInters / (jaccQ + jaccA + jaccInters);
	  return jaccard;
	}
	
	/**
	 * @param queryVector
	 * @param docVector
	 * @return dice
	 */
	private double computeDice(Map<String, Integer> queryVector,
	        Map<String, Integer> docVector) {
	  //Compute dice coefficient between two sentences
	  double dice=0.0;
    double diceQ=0.0;
    double diceA=0.0;
    double diceInters=0.0;
    Map<String, Integer> intersect = new HashMap<String, Integer>(queryVector);
    intersect.keySet().retainAll(docVector.keySet());
    
    diceQ = queryVector.size() - intersect.size();
    diceA = docVector.size() - intersect.size();
    diceInters = intersect.size();
    dice = 2*diceInters / (diceQ + diceA + 2*diceInters);
	  return dice;
	}
	
	/**
	 * 
	 * @return mrr
	 */
	private double compute_mrr() {
		// TODO :: compute Mean Reciprocal Rank (MRR) of the text collection
		double mrr = 0.0;
//		System.out.println(rankL.size());
//		System.out.println(rankL);
		for (int i=0; i<rankL.size(); i++) {
      mrr += 1.0 / rankL.get(i);
    }
    mrr = mrr/ rankL.size();
		return mrr;
	}

}
