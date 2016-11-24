package DocProcessClassification.DataAdapter;
import pipeline.CompositeDoc;
import shared.datatypes.ItemFeature;
public class ClassifierNormalizedFeatureAdapter implements ClassifierInputTarget {
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
		
		return sb.toString();
	}
}
