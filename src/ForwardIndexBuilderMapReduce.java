import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import DocProcess.CompositeDocSerialize;
import pipeline.CompositeDoc;

public class ForwardIndexBuilderMapReduce {

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
		    		if (!word.isEmpty() && word != ".") {
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
		    		if (!word.isEmpty() && word != ".") {
		    			Integer word_map_value = word_map.get(word);
		    			Integer new_word_map_value = word_map_value == null ? 1 : word_map_value + 1;
		    			word_map.put(word, new_word_map_value);
		    			total_word_count++;
		    			//context.write(new Text(word), new Text(segments[0]));
		    		}
		    	}
		    }
		    
		    Iterator<Entry<String, Integer>> iter = word_map.entrySet().iterator();
		    StringBuilder sb = new StringBuilder();
		    sb.append(total_word_count);
		    sb.append('\t');
		    while(iter.hasNext()) {
		    	Entry<String, Integer> entry = iter.next(); 
		    	String temp = entry.getKey();
		    	temp = temp.replace(':', '|').replace(',', '|');
		    	
		    	sb.append(temp);
		    	sb.append(':');
		    	sb.append(entry.getValue());
		    	sb.append(',');
		    	/*sb.append(':');
		    	sb.append(entry.getValue());
		    	sb.append(':');
		    	sb.append(total_word_count);*/
		    }
		    context.write(new Text(String.valueOf(compositeDoc.doc_id)), new Text(sb.toString()));
		} 
    }
    
    public static void main(String[] args) throws Exception
    {
    	Configuration conf = new Configuration();

        String[] libjarsArr = args[2].split(",");
        for (int i = 0; i < libjarsArr.length; ++i) {
        	addTmpJar(libjarsArr[i], conf);
        }
    	Job job = new Job(conf, "Forward index builder mapreduce");
    	job.setJarByClass(ForwardIndexBuilderMapReduce.class);
    	job.setMapperClass(Map.class);
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
