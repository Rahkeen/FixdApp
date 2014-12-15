/**
 * 
 */
package com.fixd.requests;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Class for Mode 3 DTC Request. Gets the most recent stored DTC that was thrown,
 * Also formats the response for use on the client side.
 * 
 * @author Rikin Marfatia (rikin@fixdapp.com)
 *
 */
public class DTCRequest extends OBDRequest {
	
	private final String NONE = "none";

	public DTCRequest(InputStream in, OutputStream out) {
		super(in, out, "03", "DTCStore");
	} 
	
	@Override
	public String formatResponse() {
		String response = removeSpaces();
		if(response.length() <= 6) {
			return NONE;
		} else {
			response = convertHexToDTC(response.substring(4,8));
			return response;
		}
	}
	
	/**
	 * Converts the response into the proper DTC
	 * 
	 * (temporary, use hex comparisons in future)
	 * 
	 * @param response the obdii response format without spaces
	 * @return the correct DTC
	 */
	private String convertHexToDTC(String response) {  // TODO Make this a cleaner transformation
		StringBuilder convertedResponse = new StringBuilder();

		for(int i = 0; i < 1; i++) {
			if(response.charAt(i) == '0')
				convertedResponse.append("P0");
			else if(response.charAt(i) == '1')
				convertedResponse.append("P1");
			else if(response.charAt(i) == '2')
				convertedResponse.append("P2");
			else if(response.charAt(i) == '3')
				convertedResponse.append("P3");
			else if(response.charAt(i) == '4')
				convertedResponse.append("C0");
			else if(response.charAt(i) == '5')
				convertedResponse.append("C1");
			else if(response.charAt(i) == '6')
				convertedResponse.append("C2");
			else if(response.charAt(i) == '7')
				convertedResponse.append("C3");
			else if(response.charAt(i) == '8')
				convertedResponse.append("B0");
			else if(response.charAt(i) == '9')
				convertedResponse.append("B1");
			else if(response.charAt(i) == 'A')
				convertedResponse.append("B2");
			else if(response.charAt(i) == 'B')
				convertedResponse.append("B3");
			else if(response.charAt(i) == 'C')
				convertedResponse.append("U0");
			else if(response.charAt(i) == 'D')
				convertedResponse.append("U1");
			else if(response.charAt(i) == 'E')
				convertedResponse.append("U2");
			else
				convertedResponse.append("U3");
		}
		convertedResponse.append(response.substring(1));
		return convertedResponse.toString();
	}

}
