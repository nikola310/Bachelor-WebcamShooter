package com.webcamshooter.controller;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * Singleton klasa za komunikaciju sa Unity-em. FIXME: mreznu komunikaciju
 * izdvojiti u posebnu nit da se ne bi blokirao UI za vreme slanja komande preko
 * mreze
 *
 */
public class Connection {

	private static Connection instance;

	private Socket socket;
	private PrintWriter out;
	private String playerName;

	private ConnectionStateListener connectionStateListener;

	private Connection() {
	}

	/**
	 * 
	 * @return
	 */
	public static final Connection getInstance() {
		if (instance == null) {
			instance = new Connection();
		}
		return instance;
	}

	/**
	 * 
	 * @param message
	 */
	public void sendMessage(String message) {
		System.out.println(message);

		if (out == null) {
			throw new CameraShooterException("Not connected");
		}
		out.println(message);
		if (out.checkError()) {
			if (connectionStateListener != null) {
				connectionStateListener.connectionClosed();
			}
			throw new CameraShooterException("Not connected");
		}
	}

	/**
	 * IP:PORT
	 */
	public void connect(String playerName, String ipAndPort) {
		try {
			String[] ipPort = ipAndPort.trim().split(":");

			socket = new Socket(ipPort[0], Integer.parseInt(ipPort[1]));
			out = new PrintWriter(socket.getOutputStream(), true);

			this.playerName = playerName;

			sendMessage(playerName + "|HELLO");

			if (connectionStateListener != null) {
				connectionStateListener.connectionOpened();
			}

		} catch (Exception e1) {
			disconnect();
			throw new CameraShooterException("Can not connect to server!", e1);
		}
	}

	/**
	 * 
	 */
	public void disconnect() {
		try {
			if (playerName != null) {
				sendMessage(playerName + "|DISCONNECT");
			}
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			this.playerName = null;

			if (connectionStateListener != null) {
				connectionStateListener.connectionClosed();
			}
		}

	}

	public ConnectionStateListener getConnectionStateListener() {
		return connectionStateListener;
	}

	public void setConnectionStateListener(ConnectionStateListener connectionStateListener) {
		this.connectionStateListener = connectionStateListener;
	}
}
