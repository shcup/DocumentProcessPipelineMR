package DocProcessClassification.DataAdapter;
import pipeline.CompositeDoc;
import shared.datatypes.ItemFeature;
public class ClassifierOriginalWithCrawlerFeatureAdapter  implements ClassifierInputTarget {
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
		
		if (compositeDoc.title != null && !compositeDoc.title.isEmpty()) {
			sb.append(compositeDoc.title);
			sb.append(' ');
		}
		if (compositeDoc.short_desc != null && !compositeDoc.short_desc.isEmpty()) {
			sb.append(compositeDoc.short_desc);
			sb.append(' ');
		}
		for (int i = 0; compositeDoc.main_text_list != null && i < compositeDoc.main_text_list.size(); ++i) {
			sb.append(compositeDoc.main_text_list.get(i));
			sb.append(' ');
		}

		
		return sb.toString();
	}
}
