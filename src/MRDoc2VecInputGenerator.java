import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import DocProcess.CompositeDocSerialize;
import pipeline.CompositeDoc;

public class MRDoc2VecInputGenerator {

	
	
	public static class HBaseArticleExtractorMapper extends Mapper<Object, Text, Text, Text>{
		private Text outKey= new Text();
		private Text outValue=new Text();
		
		protected void map(Object key, Text value, Context context) throws IOException, InterruptedException{
			//key is mean to rowkey
			System.out.println("begin to mapper");
		    String line = value.toString();
		    String[] segments = line.split("\t");
		    if (segments.length != 2) {
		    	context.getCounter("custom", "input column error").increment(1);
		    	return;
		    }

		    outKey.set(segments[0]);
			List<String> res = new ArrayList<String>();
			CompositeDoc compositeDoc = CompositeDocSerialize.DeSerialize(segments[1], context);	
			
			StringBuilder sb = new StringBuilder();
			for (String word : compositeDoc.title_words) {
				if (!word.isEmpty() && !word.equals(".") && !word.equals("\t") && !word.equals("|")) {		
					sb.append(word);
					sb.append(' ');
				}
			}
			if (compositeDoc.body_words != null) {
				for (String word : compositeDoc.body_words) {
					if (!word.isEmpty() && !word.equals(".") && !word.equals("\t") && !word.equals("|")) {		
						sb.append(word);
						sb.append(' ');
					}
				}
			}
			outValue.set(sb.toString().trim());
			context.write(outKey, outValue);
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
    	job.setJarByClass(MRDoc2VecInputGenerator.class);
    	job.setMapperClass(HBaseArticleExtractorMapper.class);
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
