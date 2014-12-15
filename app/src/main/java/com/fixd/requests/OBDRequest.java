package com.fixd.requests;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class OBDRequest {
	protected InputStream inStream = null;
	protected OutputStream outStream = null;
	protected ArrayList<Byte> buffer = null;
	protected String command = null;
	protected String result = null;
	protected String description = null;
	
	public OBDRequest(InputStream in, OutputStream out, String code, String desc) {
		inStream = in;
		outStream = out;
		command = code + "\r";
		description = desc;
		buffer = new ArrayList<Byte>();
	}
	
	public void sendMessage() {
		try {
			outStream.write(command.getBytes());
			outStream.flush();
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	public void readResult() {
		byte b = 0;
		try {
			while((char)(b = (byte)inStream.read()) != '>') {
				buffer.add(b);
			}
		} catch(IOException e) {
			e.printStackTrace();
		}
	}
	
	public String rawResult() {
		String result = new String(getByteArray());
		return result;
	}
	
	public String formatResponse() {
		return removeSpaces();
	}
	
	public String removeSpaces() {
		String result = new String(getByteArray());
		StringBuilder formattedResult = new StringBuilder();

		// remove spaces
		for(int i = 0; i < result.length(); i++) {
			char c = result.charAt(i);
			if(c != ' ' && c != '\r' && c != '\t') {
				formattedResult.append(c);
			}
		}
		return formattedResult.toString();
	}

	public byte[] getByteArray() {
		byte[] response = new byte[buffer.size()];

		for(int i = 0; i < response.length; i++) {
			response[i] = buffer.get(i);
		}

		return response;
	}
	
	public String getCode() {
		return command;
	}
}
