package com.mandaGames.fightingfruits;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class ConnectionSocket
{
	public static final int ERROR_RETURN = -16 ;
	private final BluetoothSocket mmSocket;
	private final InputStream mmInStream;
	private final OutputStream mmOutStream;

	public ConnectionSocket(BluetoothSocket socket)
	{
		mmSocket = socket;
		InputStream tmpIn = null;
		OutputStream tmpOut = null;

		// Get the input and output streams, using temporary objects because
		// member streams are final
		try
		{
			tmpIn = socket.getInputStream();
			tmpOut = socket.getOutputStream();
		} catch (IOException e)
		{
		}

		mmInStream = tmpIn;
		mmOutStream = tmpOut;
	}

	public int read_next()
	{
		byte[] buffer = new byte[4];
		try
		{
			// Read from the InputStream
			mmInStream.read(buffer);
			Log.e("msg", buffer[0] + "");
			return buffer[0];
		} catch (IOException e)
		{
			return ERROR_RETURN;
		}
	}

	/* Call this from the main activity to send data to the remote device */
	public void write(byte value)
	{
		try
		{
			mmOutStream.write(value);
		} catch (IOException e)
		{
		}
	}

	/* Call this from the main activity to shutdown the connection */
	public void cancel()
	{
		try
		{
			mmSocket.close();
		} catch (Exception e)
		{
		}
	}
}
