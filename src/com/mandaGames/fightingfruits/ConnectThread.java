package com.mandaGames.fightingfruits;

import java.io.IOException;
import java.util.UUID;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class ConnectThread extends Thread
{

	private final BluetoothSocket mmSocket;

	public ConnectThread(BluetoothDevice device)
	{
		// Use a temporary object that is later assigned to mmSocket,
		// because mmSocket is final
		BluetoothSocket tmp = null;
		

		// Get a BluetoothSocket to connect with the given BluetoothDevice
		try
		{
			// MY_UUID is the app's UUID string, also used by the server
			// code
			UUID uuid = UUID.fromString("0000110E-0000-1000-8000-00805F9B34FB");
			tmp = device.createRfcommSocketToServiceRecord(uuid);
		} catch (IOException e)
		{
		}
		mmSocket = tmp;
	}

	public void run()
	{
		// Cancel discovery because it will slow down the connection
		// mBluetoothAdapter.cancelDiscovery();

		try
		{
			// Connect the device through the socket. This will block
			// until it succeeds or throws an exception
			mmSocket.connect();
			BluetoothConnection.transferSocket = mmSocket;
			BluetoothConnection.connection = true;
		} catch (IOException connectException)
		{
			// Unable to connect; close the socket and get out
			try
			{
				mmSocket.close();
			} catch (IOException closeException)
			{
			}
			return;
		}
	}

	/** Will cancel an in-progress connection, and close the socket */
	public void cancel()
	{
		try
		{
			mmSocket.close();
		} catch (IOException e)
		{
		}
	}
}
