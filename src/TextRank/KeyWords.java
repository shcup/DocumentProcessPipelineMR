package TextRank;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;

import shared.datatypes.ItemFeature;

/**
 * TextRank关键词提�? * @author hankcs
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
			result.add(itemFeature);
		}
		return result;
	}
	static public KeyWords getKeyWords(List<String> document,int num){
		return new TextRank().getKeyword(document,num);
	}
	public KeyWords(List<Entry<String,Float>> keyWords,List<Entry<String,Float>> keyTerms){
		 this.keyWords=keyWords;
		 this.keyTerms=keyTerms;
	}
	public static void main(String[] args) throws IOException
	{
		List<String> document = FileUtils.readLines(new File("text"));
		KeyWords keywords=KeyWords.getKeyWords(document, 10);

	}

}
