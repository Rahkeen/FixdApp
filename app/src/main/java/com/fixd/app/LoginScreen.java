package com.fixd.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

public class LoginScreen extends Activity {
	
	private Button loginButton;
	private EditText loginName;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_screen_activity);
		
		init();

	}
	
	public void init() {
		loginButton = (Button) findViewById(R.id.login_button);
		loginName = (EditText) findViewById(R.id.login_name);
		
		loginButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(LoginScreen.this, FixdActivity.class);
				i.putExtra("USERNAME", loginName.getText().toString());
				startActivity(i);
				finish();
			}
		});
		
		loginName.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(!hasFocus) {
					hideKeyboard(v);
				}
				
			}
		});
	}
	
	public void hideKeyboard(View view) {
        InputMethodManager inputMethodManager =(InputMethodManager)getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
	
	
	

}
