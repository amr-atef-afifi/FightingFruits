package com.mandaGames.fightingfruits;

import java.lang.reflect.Method;
import java.util.Iterator;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

public class GamePlayView extends View
{

	private GamePlay game_play;
	private Bitmap[] fruits;
	private Point screen_size;
	Paint explosion_painter;
	Rect explosion_rect;
	Bitmap explosion;
	 
	Context context;

	public GamePlayView(Context context, int game_seed)
	{
		super(context);

		this.context = context;

		// initializing game bitmaps.

		fruits = new Bitmap[GamePlay.FRUITS_NUMBER];
		fruits[Fruit.BANANA] = BitmapFactory.decodeResource(getResources(),
				R.drawable.game_banana);
		fruits[Fruit.COCO_NUT] = BitmapFactory.decodeResource(getResources(),
				R.drawable.game_coconut);
		fruits[Fruit.RED_APPLE] = BitmapFactory.decodeResource(getResources(),
				R.drawable.game_red_apple);
		fruits[Fruit.GREEN_APPLE] = BitmapFactory.decodeResource(
				getResources(), R.drawable.game_green_apple);
		fruits[Fruit.YELLOW_APPLE] = BitmapFactory.decodeResource(
				getResources(), R.drawable.game_yellow_apple);
		fruits[Fruit.ICE] = BitmapFactory.decodeResource(getResources(),
				R.drawable.game_ice);
		fruits[Fruit.BOMB] = BitmapFactory.decodeResource(getResources(),
				R.drawable.game_bomb);

		screen_size = new Point();
		get_screen_size(screen_size, context);
		game_play = new GamePlay(game_seed, screen_size.x, screen_size.y,context);
		this.setClickable(true);

		explosion_painter = new Paint();
		explosion_painter.setColor(Color.BLACK);
		explosion_painter.setAlpha(250);
		explosion_rect = new Rect(0, 0, screen_size.x, screen_size.y);
		explosion = BitmapFactory.decodeResource(getResources(),
				R.drawable.explosion);

		
		

	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		draw_falling_fruits(canvas);
		draw_score_fruits(canvas);
		if (game_play.is_frozen()
				|| game_play.get_game_state() != GamePlay.PLAYING)
		{
			if (this.isClickable())
			{
				this.setClickable(false);
			}
		} else if (game_play.is_exploded())
		{
			if (this.isClickable())
			{
				this.setClickable(false);
			}
			draw_exploion(canvas, game_play.get_explosion_x(),
					game_play.get_explosion_y(),
					game_play.get_explosion_alpha());
		} else if (!isClickable())
		{
			this.setClickable(true);
		}
	}

	private void draw_falling_fruits(Canvas canvas)
	{

		Iterator<Fruit> f_itr = game_play.get_falling_fruits().iterator();
		while (f_itr.hasNext())
		{
			Fruit fruit = f_itr.next();
			canvas.drawBitmap(fruits[fruit.get_type()], fruit.get_x(),
					fruit.get_y(), null);
		}
	}

	private void draw_score_fruits(Canvas canvas)
	{
		Iterator<Fruit> f_itr = game_play.get_score_fruits().iterator();
		while (f_itr.hasNext())
		{
			Fruit fruit = f_itr.next();
			canvas.drawBitmap(fruits[fruit.get_type()], fruit.get_x(),
					fruit.get_y(), null);
		}
	}

	private void draw_exploion(Canvas canvas, int x, int y, int alpha)
	{
		explosion_painter.setAlpha(alpha);
		canvas.drawRect(explosion_rect, explosion_painter);
		canvas.drawBitmap(explosion, x, y, explosion_painter);
	}

	public void update()
	{	
		game_play.update();
		invalidate();
	}

	public GamePlay get_game_play()
	{
		return game_play;
	}

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	private void get_screen_size(Point size, Context context)
	{
		WindowManager window_manager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = window_manager.getDefaultDisplay();
		try
		{
			Class<?> pointClass = Class.forName("android.graphics.Point");
			Method new_getSize = Display.class.getMethod("getSize", new Class[]
			{ pointClass });
			new_getSize.invoke(display, size);
		} catch (Exception e)
		{
			size.x = display.getWidth();
			size.y = display.getHeight();
		}
	}
}
