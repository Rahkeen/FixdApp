package com.fixd.app;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.widget.TextView;

public class TroubleInfo extends Activity {
	
	private final String DEFAULT_DESCRIPTION = "I don't think you are connected, Make sure that"
			+ " the device is plugged in and that your are paired with it";
	
	private String theDTC;
	private String description;
	private String threatLevel;
	private TextView DTCtitle, DTCinfo;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_trouble_info);
		
		Typeface font = Typeface.createFromAsset(getAssets(), "fonts/Exo-Regular.otf");
		
		getViews(font);
		
		Intent intent = getIntent();
		theDTC = intent.getStringExtra("DTC");
		description = intent.getStringExtra("INFO");
		threatLevel = intent.getStringExtra("COLOR");
		
		if(theDTC.equals("")) {
			DTCtitle.setText("Nothing Here");
			description = DEFAULT_DESCRIPTION;
		} else {
			DTCtitle.setText(theDTC);
		}
		
		parseDTCInfo();
		changeColorForThreatLevel();
		
	}
	
	private void getViews(Typeface font) {
		DTCtitle = (TextView) findViewById(R.id.the_dtc);
		DTCtitle.setTypeface(font);
		DTCinfo = (TextView) findViewById(R.id.description);
	}
	
	private void parseDTCInfo() {
		if(description.length() > 0) {
			description.trim();
			String[] results = description.split(",");
			StringBuilder resultBuilder = new StringBuilder();
			for(String result : results) {
				
				resultBuilder.append("- " + result.trim() + '\n');
			}
			DTCinfo.setText(resultBuilder.toString());
		}
	}
	
	// TODO (rikin): Make sure to add colors to xml, rather than hard coded
	private void changeColorForThreatLevel() {
		if(threatLevel.length() > 0) {
			if("gray".equalsIgnoreCase(threatLevel)) {
				// nothing right now
			} else if("yellow".equalsIgnoreCase(threatLevel)) {
				DTCtitle.setBackgroundColor(0xFFCC00);
			} else if("red".equalsIgnoreCase(threatLevel)) {
				DTCtitle.setBackgroundColor(0xFF0033);
			} else if("green".equalsIgnoreCase(threatLevel)) {
				DTCtitle.setBackgroundColor(0x254DAD);
			}
		}
	}


}
