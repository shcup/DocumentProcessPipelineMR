
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
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
import DocProcess.CompositeDocSerialize;
import DocProcess.IDF.IDFGenerator;
import pipeline.CompositeDoc;


public class IDFMapReduce {

    public static class Map extends  Mapper<Object, Text, Text, Text>
    {
		@Override
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
		    
	    	IDFGenerator idfGenerator = new IDFGenerator();
	    	HashSet<String> items = idfGenerator.GetItemList(compositeDoc);
	    	for (Iterator<String> it = items.iterator(); it.hasNext();) {
	    		context.write(new Text(it.next()), new Text(segments[0]));
	    	}
		}
    }
    
	public static class Reduce extends Reducer<Text, Text, Text, IntWritable>
	{
		@Override
		public void reduce(Text key, Iterable<Text> values,Context context)
				throws IOException, InterruptedException
		{
			HashSet<String> count = new HashSet<String>();
			for (Text text : values) {
				count.add(text.toString());
			}
			context.write(key, new IntWritable(count.size()));
		}
	}
	
    public static void main(String[] args) throws Exception
    {
    	Configuration conf = new Configuration();
    	
        String[] libjarsArr = args[2].split(",");
        for (int i = 0; i < libjarsArr.length; ++i) {
        	addTmpJar(libjarsArr[i], conf);
        }
    	
    	Job job = new Job(conf, "IDF Mapreduce");
    	job.setJarByClass(IDFMapReduce.class);
    	job.setMapperClass(Map.class);
    	job.setMapOutputKeyClass(Text.class);
    	job.setMapOutputValueClass(Text.class);
    	//job.setCombinerClass(Reduce.class);
    	job.setReducerClass(Reduce.class);
    	job.setNumReduceTasks(30);
    	job.setOutputKeyClass(Text.class);
    	job.setOutputValueClass(IntWritable.class);

    	
    	FileInputFormat.addInputPath(job, new Path(args[0]));
    	FileOutputFormat.setOutputPath(job, new Path(args[1]));

    	System.exit(job.waitForCompletion(true) ? 0 : 1);

    }
    
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
