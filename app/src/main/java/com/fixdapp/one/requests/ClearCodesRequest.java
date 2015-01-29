package com.fixdapp.one.requests;

import java.io.InputStream;
import java.io.OutputStream;

public class ClearCodesRequest extends OBDRequest {

	public ClearCodesRequest(InputStream in, OutputStream out, String code,
			String desc) {
		super(in, out, "04", "ClearCodes");
		// TODO Auto-generated constructor stub
	}

}
