package DocProcessUtil;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.zip.GZIPInputStream;

/**
 * Just a copy of {@link Stopword} class for Hindi stopwords
 * @author Chandrakant
 *
 */
public class StopwordHindi {

	public StopwordHindi() throws IOException {
		String path = "stopwords_hindi.txt";
	    InputStream is = getClass().getClassLoader().getResourceAsStream(path);
	    if (is == null) {
	    	throw new IOException();
	    }
	    try {
	      if (path.endsWith(".gz"))
	        is = new GZIPInputStream(new BufferedInputStream(is));
	      else
	        is = new BufferedInputStream(is);
	    } catch (IOException e) {
	      System.err.println("CLASSPATH resource " + path + " is not a GZIP stream!");
	    }
		stopword = new HashSet<String>();
		BufferedReader br=new BufferedReader(new InputStreamReader(is));
        String line;
        while((line=br.readLine())!=null) {
            stopword.add(line);
        }
	}
    private HashSet<String> stopword = null;
    public boolean IsStopWord(String word) {
    	return stopword.contains(word);
    }
}
