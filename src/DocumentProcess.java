
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import TextRank.TextRank;
import DocProcess.CompositeDocSerialize;
import DocProcessClassification.DataAdapter.ClassifierInputAllNLPAdapter;
import DocProcessClassification.DataAdapter.ClassifierInputTarget;
import DocProcessClassification.PatternMatch.URLPrefixPatternMatch;
import pipeline.CompositeDoc;
import pipeline.basictypes.CategoryItem;

// this maprecude program is based on hadoop 2.6, the low version is not supported
public class DocumentProcess {

	
    public static class Map extends  Mapper<Object, Text, Text, Text>
    {

		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();
		private CompositeDocTextProcess textProcess;
		private CompositeDocNLPProcess nlpProcess;
		URLPrefixPatternMatch prefixMatch = null;
		
		@Override
		public void setup(org.apache.hadoop.mapreduce.Mapper.Context context) throws IOException, InterruptedException {
			System.out.println("begin to setup function!");
			try {
				textProcess = new CompositeDocTextProcess();
				//nlpProcess = new CompositeDocNLPProcess();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
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
    		 //added by lujing
    		TextRank.loadStopWords("stopWords");
		}

		@Override
		public void map(Object key, Text value, Context context)
				throws IOException, InterruptedException
		{
			//System.out.println("begin to mapper");
		    String line = value.toString();
		    String[] segments = line.split("\t");
		    if (segments.length != 2) {
		    	context.getCounter("custom", "input column error").increment(1);
		    	return;
		    }
		    
		    CompositeDoc compositeDoc = CompositeDocSerialize.DeSerialize(segments[1], context);
		    
		    int res_status;
		    res_status = textProcess.Process(compositeDoc);
		    if ((res_status & 2) != 0) {
		    	//System.out.println("Doc url " + compositeDoc.doc_url);
		    	context.getCounter("custom", "Too long text").increment(1);;
		    }
		    
	    	String label = prefixMatch.GetMatchedPatternLabel(compositeDoc.doc_url);
	    	ClassifierInputTarget inputAdapter = new ClassifierInputAllNLPAdapter();
	    	String res = inputAdapter.GetInputText(compositeDoc);
	    	compositeDoc.classifier_input = res;
	    	if (label != null && !label.isEmpty()) {
	    		context.getCounter("custom", "Get prefix label").increment(1);;
	    	} else {
	    		context.getCounter("custom", "Empty label").increment(1);;
	    	}		    
	    	if (label != null) {
	    		if (compositeDoc.media_doc_info.normalized_category_info == null) {
	    			compositeDoc.media_doc_info.normalized_category_info = new pipeline.basictypes.CategoryInfo();
	    		}
	    		CategoryItem categoryItem = new CategoryItem();
	    		categoryItem.category_path = new ArrayList<String>();
	    		String[] full_category = label.split(":");
	    		for (int i = 0; i < full_category.length; ++i) {
	    			categoryItem.category_path.add(full_category[i]);
	    		}
	    		
	    		if (compositeDoc.media_doc_info.normalized_category_info.category_item == null) {
	    			compositeDoc.media_doc_info.normalized_category_info.category_item = new ArrayList<pipeline.basictypes.CategoryItem>();
	    		}
	    		compositeDoc.media_doc_info.normalized_category_info.category_item.add(categoryItem);	   			    		
	    	}
		    
		    context.write(new Text(segments[0]), new Text(CompositeDocSerialize.Serialize(compositeDoc, context)));

		}
    }
	public static class Reduce extends Reducer<Text, Text, Text, Text>
	{
		@Override
		public void reduce(Text key, Iterable<Text> values,Context context)
				throws IOException, InterruptedException
		{
			for (Text text : values) {
				context.write(key, text);
			}
		}
	}
    public static void main(String[] args) throws Exception
    {
    	Configuration conf = new Configuration();
    	
    	conf.set("type", "classifier_data");
    	conf.set("mapreduce.map.memory.mb", "5000");
    	conf.set("mapreduce.map.java.opts", "-Xmx4608m");
    	
    	/*String[] otherArgs = new GenericOptionsParser(conf, args).getRemainingArgs();
    	if (otherArgs.length != 2) {
    		System.err.println("Usage: wordcount <in> <out>");
    		System.exit(2);
    	}*/
        String[] libjarsArr = args[2].split(",");
        for (int i = 0; i < libjarsArr.length; ++i) {
        	addTmpJar(libjarsArr[i], conf);
        }
    	Job job = new Job(conf, "Stanford NLP process");
    	job.setJarByClass(DocumentProcess.class);
    	job.setMapperClass(Map.class);
    	//job.setCombinerClass(Reduce.class);
    	job.setReducerClass(Reduce.class);
    	job.setNumReduceTasks(0);
    	job.setOutputKeyClass(Text.class);
    	job.setOutputValueClass(Text.class);

    	
    	FileInputFormat.addInputPath(job, new Path(args[0]));
    	FileOutputFormat.setOutputPath(job, new Path(args[1]));

    	System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
    
	/**
	 * 为Mapreduce添加第三方jar包
	 * 
	 * @param jarPath
	 *            举例：D:/Java/new_java_workspace/scm/lib/guava-r08.jar
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