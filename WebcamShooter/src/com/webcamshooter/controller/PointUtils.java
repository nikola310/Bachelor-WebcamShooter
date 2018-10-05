package com.webcamshooter.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.DecompositionSolver;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.opencv.core.Point;

/**
 * Pomocne metode za rad sa tackama i analitickom geometrijom
 */
public class PointUtils {

	/**
	 * Sortiranje tacaka po X osi
	 * 
	 * @param points
	 * @return
	 */
	public static ArrayList<Point> sortPointsByX(ArrayList<Point> points) {
		Collections.sort(points, new Comparator<Point>() {
			@Override
			public int compare(Point o1, Point o2) {

				if (o1.x < o2.x)
					return -1;
				if (o1.x > o2.x)
					return 1;

				return 0;
			}
		});
		return points;
	}

	/**
	 * Nalazi projekciju tacke na ravan
	 * 
	 * tako sto nalazi normalnu pravu na zadatu pravu kroz zadatu tacku i odredjuje
	 * presek prvobitne prave i novodobijene prave.
	 * 
	 * 
	 * @param lineStart
	 * @param lineEnd
	 * @param perpendicularPoint
	 * @param result
	 */
	public static void getPerpendicularPoint(Point lineStart, Point lineEnd, Point perpendicularPoint, Point result) {

		// Koeficijent pravca gornje linije
		double k = (lineStart.y - lineEnd.y) / (lineStart.x - lineEnd.x);

		// n gornje linije
		double n = lineEnd.y - lineEnd.x * k;

		// Koeficijent pravca normalne linije
		double kN = -1 / k;

		// Jednacina prave
		// y = kx + n
		// Jednacina normalne prave
		// y = kNx + nN
		// Jednacina normalne prave kroz centar nisanjenja
		// yC = kN(xC) + nN
		// nN = yC - kN(xC)
		double nN = perpendicularPoint.y - kN * perpendicularPoint.x;

		// Sistem od dve jednacine
		// y - kx = n
		// yC - kNxC = nN

		RealMatrix coefficients = new Array2DRowRealMatrix(new double[][] { { -k, 1 }, { -kN, 1 } }, false);
		DecompositionSolver solver = new LUDecomposition(coefficients).getSolver();
		RealVector constants = new ArrayRealVector(new double[] { n, nN }, false);
		RealVector solution = solver.solve(constants);

		result.x = solution.getEntry(0);
		result.y = solution.getEntry(1);
	}

	/**
	 * Racuna rastojanje dve zadate tacke
	 * 
	 * @param p1
	 * @param p2
	 * @return
	 */
	public static double pointsDistance(Point p1, Point p2) {
		return Math.sqrt(Math.pow((p1.x - p2.x), 2) + Math.pow((p1.y - p2.y), 2));
	}

}
