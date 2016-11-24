package DocProcessClassification.DataAdapter;


import pipeline.CompositeDoc;
import shared.datatypes.ItemFeature;


public class ClassifierOriginalFeatureAdapter implements ClassifierInputTarget {
	@Override
	public String GetInputText(CompositeDoc compositeDoc) {
		// TODO Auto-generate method stub
		StringBuilder sb = new StringBuilder();
		
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
