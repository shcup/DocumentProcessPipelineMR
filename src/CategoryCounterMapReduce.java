import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.util.GenericOptionsParser;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TMemoryBuffer;
//import org.apache.commons.cli.Options;

import DocProcess.CompositeDocSerialize;
import leso.media.ImageTextDoc;
import pipeline.CompositeDoc;
import pipeline.basictypes.CategoryItem;

// this maprecude program is based on hadoop 2.6, the low version is not supported
public class CategoryCounterMapReduce {

	
    public static class Map extends  Mapper<Object, Text, Text, Text>
    {
		private final static IntWritable one = new IntWritable(1);
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
		    
		    StringBuilder sb = new StringBuilder();
		    if (    compositeDoc.media_doc_info.normalized_category_info != null && 
		    		compositeDoc.media_doc_info.normalized_category_info.category_item != null &&
		    		compositeDoc.media_doc_info.normalized_category_info.category_item.size() != 0) {
		    	context.getCounter("custom", "Matched").increment(1);	    	
		    } else {
		    	context.getCounter("custom", "NoMatch").increment(1);	
		    }	
		    
		    if (    compositeDoc.media_doc_info.normalized_category_info != null && 
		    		compositeDoc.media_doc_info.normalized_category_info.category_item != null &&
		    		compositeDoc.media_doc_info.normalized_category_info.category_item.size() != 0){
		    for(int i=0;i<compositeDoc.media_doc_info.normalized_category_info.category_item.size();i++){
		    	//String category=compositeDoc.media_doc_info.normalized_category_info.category_item.get(i).toString();
		    	String str=compositeDoc.media_doc_info.normalized_category_info.category_item.get(i).category_path.get(0);
		    	sb.append(str).append("\t").append(compositeDoc.domain);
		    }
		    }
//		    context.write(new Text(segments[0]), new Text(CompositeDocSerialize.Serialize(compositeDoc, context)));
		    context.write(new Text(compositeDoc.doc_url), new Text(sb.toString()));

		}
    }
	public static class Reduce extends Reducer<Text, Text, Text, Text>
	{
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
    	
        String[] libjarsArr = args[2].split(",");
        for (int i = 0; i < libjarsArr.length; ++i) {
        	addTmpJar(libjarsArr[i], conf);
        }
    	Job job = new Job(conf, "Stanford NLP process");
    	job.setJarByClass(CategoryCounterMapReduce.class);
    	job.setMapperClass(Map.class);;
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