package DocProcessClassification.PatternMatch;

import java.io.IOException;

import DocProcessUtil.Trie;

public class URLPrefixPatternMatch {
	private Trie patterns = new Trie();
	
	public void Load(String patternFile) throws IOException {
		patterns.Load(patternFile);
	}
	
	public String GetMatchedPatternLabel(String URL) {
		String url = patterns.URLPreProcess(URL);
		return patterns.FindFirstMatch(url);
	}
}
