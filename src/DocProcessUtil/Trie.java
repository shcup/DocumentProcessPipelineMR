package DocProcessUtil;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Trie {
	
	//static area
	public static int [] characterTable;
	public static int tableSize;
	
	static {
		int idx = 0;
		characterTable = new int [256];
		for (int i = 0; i < 256; ++i) {
			characterTable[i] = -1;
		}
		for (char c = 'a'; c <= 'z'; ++c ) {
			characterTable[c]=idx;
			idx++;
		}
		for (char c = '0'; c <= '9'; ++c) {
			characterTable[c]=idx;
			idx++;
		}
		characterTable['.'] = idx;
		idx++;
		characterTable['/'] = idx;
		idx++;
		
		tableSize = idx;
	}
	
	// trie node
	public class TrieNode {
		public char character;
		// if null, leav node, otherwise length is tableSize
		public TrieNode[] childList;
		public String value;
		
		public TrieNode () {
			childList = new TrieNode[tableSize];
			value = null;
		}
	}
	
	// member
	public ArrayList<TrieNode> nodes;
	
	public String URLPreProcess(String url) {
		String res = url.toLowerCase();
		if (res.startsWith("http://")) {
			return res.substring(7);
		} else if (res.startsWith("https://")) {
			return res.substring(8);
		}
		return  res;
	}
	
	public String FindFirstMatch(String URL) {
		Trie.TrieNode parentNode = nodes.get(0);
		String url = URLPreProcess(URL);
		for (int i = 0; i < url.length(); ++i) {
			int idx = characterTable[url.charAt(i)];
			if (idx == -1) {
				continue;
			}			
			
			if (parentNode.childList[idx] == null) {
				return null;
			}
			TrieNode curNode = parentNode.childList[idx];
			if (curNode.value != null) {
				return curNode.value;
			}
			parentNode = curNode;
		}
		return null;
	}

	public ArrayList<String> FindAllMatch(String URL) {
		ArrayList<String> res = new ArrayList<String>();
		Trie.TrieNode parentNode = nodes.get(0);		
		String url = URLPreProcess(URL);
		for (int i = 0; i < url.length(); ++i) {
			int idx = characterTable[url.charAt(i)];
			if (idx == -1) {
				continue;
			}			
			
			if (parentNode.childList[idx] == null) {
				return null;
			}
			TrieNode curNode = parentNode.childList[idx];
			if (curNode.value != null) {
				res.add(curNode.value);
			}
			parentNode = curNode;
		}
		return res;
	}

	
	private void AddOneString(String str, String value) {
		Trie.TrieNode parentNode = nodes.get(0);
		for (int i = 0; i < str.length(); ++i) {
			int idx = characterTable[str.charAt(i)];
			if (idx == -1) {
				continue;
			}
			
			if (parentNode.childList[idx] != null) {
				parentNode = parentNode.childList[idx];
			} else {
				TrieNode curNode = new TrieNode();
				curNode.character = str.charAt(i);
				parentNode.childList[idx] = curNode;
				parentNode = curNode;
			}			
		}
		parentNode.value = value;
	}
	
	public void Load(String file) throws IOException {
		if (file.isEmpty()) {
			return;
		}
		
		
		if (nodes != null) {
			nodes.clear();
		} else {
			nodes = new ArrayList<TrieNode> ();
		}

		
		BufferedReader br=new BufferedReader(new FileReader(file));
        String line;
        while((line=br.readLine())!=null) {
        	String[] items = line.split("\t");
        	if (items.length != 2) {
        		System.exit(-1);
        	}
        	AddOneString(items[0], items[1]);
        }
	}
	
}
