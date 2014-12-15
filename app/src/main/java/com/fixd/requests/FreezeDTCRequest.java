package com.fixd.requests;

import java.io.InputStream;
import java.io.OutputStream;

public class FreezeDTCRequest extends OBDRequest{

	public FreezeDTCRequest(InputStream in, OutputStream out) {
		super(in, out, "0102", "FreezeDTC");
		// TODO Auto-generated constructor stub
	}

}
