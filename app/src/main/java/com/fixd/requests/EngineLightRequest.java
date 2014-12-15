package com.fixd.requests;

import java.io.InputStream;
import java.io.OutputStream;

public class EngineLightRequest extends OBDRequest{

	public EngineLightRequest(InputStream in, OutputStream out) {
		super(in, out, "0101", "CheckEngine");
	}
	
	@Override
	public String formatResponse() {
		String response = removeSpaces();
		StringBuilder finalResponse = new StringBuilder();
		String bite = response.substring(4,6);
		
		int[] results = new int[2];
		int mil = Integer.parseInt(bite, 16);
		
		results[0] = mil & 0x80;
		results[1] = mil & 0x7F;
		
		if(results[0] == 1) {
			finalResponse.append("ON,");
			finalResponse.append(results[1] + "");
		} else {
			finalResponse.append("OFF");
		}
		
		return finalResponse.toString();
	}

}
