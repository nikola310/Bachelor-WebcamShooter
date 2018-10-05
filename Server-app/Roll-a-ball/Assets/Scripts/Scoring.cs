using UnityEngine;
using System.Collections;
using System;

public class Scoring {

	public String player1Name = "";
	
	public String player2Name = "";
	
	public int p1Score = 0;
	
	public int p2Score = 0;


	//We make a static variable to our MusicManager instance
	private static Scoring instance = null;
	
	public static Scoring GetInstance()
	{
		if(instance == null)
		{
			instance = new Scoring();
		}
		return instance;
	}


	public void resetScore(){
		p1Score = 0;
		p2Score = 0;
	}


	/**
	 * Dodaje igraca ukoliko postoji slobodan slot.
	 * 
	 * */
	public void newPlayer(string name){
		if(name.Equals(player1Name) || name.Equals(player2Name)){
			return;
		}
		if(player1Name == ""){
			player1Name = name;
		}else if(player2Name == ""){
			player2Name = name;
		}else{
			throw new UnityException();
		}
	}

	/**
	 * 
	 * 
	 */
	public void playerGone(string name){
		if(player1Name.Equals(name)){
			player1Name = "";
			p1Score = 0;
		}
		if(player2Name.Equals(name)){
			player2Name = "";
			p2Score = 0;
		}
	}

	public void addScore(string name, int scorePoints){
		if(player1Name.Equals(name)){
			p1Score += scorePoints;
		}
		if(player2Name.Equals(name)){
			p2Score += scorePoints;
		}
	}

}