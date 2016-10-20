package TextRank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import DocProcessUtil.Stopword;

//import org.apache.commons.io.FileUtils;

import shared.datatypes.ItemFeature;

/**
 * TextRank鍏抽敭璇嶆彁锟� * @author hankcs
 */

public class  KeyWords{
	public List<Entry<String,Float>> keyWords=new ArrayList<Entry<String,Float>>();
	public List<Entry<String,Float>> keyTerms=new ArrayList<Entry<String,Float>>();
	public List<ItemFeature> toItemFeature(List<Entry<String,Float>> keyWords){
		List<ItemFeature> result=new ArrayList<ItemFeature>();
		for(Entry<String,Float> e:keyWords){
			ItemFeature itemFeature=new ItemFeature();
			itemFeature.name=e.getKey();
			itemFeature.weight=(short) (e.getValue()*10000);
			itemFeature.type = shared.datatypes.FeatureType.LABEL;
			result.add(itemFeature);
		}
		return result;
	}
	static public KeyWords getKeyWords(List<String> document,int num, Stopword stopWord){
		return new TextRank().getKeyword(document,num,stopWord);
	}
	public KeyWords(List<Entry<String,Float>> keyWords,List<Entry<String,Float>> keyTerms){
		 this.keyWords=keyWords;
		 this.keyTerms=keyTerms;
	}
	public static void main(String[] args) throws IOException
	{
		//List<String> document = FileUtils.readLines(new File("text"));
		//KeyWords keywords=KeyWords.getKeyWords(document, 10);

	}

}
