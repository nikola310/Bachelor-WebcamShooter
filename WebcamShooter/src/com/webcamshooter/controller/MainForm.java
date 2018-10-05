package com.webcamshooter.controller;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;

import net.miginfocom.swing.MigLayout;

public class MainForm extends JFrame implements ConnectionStateListener {

	private static final long serialVersionUID = -7430908250097680303L;

	/**
	 * Video stream sa kamere se konstantno analizira na posebnoj niti i informacije
	 * o poziciji nisana se salju na swing main thread. Zapisujemo i vreme slanja
	 * koordinata pucnja kako bismo u trenutku pucanja znali koliko su poslednje
	 * koordinate "sveze". Konstanta ispod kaze koliko milisekundi zelimo da
	 * smatramo informaciju o koordinatama validnom
	 */
	public static final int DELTA_TIME_VALID_SHOT = 500;

	private JLabel lblCamera;

	private JTextField tfIpPort;
	private JTextField txtPlayerName;

	private JButton btnConnect;
	private JButton btnDisconnect;
	private JButton btnStartCapture;
	private JButton btnStopCapture;

	private CameraPanel cameraPanel;
	private JPanel panelClickTest;

	private JSlider sliderDistance;
	private JSlider sliderCalibX;
	private JSlider sliderCalibY;

	private JComboBox<String> cmbCameraNumber;

	private int lastX, lastY;
	private long shotTime = 0;
	private Object semaphore = new Object();

	private Thread captureThread = null;
	private CaptureWorker captureWorker;

	/**
	 * 
	 */
	@Override
	public void connectionOpened() {
		btnConnect.setEnabled(false);
		btnDisconnect.setEnabled(true);
	}

	/**
	 * 
	 */
	@Override
	public void connectionClosed() {
		btnConnect.setEnabled(true);
		btnDisconnect.setEnabled(false);
	}

	/**
	 * 
	 * @param lastX
	 * @param lastY
	 * @param shotTime
	 */
	public void updateShootCoords(int lastX, int lastY, long shotTime) {
		synchronized (semaphore) {
			this.lastX = lastX;
			this.lastY = lastY;
			this.shotTime = shotTime;
		}
	}

	/**
	 * 
	 * @param percW
	 * @param percH
	 */
	public void shoot(int percW, int percH) {
		synchronized (semaphore) {
			if (System.currentTimeMillis() - shotTime < DELTA_TIME_VALID_SHOT) {
				Connection.getInstance().sendMessage(txtPlayerName.getText() + "|SHOOT|"
						+ (percW + getSliderCalibX().getValue()) + "|" + (percH + getSliderCalibY().getValue()));
			} else {
				System.out.println("Invalid shot");
			}
		}
	}

