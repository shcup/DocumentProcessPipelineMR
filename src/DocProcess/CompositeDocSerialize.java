package DocProcess;

import org.apache.commons.codec.binary.Base64;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TMemoryBuffer;

import pipeline.CompositeDoc;

public class CompositeDocSerialize {
	public static CompositeDoc DeSerialize(String line, Context context) {

	    
	    //deserlize the thrift 
	    CompositeDoc compositeDoc = new CompositeDoc();
	    byte[] data = Base64.decodeBase64(line.getBytes());
	    if(data.length < 0) {
	    	context.getCounter("custom", "base64 decode failed").increment(1);
	        return null;
	    }	    
	    try {
	    	TMemoryBuffer buffer = new TMemoryBuffer(data.length);
	    	buffer.write(data);
		    TBinaryProtocol b = new TBinaryProtocol(buffer);
		    compositeDoc.read(b);
	    } catch(Exception e) {
	    	context.getCounter("custom", "deserial exception").increment(1);
	    }
	    
	    return compositeDoc;
	}
	
	public static Text Serialize(CompositeDoc compositeDoc, Context context) {
	    // serialize the thrift
	    TMemoryBuffer mb;
	    try {
	    	mb = new TMemoryBuffer(32);
	    	TBinaryProtocol proto = new org.apache.thrift.protocol.TBinaryProtocol(mb);
	    	compositeDoc.write(proto);
	    } catch (Exception e) {
	    	context.getCounter("custom", "serial exception").increment(1);
	    	return null;
	    }
	    byte[] base64buffer = Base64.encodeBase64(mb.getArray());
	    String output_composite_doc = new String(base64buffer);
	    return new Text(output_composite_doc);
	}
}
