import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import DocProcess.IDocProcessor;
import DocProcessUtil.Stopword;
import TextRank.KeyWords;
import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import pipeline.CompositeDoc;
import shared.datatypes.ItemFeature;

/**
 * This class is supposed to work as base class for NLP related work for Hindi
 * Language Its mostly a copy of english version now with some changes specific
 * to Hindi language.  Please note that there multiple changes need to be done is this class and some copy pasted code , 
 * who have no work ,are still here
 * 
 * @author Chandrakant
 */
//TODO pos tagging, NER 
public class CompositeDocTextProcessHindi implements IDocProcessor {

	DocProcessUtil.Stopword stopword = null;
	HindiStemmer stemmer = new HindiStemmer();
	protected StanfordCoreNLP pipeline;
	protected String url;
	protected String id;

	CompositeDocTextProcessHindi() throws Exception {
		stopword = new Stopword();

		// Create StanfordCoreNLP object properties, with POS tagging
		// (required for lemmatization), and lemmatization
		Properties props;
		props = new Properties();
		// right now I am leaving pos annotator here as stanford lemmtizer
		// depends on it.
		// Would refactor it once hindi pos tagger is complete

		props.setProperty("annotators", "tokenize, ssplit, pos, lemma");

		// StanfordCoreNLP loads a lot of models, so you probably
		// only want to do this once per execution
		this.pipeline = new StanfordCoreNLP(props);

	}

	@Override
	public int Process(CompositeDoc compositeDoc) {
		url = compositeDoc.doc_url;
		id = compositeDoc.media_doc_info.id;
		compositeDoc.title_words = new ArrayList<String>();
		compositeDoc.body_words = new ArrayList<String>();
		compositeDoc.title_2grams = new ArrayList<String>();
		compositeDoc.body_2grams = new ArrayList<String>();
		compositeDoc.title_ner = new ArrayList<String>();
		compositeDoc.body_ner = new ArrayList<String>();
		compositeDoc.title_np = new ArrayList<String>();
		compositeDoc.title_nnp = new ArrayList<String>();
		compositeDoc.body_np = new ArrayList<String>();
		compositeDoc.body_nnp = new ArrayList<String>();

		compositeDoc.feature_list = new ArrayList<ItemFeature>();

		HashMap<String, Integer> entity_hashmap = new HashMap<String, Integer>();
		HashMap<String, Integer> np_hashmap = new HashMap<String, Integer>();
		HashMap<String, Integer> nnp_hashmap = new HashMap<String, Integer>();

		int res = 0;
		res = res | GetWords2GramFromText(compositeDoc.title, compositeDoc.title_words, compositeDoc.title_2grams, 0);
		if (compositeDoc.short_desc != null && !compositeDoc.short_desc.isEmpty()) {
			res = res | GetWords2GramFromText(compositeDoc.short_desc, compositeDoc.body_words,
					compositeDoc.body_2grams, 0);
		}
		if (compositeDoc.main_text_list != null) {
			for (String text : compositeDoc.main_text_list) {
				res = res | GetWords2GramFromText(text, compositeDoc.body_words, compositeDoc.body_2grams, 0);
			}
		}

		// add the ner feature to feature list
		for (int i = 0; i < compositeDoc.title_ner.size(); ++i) {
			if (entity_hashmap.containsKey(compositeDoc.title_ner.get(i))) {
				entity_hashmap.put(compositeDoc.title_ner.get(i),
						entity_hashmap.get(compositeDoc.title_ner.get(i)) + 3);
			} else {
				entity_hashmap.put(compositeDoc.title_ner.get(i), 3);
			}
		}
		for (int i = 0; i < compositeDoc.body_ner.size(); ++i) {
			if (entity_hashmap.containsKey(compositeDoc.body_ner.get(i))) {
				entity_hashmap.put(compositeDoc.body_ner.get(i), entity_hashmap.get(compositeDoc.body_ner.get(i)) + 1);
			} else {
				entity_hashmap.put(compositeDoc.body_ner.get(i), 1);
			}
		}
		for (Entry<String, Integer> pair : entity_hashmap.entrySet()) {
			ItemFeature item_feature = new ItemFeature();
			item_feature.name = pair.getKey().toLowerCase();
			item_feature.weight = (pair.getValue().shortValue());
			item_feature.type = shared.datatypes.FeatureType.ORGANIZATION;
			compositeDoc.feature_list.add(item_feature);
		}

		// np
		for (int i = 0; i < compositeDoc.title_np.size(); ++i) {
			if (np_hashmap.containsKey(compositeDoc.title_np.get(i))) {
				np_hashmap.put(compositeDoc.title_np.get(i), np_hashmap.get(compositeDoc.title_np.get(i)) + 3);
			} else {
				np_hashmap.put(compositeDoc.title_np.get(i), 3);
			}
		}
		for (int i = 0; i < compositeDoc.body_np.size(); ++i) {
			if (np_hashmap.containsKey(compositeDoc.body_np.get(i))) {
				np_hashmap.put(compositeDoc.body_np.get(i), np_hashmap.get(compositeDoc.body_np.get(i)) + 1);
			} else {
				np_hashmap.put(compositeDoc.body_np.get(i), 1);
			}
		}
		for (Entry<String, Integer> pair : np_hashmap.entrySet()) {
			ItemFeature item_feature = new ItemFeature();
			item_feature.name = pair.getKey().toLowerCase();
			item_feature.weight = (pair.getValue().shortValue());
			item_feature.type = shared.datatypes.FeatureType.NP;
			compositeDoc.feature_list.add(item_feature);
		}

		// nnp
		for (int i = 0; i < compositeDoc.title_nnp.size(); ++i) {
			if (nnp_hashmap.containsKey(compositeDoc.title_nnp.get(i))) {
				nnp_hashmap.put(compositeDoc.title_nnp.get(i), nnp_hashmap.get(compositeDoc.title_nnp.get(i)) + 3);
			} else {
				nnp_hashmap.put(compositeDoc.title_nnp.get(i), 3);
			}
		}
		for (int i = 0; i < compositeDoc.body_nnp.size(); ++i) {
			if (nnp_hashmap.containsKey(compositeDoc.body_nnp.get(i))) {
				nnp_hashmap.put(compositeDoc.body_nnp.get(i), nnp_hashmap.get(compositeDoc.body_nnp.get(i)) + 1);
			} else {
				nnp_hashmap.put(compositeDoc.body_nnp.get(i), 1);
			}
		}
		for (Entry<String, Integer> pair : nnp_hashmap.entrySet()) {
			ItemFeature item_feature = new ItemFeature();
			item_feature.name = pair.getKey().toLowerCase();
			item_feature.weight = (pair.getValue().shortValue());
			item_feature.type = shared.datatypes.FeatureType.NNP;
			compositeDoc.feature_list.add(item_feature);
		}
		// added by lujing
		KeyWords keyWords = KeyWords.getKeyWords(compositeDoc.main_text_list, 10, stopword);
		compositeDoc.text_rank = keyWords.toItemFeature(keyWords.keyWords, shared.datatypes.FeatureType.TAG);
		compositeDoc.text_rank_phrase = keyWords.toItemFeature(keyWords.keyTerms, shared.datatypes.FeatureType.LABEL);
		compositeDoc.feature_list.addAll(compositeDoc.text_rank);
		compositeDoc.feature_list.addAll(compositeDoc.text_rank_phrase);
		return res;
		// ElementWeightCalculate(compositeDoc.title_ner, weight, null, null);
	}

	
	private Map<String, String> normalizationMap = new HashMap<String, String>() {
		private static final long serialVersionUID = -5686972915633286599L;

		{
			put("\u0901", "\u0902"); // chandrabindu to anuswar conversion
			put("\u0964", ""); // purnaviram to empty character
			put("ऩ", "न");
			put("ऱ", "र");
			put("ऴ", "ळ");
			put("क़", "क");
			put("ख़", "ख");
			put("ग़", "ग");
			put("ज़", "ज");
			put("ड़", "ड");
			put("ढ़", "ढ");
			put("फ़", "फ");
			put("य़", "य");
			put("ॠ", "ऋ");
			put("ॡ", "ऌ");
		}
	};

