package com.mandaGames.fightingfruits;

import java.util.Set;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class JoinGame extends Activity
{

	private static final int REQUEST_ENABLE_BT = -1;
	ListView listDevicesFound;
	ArrayAdapter<String> arrayAdapter;

	private BluetoothAdapter bluetoothAdapter;
	private BluetoothSocket transferSocket;
	
	Context context ;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_join_game);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		bluetoothAdapter = BluetoothConnection.mBluetoothAdapter;
		context = this;

		listDevicesFound = (ListView) findViewById(R.id.id_ListView1_receive);
		arrayAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1);
		listDevicesFound.setAdapter(arrayAdapter);
		listDevicesFound.setOnItemClickListener(new OnItemClickListener()
		{
			public void onItemClick(AdapterView<?> av, View v, int arg2,
					long arg3)
			{
				String info = ((TextView) v).getText().toString();
				String address = info.substring(info.length() - 17);

				ConnectThread connect_thread = new ConnectThread(
						bluetoothAdapter.getRemoteDevice(address));
				connect_thread.run();

				new Thread(new Runnable()
				{

					@Override
					public void run()
					{
						while (true)
						{
							if (BluetoothConnection.connection)
							{
								BluetoothConnection.connection_socket = new ConnectionSocket(
										BluetoothConnection.transferSocket);
								BluetoothConnection.connection_socket.write((byte)-2);
								
								Intent game_intent = new Intent(context,
										GamePlayActivity.class);
								startActivity(game_intent);
								break;
							}
						}
					}
				}).start();
			}
		});
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		enable_bluetooth();
		registerReceiver(ActionFoundReceiver, new IntentFilter(
				BluetoothDevice.ACTION_FOUND));
		scan();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.join_game, menu);
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
		Intent launch_screen_intent = new Intent(this, LaunchScreen.class);
		startActivity(launch_screen_intent);
		finish();
	}

	private final BroadcastReceiver ActionFoundReceiver = new BroadcastReceiver()
	{

		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action))
			{
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				arrayAdapter.add(device.getName() + "\n" + device.getAddress());
				arrayAdapter.notifyDataSetChanged();
			}

		}
	};

	private void enable_bluetooth()
	{
		if (bluetoothAdapter.isEnabled())
		{
			if (bluetoothAdapter.isDiscovering())
			{
				bluetoothAdapter.cancelDiscovery();
			}
		} else
		{
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		unregisterReceiver(ActionFoundReceiver);
	}

	public void scan()
	{
		// If we're already discovering, stop it
		if (bluetoothAdapter.isDiscovering())
		{
			bluetoothAdapter.cancelDiscovery();
		}
		// get the paired devices
		Set<BluetoothDevice> pairedDevices = bluetoothAdapter
				.getBondedDevices();
		// If there are paired devices, add each one to the ArrayAdapter
		if (pairedDevices.size() > 0)
		{
			for (BluetoothDevice device : pairedDevices)
			{
				arrayAdapter.add(device.getName() + "\n" + device.getAddress());
			}
		}
		// Request discover from BluetoothAdapter
		bluetoothAdapter.startDiscovery();
		arrayAdapter.clear();
	}
}
