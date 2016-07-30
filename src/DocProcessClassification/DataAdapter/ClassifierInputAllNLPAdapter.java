package DocProcessClassification.DataAdapter;

import pipeline.CompositeDoc;
import shared.datatypes.ItemFeature;

public class ClassifierInputAllNLPAdapter implements ClassifierInputTarget {

	@Override
	public String GetInputText(CompositeDoc compositeDoc) {
		// TODO Auto-generated method stub
		StringBuilder sb = new StringBuilder();
		
		// text process
		for (String word : compositeDoc.title_words) {
			sb.append("tw_");
			sb.append(word);
			sb.append('\t');
		}

		for (String word : compositeDoc.body_words) {
			sb.append("bw_");
			sb.append(word);
			sb.append('\t');
		}
		
		for (String word : compositeDoc.title_2grams) {
			sb.append("t2_");
			sb.append(word);
			sb.append('\t');
		}

		for (String word : compositeDoc.body_2grams) {
			sb.append("b2_");
			sb.append(word);
			sb.append('\t');
		}
		
		// NLP
		for (String word : compositeDoc.title_np) {
			sb.append("tnp_");
			sb.append(word);
			sb.append('\t');
		}
		for (String word : compositeDoc.title_nnp) {
			sb.append("tnnp_");
			sb.append(word);
			sb.append('\t');
		}
		for (String word : compositeDoc.title_NER_person) {
			sb.append("tep_");
			sb.append(word);
			sb.append('\t');
		}
		for (String word : compositeDoc.title_NER_location) {
			sb.append("tel_");
			sb.append(word);
			sb.append('\t');
		}
		for (String word : compositeDoc.title_NER_organization) {
			sb.append("teo_");
			sb.append(word);
			sb.append('\t');
		}
		
		for (ItemFeature item : compositeDoc.feature_list) {
			if (item.type == shared.datatypes.FeatureType.NP) {
				sb.append("np_");
			} else if (item.type == shared.datatypes.FeatureType.NNP) {
				sb.append("nnp_");
			} else if (item.type == shared.datatypes.FeatureType.VB) {
				sb.append("vb_");
			} else if (item.type == shared.datatypes.FeatureType.PEOPLE) {
				sb.append("ep_");
			} else if (item.type == shared.datatypes.FeatureType.LOCATION) {
				sb.append("el_");
			} else if (item.type == shared.datatypes.FeatureType.ORGANIZATION) {
				sb.append("eo_");
			} else {
				continue;
			}
			sb.append(item.name);
			sb.append('\t');
		}
		
		return sb.toString();
	}
	
	

}
