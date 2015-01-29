package com.fixdapp.one.requests;

import java.io.InputStream;
import java.io.OutputStream;

public class ResetRequest extends OBDRequest {

	public ResetRequest(InputStream in, OutputStream out) {
		super(in, out, "ATZ", "reset");
	}

}
