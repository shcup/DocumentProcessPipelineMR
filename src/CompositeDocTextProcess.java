import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Map.Entry;

import org.apache.hadoop.mapreduce.Mapper.Context;

import TextRank.KeyWords;
import TextRank.TextRank;
import DocProcess.CompositeDocSerialize;
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
import edu.stanford.nlp.util.Pair;
import pipeline.CompositeDoc;
import shared.datatypes.FeatureType;
import shared.datatypes.ItemFeature;

public class CompositeDocTextProcess implements IDocProcessor {

	Stopword stopword = null;	
	Class stemClass = Class.forName("englishStemmer");
    SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
    protected StanfordCoreNLP pipeline;
    protected String url;
    protected String id;
    
	CompositeDocTextProcess() throws Exception {
		stopword = new Stopword();
		// Create StanfordCoreNLP object properties, with POS tagging
        // (required for lemmatization), and lemmatization
        Properties props;
        props = new Properties();
        //props.put("annotators", "tokenize, ssplit, pos, lemma, ner");
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
        props.setProperty("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz");
        props.setProperty("ner.model", "edu/stanford/nlp/models/ner/english.all.3class.distsim.crf.ser.gz");


        // StanfordCoreNLP loads a lot of models, so you probably
        // only want to do this once per execution
        this.pipeline = new StanfordCoreNLP(props);
       
	}
	
