package TextRank;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;


public class TextRank
{
	 

	//	static{
	//		try {
	//			List<String> text=FileUtils.readLines(new File("stopWords.txt"),"utf-8");
	//			stopWords.addAll(text);
	//		} catch (IOException e) {
	//			// TODO Auto-generated catch block
	//			e.printStackTrace();
	//		}
	//	}
	//	public static final int nKeyword = 10;
	/**
	 * 闃诲凹绯绘暟锛堬激锝侊綅锝愶綁锝庯絿锛︼絹锝冿拷?锝旓綇锝掞級锛屼竴鑸彇鍊间负0.85
	 */
	static final float d = 0.85f;
	/**
	 * 锟�锟斤拷杩唬娆℃暟
	 */
	static final int max_iter = 200;
	static final float min_diff = 0.001f;

	/**
	 * 灏嗘枃绔犲垎鍓蹭负鍙ュ瓙
	 * @param document
	 * @return
	 */
	static List<String[]> spiltSentence(List<String> document)
	{
		List<String[]> sentences = new ArrayList<String[]>();
		if (document == null) return sentences;
		for (String line : document)
		{
			line = line.trim();
			if (line.length() == 0) continue;
			for (String sent : line.split("[,.;!?]"))
			{
				sent = sent.trim();
				if (sent.length() == 0) continue;
				String[] ss=sent.split(" |\t|\n");
				for(int i=0;i<ss.length;i++){
					ss[i]=modifyWord(ss[i]);
				}
				sentences.add(ss);
			}
		}

		return sentences;
	}
	public boolean filterWord(String word,DocProcessUtil.Stopword stopWord){
		if(stopWord.IsStopWord(word)){
			return false;
		}
		Pattern p=Pattern.compile("^[\\-0-9]*$");
		Matcher m=p.matcher(word);
		if(m.find()){
			return false;
		}
		return true;		
	}
	public static String modifyWord(String word){
		// word="'the";
		word=word.replaceAll("'s$", "");
		String regEx="[\"`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
		word=word.replaceAll(regEx, "");
		return word.toLowerCase();

	}
	public Map<String, Set<String>> getWordsRelation(List<String[]> sentences,DocProcessUtil.Stopword stopWord){
		Map<String, Set<String>> words = new HashMap<String, Set<String>>();
		for(String[] termList:sentences){
			List<String> wordList = new ArrayList<String>();
			for (String t : termList)
			{
				if(filterWord(t, stopWord)){
					wordList.add(t);
				}
			}
			//        System.out.println(wordList);

			Queue<String> que = new LinkedList<String>();
			for (String w : wordList)
			{
				if (!words.containsKey(w))
				{
					words.put(w, new HashSet<String>());
				}
				que.offer(w);
				if (que.size() > 5)
				{
					que.poll();
				}

				for (String w1 : que)
				{
					for (String w2 : que)
					{
						if (w1.equals(w2))
						{
							continue;
						}

						words.get(w1).add(w2);
						words.get(w2).add(w1);
					}
				}
			}
		}
		return words;
	}
	public Map<String, Float> calWordsRank(Map<String, Set<String>> words){
		Map<String, Float> score = new HashMap<String, Float>();
		for (int i = 0; i < max_iter; ++i)
		{
			Map<String, Float> m = new HashMap<String, Float>();
			float max_diff = 0;
			for (Map.Entry<String, Set<String>> entry : words.entrySet())
			{
				String key = entry.getKey();
				Set<String> value = entry.getValue();
				m.put(key, 1 - d);
				for (String other : value)
				{
					int size = words.get(other).size();
					if (key.equals(other) || size == 0) continue;
					m.put(key, m.get(key) + d / size * (score.get(other) == null ? 0 : score.get(other)));
				}
				max_diff = Math.max(max_diff, Math.abs(m.get(key) - (score.get(key) == null ? 0 : score.get(key))));
			}
			score = m;
			if (max_diff <= min_diff) break;
		}
		return score;
	}
	public List<Entry<String,Float>> getTerms(List<Entry<String,Float>> listkeyWords,List<String[]> sentences){
		Map<String,Float> terms=new HashMap<String,Float>();
		Map<String,Float> keyWords=new HashMap<String,Float>();
		for(Entry<String,Float> entry:listkeyWords){
			keyWords.put(entry.getKey(), entry.getValue());
		}
		for(String[] words:sentences){
			int num=0;
			StringBuffer sb=new StringBuffer();
			float score=0;
			for(int i=0;i<words.length;i++){
				String word=words[i];				
				if(keyWords.containsKey(word)){
					sb.append(word).append(" ");
					score+=keyWords.get(word);
					num++;
				}else{
					if(num>1){

						terms.put(sb.toString(),score);

					}
					num=0;
					sb.setLength(0);
					score=0;
				}
			}
			if(num>1){
				terms.put(sb.toString(),score);
			}

		}		 
		List<Entry<String,Float>> result=new ArrayList<Entry<String,Float>>();

		result.addAll(terms.entrySet());
		return result;

	}
	public KeyWords getKeyword(List<String> document,int nKeyword,DocProcessUtil.Stopword stopWord)
	{
		List<String[]> sentences=spiltSentence(document);
		Map<String, Set<String>> words=getWordsRelation(sentences, stopWord);
		//        System.out.println(words);
		Map<String, Float> score = calWordsRank(words);		
		List<Map.Entry<String, Float>> entryList = new ArrayList<Map.Entry<String, Float>>(score.entrySet());
		Collections.sort(entryList, new Comparator<Map.Entry<String, Float>>()
				{
			@Override
			public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2)
			{
				return o2.getValue().compareTo(o1.getValue());
				//return (o1.getValue() - o2.getValue() > 0 ? -1 : 1);
			}
				});
		//        System.out.println(entryList);

		List<Entry<String,Float>> words1=new ArrayList<Entry<String,Float>>();
		List<Entry<String,Float>> words2=new ArrayList<Entry<String,Float>>();
		for (int i = 0; i < Math.min(2*nKeyword,entryList.size()); ++i)
		{
			if(i<nKeyword){
				words1.add(entryList.get(i));
			}
			words2.add(entryList.get(i));
		}

		List<Entry<String,Float>> terms=getTerms(words2,sentences);

		return new KeyWords(words1,terms);
	}


}