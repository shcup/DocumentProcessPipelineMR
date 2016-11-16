package DocProcess.IDF;

import java.util.HashSet;

import pipeline.CompositeDoc;

public class IDFGenerator {
	public HashSet<String> GetItemList(CompositeDoc compositeDoc) {
		HashSet<String> res = new HashSet<String>();
		
		if (compositeDoc.title_words != null) {
			for (String item : compositeDoc.title_words) {
				res.add(item);
			}
		}
		if (compositeDoc.body_words != null) {
			for (String item : compositeDoc.body_words) {
				res.add(item);
			}
		}
		
		return res;
	}
}
