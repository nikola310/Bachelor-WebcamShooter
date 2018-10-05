using UnityEngine;
using System.Collections;
using UnityEngine.UI;

public class CanvasDataUpdate : MonoBehaviour {

	public Text p1Scoretext;
	public Text p2Scoretext;

	Color[] anchorColors = new Color[7];
	
	// Use this for initialization
	void Start () {
		anchorColors[0] = new Color(0f, 0f, 0f);
		anchorColors[1] = new Color(0f, 1f, 0f);
		anchorColors[2] = new Color(0f, 0f, 1f);
		anchorColors[3] = new Color(1f, 0f, 1f);
		anchorColors[4] = new Color(1f, 0.6f, 0f);
		anchorColors[5] = new Color(1f, 1f, 1f);
		anchorColors[6] = new Color(0.2f, 0.6f, 0.8f);
        anchorColors[7] = new Color(0f, 0f, 0f);

		updateAnchors();
	}
	
	// Update is called once per frame
	void Update () {
		p1Scoretext.text = Scoring.GetInstance().player1Name + ": " + Scoring.GetInstance().p1Score;
		p2Scoretext.text = Scoring.GetInstance().player2Name + ": " + Scoring.GetInstance().p2Score;

		if (Input.GetKeyDown(KeyCode.Space)){
			print("space key was pressed");
			GlobalVars.handleColorIndex++;
		}
		updateAnchors();
	}


	void updateAnchors(){
		GameObject[] anchors = GameObject.FindGameObjectsWithTag("anchor");
		for (int i = 0; i < anchors.Length; i++) {
			if(GlobalVars.handleColorIndex > anchorColors.Length-1){
				GlobalVars.handleColorIndex = 0;
			}
			anchors[i].GetComponent<RawImage>().color = anchorColors[GlobalVars.handleColorIndex];
		}
	}
}