	/**
	 * 
	 */
	public MainForm() {
		Connection.getInstance().setConnectionStateListener(this);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("Master");
		setResizable(false);
		setSize(1140, 600);
		getContentPane().setLayout(new MigLayout("", "[700px:n,grow][200px:200px:200px,grow][200px]", "[grow][]"));
		cameraPanel = new CameraPanel();
		cameraPanel.setBackground(Color.WHITE);
		getContentPane().add(cameraPanel, "cell 0 0,grow");

		btnDisconnect = new JButton("Disconnect");
		btnDisconnect.setEnabled(false);
		JPanel panel_1 = new JPanel();
		getContentPane().add(panel_1, "cell 2 0,growx,aligny top");
		panel_1.setLayout(new MigLayout("", "[grow]", "[][grow][]"));
		JPanel panel = new JPanel();
		panel.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_1.add(panel, "cell 0 0,grow");
		panel.setLayout(new MigLayout("", "[grow]", "[][][]"));
		txtPlayerName = new JTextField();
		txtPlayerName.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(txtPlayerName, "cell 0 0,growx");
		txtPlayerName.setText("Player Name");
		txtPlayerName.setColumns(10);
		tfIpPort = new JTextField();
		tfIpPort.setHorizontalAlignment(SwingConstants.CENTER);
		panel.add(tfIpPort, "cell 0 1,growx");
		// tfIpPort.setText("127.0.0.1:13001");
		tfIpPort.setText("192.168.1.20:13001");
		tfIpPort.setColumns(10);
		btnConnect = new JButton("Connect");
		panel.add(btnDisconnect, "cell 0 2");
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_1.add(panel_2, "cell 0 1,grow");
		panel_2.setLayout(new MigLayout("", "[]", "[][][][]"));
		JLabel lblCalibX = new JLabel("calib X");
		panel_2.add(lblCalibX, "cell 0 0");
		sliderCalibX = new JSlider();
		sliderCalibX.setMajorTickSpacing(2);
		sliderCalibX.setValue(0);
		sliderCalibX.setMinimum(-2000);
		sliderCalibX.setMaximum(2000);
		panel_2.add(sliderCalibX, "cell 0 1");
		JLabel lblCalibY = new JLabel("calib Y");
		panel_2.add(lblCalibY, "cell 0 2");
		sliderCalibY = new JSlider();
		sliderCalibY.setValue(0);
		sliderCalibY.setMinimum(-2000);
		sliderCalibY.setMaximum(2000);
		sliderCalibY.setMajorTickSpacing(2);
		panel_2.add(sliderCalibY, "flowy,cell 0 3");

		JSeparator separator_1 = new JSeparator();
		panel_2.add(separator_1, "cell 0 8,growx");

		sliderDistance = new JSlider();
		sliderDistance.setToolTipText("Distance");
		sliderDistance.setPaintTicks(true);
		sliderDistance.setMajorTickSpacing(100);
		sliderDistance.setMaximum(1000);
		sliderDistance.setValue(200);
		panel_2.add(sliderDistance, "cell 0 9,growx");
		panelClickTest = new JPanel();
		panelClickTest.setForeground(Color.PINK);
		panelClickTest.setBackground(Color.PINK);
		panel_1.add(panelClickTest, "cell 0 2,growx");
		panelClickTest.setBorder(new LineBorder(new Color(0, 0, 0)));
		panelClickTest.setLayout(new MigLayout("", "[grow]", "[120px]"));
		btnStartCapture = new JButton("Start Capture");
		getContentPane().add(btnStartCapture, "flowx,cell 0 1");
		btnStopCapture = new JButton("Stop Capture");
		getContentPane().add(btnStopCapture, "cell 0 1");

		panel.add(btnConnect, "flowx,cell 0 2,alignx left");

		lblCamera = new JLabel("Camera:");
		getContentPane().add(lblCamera, "cell 0 1");

		cmbCameraNumber = new JComboBox<String>();
		cmbCameraNumber.setModel(new DefaultComboBoxModel<String>(new String[] { "0", "1", "2", "3" }));
		cmbCameraNumber.setToolTipText("Camera number:");
		getContentPane().add(cmbCameraNumber, "cell 0 1");

		initListeners();
	}

	/**
	 * 
	 */
	private void initListeners() {

		btnConnect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Connection.getInstance().connect(txtPlayerName.getText(), tfIpPort.getText());
			}
		});

		btnDisconnect.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Connection.getInstance().disconnect();
			}
		});

		panelClickTest.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				JPanel p = (JPanel) e.getComponent();
				int percW = (int) ((float) e.getX() / (float) p.getWidth() * 10000);
				int percH = (int) ((float) e.getY() / (float) p.getHeight() * 10000);
				shotTime = System.currentTimeMillis();
				shoot(percW, percH);
			}
		});

		btnStartCapture.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				startCapture();
			}
		});

		btnStopCapture.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopCapture();
			}
		});

		cameraPanel.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				shoot(lastX, lastY);
			}
		});

	}

	public void startCapture() {
		if (captureThread == null || !captureThread.isAlive()) {
			captureWorker = new CaptureWorker(cameraPanel, getCmbCameraNumber().getSelectedIndex());
			captureThread = new Thread(captureWorker);
			captureThread.start();
			System.out.println("Started thread");
			this.btnStartCapture.setEnabled(false);
			this.btnStopCapture.setEnabled(true);
		} else {
			System.out.println("Error in starting capture thread:" + captureThread);
		}

	}

	public void stopCapture() {
		if (captureThread != null) {
			captureThread.interrupt();
			this.btnStartCapture.setEnabled(true);
			this.btnStopCapture.setEnabled(false);
		}
	}

	public JButton getBtnConnect() {
		return btnConnect;
	}

	public JButton getBtnDisconnect() {
		return btnDisconnect;
	}

	public JSlider getSliderDistance() {
		return sliderDistance;
	}

	public JSlider getSliderCalibX() {
		return sliderCalibX;
	}

	public JSlider getSliderCalibY() {
		return sliderCalibY;
	}

	public JComboBox<String> getCmbCameraNumber() {
		return cmbCameraNumber;
	}

	public JButton getBtnStopCapture() {
		return btnStopCapture;
	}

}
