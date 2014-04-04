package dk.bigherman.android.rfkweather;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.widget.TextView;

public class HelpActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_help);
		
		Intent i = getIntent();
		boolean language_english = i.getBooleanExtra("language_english", true);
		
		TextView helpTextView = (TextView) findViewById(R.id.textViewHelp);
		if(language_english)
		{
			helpTextView.setText(R.string.help_text_eng);
		}
		else
		{
			helpTextView.setText(R.string.help_text_dan);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.help, menu);
		return true;
	}

}
