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

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.HasWord;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.DocumentPreprocessor;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

public class CompositeDocTextProcess {

	Stopword stopword = null;	
	Class stemClass = Class.forName("englishStemmer");
    SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
    
	CompositeDocTextProcess() throws Exception {
		stopword = new Stopword();
	}
	
	public void Process(CompositeDoc compositeDoc) {
		
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
