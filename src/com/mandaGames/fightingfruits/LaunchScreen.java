package com.mandaGames.fightingfruits;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;


public class LaunchScreen extends Activity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_launch_screen);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.launch_screen, menu);
        return true;
    }
    
    public void to_new_game(View view)
    {
    	Intent new_game_intent = new Intent(this, NewGame.class);
		startActivity(new_game_intent);
    }
    
    public void to_join_game(View view)
    {
    	Intent join_game_intent = new Intent(this, JoinGame.class);
		startActivity(join_game_intent);
    }
    
    public void exit(View view)
    {
    	finish();
    	System.exit(0);
    }
    
    @Override
    public void onBackPressed()
    {
    	finish();
    	System.exit(0);
    }
    
    @Override
	protected void onStop()
	{
		super.onStop();
		finish();
	}

   
}