package com.webcamshooter.controller;

import java.util.ArrayList;
import java.util.Arrays;

import org.opencv.core.Mat;

public class ImageUtils {

	public static double getMedianValue(Mat img) {

		ArrayList<Double> values = new ArrayList<>();
		for (int i = 0; i < img.rows(); i++) {
			for (int j = 0; j < img.cols(); j++) {
				System.out.println("===========================================");
				for (int k = 0; k < img.get(i, j).length; k++) {
					System.out.println(img.get(i, j)[k]);
					values.add(img.get(i, j)[k]);
				}
				System.out.println("===========================================");
			}
		}
		Double[] val = values.toArray(new Double[values.size()]);
		Arrays.sort(val);
		if (values.size() % 2 == 0) {
			return (val[val.length / 2] + val[val.length / 2 - 1]) / 2;
		} else {
			return val[val.length / 2];
		}
	}
}
