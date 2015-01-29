package com.fixdapp.one;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class TroubleInfo extends Activity {
	
	private final String DEFAULT_DESCRIPTION = "I don't think you are connected, Make sure that"
			+ " the device is plugged in and that your are paired with it";
	
	private String theDTC;
	private String description;
	private String threatLevel;
	private TextView DTCtitle, DTCinfo, DTCname, consequencesHeader;
    private ImageView thumbsUp;

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
			DTCname.setText("Nothing Here");
			description = DEFAULT_DESCRIPTION;
		} else {
			DTCname.setText(theDTC);
		}

		parseDTCInfo();
		changeColorForThreatLevel();
		
	}

    @Override
    protected void onResume() {
        super.onResume();
        changeColorForThreatLevel();
    }

    private void getViews(Typeface font) {
		DTCtitle = (TextView) findViewById(R.id.info_screen_title);
		DTCtitle.setTypeface(font);
        DTCname = (TextView) findViewById(R.id.the_dtc);
        DTCname.setTypeface(font);
		DTCinfo = (TextView) findViewById(R.id.description);
        consequencesHeader = (TextView) findViewById(R.id.consequences_header);
        thumbsUp = (ImageView) findViewById(R.id.thumbs_up);
	}
	
	private void parseDTCInfo() {
		if(description.length() > 0) {
			String[] results = description.split(",");
			StringBuilder resultBuilder = new StringBuilder();
			for(String result : results) {
                result = result.trim();
				resultBuilder.append("- " + Character.toUpperCase(result.charAt(0)) + result.substring(1) + '\n');
			}
            DTCinfo.setText(resultBuilder.toString());
		}
	}
	
	// TODO (rikin): Make sure to add colors to xml, rather than hard coded
	private void changeColorForThreatLevel() {
		if(threatLevel.length() > 0) {
			if("gray".equalsIgnoreCase(threatLevel)) {
				DTCname.setBackgroundColor(Color.BLACK);
                consequencesHeader.setVisibility(View.INVISIBLE);
                thumbsUp.setVisibility(View.INVISIBLE);
			} else if("yellow".equalsIgnoreCase(threatLevel)) {
				DTCname.setBackgroundColor(Color.parseColor("#FFCC00"));
                DTCinfo.setVisibility(View.VISIBLE);
                consequencesHeader.setVisibility(View.VISIBLE);
                thumbsUp.setVisibility(View.INVISIBLE);
			} else if("red".equalsIgnoreCase(threatLevel)) {
				DTCname.setBackgroundColor(Color.parseColor("#FF0033"));
                DTCinfo.setVisibility(View.VISIBLE);
                consequencesHeader.setVisibility(View.VISIBLE);
                thumbsUp.setVisibility(View.INVISIBLE);
			} else if("green".equalsIgnoreCase(threatLevel)) {
				DTCname.setBackgroundColor(Color.parseColor("#00A600"));
                DTCinfo.setVisibility(View.INVISIBLE);
                consequencesHeader.setVisibility(View.INVISIBLE);
                thumbsUp.setVisibility(View.VISIBLE);
			}
		}
	}


}
