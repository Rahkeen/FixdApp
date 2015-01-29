package com.fixdapp.one.requests;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by rikin on 1/21/15.
 */
public class HeadersOffRequest extends OBDRequest {

    public HeadersOffRequest (InputStream in, OutputStream out) {
        super(in, out, "ATH0", "HeadersOff");
    }

}
