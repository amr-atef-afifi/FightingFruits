package com.mandaGames.fightingfruits;

import java.util.Timer;
import java.util.TimerTask;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class GamePlayActivity extends Activity
{

	public static final int UPDATE_PERIOD = 10;
	private GamePlayView game_play_view;
	private ViewGroup layout;
	private Runnable game_main_thread;
	private OnTouchListener touch_listener;
	private TimerTask timer_task;
	private TextView score_1;
	private TextView score_2;
	private TextView freezing_count_down;
	private TextView game_timer;
	private ImageView target_fruit;
	private ImageView frozen_screen;
	private ImageView background;
	private ImageView player_state;
	private int finish_count_down;
	private boolean finished;
	private Context context ;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_game_play);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		context = this ;
		finished = false;

		finish_count_down = 500;
		score_1 = (TextView) findViewById(R.id.text_score_1);
		score_2 = (TextView) findViewById(R.id.text_score_2);
		game_timer = (TextView) findViewById(R.id.game_timer);

		freezing_count_down = (TextView) findViewById(R.id.freezing_count_down);
		frozen_screen = (ImageView) findViewById(R.id.frozen_screen);
		frozen_screen.setVisibility(ImageView.INVISIBLE);

		background = (ImageView) findViewById(R.id.game_background);
		target_fruit = (ImageView) findViewById(R.id.target_fruit);
		player_state = (ImageView) findViewById(R.id.player_state);

		game_play_view = new GamePlayView(this, 1000);

		touch_listener = new OnTouchListener()
		{
			@Override
			public boolean onTouch(View v, MotionEvent event)
			{
				if (event.getAction() == MotionEvent.ACTION_UP)
				{
					game_play_view.get_game_play().hit((int) event.getX(),
							(int) event.getY());
				}
				return false;
			}

		};

		game_play_view.setOnTouchListener(touch_listener);

		game_main_thread = new Runnable()
		{

			@Override
			public void run()
			{
				if (!finished)
				{
					game_play_view.update();

					if (game_play_view.get_game_play().get_game_state() == GamePlay.PLAYING)
					{
						score_1.setText(game_play_view.get_game_play()
								.get_player_one_score() + "");
						score_2.setText(game_play_view.get_game_play()
								.get_player_two_score() + "");
						game_timer.setText(game_play_view.get_game_play()
								.get_current_time());
						set_target_fruit(game_play_view.get_game_play()
								.get_target_fruit());

						if (game_play_view.get_game_play().is_frozen())
						{
							if (frozen_screen.getVisibility() == ImageView.INVISIBLE)
							{
								frozen_screen.setVisibility(ImageView.VISIBLE);
								frozen_screen.bringToFront();
								freezing_count_down.bringToFront();
							}
							freezing_count_down.setText(game_play_view
									.get_game_play().get_freezing_count_down()
									+ "");
						} else if (frozen_screen.getVisibility() == ImageView.VISIBLE)
						{
							frozen_screen.setVisibility(ImageView.INVISIBLE);
							freezing_count_down.setText("");
						}
					} else
					{
						if (game_play_view.get_game_play().get_game_state() == GamePlay.WINNER)
						{
							if (player_state.getVisibility() == ImageView.INVISIBLE)
							{
								
								background.bringToFront();
								game_play_view.bringToFront();
								player_state.setVisibility(ImageView.VISIBLE);
								player_state.bringToFront();
								game_play_view.get_game_play().play_win();
							}
						} else if (game_play_view.get_game_play()
								.get_game_state() == GamePlay.LOOSER)
						{
							if (player_state.getVisibility() == ImageView.INVISIBLE)
							{
								background.bringToFront();
								player_state.setImageResource(R.drawable.lost);
								player_state.setVisibility(ImageView.VISIBLE);
								player_state.bringToFront();
								game_play_view.get_game_play().play_lose();
							}

						}
						finish_count_down--;
						if (finish_count_down < 0)
						{
							finished = true;
							game_play_view.get_game_play().stop_players();
							Intent launch_screen_intent = new Intent(context,
									LaunchScreen.class);
							startActivity(launch_screen_intent);
							if(BluetoothConnection.connection_socket!=null)
							{
								BluetoothConnection.connection_socket.cancel();
							}
							finish();
							return;
						}
					}
				}
			}

		};

		timer_task = new TimerTask()
		{
			@Override
			public void run()
			{
				runOnUiThread(game_main_thread);
			}
		};

		layout = (ViewGroup) findViewById(R.id.game_play_main_layout);
		layout.addView(game_play_view);

		update_game();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.game_play_actvity, menu);
		return true;
	}

	@Override
	public void onBackPressed()
	{
		finished = true;
		game_play_view.get_game_play().stop_players();
		if(BluetoothConnection.connection_socket!=null)
		{
			BluetoothConnection.connection_socket.cancel();
		}
		Intent launch_screen_intent = new Intent(this, LaunchScreen.class);
		startActivity(launch_screen_intent);
		finish();
		return;
	}

	@Override
	protected void onPause()
	{
		super.onPause();
		finished = true;
		game_play_view.get_game_play().stop_players();
		if(BluetoothConnection.connection_socket!=null)
		{
			BluetoothConnection.connection_socket.cancel();
		}
		finish();
		return;
	}

	@Override
	protected void onStop()
	{
		super.onStop();
		finished = true;
		game_play_view.get_game_play().stop_players();
		if(BluetoothConnection.connection_socket!=null)
		{
			BluetoothConnection.connection_socket.cancel();
		}
		finish();
		return;
	}

	private void update_game()
	{
		Timer t = new Timer();
		t.scheduleAtFixedRate(timer_task, 0, UPDATE_PERIOD);
	}

	private void set_target_fruit(int fruit_type)
	{
		switch (fruit_type)
		{
		case Fruit.BANANA:
			target_fruit.setImageResource(R.drawable.game_banana);
			break;
		case Fruit.COCO_NUT:
			target_fruit.setImageResource(R.drawable.game_coconut);
			break;
		case Fruit.RED_APPLE:
			target_fruit.setImageResource(R.drawable.game_red_apple);
			break;
		case Fruit.GREEN_APPLE:
			target_fruit.setImageResource(R.drawable.game_green_apple);
			break;
		case Fruit.YELLOW_APPLE:
			target_fruit.setImageResource(R.drawable.game_yellow_apple);
			break;
		default:
			target_fruit.setImageResource(R.drawable.game_banana);
			break;
		}
	}

}
