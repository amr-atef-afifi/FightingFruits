package com.mandaGames.fightingfruits;

import java.util.ArrayList;
import java.util.Random;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Vibrator;

public class GamePlay
{

	public static final int FRUIT_SIZE = 100;
	public static final int POOL_SIZE = 30;
	public static final int INITIAL_FRUITS_NUMBER = 8;
	public static final int FRUITS_NUMBER = 7;
	public static final int FREEZING_TIME = 500;
	public static final int WINNER = 1;
	public static final int LOOSER = 2;
	public static final int PLAYING = 3;
	public static final int FREEZE = -1;
	public static final int INC_SCORE = 10;
	public static final int DEC_SCORE = 5;
	public static final int EXPLODE = 0;

	private int score_1;
	private int score_2;
	private int game_seed;
	private ArrayList<Fruit> falling_fruits;
	private ArrayList<Fruit> score_fruits;
	private int target_fruit;
	private Random game_random;
	private int screen_width;
	private int screen_height;
	private String current_time;
	private int minutes;
	private int seconds;
	private int clock_ticks;
	private int explosion_x;
	private int explosion_y;
	private int explosion_alpha;
	private boolean time_out;
	private boolean is_frozen;
	private boolean is_exploded;
	private int freezing_timer;
	private int displacement;
	private int game_state;
	private int connection_return;
	private Vibrator vibrator;

	MediaPlayer main_track_player;
	MediaPlayer freeze_player;
	MediaPlayer explosion_player;
	MediaPlayer ticks_player;
	MediaPlayer loser_player;
	MediaPlayer winner_player;
	MediaPlayer right_fruit_player;
	MediaPlayer wrong_fruit_player;

	AssetFileDescriptor main_track_afd;
	AssetFileDescriptor freeze_afd;
	AssetFileDescriptor explosion_afd;
	AssetFileDescriptor ticks_afd;
	AssetFileDescriptor loser_afd;
	AssetFileDescriptor winner_afd;
	AssetFileDescriptor right_fruit_afd;
	AssetFileDescriptor wrong_fruit_afd;

	Thread connectionThread;
	Context context;