	@Override
	public int Process(CompositeDoc compositeDoc) {
		url = compositeDoc.doc_url;
		id = compositeDoc.media_doc_info.id;
		compositeDoc.title_words = new ArrayList<String>();
		compositeDoc.body_words  = new ArrayList<String>();
		compositeDoc.title_2grams  = new ArrayList<String>();
		compositeDoc.body_2grams  = new ArrayList<String>();
		compositeDoc.title_ner = new ArrayList<String>();
		compositeDoc.body_ner = new ArrayList<String>();
		compositeDoc.title_np = new ArrayList<String>();
		compositeDoc.title_nnp = new ArrayList<String>();
		compositeDoc.body_np = new ArrayList<String>();
		compositeDoc.body_nnp = new ArrayList<String>();
		
		compositeDoc.feature_list = new ArrayList<ItemFeature>();
		
		/*HashMap<String, MatchType> np_hashmap = new HashMap<String, MatchType>(); 
		HashMap<String, MatchType> nnp_hashmap = new HashMap<String, MatchType>(); 
		HashMap<String, MatchType> vb_hashmap = new HashMap<String, MatchType>();*/
		
		HashMap<String, Integer> entity_hashmap = new HashMap<String, Integer>();
		HashMap<String, Integer> np_hashmap = new HashMap<String, Integer>();
		HashMap<String, Integer> nnp_hashmap = new HashMap<String, Integer>();
		
		int res = 0;
		res = res | GetWords2GramFromText(compositeDoc.title, compositeDoc.title_words, compositeDoc.title_2grams,
				compositeDoc.title_ner, compositeDoc.title_np, compositeDoc.title_nnp,
				0);
		if (compositeDoc.short_desc != null && !compositeDoc.short_desc.isEmpty()) {
			res = res | GetWords2GramFromText(compositeDoc.short_desc, compositeDoc.body_words, compositeDoc.body_2grams, 
					compositeDoc.body_ner, compositeDoc.body_np, compositeDoc.body_nnp, 
					0);
		}
		if (compositeDoc.main_text_list != null) {
			for (String text : compositeDoc.main_text_list) {
				res = res | GetWords2GramFromText(text, compositeDoc.body_words, compositeDoc.body_2grams,
						compositeDoc.body_ner,  compositeDoc.body_np, compositeDoc.body_nnp, 
						0);
			}
		}
		
		/*ArrayList<Pair<String, Double>> weight = new ArrayList<Pair<String, Double>>();
		ElementWeightCalculate(np_hashmap, weight, compositeDoc.title_np, compositeDoc.body_np);
		AddWeight2CompositeDoc(weight, compositeDoc, shared.datatypes.FeatureType.NP);
		
		weight.clear();
		ElementWeightCalculate(nnp_hashmap, weight, compositeDoc.title_nnp, compositeDoc.body_nnp);
		AddWeight2CompositeDoc(weight, compositeDoc, shared.datatypes.FeatureType.NNP);
		
		weight.clear();
		ElementWeightCalculate(vb_hashmap, weight, null, null);
		AddWeight2CompositeDoc(weight, compositeDoc, shared.datatypes.FeatureType.VB);*/
		
		// add the ner feature to feature list
		for (int i = 0 ;i < compositeDoc.title_ner.size(); ++i) {
			if (entity_hashmap.containsKey(compositeDoc.title_ner.get(i))) {
				entity_hashmap.put(compositeDoc.title_ner.get(i), entity_hashmap.get(compositeDoc.title_ner.get(i)) + 3) ;
			} else {
				entity_hashmap.put(compositeDoc.title_ner.get(i), 3) ;
			}
		}
		for (int i = 0;i < compositeDoc.body_ner.size(); ++i) {
			if (entity_hashmap.containsKey(compositeDoc.body_ner.get(i))) {
				entity_hashmap.put(compositeDoc.body_ner.get(i), entity_hashmap.get(compositeDoc.body_ner.get(i)) + 1) ;
			} else {
				entity_hashmap.put(compositeDoc.body_ner.get(i), 1) ;
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
		for (int i = 0 ;i < compositeDoc.title_np.size(); ++i) {
			if (np_hashmap.containsKey(compositeDoc.title_np.get(i))) {
				np_hashmap.put(compositeDoc.title_np.get(i), np_hashmap.get(compositeDoc.title_np.get(i)) + 3) ;
			} else {
				np_hashmap.put(compositeDoc.title_np.get(i), 3) ;
			}
		}
		for (int i = 0;i < compositeDoc.body_np.size(); ++i) {
			if (np_hashmap.containsKey(compositeDoc.body_np.get(i))) {
				np_hashmap.put(compositeDoc.body_np.get(i), np_hashmap.get(compositeDoc.body_np.get(i)) + 1) ;
			} else {
				np_hashmap.put(compositeDoc.body_np.get(i), 1) ;
			}			
		}
		for (Entry<String, Integer> pair : np_hashmap.entrySet()) {
			ItemFeature item_feature = new ItemFeature();
			item_feature.name = pair.getKey().toLowerCase();
			item_feature.weight = (pair.getValue().shortValue());
			item_feature.type = shared.datatypes.FeatureType.ORGANIZATION;
			compositeDoc.feature_list.add(item_feature);
		}
		
		// nnp
		for (int i = 0 ;i < compositeDoc.title_nnp.size(); ++i) {
			if (nnp_hashmap.containsKey(compositeDoc.title_nnp.get(i))) {
				nnp_hashmap.put(compositeDoc.title_nnp.get(i), nnp_hashmap.get(compositeDoc.title_nnp.get(i)) + 3) ;
			} else {
				nnp_hashmap.put(compositeDoc.title_nnp.get(i), 3) ;
			}
		}
		for (int i = 0;i < compositeDoc.body_nnp.size(); ++i) {
			if (nnp_hashmap.containsKey(compositeDoc.body_nnp.get(i))) {
				nnp_hashmap.put(compositeDoc.body_nnp.get(i), nnp_hashmap.get(compositeDoc.body_nnp.get(i)) + 1) ;
			} else {
				nnp_hashmap.put(compositeDoc.body_nnp.get(i), 1) ;
			}			
		}
		for (Entry<String, Integer> pair : nnp_hashmap.entrySet()) {
			ItemFeature item_feature = new ItemFeature();
			item_feature.name = pair.getKey().toLowerCase();
			item_feature.weight = (pair.getValue().shortValue());
			item_feature.type = shared.datatypes.FeatureType.ORGANIZATION;
			compositeDoc.feature_list.add(item_feature);
		}				
		//added by lujing		
		KeyWords keyWords=KeyWords.getKeyWords(compositeDoc.main_text_list,10);
		compositeDoc.text_rank=keyWords.toItemFeature(keyWords.keyWords);
		compositeDoc.text_rank_phrase=keyWords.toItemFeature(keyWords.keyTerms);
		return res;
		//ElementWeightCalculate(compositeDoc.title_ner, weight, null, null);
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
	
	public String NormalizedString(String input) {
		String res = input;
		if (res.endsWith("'s")) {
			res = res.substring(0, res.length() - 2);
		}
		
		int start , end;
		for (start = 0; start < res.length(); ++start) {
			if (Character.isLetterOrDigit(res.charAt(start))) {
				break;
			}
		}
		for (end = res.length() -1 ; end >= 0; --end) {
			if (Character.isLetterOrDigit(res.charAt(end))) {
				break;
			}
		}
		
		if (start <= end) {
			return res.substring(start, end + 1).toLowerCase();
		}
		return null;
	}
	
	public int GetWords2GramFromText(String documentText, 
									   List<String> lemmas, 
									   List<String> two_grams, 
									   List<String> ner,
									   List<String> np_list,
									   List<String> nnp_list,
									   int paragraph_idx) {
		if (documentText == null || documentText.isEmpty()) {
			return 1;
		}
		if (documentText.length() > 10000) {
			System.out.println("xxddd " + url + " " + id + " " + documentText.length());
			return 2;
		}
			
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(documentText);

        // run all Annotators on this text
        this.pipeline.annotate(document);


        // Iterate over all of the sentences found
        List<CoreMap> sentences = document.get(SentencesAnnotation.class);
        int idx = 0;
        
        StringBuilder sb = new StringBuilder();
        for(CoreMap sentence: sentences) {
            // Iterate over all tokens in a sentence
        	String preWord = null;
        	String preNer = null;
        	boolean preWordIsNer = false;
            for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
                // Retrieve and add the lemma for each word into the list of lemmas
            	String word = token.get(LemmaAnnotation.class).toLowerCase();
            	String ne = token.get(NamedEntityTagAnnotation.class);
            	if (ne.equals("PERSON") || ne.equals("LOCATION") || ne.equals("ORGANIZATION")) {
            		if (preWordIsNer) {
            			sb.append(" ");
            			sb.append(word);
            		} else {
            			sb.append(word);
            		}        		
            		preWordIsNer = true;
            	} else {
            		if (sb.length() != 0) {
            			ner.add(sb.toString());
            			sb = new StringBuilder();
            		}
            		preWordIsNer = false;
            	}

            	if (!stopword.IsStopWord(word)) {
            		lemmas.add(word);
            	}
                
                if (preWord != null && IsWord(word) && IsWord(preWord)) {
                	two_grams.add(preWord + "_" + word);
                }
                preWord = word;
            }
    		if (sb.length() != 0) {
    			ner.add(sb.toString());
    			sb = new StringBuilder();
    		}
            // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeAnnotation.class);
  	      	ArrayList<String> np_array = new ArrayList<String>();
  	      	ArrayList<String> nnp_array = new ArrayList<String>();
  	      	ArrayList<String> vb_array = new ArrayList<String>();
  	      	SentencePhraseParse(tree, np_array, nnp_array, vb_array);
  	      	
  	      	
			  for (String np : np_array) {
				  String tmp = NormalizedString(np);
				  if (tmp == null || stopword.IsStopWord(tmp)) { 
					  continue;
				  }
				  np_list.add(tmp);
				  /*if (np_hashmap.containsKey(tmp)) {
					  np_hashmap.get(tmp).match.add(new Pair(paragraph_idx, idx));
				  } else {
					  np_hashmap.put(tmp, new MatchType());
					  np_hashmap.get(tmp).match.add(new Pair(paragraph_idx, idx));
				  }*/
			  }
			  
			  for (String nnp : nnp_array) {
				  String tmp = NormalizedString(nnp);
				  if (tmp == null || stopword.IsStopWord(tmp)) { 
					  continue;
				  }
				  nnp_list.add(tmp);
				  /*if (nnp_hashmap.containsKey(tmp)) {
					  nnp_hashmap.get(tmp).match.add(new Pair(paragraph_idx, idx));
				  } else {
					  nnp_hashmap.put(tmp, new MatchType());
					  nnp_hashmap.get(tmp).match.add(new Pair(paragraph_idx, idx));
				  }*/
			  }
			  
			  /*for (String vb : vb_array) {
				  if (vb_hashmap.containsKey(vb)) {
					  vb_hashmap.get(vb).match.add(new Pair(paragraph_idx, idx));
				  } else {
					  vb_hashmap.put(vb, new MatchType());
					  vb_hashmap.get(vb).match.add(new Pair(paragraph_idx, idx));
				  }
			  }*/
			  idx++;
        }
        
        return 0;

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
	
    private static String NP = "NP";
    private static String NNP = "NNP";
    private static String NNPS = "NNPS";
    private static String VBZ = "VBZ";
    private static String VBN = "VBN";
    private static String VBG = "VBG";
    private static String VB = "VB";
    
    private void SentencePhraseParse(Tree tree, ArrayList<String> np, ArrayList<String> nnp, ArrayList<String> vb) {
    	//ArrayList<Pair<String, Integer>> res = new ArrayList<Pair<String, Integer>>();
    	
    	if (tree.label().value().equals(NP)) {
    		ArrayList<Pair<ArrayList<String>, Boolean>>  words = new ArrayList<Pair<ArrayList<String>, Boolean>>();
    		words.add(new Pair(new ArrayList<String>(), false));
    		GenerateNP(tree, words, vb);
    		ConvertWordsToString(words, np, nnp);
    		return ;
    	}
    	
    	if (tree.label().value().equals(VBZ) || tree.label().value().equals(VBN) || 
    			tree.label().value().equals(VBG) || tree.label().value().equals(VB)) {
    		if (!tree.isLeaf() && tree.getChild(0).label() != null) {
    			vb.add(tree.getChild(0).label().value().toString());
    		}
    	}
    	
    	Tree[] kids = tree.children();
    	if (kids != null) {
    		for (Tree kid : kids) {
    			SentencePhraseParse(kid, np, nnp, vb);
    		}
    	}
    	
     	return;
    }
    
    // if in NP there are some other sub NP, it will only generate the words for lowest NP
    // and also it will seperate the NP and NNP
    private void GenerateNP(Tree tree, ArrayList<Pair<ArrayList<String>, Boolean>> words, ArrayList<String> vb) {
    	if (tree.isLeaf()) {
    		if (tree.label() != null) {
    			words.get(words.size() - 1).first.add(tree.label().value());
    		}
    		return;
    	}
    	
    	if (tree.label().value().equals(NP)) {
    		words.get(words.size() - 1).first.clear();
    		words.get(words.size() - 1).second = false;
    	}
    	
    	if (tree.label().value().equals(NNP) || tree.label().value().equals(NNPS)) {
    		words.get(words.size() - 1).second = true;
    	}
    	
    	if (tree.label().value().equals(VBZ) || tree.label().value().equals(VBN) || 
    			tree.label().value().equals(VBG) || tree.label().value().equals(VB)) {
    		if (!tree.isLeaf() && tree.getChild(0).label() != null) {
    			vb.add(tree.getChild(0).label().value().toString());
    		}
    	}
    	
    	Tree[] kids = tree.children();
    	if (kids != null) {
    		for (Tree kid : kids) {
    			GenerateNP(kid, words, vb);
    		}
    	}
    	
    	if (tree.label().value().equals(NP)) {
    		words.add(new Pair(new ArrayList<String>(), false));
    	}
    }
    
    private void ConvertWordsToString(ArrayList<Pair<ArrayList<String>, Boolean>> words, ArrayList<String> np, ArrayList<String> nnp) {
    	for (Pair<ArrayList<String>, Boolean> arraylist : words) {
    		if (arraylist.first.size() != 0) {
		    	StringBuilder sb = new StringBuilder();
		    	boolean has_charactor = false;
		    	for (int i = 0; i < arraylist.first.size(); ++i) {
		    		sb.append(arraylist.first.get(i));
		    		if (i != arraylist.first.size() - 1) {
		    			sb.append(" ");
		    		}
		    	}
		    	String phrase = sb.toString();
		    	if (phrase.length() == 1 && !Character.isLetter(phrase.charAt(0))) {
		    		continue;
		    	}
		    	if (arraylist.second == true) {
		    		nnp.add(phrase);
		    	} else {
		    		np.add(phrase);
		    	}
    		}
    	}
    }
    
	
	private void ElementWeightCalculate(HashMap<String, MatchType> hashmap, ArrayList<Pair<String, Double>> weight, List<String>  title, List<String> body) {

		for (Entry<String, MatchType> item : hashmap.entrySet()) {
			double feature_score = 0.0f;
			boolean intitle = false;
			boolean inbody = false;
			for (Pair<Integer, Integer> pair : item.getValue().match) {
				if (pair.first == 0) {
					feature_score = feature_score + 2.0f;
					intitle = true;
				} else {
					feature_score = feature_score + (1.0f/pair.first) * (1.0f/Math.sqrt(pair.second)); // also should consider the idf
					inbody = true;
				}
			}
			/*if (intitle == true && title != null) {
				title.add(item.getKey().toLowerCase());
			}
			if (inbody == true && body != null) {
				body.add(item.getKey().toLowerCase());
			}*/
			weight.add(new Pair(item.getKey(), feature_score));
		}
		

		
		// normalize
		double max = -1.0f;
		for (Pair<String, Double> pair : weight) {
			if (pair.second > max) {
				max = pair.second;
			}
		}

		for (Pair<String, Double> pair : weight) {
			pair.second = pair.second / max;
		}
	}
	
	private void AddWeight2CompositeDoc(ArrayList<Pair<String, Double>> weight, CompositeDoc compositeDoc, FeatureType feature_type) {
		for (Pair<String, Double> pair : weight) {
			ItemFeature item_feature = new ItemFeature();
			item_feature.name = pair.first.toLowerCase();
			item_feature.weight = (short) (pair.second * 1000);
			item_feature.type = feature_type;
			compositeDoc.feature_list.add(item_feature);
		}
	}
	
	private boolean IsWord(String word) {
		if (word.equals("'s")) {
			return false;
		}
		
		for (int i = 0 ; i < word.length(); ++i) {
			if (Character.isLetterOrDigit(word.charAt(i))) {
				return true;
			}
		}
		
		return  false;
	}
    
    public static void main(String[] args) throws Exception 
    {
    	CompositeDocTextProcess textProcess = new CompositeDocTextProcess();
    	Scanner scanner = new Scanner(System.in); 
    	
    	File file = new File("D:\\Temp\\part-00017");//Text文件
    	BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
    	String line = null;
    	
        FileOutputStream out = new FileOutputStream("d://Temp//out.txt"); // 输出文件路径   
    	while((line = br.readLine())!=null){//使用readLine方法，一次读一行
    		ArrayList<String> lemmas = new  ArrayList<String>();
    		ArrayList<String> two_grams = new  ArrayList<String>();
    		ArrayList<String> ner = new  ArrayList<String>();
    		HashMap<String, MatchType> np_hashmap = new HashMap<String, MatchType>(); 
    		HashMap<String, MatchType> nnp_hashmap = new HashMap<String, MatchType>(); 
    		HashMap<String, MatchType> vb_hashmap = new HashMap<String, MatchType>();
    		
    		
		    String[] segments = line.split("\t");
		    if (segments.length != 2) {
		    	continue;
		    }
		    
		    CompositeDoc compositeDoc = CompositeDocSerialize.DeSerialize(segments[1], null);
		    int res_status = textProcess.Process(compositeDoc);	    
		    StringBuilder sb = new StringBuilder();
		    sb.append(compositeDoc.media_doc_info.id + "\t" + compositeDoc.doc_url + "\n");
		    
		    sb.append("Title NP: ");
    		for (String word : compositeDoc.title_np) {
    			sb.append(word + ",");
    		}
    		sb.append("\nBody NP: ");
    		for (String word : compositeDoc.body_np) {
    			sb.append(word + ",");
    		}
    		sb.append("\nTitle NNP: ");
    		
    		for (String word : compositeDoc.title_nnp) {
    			sb.append(word + ",");
    		}
    		sb.append("\nBody NNP: ");
    		for (String word : compositeDoc.body_nnp) {
    			sb.append(word + ",");
    		}
    		sb.append("\nTitle NER: ");
    		
    		for (String word : compositeDoc.title_ner) {
    			sb.append(word + ",");
    		}
    		sb.append("\nBody NER: ");
    		for (String word : compositeDoc.body_ner) {
    			sb.append(word + ",");
    		}
    		sb.append("\nText rank");
    		
    		for (shared.datatypes.ItemFeature item : compositeDoc.text_rank) {
    			sb.append(item.name + ",");
    		}
    		sb.append("\nText rank phrase");
    		for (shared.datatypes.ItemFeature item : compositeDoc.text_rank_phrase) {
    			sb.append(item.name + ",");
    		}
    		sb.append('\n');
    		
    		out.write(sb.toString().getBytes());
    		out.flush();
    	}
    	br.close();;

    }
}
