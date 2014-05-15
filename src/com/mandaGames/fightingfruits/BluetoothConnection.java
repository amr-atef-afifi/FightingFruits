package com.mandaGames.fightingfruits;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


public class BluetoothConnection
{
	public static BluetoothAdapter mBluetoothAdapter;
	public static BluetoothSocket transferSocket;
	public static boolean connection = false;
	public static boolean is_ready = false;
	public static ConnectionSocket connection_socket ;
	
	public static void establish_connection(Context context)
	{
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		set_discoverable(context);
		start_listen();
	}
	
	private static void set_discoverable(Context context)
	{
		Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
		discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 180);
		context.startActivity(discoverableIntent);
	}
	
	public static void start_listen()
	{
		startServerSocket(mBluetoothAdapter);
		new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (true)
				{
					if (connection)
					{
						connection_socket = new ConnectionSocket(transferSocket);
						is_ready = true ;
						break;
					}
				}
			}
		}).start();
	}

	public static void startServerSocket(BluetoothAdapter bluetooth)
	{
		UUID uuid = UUID.fromString("0000110E-0000-1000-8000-00805F9B34FB");
		String name = "bluetoothserver";
		try
		{
			final BluetoothServerSocket btserver = bluetooth
					.listenUsingRfcommWithServiceRecord(name, uuid);
			Thread acceptThread = new Thread(new Runnable()
			{
				public void run()
				{
					try
					{
						// Block until client connection established.
						BluetoothSocket serverSocket = btserver.accept();

						// Add a reference to the socket used to send messages.
						transferSocket = serverSocket;
						connection = true;
						Log.w("sockeeeeeeeeeeet", "created");
					} catch (IOException e)
					{
						Log.e("BLUETOOTH", "Server connection IO Exception", e);
					}
				}
			});
			acceptThread.start();
		} catch (IOException e)
		{

		}
	}
}
