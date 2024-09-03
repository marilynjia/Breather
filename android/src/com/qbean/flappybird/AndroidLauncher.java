package com.qbean.flappybird;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.qbean.breatherlib.BreatherActivity;

public class AndroidLauncher extends AndroidApplication {

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new FlappyBird(), config);

		if (!BreatherActivity.isConfigured()) {
			BreatherActivity.setMainActivity(this);
			BreatherActivity.configureBreather(this, false);
		}
	}


}
