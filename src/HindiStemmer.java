

import java.util.*;

/**
 * Stemmer for hindi language based on paper by Ramanathan
 * @author Chandrakant
 *
 */
public class HindiStemmer {

	private  Map<Integer,List<String>> suffixesMap =  new HashMap<Integer, List<String>>();
	
	public HindiStemmer(){
		suffixesMap.put(1, new ArrayList<String>(
				Arrays.asList("ो", "े", "ू", "ु", "ी", "ि", "ा")
		));
		suffixesMap.put(2, new ArrayList<String>(
				Arrays.asList("कर", "ाओ", "िए", "ाई", "ाए", "ने", "नी", "ना", "ते", "ीं", "ती", "ता", "ाँ", "ां", "ों", "ें")
		));
		suffixesMap.put(3, new ArrayList<String>(
				Arrays.asList("ाकर", "ाइए", "ाईं", "ाया", "ेगी", "ेगा", "ोगी", "ोगे", "ाने", "ाना", "ाते", "ाती", "ाता", "तीं", "ाओं", "ाएं", "ुओं", "ुएं", "ुआं")
		));
		suffixesMap.put(4, new ArrayList<String>(
				Arrays.asList("ाएगी", "ाएगा", "ाओगी", "ाओगे", "एंगी", "ेंगी", "एंगे", "ेंगे", "ूंगी", "ूंगा", "ातीं", "नाओं", "नाएं", "ताओं", "ताएं", "ियाँ", "ियों", "ियां")
		));
		suffixesMap.put(5, new ArrayList<String>(
				Arrays.asList("ाएंगी", "ाएंगे", "ाऊंगी", "ाऊंगा", "ाइयाँ", "ाइयों", "ाइयां")
		));
		
	}
	
	public  String stem(String word){
		for(int i = 5; i >0; i--){
			if(word.length() > i + 1){
				for(String suffix : suffixesMap.get(i)){
					if(word.endsWith(suffix)){
						return word.substring(0, word.length() - i);
					}
				}
			}
		}
		return word;
	}
	public static void main(String[] args) {
		HindiStemmer hindiStemmerRamanathan = new HindiStemmer();
		String word = "ठीकेदारों";
		System.out.println("length " + word.length());
		System.out.println(hindiStemmerRamanathan.stem(word));
	}
}
