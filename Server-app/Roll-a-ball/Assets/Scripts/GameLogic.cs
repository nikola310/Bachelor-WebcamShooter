using UnityEngine;
using System.Collections;
using System;
using System.IO;
using System.Net.Sockets;

[RequireComponent(typeof(AudioSource))]
public class GameLogic : MonoBehaviour {

	public Action action;
	public AudioClip gunshot;
	public GameObject bulletHole;
	public Camera camera;

	private Vector3 offset;

	// Use this for initialization
	void Start () {
		print("Starting game logic");
		NetworkServer.GetInstance().setGameLogic(this);
		Scoring.GetInstance().resetScore();
	}


	void OnApplicationQuit()
	{
		NetworkServer.GetInstance().stopListening();
	}


	void Update(){
		if (action != null) {
			if(action is ShootAction){
				ShootAction a = (ShootAction) action;
				shoot(action.player, a.x, a.y);
			}
			action = null;
		}

		GameObject[] gos = GameObject.FindGameObjectsWithTag("target");
		if(gos.Length == 0){
			Debug.Log("Game over");
			restartLevel();
		}
			
	}



	public void restartLevel(){
		Application.LoadLevel (0);
	}



	/**
	 * 
	 *
	 **/
	public void shoot(string player, int w, int h){
		//Konverzija ulaznih koordinata u unity koordinate
		Vector2 spot = new Vector2(w, Screen.height - h);
		
		//RaycastHit RaycastHit;
		Ray ray = camera.ScreenPointToRay(spot);
		
		RaycastHit hit;
		if (Physics.Raycast(ray, out hit, 100)){
			Debug.DrawLine(ray.origin, hit.point);

			// Pogodak
			if(hit.rigidbody != null){

				// Pogodak u mete
				if("target".Equals(hit.rigidbody.tag)){
					// Poeni
					Scoring.GetInstance().addScore(player, 1);
					hit.rigidbody.AddForce(ray.direction * 800);
					// Oznaci kao pogodjeno
					hit.rigidbody.tag = null;
				}

				// Bullet hole
				Vector3 pos = hit.point;
				Quaternion rot = hit.rigidbody.transform.rotation;
				rot.eulerAngles += new Vector3(270, 0, 0);
				GameObject bHole = (GameObject)Instantiate(bulletHole, pos, rot);
				bHole.transform.parent = hit.rigidbody.transform;
			}

		}

		// Zvuk
		AudioSource.PlayClipAtPoint(gunshot, camera.transform.position);
	}
	
}
