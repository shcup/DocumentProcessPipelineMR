import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import DocProcess.CompositeDocSerialize;
import DocProcess.IDF.IDFGenerator;
import DocProcessClassification.DataAdapter.ClassifierInputAllNLPAdapter;
import DocProcessClassification.DataAdapter.ClassifierInputAllNLP_Adapter;
import DocProcessClassification.DataAdapter.ClassifierInputTarget;
import DocProcessClassification.PatternMatch.URLPrefixPatternMatch;
import pipeline.CompositeDoc;
import DocProcessUtil.Stopword;

public class ClassificationTrainingDataMatchMRnewHindi {
    public static class Map extends  Mapper<Object, Text, Text, Text>
    {
    	/*URLPrefixPatternMatch prefixMatch = null;
    	public void setup(org.apache.hadoop.mapreduce.Mapper.Context context) throws IOException, InterruptedException {
    		prefixMatch = new URLPrefixPatternMatch();
    		
    		String path = "pattern.txt";
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
    		
    		prefixMatch.Load(is);
    	}*/
    	
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException
		{
			System.out.println("begin to mapper");
		    String line = value.toString();
		    String[] segments = line.split("\t");
		    if (segments.length != 2) {
		    	context.getCounter("custom", "input column error").increment(1);
		    	return;
		    }
		    
		    CompositeDoc compositeDoc = CompositeDocSerialize.DeSerialize(segments[1], context);
		    
	    	ClassifierInputTarget inputAdapter = new ClassifierInputAllNLPAdapter();
	    	String res = inputAdapter.GetInputText(compositeDoc);
			//String restext = replaceSpecStr(res);
			
			StringBuilder sb = new StringBuilder();
			String[] wordlist = null;
			//wordlist = restext.split(" ");
			wordlist = res.split(" ");
			DocProcessUtil.StopwordHindi stopWord = new DocProcessUtil.StopwordHindi() ;
			for(String word:wordlist){
			if(stopWord.IsStopWord(word)){
				sb.append("");
			}else
				sb.append(word).append(" ");
			} 
			String text = sb.toString();
//	    	String label = prefixMatch.GetMatchedPatternLabel(compositeDoc.doc_url);
	    	String label = null;
	    	 if (    compositeDoc.media_doc_info.normalized_category_info != null && 
			    		compositeDoc.media_doc_info.normalized_category_info.category_item != null &&
			    		compositeDoc.media_doc_info.normalized_category_info.category_item.size() != 0 &&
			    		compositeDoc.media_doc_info.normalized_category_info.category_item.get(0).category_path !=null &&
			    		compositeDoc.media_doc_info.normalized_category_info.category_item.get(0).category_path.size() !=0){
	    	label = compositeDoc.media_doc_info.normalized_category_info.category_item.get(0).category_path.get(0);
	    		context.getCounter("custom", "Get prefix label").increment(1);;
	    	} else {
	    		label = "NoMatch";
	    		context.getCounter("custom", "Empty label").increment(1);;
	    	}
	    	context.write(new Text(compositeDoc.doc_url), new Text(compositeDoc.media_doc_info.id+"\t"+label + "\t" + text));
		
		}
    }
    
	/*public static String replaceSpecStr(String orgStr){  
	    if (null!=orgStr&&!"".equals(orgStr.trim())) {  
	        String regEx="[~��`!��@#��$%^����&*��()��\\����\\=+��\\[\\]����{}��\\|��\\\\��;��:��'����\"��,��<��.��>��/��?^[0-9]]";  
	        Pattern p = Pattern.compile(regEx);  
	        Matcher m = p.matcher(orgStr);  
	        return m.replaceAll(" ");  
	    }  
	    return null;  
	}*/
    public static void main(String[] args) throws Exception
    {
    	Configuration conf = new Configuration();

        String[] libjarsArr = args[2].split(",");
        for (int i = 0; i < libjarsArr.length; ++i) {
        	addTmpJar(libjarsArr[i], conf);
        }
    	Job job =  Job.getInstance(conf, "Classification training");
    	job.setJarByClass(ClassificationTrainingDataMatchMRnewHindi.class);
    	job.setMapperClass(Map.class);
    	job.setNumReduceTasks(0);
    	job.setOutputKeyClass(Text.class);
    	job.setOutputValueClass(Text.class);

    	
    	FileInputFormat.addInputPath(job, new Path(args[0]));
    	FileOutputFormat.setOutputPath(job, new Path(args[1]));

    	System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
    
	/**
	 * ΪMapreduce��ӵ�����jar��
	 * 
	 * @param jarPath
	 *            ������D:/Java/new_java_workspace/scm/lib/guava-r08.jar
	 * @param conf
	 * @throws IOException
	 */
	public static void addTmpJar(String jarPath, Configuration conf) throws IOException {
		System.setProperty("path.separator", ":");
		FileSystem fs = FileSystem.getLocal(conf);
		String newJarPath = new Path(jarPath).makeQualified(fs).toString();
		String tmpjars = conf.get("tmpjars");
		if (tmpjars == null || tmpjars.length() == 0) {
			conf.set("tmpjars", newJarPath);
		} else {
			conf.set("tmpjars", tmpjars + "," + newJarPath);
		}
	}
}
