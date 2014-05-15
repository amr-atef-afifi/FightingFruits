package com.mandaGames.fightingfruits;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;

public class NewGame extends Activity
{
	Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_new_game);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		context = this;
		BluetoothConnection.establish_connection(context);
		new Thread(new Runnable()
		{

			@Override
			public void run()
			{
				while (true)
				{
					if (BluetoothConnection.is_ready && BluetoothConnection.connection_socket.read_next() == -2)
					{
						Intent game_intent = new Intent(context,
								GamePlayActivity.class);
						startActivity(game_intent);
						break;
					}
				}
			}
		}).start();
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.new_game, menu);
		return true;
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		finish();
	}
	

	
	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		BluetoothConnection.connection_socket.cancel();
		Intent launch_screen_intent = new Intent(this, LaunchScreen.class);
		startActivity(launch_screen_intent);
		finish();
	}

}
