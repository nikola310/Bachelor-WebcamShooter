using UnityEngine;
using System.Collections;

public class ShootAction : Action
{
	public int x;
	public int y;

	public ShootAction(string player, string command, int x, int y){
		this.player = player;
		this.command = command;
		this.x = x;
		this.y = y;
	}
}

