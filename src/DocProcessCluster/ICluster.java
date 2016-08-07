package DocProcessCluster;

import java.util.List;

import DocProcessCluster.DocSimilarity.ICompositeDocSimilarityGenerator;
import pipeline.CompositeDoc;

public interface ICluster {
	
	public void Cluster(List<CompositeDoc> docs);
}
