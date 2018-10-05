package com.webcamshooter.controller;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.opencv.core.Core;

public class Main {

	public static MainForm mf;

	public static void displayError(String msg) {
		JOptionPane.showMessageDialog(mf, msg, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException, UnsupportedLookAndFeelException {
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		mf = new MainForm();

		// Exception handler
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				if (e instanceof CameraShooterException) {
					displayError(e.getMessage());
				} else {
					displayError("Unexpected error has occured");
				}
				e.printStackTrace();
			}
		});

		// Close listener
		mf.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				mf.stopCapture();
				Connection.getInstance().disconnect();
			}
		});

		mf.setVisible(true);
		mf.startCapture();
	}

}
