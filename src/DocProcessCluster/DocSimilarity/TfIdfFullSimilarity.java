package DocProcessCluster.DocSimilarity;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import pipeline.CompositeDoc;

public class TfIdfFullSimilarity implements ICompositeDocSimilarityGenerator {

	private HashMap<String, Double> idfTable;
	private int totalDoc;
	
	public TfIdfFullSimilarity() {
		idfTable = new HashMap<String, Double>();
	}
	public void Load(String file) throws NumberFormatException, IOException {
		try(BufferedReader br=new BufferedReader(new FileReader(file))){
			String line;
	        boolean first = true;
	        while((line=br.readLine())!=null) {
	        	if (first) {
	        		totalDoc = Integer.valueOf(line);
	        		first = false;
	        	}
	        	String[] items = line.split("\t");
	        	if (items.length != 2) {
	        		System.exit(-1);
	        	}
	        	Integer df = Integer.valueOf(items[1]);
	        	
	        	idfTable.put(items[0], Math.log((double)(totalDoc) / df));
	        }
		}
	}
	@Override
	public double Get(CompositeDoc doc1, CompositeDoc doc2) {
		// TODO Auto-generated method stub
		HashMap<String, Integer> doc1_vec = new HashMap<String, Integer>();
		HashMap<String, Integer> doc2_vec = new HashMap<String, Integer>();
		
		SetVec(doc1.title_words, doc1_vec, 2);
		SetVec(doc2.title_words, doc2_vec, 2);
		SetVec(doc1.body_words, doc1_vec, 1);
		SetVec(doc2.body_words, doc2_vec, 1);
		
		return Consine(doc1_vec, doc2_vec);
	}
	
	private double Consine(HashMap<String, Integer> doc1_vec, HashMap<String, Integer> doc2_vec) {
		Iterator<Entry<String, Integer>> it;
		double dotProduct = 0;
		for (it = doc1_vec.entrySet().iterator(); it.hasNext();) {
			Entry<String, Integer> entry = it.next();
			Integer value = doc2_vec.get(entry.getKey());
			if (value != null) {
				dotProduct += value * entry.getValue();
			}
		}
		
		if (doc1_vec.isEmpty() || doc2_vec.isEmpty()) {
			return 0.0f;
		}
		
		return dotProduct / (GetInnerProduct(doc1_vec) * GetInnerProduct(doc2_vec));
	}
	
	private double GetInnerProduct(HashMap<String, Integer> doc_vec) {
		Iterator<Entry<String, Integer>> it;
		double product = 0;
		for (it = doc_vec.entrySet().iterator(); it.hasNext();) {
			Entry<String, Integer> entry = it.next();
			product += entry.getValue() * entry.getValue();
		}
		return Math.sqrt(product);
	}
	
	private void SetVec(List<String> words, HashMap<String, Integer> doc_vec, int weight) {
		if (words != null) {
			for (String word : words) {
				Integer count = doc_vec.get(word);
				if (count == null) {
					doc_vec.put(word, weight);
				} else {
					doc_vec.put(word, count + weight);
				}
			}
		}
	}

}
