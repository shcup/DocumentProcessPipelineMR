
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

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

import DocProcessClassification.DataAdapter.ClassifierInputAllNLPAdapter;
import DocProcessClassification.DataAdapter.ClassifierInputTarget;
import leso.media.ImageTextDoc;
import pipeline.CompositeDoc;

// this maprecude program is based on hadoop 2.6, the low version is not supported
public class DocumentProcess {

	
    public static class Map extends  Mapper<Object, Text, Text, Text>
    {

		private final static IntWritable one = new IntWritable(1);
		private Text word = new Text();
		private CompositeDocTextProcess textProcess;
		private CompositeDocNLPProcess nlpProcess;
		
		public void setup(org.apache.hadoop.mapreduce.Mapper.Context context) throws IOException, InterruptedException {
			System.out.println("begin to setup function!");
			try {
				textProcess = new CompositeDocTextProcess();
				//nlpProcess = new CompositeDocNLPProcess();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}

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
		    
		    //deserlize the thrift 
		    CompositeDoc compositeDoc = new CompositeDoc();
		    byte[] data = Base64.decodeBase64(segments[1].getBytes());
		    if(data.length < 0) {
		    	context.getCounter("custom", "base64 decode failed").increment(1);
		        return;
		    }	    
		    try {
		    	TMemoryBuffer buffer = new TMemoryBuffer(data.length);
		    	buffer.write(data);
			    TBinaryProtocol b = new TBinaryProtocol(buffer);
			    compositeDoc.read(b);
		    } catch(Exception e) {
		    	context.getCounter("custom", "deserial exception").increment(1);
		    }
		    
		    if (false) {
		    	textProcess.Process(compositeDoc);
		    	nlpProcess.Process(compositeDoc);
		    	ClassifierInputTarget inputAdapter = new ClassifierInputAllNLPAdapter();
		    	String res = inputAdapter.GetInputText(compositeDoc);
		    }

		    // document process logic
		    //double t1 = System.currentTimeMillis();
		    //textProcess.Process(compositeDoc);
		    //double t2 = System.currentTimeMillis();
		    textProcess.Process(compositeDoc);
		    //double t3 = System.currentTimeMillis();
		    //System.out.println((t2 - t1) + " time " + (t3 - t2));
		    //nlpProcess.Process(compositeDoc);

		    
		    
		    // serialize the thrift
		    TMemoryBuffer mb;
		    try {
		    	mb = new TMemoryBuffer(32);
		    	TBinaryProtocol proto = new org.apache.thrift.protocol.TBinaryProtocol(mb);
		    	compositeDoc.write(proto);
		    } catch (Exception e) {
		    	context.getCounter("custom", "serial exception").increment(1);
		    	return;
		    }
		    byte[] base64buffer = Base64.encodeBase64(mb.getArray());
		    String output_composite_doc = new String(base64buffer);
		    context.write(new Text(segments[0]), new Text(output_composite_doc));
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
    	job.setCombinerClass(Reduce.class);
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