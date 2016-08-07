package DocProcessCluster.DocSimilarity;

import pipeline.CompositeDoc;

public interface ICompositeDocSimilarityGenerator {
	double Get(CompositeDoc doc1, CompositeDoc doc2);
}
