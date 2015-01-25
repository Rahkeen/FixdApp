package com.fixdapp.one.requests;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by rikin on 1/21/15.
 */
public class DisplayProtocolRequest extends OBDRequest {

    public DisplayProtocolRequest(InputStream in, OutputStream out) {
        super(in, out, "ATDP", "DisplayProtocol");
    }
}
