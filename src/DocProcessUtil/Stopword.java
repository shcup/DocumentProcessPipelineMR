package DocProcessUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.zip.GZIPInputStream;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.Label;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;

public class Stopword {
	public static void main(String[] args) throws IOException {
		String context = "Euro 2016 Final: Yuvraj Singh, Chris Gayle, others celebrate Portugal's victory";
		Stopword st = new Stopword();
		ArrayList<String> wordlist = new ArrayList<String>();
		st.Deword(context, wordlist);
		st.print(wordlist);
	}

	public Stopword() throws IOException {
		String path = "stopwords.txt";
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
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		String line;
		while ((line = br.readLine()) != null) {
			stopword.add(line);
		}
	}

	private String sw = null;
	private HashSet<String> stopword = null;

	public boolean IsStopWord(String word) {
		return stopword.contains(word);
	}

	public void Deword(String context, ArrayList<String> wordlist) throws IOException {
		// TODO Auto-generated method stub
		// loading the stopwords.
		File f1 = new File("src\\stopwords.txt");
		try (BufferedReader br1 = new BufferedReader(new InputStreamReader(new FileInputStream(f1)))) {
			List<String> stopword = new ArrayList<String>();
			while ((sw = br1.readLine()) != null) {
				stopword.add(new String(sw));
			}
			TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
			Tokenizer<CoreLabel> tok = tokenizerFactory.getTokenizer(new StringReader(context));
			List<CoreLabel> sentence = tok.tokenize();
			for (Label lab : sentence) {
				wordlist.add(lab.value());
			}

			for (int i = 0; i < wordlist.size(); i++) {
				for (int j = 0; j < stopword.size(); j++) {
					if (wordlist.get(i).equals(stopword.get(j))) {
						// System.out.println(list.get(i)+" "+stop[j]+"ɾ���ɹ�");
						wordlist.remove(i);
						i--;
						break;
					}
				}
			}
		}

	}

	public void print(ArrayList<String> list) {
		for (int i = 0; i < list.size(); i++) {
			System.out.println("" + list.get(i));
		}
	}
}
