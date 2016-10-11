package DocProcess;

import pipeline.CompositeDoc;

public interface IDocProcessor {
	public int Process(CompositeDoc compositeDoc);
}
