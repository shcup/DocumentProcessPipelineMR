package DocProcessClassification.PatternMatch;

import java.io.IOException;
import java.io.InputStream;

import DocProcessUtil.Trie;

public class URLPrefixPatternMatch {
	private Trie patterns = new Trie();
	
	public void Load(String patternFile) throws IOException {
		patterns.Load(patternFile);
	}
	
	public void Load(InputStream input) throws IOException {
		patterns.Load(input);
	}
	
	public String GetMatchedPatternLabel(String URL) {
		String url = patterns.URLPreProcess(URL);
		return patterns.FindFirstMatch(url);
	}
}
