package DocProcessCluster;

import java.util.List;

import DocProcessCluster.DocSimilarity.ICompositeDocSimilarityGenerator;
import DocProcessCluster.DocSimilarity.TfIdfFullSimilarity;
import pipeline.CompositeDoc;

public class GraphCluster implements ICluster {

	ICompositeDocSimilarityGenerator similarity = null;
	public GraphCluster() {
		similarity = new TfIdfFullSimilarity();
	}
	@Override
	public void Cluster(List<CompositeDoc> docs) {
		// TODO Auto-generated method stub

	}

}
