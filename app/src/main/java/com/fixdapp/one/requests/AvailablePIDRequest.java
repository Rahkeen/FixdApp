package com.fixdapp.one.requests;

import java.io.InputStream;
import java.io.OutputStream;

public class AvailablePIDRequest extends OBDRequest {

	public AvailablePIDRequest(InputStream in, OutputStream out, String code,
			String desc) {
		super(in, out, "0100", "AvailCodes");
		
	}
	
	

}
