
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import edu.stanford.nlp.util.Pair;
import pipeline.CompositeDoc;
import shared.datatypes.FeatureType;
import shared.datatypes.ItemFeature;

public class CompositeDocNLPProcess {
	
	ShiftReduceParserWraper srparser;
	NERLibrary nl;
	
	public CompositeDocNLPProcess() throws ClassCastException, ClassNotFoundException, IOException {
		srparser = new ShiftReduceParserWraper();
		nl = new NERLibrary();
		nl.NERLibrary(3);
	}
	
	public void Process(CompositeDoc compositeDoc) {
		NLPParserProcess(compositeDoc);
		//NLPNERProcess(compositeDoc);
	}
	
	private void NLPParserProcess(CompositeDoc compositeDoc) {
		
		compositeDoc.feature_list = new ArrayList<ItemFeature>();
		
		HashMap<String, MatchType> np_hashmap = new HashMap<String, MatchType>(); 
		HashMap<String, MatchType> nnp_hashmap = new HashMap<String, MatchType>(); 
		HashMap<String, MatchType> vb_hashmap = new HashMap<String, MatchType>();

		srparser.ParagraphPhraseParse(compositeDoc.title, 0, np_hashmap, nnp_hashmap, vb_hashmap);
		if (compositeDoc.short_desc != null && !compositeDoc.short_desc.isEmpty()) {
			srparser.ParagraphPhraseParse(compositeDoc.short_desc, 1, np_hashmap, nnp_hashmap, vb_hashmap);
		}
		// write to the composteDoc
		compositeDoc.title_np = new ArrayList<String>();
		for (Entry<String, MatchType> entry : np_hashmap.entrySet()) {
			compositeDoc.title_np.add(entry.getKey());
		}
		compositeDoc.title_nnp = new ArrayList<String>();
		for (Entry<String, MatchType> entry : nnp_hashmap.entrySet()) {
			compositeDoc.title_nnp.add(entry.getKey());
		}
		
		int idx = 1;
		if (compositeDoc.main_text_list != null) {
			for (String body_paragraph : compositeDoc.main_text_list) {
				srparser.ParagraphPhraseParse(body_paragraph, idx, np_hashmap, nnp_hashmap, vb_hashmap);
				idx = idx + 1;
			}
		}
		
		ArrayList<Pair<String, Double>> weight = new ArrayList<Pair<String, Double>>();
		ElementWeightCalculate(np_hashmap, weight, compositeDoc.title_np, compositeDoc.body_np);
		AddWeight2CompositeDoc(weight, compositeDoc, shared.datatypes.FeatureType.NP);
		
		weight.clear();
		ElementWeightCalculate(nnp_hashmap, weight, compositeDoc.title_nnp, compositeDoc.body_nnp);
		AddWeight2CompositeDoc(weight, compositeDoc, shared.datatypes.FeatureType.NNP);
		
		weight.clear();
		ElementWeightCalculate(vb_hashmap, weight, null, null);
		AddWeight2CompositeDoc(weight, compositeDoc, shared.datatypes.FeatureType.VB);
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
			if (intitle == true && title != null) {
				title.add(item.getKey().toLowerCase());
			}
			if (inbody == true && body != null) {
				body.add(item.getKey().toLowerCase());
			}
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


	private void NLPNERProcess(CompositeDoc compositeDoc) {
		List<Object> ner = new ArrayList<Object>();
		HashMap<String, MatchType> entitys_people = new HashMap<String, MatchType>();
		HashMap<String, MatchType> entitys_location = new HashMap<String, MatchType>();
		HashMap<String, MatchType> entitys_organization = new HashMap<String, MatchType>();
		
		nl.GetEntity(compositeDoc.title , 0, entitys_people, entitys_location, entitys_organization);
		if (compositeDoc.short_desc != null && !compositeDoc.short_desc.isEmpty()) {
			nl.GetEntity(compositeDoc.short_desc, 1, entitys_people, entitys_location, entitys_organization);
		}
		// write the NER to compositeDoc
		compositeDoc.title_NER_person = new ArrayList<String>();
		for (Entry<String, MatchType> entry : entitys_people.entrySet()) {
			compositeDoc.title_NER_person.add(entry.getKey());
		}
		compositeDoc.title_NER_location = new ArrayList<String>();
		for (Entry<String, MatchType> entry : entitys_location.entrySet()) {
			compositeDoc.title_NER_location.add(entry.getKey());
		}
		compositeDoc.title_NER_organization = new ArrayList<String>();
		for (Entry<String, MatchType> entry : entitys_organization.entrySet()) {
			compositeDoc.title_NER_organization.add(entry.getKey());
		}		
		int idx = 1;
		if (compositeDoc.main_text_list != null) {
			for (String body_paragraph : compositeDoc.main_text_list) {
				nl.GetEntity(body_paragraph, idx, entitys_people, entitys_location, entitys_organization);
			}
		}
		
		ArrayList<Pair<String, Double>> weight = new ArrayList<Pair<String, Double>>();
		ElementWeightCalculate(entitys_people, weight, compositeDoc.title_NER_person, compositeDoc.body_NER_person);
		AddWeight2CompositeDoc(weight, compositeDoc, shared.datatypes.FeatureType.PEOPLE);
		
		weight.clear();
		ElementWeightCalculate(entitys_location, weight, compositeDoc.title_NER_location, compositeDoc.body_NER_location);
		AddWeight2CompositeDoc(weight, compositeDoc, shared.datatypes.FeatureType.LOCATION);
		
		weight.clear();
		ElementWeightCalculate(entitys_organization, weight, compositeDoc.title_NER_organization, compositeDoc.body_NER_organization);
		AddWeight2CompositeDoc(weight, compositeDoc, shared.datatypes.FeatureType.ORGANIZATION);
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
}
