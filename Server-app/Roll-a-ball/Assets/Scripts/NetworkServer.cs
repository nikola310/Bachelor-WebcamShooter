using UnityEngine;
using System.Collections;
using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Threading;



public class NetworkServer
{
	private int port = 13001;
	private String ip = "0.0.0.0";
	private GameLogic gameLogic;
	private Thread clientAcceptorThread;
	private TcpListener tcp_Listener = null;
	private Boolean mRunning = true;

	//We make a static variable to our MusicManager instance
	private static NetworkServer instance = null;

	public static NetworkServer GetInstance()
	{
		if(instance == null)
		{
			instance = new NetworkServer();
		}
		return instance;
	}


	public NetworkServer(){
		Start();
	}


	public void setGameLogic(GameLogic gameLogic){
		this.gameLogic = gameLogic;
	}


	// Use this for initialization
	private void Start() {
		mRunning = true;
		Debug.Log ("Start listening for clients...");
		ThreadStart ts = new ThreadStart(ClientAcceptor);
		clientAcceptorThread = new Thread(ts);
		clientAcceptorThread.Start();
	}

	// Postavlja flag na false. Flag signalizira 
	// nitima za klijente i niti za prijem klijenata 
	// da prestanu da se izvrsavaju
	public void stopListening()
	{
		mRunning = false;

		// wait fpr listening thread to terminate (max. 2000ms)
		clientAcceptorThread.Join(2000);
	}


	/**
	 * Izvrsava se na niti za prihvatanje klijenata
	 * */
	void ClientAcceptor()
	{
		mRunning = true;
		try
		{
			IPAddress address = IPAddress.Parse(ip);
			tcp_Listener = new TcpListener(address, port);
			tcp_Listener.Start();
			Debug.Log("Server Start");
			while (mRunning)
			{
				// check if new connections are pending, if not, be nice and sleep 100ms
				if (!tcp_Listener.Pending())
				{
					Thread.Sleep(100);
				}
				else
				{
					TcpClient client = tcp_Listener.AcceptTcpClient();
					ThreadPool.QueueUserWorkItem(ClientHandler, client);
				}
			}
		}
		catch (ThreadAbortException)
		{
			Debug.Log("exception");
		}
		finally
		{
			mRunning = false;
			tcp_Listener.Stop();
		}
	}


	/**
	 * Izvrsava se na posebnoj niti za svakog klijenta
	 * */
	private void ClientHandler(object obj)
	{
		TcpClient client = (TcpClient)obj;
		NetworkStream ns = client.GetStream();
		StreamReader reader = new StreamReader(ns);
		string name = null;

		Boolean clientPresent = true;
		try{
			while(mRunning && clientPresent){
				String msg = reader.ReadLine();
				if(msg == null){
					// mRunning = false;
					// break;
					Thread.Sleep(2);
					continue;
				}
				Debug.Log("NET MSG: " + msg);

				if(!checkFormat(msg, 0, 2)){
					continue;
				}

				String[] commandParts = msg.Split('|');
				String player = commandParts[0].ToUpper();
				String command = commandParts[1].ToUpper();

				// Nov igrac usao u igru
				if(name == null){
					Scoring.GetInstance().newPlayer(player);
					name = player;
				}
				

				// Pucanj
				if (command.Equals("SHOOT")){
					if(!checkFormat(msg, 4, 0)){
						continue;
					}
					Debug.Log(commandParts[2] + "," + commandParts[3]);
					int pW = Int32.Parse(commandParts[2]);
					int pH = Int32.Parse(commandParts[3]);
					int x = (Screen.width * pW) / 10000;
					int y = (Screen.height * pH) / 10000;
					gameLogic.action = new ShootAction(player, command, x, y);
				}

				// Disconnect
				if (command.Equals("DISCONNECT")){
					clientPresent = false;
				}
			}

		}catch(Exception e){
			Debug.LogException(e);
		} finally{
			reader.Close();
			client.Close();
			if(name!=null){
				Scoring.GetInstance().playerGone(name);
				Debug.Log ("Disconnecting client " + name);
			}else{
				Debug.Log ("Disconnecting client ");
			}
		}
	}


	private Boolean checkFormat(String command, Int32 requiredParamsNum, Int32 requiredParamsMinNum){
		String[] commandParts = command.Split('|');
		if (requiredParamsNum != 0) {
			if(commandParts.Length != requiredParamsNum){
				Debug.LogWarning("Unknown command format: " + command + ". Expected " + requiredParamsNum + " params ");
				return false;
			}
		}
		if (requiredParamsMinNum != 0) {
			if(commandParts.Length < requiredParamsMinNum){
				Debug.LogWarning("Unknown command format: " + command + ". Expected at least " + requiredParamsMinNum + " params ");
				return false;
			}
		}
		return true;
	}
	

}