	/**
	 * 
	 * @param input  - String that needs to be normalized
	 * @return noramlized string .. A normalized string here means replacing characters which have more than one
	 * 								representation to a single representation
	 */
	public String NormalizedString(String input) {
		input = input.trim();
		int len = input.length();
		StringBuilder builder = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			String key = Character.toString(input.charAt(i));
			if (normalizationMap.containsKey(key)) {
				builder.append(normalizationMap.get(key));
			} else {
				builder.append(key);
			}
		}

		return builder.toString();
	}

	public int GetWords2GramFromText(String documentText, List<String> lemmas, List<String> two_grams,
			int paragraph_idx) {
		if (documentText == null || documentText.isEmpty()) {
			return 1;
		}
		if (documentText.length() > 10000) {
			System.out.println("xxddd " + url + " " + id + " " + documentText.length());
			return 2;
		}
		// normalize string
		documentText = NormalizedString(documentText);

		// create an empty Annotation just with the given text
		Annotation document = new Annotation(documentText);

		// run all Annotators on this text
		this.pipeline.annotate(document);

		// Iterate over all of the sentences found
		List<CoreMap> sentences = document.get(SentencesAnnotation.class);

		for (CoreMap sentence : sentences) {
			// Iterate over all tokens in a sentence
			String preWord = null;
			for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
				// Retrieve and add the lemma for each word into the list of
				// lemmas
				String word = token.get(LemmaAnnotation.class).toLowerCase();
				if (!stopword.IsStopWord(word)) {
					lemmas.add(word);
				}

				if (preWord != null && IsWord(word) && IsWord(preWord)) {
					two_grams.add(preWord + "_" + word);
				}
				preWord = word;
			}
			
		}

		return 0;

	}

	private boolean IsWord(String word) {
		if (word.equals("'s")) {
			return false;
		}

		for (int i = 0; i < word.length(); ++i) {
			char ch = word.charAt(i);
			//  check for Devanagari. Devanagari script has unicode ranging from 0900 to 097F
			if (ch < '\u0900' || ch > '\u097F') { 
				if (!Character.isLetterOrDigit(word.charAt(i))) {
					return false;
				}
			}

		}

		return true;
	}
}
