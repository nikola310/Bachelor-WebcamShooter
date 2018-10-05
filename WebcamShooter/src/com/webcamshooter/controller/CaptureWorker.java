package com.webcamshooter.controller;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDouble;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

public class CaptureWorker implements Runnable {

	public static final Scalar BLUE = new Scalar(100, 10, 10, 100);
	public static final Scalar GREEN = new Scalar(0, 255, 0, 255);
	public static final Scalar RED = new Scalar(0, 0, 255);
	public static final Scalar YELLOW = new Scalar(100, 243, 255);

	private CameraPanel cameraPanel;

	private int device;

	public CaptureWorker(CameraPanel cameraPanel, int device) {
		this.cameraPanel = cameraPanel;
		this.device = device;
	}

	@Override
	public void run() {

		// -- 2. Read the video stream
		System.out.println("Starting capture");
		VideoCapture capture = new VideoCapture(device);
		final Mat webcam_image = new Mat();
		capture.read(webcam_image);

		Mat array255 = new Mat(webcam_image.height(), webcam_image.width(), CvType.CV_8UC1);
		array255.setTo(new Scalar(255));

		Point perpPointTop = new Point();
		Point perpPointLeft = new Point();

		if (capture.isOpened()) {
			System.out.println("Capture opened");
			while (!Thread.interrupted()) {
				Mat mGray = new Mat();
				Mat mCanny = new Mat();
				Mat hierarchy = new Mat();
				capture.read(webcam_image);
				int cameraX = webcam_image.width() / 2;
				int cameraY = webcam_image.height() / 2;
				Mat copy = webcam_image.clone();
				Point cameraCenter = new Point(cameraX, cameraY);

				if (!webcam_image.empty()) {
					Imgproc.GaussianBlur(copy, copy, new Size(3, 3), 0, 0);
					Imgproc.cvtColor(copy, mGray, Imgproc.COLOR_BGR2GRAY);

					// Za odredjivanje donje i gornje granice kod Canny algoritma
					MatOfDouble mean = new MatOfDouble();
					MatOfDouble stdDev = new MatOfDouble();
					Core.meanStdDev(mGray, mean, stdDev);

					Imgproc.Canny(mGray, mCanny, 0.66 * mean.get(0, 0)[0], 1.33 * mean.get(0, 0)[0], 3, true);

					Imgproc.dilate(mCanny, mCanny,
							Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(5, 5)));
					Imgproc.blur(mCanny, mCanny, new Size(3, 3));

					List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
					Imgproc.findContours(mCanny, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_TC89_L1);
					int index = ContourUtils.getIndexOfLargest(contours);

					if (index > -1) {
						MatOfPoint largest = contours.get(index);

						MatOfPoint2f NewMtx = new MatOfPoint2f(largest.toArray());
						MatOfPoint2f simplified = new MatOfPoint2f();
						Point[] largestArray = largest.toArray();

						// Pojednostavljivanje konture upotrebom Ramer-Daglas-Pekerovog algoritma
						int flag = 1;
						while (largestArray.length > 4) {
							Imgproc.approxPolyDP(NewMtx, simplified, flag, true);
							largestArray = simplified.toArray();
							flag += 2;
						}

						if (largestArray.length == 4) {

							ArrayList<Point> sortedByX = PointUtils
									.sortPointsByX(new ArrayList<>(Arrays.asList(largestArray)));
							Point topLeft = (sortedByX.get(0).y < sortedByX.get(1).y) ? sortedByX.get(0)
									: sortedByX.get(1);
							Point topRight = (sortedByX.get(2).y < sortedByX.get(3).y) ? sortedByX.get(2)
									: sortedByX.get(3);
							Point bottomLeft = (sortedByX.get(0).y > sortedByX.get(1).y) ? sortedByX.get(0)
									: sortedByX.get(1);
							Point bottomRight = (sortedByX.get(2).y > sortedByX.get(3).y) ? sortedByX.get(2)
									: sortedByX.get(3);

							// Nacrtaj tacke koje su centri kontura
							Imgproc.circle(webcam_image, topLeft, 3, RED, 2);
							Imgproc.circle(webcam_image, topRight, 3, RED, 2);
							Imgproc.circle(webcam_image, bottomLeft, 3, RED, 2);
							Imgproc.circle(webcam_image, bottomRight, 3, RED, 2);

							// Nacrtaj linije koje spajaju tacke
							Imgproc.line(webcam_image, topLeft, topRight, YELLOW, 2);
							Imgproc.line(webcam_image, topLeft, bottomLeft, YELLOW, 2);
							Imgproc.line(webcam_image, bottomLeft, bottomRight, YELLOW, 2);
							Imgproc.line(webcam_image, bottomRight, topRight, YELLOW, 2);

							PointUtils.getPerpendicularPoint(topLeft, topRight, cameraCenter, perpPointTop);
							Imgproc.circle(webcam_image, perpPointTop, 3, RED, 2);

							PointUtils.getPerpendicularPoint(topLeft, bottomLeft, cameraCenter, perpPointLeft);
							Imgproc.circle(webcam_image, perpPointLeft, 3, RED, 2);

							double topLineLen = PointUtils.pointsDistance(topLeft, topRight);
							double horDist = PointUtils.pointsDistance(topLeft, perpPointTop);
							int shootX = (int) ((horDist / topLineLen) * 10000);

							double leftLineLen = PointUtils.pointsDistance(topLeft, bottomLeft);
							double vertDist = PointUtils.pointsDistance(topLeft, perpPointLeft);
							int shootY = (int) ((vertDist / leftLineLen) * 10000);

							// Pucamo
							Main.mf.updateShootCoords(shootX, shootY, System.currentTimeMillis());
						}
					}

					// Nisan (centar kamere)
					Imgproc.circle(webcam_image, cameraCenter, 5, RED, 2);

					// -- 5. Display the image
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							cameraPanel.setImageWithMat(webcam_image);
							cameraPanel.repaint();
						}
					});

				} else {
					System.out.println(" --(!) No captured frame -- Break!");
					break;
				}
			}
		}

		System.out.println("releasing...");
		capture.release();
	}

}
