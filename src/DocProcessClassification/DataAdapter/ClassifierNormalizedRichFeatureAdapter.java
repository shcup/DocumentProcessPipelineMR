package DocProcessClassification.DataAdapter;
import pipeline.CompositeDoc;
import shared.datatypes.ItemFeature;
public class ClassifierNormalizedRichFeatureAdapter implements ClassifierInputTarget {
	@Override
	public String GetInputText(CompositeDoc compositeDoc) {
		// TODO Auto-generate method stub
		StringBuilder sb = new StringBuilder();
		
		// text process
		if(compositeDoc.category_info != null && 
				compositeDoc.category_info.category_item != null &&
				compositeDoc.category_info.category_item.get(0).category_path !=null ) {
			String s=compositeDoc.category_info.category_item.get(0).getCategory_path().toString();
		    sb.append(s);
			sb.append(' ');
		} else {
			sb.append(' ');
		}
		
		if (compositeDoc.bread_crumbs != null) {
			for (String word : compositeDoc.bread_crumbs) {
				sb.append(word);
				sb.append(' ');
			}
		}
		if (compositeDoc.title_words != null) {
			for (String word : compositeDoc.title_words) {
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
				sb.append(' ');
			}			
		}

		if (compositeDoc.body_2grams != null) {
			for (String word : compositeDoc.body_2grams) {
				sb.append(word.replace(" ","_"));
				sb.append(' ');
			}
		}
		
		// NLP
		if (compositeDoc.title_nnp != null) {
			for (String word : compositeDoc.title_nnp) {
				sb.append(word.replace(" ","_"));
				sb.append(' ');
			}
		}
		if (compositeDoc.body_nnp != null) {
			for (String word : compositeDoc.body_nnp) {
				sb.append(word.replace(" ","_"));
				sb.append(' ');
			}
		}
		
		if (compositeDoc.title_ner != null) {
			for (String word : compositeDoc.title_ner) {
				sb.append(word.replace(" ","_"));
				sb.append(' ');				
			}
		}
		if (compositeDoc.body_ner != null) {
			for (String word : compositeDoc.body_ner) {
				sb.append(word.replace(" ","_"));
				sb.append(' ');
			}
		}
		if (compositeDoc.title_np != null) {
			for (String word : compositeDoc.title_np) {
				sb.append(word.replace(" ","_"));
				sb.append(' ');
			}
		}
		if (compositeDoc.body_np != null) {
			for (String word : compositeDoc.body_np) {
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
