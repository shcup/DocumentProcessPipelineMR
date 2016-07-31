import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import DocProcess.IDocProcessor;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.util.CoreMap;
import pipeline.CompositeDoc;

public class CompositeDocTextProcess implements IDocProcessor {

	Stopword stopword = null;	
	Class stemClass = Class.forName("englishStemmer");
    SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
    protected StanfordCoreNLP pipeline;
    
	CompositeDocTextProcess() throws Exception {
		stopword = new Stopword();
		// Create StanfordCoreNLP object properties, with POS tagging
        // (required for lemmatization), and lemmatization
        Properties props;
        props = new Properties();
        //props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
        props.setProperty("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz");


        // StanfordCoreNLP loads a lot of models, so you probably
        // only want to do this once per execution
        this.pipeline = new StanfordCoreNLP(props);
	}
	
	public void Process(CompositeDoc compositeDoc) {
		compositeDoc.title_words = new ArrayList<String>();
		compositeDoc.body_words  = new ArrayList<String>();
		compositeDoc.title_2grams  = new ArrayList<String>();
		compositeDoc.body_2grams  = new ArrayList<String>();
		compositeDoc.title_ner = new ArrayList<String>();
		compositeDoc.body_ner = new ArrayList<String>();
		
		GetWords2GramFromText(compositeDoc.title, compositeDoc.title_words, compositeDoc.title_2grams, compositeDoc.title_ner);
		if (compositeDoc.short_desc != null && !compositeDoc.short_desc.isEmpty()) {
			GetWords2GramFromText(compositeDoc.short_desc, compositeDoc.body_words, compositeDoc.body_2grams, compositeDoc.title_ner);
		}
		if (compositeDoc.main_text_list != null) {
			for (String text : compositeDoc.main_text_list) {
				GetWords2GramFromText(text, compositeDoc.body_words, compositeDoc.body_2grams, compositeDoc.body_ner);
			}
		}
	}
	
	public void Process1(CompositeDoc compositeDoc) {
		
		compositeDoc.title_words = new ArrayList<String>();
		compositeDoc.body_words  = new ArrayList<String>();
		compositeDoc.title_2grams  = new ArrayList<String>();
		compositeDoc.body_2grams  = new ArrayList<String>();
		
		GetWordsFromText(compositeDoc.title, compositeDoc.title_words);
		if (compositeDoc.short_desc != null && !compositeDoc.short_desc.isEmpty()) {
			GetWordsFromText(compositeDoc.short_desc, compositeDoc.body_words);
		}
		if (compositeDoc.main_text_list != null) {
			for (String text : compositeDoc.main_text_list) {
				GetWordsFromText(text, compositeDoc.body_words);
			}
		}
		
		
		Get2GramFromText(compositeDoc.title, compositeDoc.title_2grams);
		if (compositeDoc.short_desc != null && !compositeDoc.short_desc.isEmpty()) {
			Get2GramFromText(compositeDoc.short_desc, compositeDoc.body_2grams);
		}
		if (compositeDoc.main_text_list != null) {
			for (String text : compositeDoc.main_text_list) {
				Get2GramFromText(text, compositeDoc.body_2grams);
			}
		}
	}
	
	private void GetWords2GramFromText(String documentText, List<String> lemmas, List<String> two_grams, List<String> ner) {
		if (documentText == null || documentText.isEmpty()) {
			return;
		}
		
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(documentText);

        // run all Annotators on this text
        this.pipeline.annotate(document);

        // Iterate over all of the sentences found
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        for(CoreMap sentence: sentences) {
            // Iterate over all tokens in a sentence
        	String preWord = null;
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                // Retrieve and add the lemma for each word into the list of lemmas
            	String word = token.get(LemmaAnnotation.class);
            	String ne = token.get(NamedEntityTagAnnotation.class);
            	if (ne == "PERSON" || ne == "LOCATION" || ne == "ORGANIZATION" || ne == "MISC") {
            		ner.add(ne);
            	}
            	if (!stopword.IsStopWord(word)) {
            		lemmas.add(word);
            	}
                
                if (preWord != null) {
                	two_grams.add(preWord + "_" + word);
                }
                preWord = word;
            }
            // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeAnnotation.class);
        }
        

	}
	
	private void GetWordsFromText(String text, List<String> words) {
		
		if (text == null || text.isEmpty()) {
			return;
		}
		
		TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
	    Tokenizer<CoreLabel> tok =tokenizerFactory.getTokenizer(new StringReader(text));
	    List<CoreLabel> sentence = tok.tokenize();

		for (Label lab : sentence) {
			if (lab instanceof CoreLabel) {
				String word = lab.value().toLowerCase();
				if (stopword.IsStopWord(word)) {
					continue;
				}
				stemmer.setCurrent(word);
				stemmer.stem();
				words.add(stemmer.getCurrent());
			}
	    } 	
	}
	
	private void Get2GramFromText(String text, List<String> two_grams) {
		if (text == null || text.isEmpty()) {
			return;
		}
		DocumentPreprocessor tokenizer = new DocumentPreprocessor(new StringReader(text));
		for (List<HasWord> sentence : tokenizer) {
			String pre_word = null;
			for (HasWord word : sentence) {
				String cur_word = word.toString();
				if (pre_word == null) {
					pre_word = cur_word;
					continue;
				}
				two_grams.add(pre_word + "_" + cur_word);
				pre_word = cur_word;
			}
		}
	}
}
