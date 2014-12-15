package com.fixd.requests;

import java.io.InputStream;
import java.io.OutputStream;

public class VINRequest extends OBDRequest {
	
	public VINRequest(InputStream in, OutputStream out) {
		super(in, out, "0902", "VINRequest");
	}
		
}
