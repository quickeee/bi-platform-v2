package org.pentaho.test.platform.engine.services;

import java.io.OutputStream;

import org.pentaho.platform.api.engine.IStreamingPojoComponent;

public class TestPojo2 implements IStreamingPojoComponent {

	protected OutputStream outputStream;
	protected String input1;
	
	public boolean execute() throws Exception {

		// this will generate a null pointer if input1 is null
		String output = input1+input1;

		// this will generate an exception is outputStream is null
		outputStream.write( output.getBytes() );
		outputStream.close();
		
		return true;
	}
	
	public void setInput1( String input1 ) {
		this.input1 = input1;
	}
	
	public void setOutputStream(OutputStream outputStream) {
		this.outputStream = outputStream;
	}

	public String getMimeType( ){
		return "text/text";
	}

	public boolean validate() throws Exception {
		return true;
	}

}
