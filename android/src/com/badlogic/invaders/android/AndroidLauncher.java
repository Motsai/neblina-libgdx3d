package com.badlogic.invaders.android;

import android.content.Intent;
import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.invaders.Invaders;

public class AndroidLauncher extends AndroidApplication {
	AndroidGetQ invaderInterface = new AndroidGetQ();

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		Invaders invaders = new Invaders(invaderInterface); //Why can't I call an invaderInterface here...
		initialize(invaders, config);

		Intent intent = new Intent(this, BLEDeviceScanActivity.class);
//		intent.putExtra("invaders",InvadersParcle);
		startActivity(intent);



	}




}
