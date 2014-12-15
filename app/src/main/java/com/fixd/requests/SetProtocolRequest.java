package com.fixd.requests;

import java.io.InputStream;
import java.io.OutputStream;

public class SetProtocolRequest extends OBDRequest{

	public SetProtocolRequest(InputStream in, OutputStream out) {
		super(in, out, "ATSP0", "set-protocol");
		// TODO Auto-generated constructor stub
	}

}
