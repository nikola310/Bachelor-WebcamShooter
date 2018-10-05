package com.webcamshooter.controller;

import java.util.List;

import org.opencv.core.MatOfPoint;
import org.opencv.imgproc.Imgproc;

/**
 * @author Nikola
 *
 */
public class ContourUtils {

	/**
	 * Pronalazi indeks najvece konture
	 * 
	 * @param contours
	 * @return
	 */
	public static int getIndexOfLargest(List<MatOfPoint> contours) {
		if (contours.size() < 1) {
			return -1;
		}
		double maxArea = 0;
		int index = 0;
		for (int i = 0; i < contours.size(); i++) {
			double area = Imgproc.contourArea(contours.get(i));
			if (maxArea < area) {
				maxArea = area;
				index = i;
			}
		}

		return index;
	}

}
