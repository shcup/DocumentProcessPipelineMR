import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import DocProcess.CompositeDocSerialize;
import DocProcessClassification.DataAdapter.ClassifierInputAllNLPAdapter;
import DocProcessClassification.DataAdapter.ClassifierInputTarget;
import DocProcessClassification.PatternMatch.URLPrefixPatternMatch;
import pipeline.CompositeDoc;

public class InvertIndexBuilderMapReduce {
    public static class Map extends  Mapper<Object, Text, Text, Text>
    {
  	
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
		    
		    HashMap<String, Integer> word_map = new HashMap<String, Integer>();
		    int total_word_count = 0;
		    if (compositeDoc.title_words != null) {
		    	for (String word : compositeDoc.title_words) {
		    		if (!word.isEmpty() && !word.equals(".") && !word.equals("\t") && !word.equals("|")) {
		    			Integer word_map_value = word_map.get(word);
		    			Integer new_word_map_value = word_map_value == null ? 1 : word_map_value + 1;
		    			word_map.put(word, new_word_map_value);
		    			total_word_count++;
		    			//context.write(new Text(word), new Text(segments[0]));
		    		}
		    	}
		    }
		    if (compositeDoc.body_words != null) {
		    	for (String word : compositeDoc.body_words) {
		    		if (!word.isEmpty() && !word.equals(".") && !word.equals("\t") && !word.equals("|")) {
		    			Integer word_map_value = word_map.get(word);
		    			Integer new_word_map_value = word_map_value == null ? 1 : word_map_value + 1;
		    			word_map.put(word, new_word_map_value);
		    			total_word_count++;
		    			//context.write(new Text(word), new Text(segments[0]));
		    		}
		    	}
		    }
		    
		    Iterator<Entry<String, Integer>> iter = word_map.entrySet().iterator();
		    while(iter.hasNext()) {
		    	Entry<String, Integer> entry = iter.next();
		    	StringBuilder sb = new StringBuilder();
		    	sb.append(compositeDoc.doc_id);
		    	sb.append(':');
		    	sb.append(entry.getValue());
		    	sb.append(':');
		    	sb.append(total_word_count);
		    	
		    	context.write(new Text(entry.getKey()), new Text(sb.toString()));
		    }
		} 
    }
    
	public static class Reduce extends Reducer<Text, Text, Text, Text>
	{
		class DocInfo implements Comparable {
			public long id;
			public int word_count;
			public int total_word_count;
			@Override
			public int compareTo(Object o) {
				// TODO Auto-generated method stub
				DocInfo cmp = (DocInfo)o;
				if (id < cmp.id) {
					return -1;
				} else if (id > cmp.id) {
					return 1;
				} else {
					return 0;
				}
			}
			
		}
		public void reduce(Text key, Iterable<Text> values,Context context)
				throws IOException, InterruptedException
		{
			List<DocInfo> list = new ArrayList<DocInfo>();
			float max_tf = 0.0f;
			for (Text text : values) {
				String line = text.toString();
				String[] items = line.split(":");
				if (items.length != 3) {
					System.exit(-2);
				}
				DocInfo doc = new DocInfo();
				doc.id = Long.parseLong(items[0]);
				doc.word_count = Integer.parseInt(items[1]);
				doc.total_word_count = Integer.parseInt(items[2]);
				list.add(doc);
				if (doc.word_count/(float)doc.total_word_count > max_tf) {
					max_tf = doc.word_count/(float)doc.total_word_count;
				}
			}
			
			Collections.sort(list);
			
			StringBuilder sb = new StringBuilder();
			sb.append(list.size());
			sb.append('\t');
			sb.append(max_tf);
			sb.append('\t');
			int list_length = 0;
			for (int i = 0; i < list.size(); ++i) {
				sb.append(list.get(i).id);
				sb.append(':');
				sb.append(list.get(i).word_count);
				sb.append(':');
				sb.append(list.get(i).total_word_count);
				if (i != list.size() - 1) {
					sb.append(',');
				}
				list_length ++;			
			}
			
			if (list_length > 5000) {
				context.getCounter("custom", "too long list").increment(1);
				System.out.println(key.toString());
				return;
			}
			
			context.write(key, new Text(sb.toString()));
		}
	}
    
    public static void main(String[] args) throws Exception
    {
    	Configuration conf = new Configuration();

        String[] libjarsArr = args[2].split(",");
        for (int i = 0; i < libjarsArr.length; ++i) {
        	addTmpJar(libjarsArr[i], conf);
        }
    	Job job = new Job(conf, "Invert index builder mapreduce");
    	job.setJarByClass(ClassificationTrainingDataMapReduce.class);
    	job.setMapperClass(Map.class);
    	job.setReducerClass(Reduce.class);
    	job.setNumReduceTasks(100);
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
