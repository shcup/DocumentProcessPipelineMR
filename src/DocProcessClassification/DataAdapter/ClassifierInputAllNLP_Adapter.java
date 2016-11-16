package DocProcessClassification.DataAdapter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import pipeline.CompositeDoc;
import shared.datatypes.ItemFeature;

public class ClassifierInputAllNLP_Adapter implements ClassifierInputTarget {

	@Override
	public String GetInputText(CompositeDoc compositeDoc) {
		// TODO Auto-generate method stub
		StringBuilder sb = new StringBuilder();
		
		// text process
		if(compositeDoc.category_info != null && 
				compositeDoc.category_info.category_item != null &&
				compositeDoc.category_info.category_item.get(0).category_path !=null ) {
			String s=compositeDoc.category_info.category_item.get(0).getCategory_path().toString();
//		    sb.append(compositeDoc.category_info.category_item.get(0).getCategory_path());
		    sb.append(s);
			sb.append(' ');
		} else {
			sb.append(' ');
		}
		
		if (compositeDoc.bread_crumbs != null) {
			for (String word : compositeDoc.bread_crumbs) {
//				sb.append("");
				sb.append(word);
				sb.append(' ');
			}
		}
		if (compositeDoc.title_words != null) {
			for (String word : compositeDoc.title_words) {
//				sb.append("");
				sb.append(word.replace(" ","_"));
				sb.append(' ');
			}
		}
		
		if (compositeDoc.body_words != null ) {
			for (String word : compositeDoc.body_words) {		
				sb.append(word.replace(" ","_"));
				sb.append(' ');
			}
		}

		if (compositeDoc.title_2grams != null) {
			for (String word : compositeDoc.title_2grams) {
				sb.append(word.replace(" ","_"));
//				sb.append(word);
				sb.append(' ');
			}			
		}

		if (compositeDoc.body_2grams != null) {
			for (String word : compositeDoc.body_2grams) {
				sb.append(word.replace(" ","_"));
//				sb.append(word);
				sb.append(' ');
			}
		}
		
		// NLP
		if (compositeDoc.title_nnp != null) {
			for (String word : compositeDoc.title_nnp) {
				sb.append(word.replace(" ","_"));
//				sb.append(word);
				sb.append(' ');
			}
		}
		if (compositeDoc.body_nnp != null) {
			for (String word : compositeDoc.body_nnp) {
				sb.append(word.replace(" ","_"));
//				sb.append(word);
				sb.append(' ');
			}
		}
		if (compositeDoc.title_NER_person != null) {
			for (String word : compositeDoc.title_NER_person) {
//				sb.append("_");
//				sb.append(word);
				sb.append(word.replace(" ","_"));
				sb.append(' ');
			}			
		}

		if (compositeDoc.title_NER_location != null) {
			for (String word : compositeDoc.title_NER_location) {
//				sb.append("_");
//				sb.append(word);
				sb.append(word.replace(" ","_"));
				sb.append(' ');
			}
		}
		if (compositeDoc.title_NER_organization != null) {
			for (String word : compositeDoc.title_NER_organization) {
//				sb.append("_");
//				sb.append(word);
				sb.append(word.replace(" ","_"));
				sb.append(' ');
			}			
		}
		
		if(compositeDoc.text_rank !=null){
			for (shared.datatypes.ItemFeature item : compositeDoc.text_rank) {
				sb.append(item.name);
				sb.append(' ');
			}
		}
	
		if(compositeDoc.text_rank_phrase !=null){
			for (shared.datatypes.ItemFeature item : compositeDoc.text_rank_phrase) {
				sb.append(item.name);
				sb.append(' ');
			}
		}
		
		return sb.toString();
	}
	
	

}
