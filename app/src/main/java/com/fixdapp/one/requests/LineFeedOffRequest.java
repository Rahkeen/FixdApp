package com.fixdapp.one.requests;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by rikin on 1/21/15.
 */
public class LineFeedOffRequest extends OBDRequest {

    public LineFeedOffRequest(InputStream in, OutputStream out) {
        super(in, out, "ATL0", "LineFeedOff");
    }
}