	public GamePlay(int game_seed, int screen_width, int screen_height,
			Context context)
	{
		this.context = context;

		score_1 = 0;
		score_2 = 0;

		minutes = 3;
		seconds = 0;
		time_out = false;
		is_frozen = false;

		explosion_x = 0;
		explosion_y = 0;
		explosion_alpha = 0;
		displacement = 10;
		game_state = PLAYING;

		tick();

		this.game_seed = game_seed;
		this.screen_width = screen_width;
		this.screen_height = screen_height;

		game_random = new Random(game_seed);
		target_fruit = game_random.nextInt(5);
		freezing_timer = FREEZING_TIME;

		falling_fruits = new ArrayList<Fruit>();
		score_fruits = new ArrayList<Fruit>();

		for (int i = 0; i < INITIAL_FRUITS_NUMBER; i++)
		{
			Fruit f = new Fruit();
			reset_fruit(f);
			falling_fruits.add(f);
		}

		freeze_player = new MediaPlayer();
		explosion_player = new MediaPlayer();
		ticks_player = new MediaPlayer();
		loser_player = new MediaPlayer();
		winner_player = new MediaPlayer();
		right_fruit_player = new MediaPlayer();
		wrong_fruit_player = new MediaPlayer();
		main_track_player = new MediaPlayer();

		vibrator = (Vibrator) context
				.getSystemService(Context.VIBRATOR_SERVICE);
		play_main_track();

		connectionThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					connection_return = BluetoothConnection.connection_socket
							.read_next();
					switch (connection_return)
					{
					case FREEZE:
						freeze();
						break;
					case INC_SCORE:
						score_2 += 10;
						break;
					case DEC_SCORE:
						if (score_2 > 0)
						{
							score_2 -= 5;
						}
						break;
					case EXPLODE:
						score_2 = 0;
						break;
					default:
						break;
					}
				}
			}
		});

		connectionThread.start();
	}

	public void update()
	{
		if (time_out)
		{
			if (score_1 > score_2)
			{
				game_state = WINNER;
				int length = falling_fruits.size();
				for (int i = 0; i < length; i++)
				{

					Fruit current_fruit = falling_fruits.get(i);
					if (current_fruit.get_type() > 4)
					{
						current_fruit.set_type(Fruit.RED_APPLE);
					}
					update_position(current_fruit);
				}
			} else
			{
				game_state = LOOSER;
			}
		} else if (is_exploded)
		{

			if (explosion_alpha % 10 == 0)
			{
				for (int i = 0; i < falling_fruits.size(); i++)
				{
					Fruit current_fruit = falling_fruits.get(i);
					update_position(current_fruit);
					current_fruit.set_x(current_fruit.get_x() + displacement);
				}

				for (int i = 0; i < score_fruits.size(); i++)
				{
					Fruit current_fruit = score_fruits.get(i);
					current_fruit.set_x(current_fruit.get_x() + displacement);
					move_to_score(current_fruit);
				}

				displacement *= -1;
			}
			explosion_alpha -= 3;
			if (explosion_alpha < 0)
			{
				explosion_alpha = 0;
				is_exploded = false;
			}
		} else
		{
			if (is_frozen)
			{
				freezing_timer--;
				if (freezing_timer <= 0)
				{
					freezing_timer = FREEZING_TIME;
					is_frozen = false;
				}
			}
			for (int i = 0; i < falling_fruits.size(); i++)
			{
				Fruit current_fruit = falling_fruits.get(i);
				update_position(current_fruit);
			}

			for (int i = 0; i < score_fruits.size(); i++)
			{
				Fruit current_fruit = score_fruits.get(i);
				move_to_score(current_fruit);
			}
		}
		clock_ticks++;
		if (clock_ticks >= 1000 / GamePlayActivity.UPDATE_PERIOD)
		{
			clock_ticks = 0;
			tick();
		}

	}

	public void hit(int x, int y)
	{
		int length = falling_fruits.size();
		for (int i = 0; i < length; i++)
		{
			Fruit current_fruit = falling_fruits.get(i);
			if (x >= current_fruit.get_x() - (FRUIT_SIZE / 2)
					&& x <= current_fruit.get_x() + FRUIT_SIZE
							+ (FRUIT_SIZE / 2)
					&& y >= current_fruit.get_y() - (FRUIT_SIZE / 2)
					&& y <= current_fruit.get_y() + FRUIT_SIZE
							+ (FRUIT_SIZE / 2))
			{
				if (current_fruit.get_type() == target_fruit)
				{
					score_1 += 10;
					BluetoothConnection.connection_socket
							.write((byte) INC_SCORE);

					target_fruit = game_random.nextInt(5);
					falling_fruits.remove(current_fruit);
					score_fruits.add(current_fruit);
					play_right_fruit();
				} else if (current_fruit.get_type() == Fruit.BOMB)
				{
					score_1 = 0;
					BluetoothConnection.connection_socket.write((byte) EXPLODE);

					explode(current_fruit);
					reset_fruit(current_fruit);
					vibrator.vibrate(700);
				} else if (current_fruit.get_type() == Fruit.ICE)
				{
					// freeze();
					BluetoothConnection.connection_socket.write((byte) FREEZE);
					reset_fruit(current_fruit);
				} else
				{
					score_1 -= 5;
					BluetoothConnection.connection_socket
							.write((byte) DEC_SCORE);

					if (score_1 < 0)
					{
						score_1 = 0;
					}
					falling_fruits.remove(current_fruit);
					score_fruits.add(current_fruit);
					play_wrong_fruit();
					vibrator.vibrate(300);
				}
				break;
			}
		}
	}

	public void freeze()
	{
		is_frozen = true;
		play_freeze();
	}

	public boolean is_frozen()
	{
		return is_frozen;
	}

	public boolean is_exploded()
	{
		return is_exploded;
	}

	public ArrayList<Fruit> get_falling_fruits()
	{
		return falling_fruits;
	}

	public ArrayList<Fruit> get_score_fruits()
	{
		return score_fruits;
	}

	public int get_player_one_score()
	{
		return score_1;
	}

	public int get_player_two_score()
	{
		return score_2;
	}

	public String get_current_time()
	{
		return current_time;
	}

	public int get_target_fruit()
	{
		return target_fruit;
	}

	public int get_explosion_x()
	{
		return explosion_x;
	}

	public int get_explosion_y()
	{
		return explosion_y;
	}

	public int get_explosion_alpha()
	{
		return explosion_alpha;
	}

	public int get_freezing_count_down()
	{
		return (freezing_timer / 100) + 1;
	}

	public int get_game_state()
	{
		return game_state;
	}

	public int get_game_seed()
	{
		return game_seed;
	}

	private void tick()
	{
		current_time = "0" + minutes + ":";
		if (seconds < 10)
		{
			current_time += "0" + seconds;
		} else
		{
			current_time += seconds;
		}

		seconds--;
		if (seconds < 0)
		{
			seconds = 59;
			minutes--;
			if (minutes < 0)
			{
				time_out = true;
			}
		}

		if (minutes == 0 && seconds == 9)
		{
			play_ticks();
		}
	}

	private void update_position(Fruit f)
	{
		f.move(screen_height);
		if (f.get_y() > screen_height)
		{
			reset_fruit(f);
		}
	}

	private void move_to_score(Fruit fruit)
	{
		int x0 = fruit.get_x();
		int y0 = fruit.get_y();

		if (x0 < screen_width - 2 * FRUIT_SIZE)
		{
			fruit.set_x(fruit.get_x() + 30);
			fruit.set_y(y0 * (fruit.get_x() - (screen_width - FRUIT_SIZE))
					/ (x0 - (screen_width - FRUIT_SIZE)));
		} else
		{
			fruit.set_y(fruit.get_y() - 15);
			fruit.set_x((screen_width - FRUIT_SIZE)
					+ ((fruit.get_y() * (x0 - (screen_width - FRUIT_SIZE))) / (y0 + 1)));
		}

		if (fruit.get_x() >= (screen_width - 1.25 * FRUIT_SIZE))
		{
			score_fruits.remove(fruit);
			reset_fruit(fruit);
			falling_fruits.add(fruit);
		}
	}

	private void reset_fruit(Fruit f)
	{
		f.set_x(game_random.nextInt(screen_width / FRUIT_SIZE) * FRUIT_SIZE);
		f.set_y(screen_height);
		f.set_type(game_random.nextInt(7));
		f.set_y_final((screen_height / 2)
				+ (game_random.nextDouble() * (screen_height / 2)));
		f.set_age(0);
	}

	private void explode(Fruit f)
	{
		is_exploded = true;
		explosion_x = f.get_x() - 110;
		explosion_y = f.get_y() - 70;
		explosion_alpha = 250;
		reset_fruit(f);
		play_explosion();
	}

	public void play_main_track()
	{
		try
		{
			main_track_afd = context.getAssets().openFd("midnight-ride.mp3");
			main_track_player
					.setDataSource(main_track_afd.getFileDescriptor(),
							main_track_afd.getStartOffset(),
							main_track_afd.getLength());
			main_track_player.prepare();
			main_track_player.start();
			main_track_player.setLooping(true);

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void play_win()
	{
		try
		{
			winner_player.reset();
			winner_afd = context.getAssets().openFd("winner.mp3");
			winner_player.setDataSource(winner_afd.getFileDescriptor(),
					winner_afd.getStartOffset(), winner_afd.getLength());
			winner_player.prepare();
			winner_player.start();

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void play_lose()
	{
		try
		{
			loser_player.reset();
			loser_afd = context.getAssets().openFd("loser.mp3");
			loser_player.setDataSource(loser_afd.getFileDescriptor(),
					loser_afd.getStartOffset(), loser_afd.getLength());
			loser_player.prepare();
			loser_player.start();

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void play_ticks()
	{
		try
		{
			ticks_player.reset();
			ticks_afd = context.getAssets().openFd("clock_ticks.mp3");
			ticks_player.setDataSource(ticks_afd.getFileDescriptor(),
					ticks_afd.getStartOffset(), ticks_afd.getLength());
			ticks_player.prepare();
			ticks_player.start();

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void play_freeze()
	{
		try
		{
			freeze_player.reset();
			freeze_afd = context.getAssets().openFd("ice_cracking.mp3");
			freeze_player.setDataSource(freeze_afd.getFileDescriptor(),
					freeze_afd.getStartOffset(), freeze_afd.getLength());
			freeze_player.prepare();
			freeze_player.start();

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void play_explosion()
	{
		try
		{
			explosion_player.reset();
			explosion_afd = context.getAssets().openFd("explosion.mp3");
			explosion_player.setDataSource(explosion_afd.getFileDescriptor(),
					explosion_afd.getStartOffset(), explosion_afd.getLength());
			explosion_player.prepare();
			explosion_player.start();

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void play_right_fruit()
	{
		try
		{
			right_fruit_player.reset();
			right_fruit_afd = context.getAssets().openFd("right_fruit.mp3");
			right_fruit_player.setDataSource(
					right_fruit_afd.getFileDescriptor(),
					right_fruit_afd.getStartOffset(),
					right_fruit_afd.getLength());
			right_fruit_player.prepare();
			right_fruit_player.start();

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void play_wrong_fruit()
	{
		try
		{
			wrong_fruit_player.reset();
			wrong_fruit_afd = context.getAssets().openFd("wrong_fruit.mp3");
			wrong_fruit_player.setDataSource(
					wrong_fruit_afd.getFileDescriptor(),
					wrong_fruit_afd.getStartOffset(),
					wrong_fruit_afd.getLength());
			wrong_fruit_player.prepare();
			wrong_fruit_player.start();

		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public void stop_players()
	{
		main_track_player.release();
		ticks_player.release();
		winner_player.release();
		loser_player.release();
		right_fruit_player.release();
		wrong_fruit_player.release();
		freeze_player.release();
		explosion_player.release();
	}
}
