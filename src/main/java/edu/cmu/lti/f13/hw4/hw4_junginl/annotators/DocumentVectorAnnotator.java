package edu.cmu.lti.f13.hw4.hw4_junginl.annotators;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.uima.analysis_component.JCasAnnotator_ImplBase;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.FSIterator;
import org.apache.uima.jcas.JCas;
import org.apache.uima.jcas.cas.FSArray;
import org.apache.uima.jcas.cas.FSList;
import org.apache.uima.jcas.cas.IntegerArray;
import org.apache.uima.jcas.cas.StringArray;
import org.apache.uima.jcas.tcas.Annotation;

import edu.cmu.lti.f13.hw4.hw4_junginl.typesystems.Document;
import edu.cmu.lti.f13.hw4.hw4_junginl.typesystems.Token;

/**
 * Identify the word and word frequency in each sentence. 
 * @author junginlee
 *
 */

public class DocumentVectorAnnotator extends JCasAnnotator_ImplBase {

	@Override
	public void process(JCas jcas) throws AnalysisEngineProcessException {

		FSIterator<Annotation> iter = jcas.getAnnotationIndex().iterator();
		if (iter.isValid()) {
			iter.moveToNext();
			Document doc = (Document) iter.get();
			System.out.println(doc.getText());
			createTermFreqVector(jcas, doc);
		}

	}
	/**
	 * 
	 * @param jcas
	 * @param doc
	 */

	private void createTermFreqVector(JCas jcas, Document doc) {

		String docText = doc.getText();
		
		//TO DO: construct a vector of tokens and update the tokenList in CAS
		//list of tokens (including qid, rel)
		List<String> tokens = new ArrayList<String>();
		for (String e: docText.split(" ")) {
      tokens.add(e);
    }
		List<Token> tokensL = new ArrayList<Token>();
		Set<String> unique = new HashSet<String>(tokens);
		for (String key : unique) {
      Token token1 = null;
		  token1.setText(key);
		  token1.setFrequency(Collections.frequency(tokens,key));
		  tokensL.add(token1);
		}
		
		FSList tokensF = new FSList(jcas);
//		tokensF.fromCollectionToFSList(jcas,tokensL);
		FSArray tokensFa = new FSArray(jcas,tokensL.size());
		Iterator<Token> iter = tokensL.iterator();
    Token token2 = null;
    int tokIndex = 0;
    while (iter.hasNext()) {
      token2 = iter.next();
      tokensFa.set(tokIndex,token2);
      tokIndex++;
    }
   
		doc.setTokenList(tokensF);
		
	}
}
