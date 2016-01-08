package com.hyun.test.mouse.server;
import java.awt.AWTException;
import java.awt.Container;
import java.awt.GridLayout;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Robot;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class Server extends JFrame implements ActionListener {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final int PORT = 10000;
	final String MESSAGE_FULL = "FULL";
	final String MESSAGE_SOUNDUP = "SOUNDUP";
	final String MESSAGE_SOUNDDOWN = "SOUNDDOWN";
	final String MESSAGE_LEFT = "LEFT";
	final String MESSAGE_RIGHT = "RIGHT";
	final String MESSAGE_MOUSELEFT = "MOUSELEFT";
	final String MESSAGE_MOUSERIGHT = "MOUSERIGHT";
	final String MOUSE = "MOUSE";
	
	private Container con;
	private JLabel ipInfo;
	private JButton serverStart;
	private JButton serverStop;
	private JComboBox jcb;
	private GridLayout gl;
	
	private ThreadServer threadServer;
	
	public void serverStart() {
		gl = new GridLayout(9, 1);
		init();
		start();
		setSize(300, 200);
		setVisible(true);
	}

	private void start() {
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		serverStart.addActionListener(this);
		serverStop.addActionListener(this);
	}

	private void init() {
		setupLocalInfo();
		con = getContentPane();
		ipInfo = new JLabel("IP");
		serverStart = new JButton("Server Start");
		serverStop = new JButton("Server Stop");
		serverStop.setEnabled(false);
		con.setLayout(gl);
		con.add(serverStart);
		con.add(serverStop);
		con.add(ipInfo);
		con.add(jcb);
	}

	private void setupLocalInfo() {
		InetAddress ia[] = null;
		try {
			String hostName = InetAddress.getLocalHost().getHostName();
			ia = InetAddress.getAllByName(hostName);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		jcb = new JComboBox();
		InetAddress ainetaddress[];
		int j = (ainetaddress = ia).length;
		for (int i = 0; i < j; i++) {
			InetAddress inet = ainetaddress[i];
			jcb.addItem((new StringBuilder()).append(inet.getHostAddress())	.toString());
		}

	}

	public void actionPerformed(ActionEvent e) {
		if ((JButton) e.getSource() == serverStart) {
			threadServer = new ThreadServer();
			threadServer.start();
			serverStart.setEnabled(false);
			serverStop.setEnabled(true);
		}
		if ((JButton) e.getSource() == serverStop) {
			threadServer.interrupt();
			serverStop.setEnabled(false);
			serverStart.setEnabled(true);
		}
	}
	
	class ThreadServer extends Thread {

		Robot robot;
		PointerInfo a;
		Point b;
		String split[];
		DatagramSocket socket;
		DatagramPacket packet;
		boolean connected;

		public ThreadServer() {
			super();
			connected = false;
			try {
				robot = new Robot();
			} catch (AWTException e) {
				e.printStackTrace();
			}
			try {
				socket = new DatagramSocket(PORT);
				connected = true;
			} catch (SocketException e) {
				e.printStackTrace();
			}
			System.out.println("Server Start");
		}

		public void interrupt() {
			socket.close();
			System.out.println("Server Stop");
		}

		public void run() {
			try {
				do {
					byte data[] = new byte[100];
					packet = new DatagramPacket(data, data.length);
					if(!socket.isClosed()){
						socket.receive(packet);
					}
					process((new String(packet.getData())).trim());
				} while (true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private void process(String message) {
			a = MouseInfo.getPointerInfo();
			b = a.getLocation();
			int x = (int) b.getX();
			int y = (int) b.getY();
			split = message.split(":");

			if (split[1].compareTo(MESSAGE_MOUSELEFT) == 0) {
				robot.mousePress(16);
				robot.mouseRelease(16);
			} else if (split[1].compareTo(MESSAGE_MOUSERIGHT) == 0) {
				robot.mousePress(4);
				robot.mouseRelease(4);
			} else if (split[1].compareTo(MESSAGE_LEFT) == 0) {
				robot.keyPress(37);
				robot.keyRelease(37);
			} else if (split[1].compareTo(MESSAGE_RIGHT) == 0) {
				robot.keyPress(39);
				robot.keyRelease(39);
			} else if (split[1].compareTo(MESSAGE_FULL) == 0) {
				robot.keyPress(10);
				robot.keyRelease(10);
			} else if (split[1].compareTo(MESSAGE_SOUNDUP) == 0) {
				robot.keyPress(38);
				robot.keyRelease(38);
			} else if (split[1].compareTo(MESSAGE_SOUNDDOWN) == 0) {
				robot.keyPress(40);
				robot.keyRelease(40);
			} else if (split[1].compareTo(MOUSE) == 0) {
				robot.mouseMove(x + Integer.parseInt(split[2]), y + Integer.parseInt(split[3]));
			}
		}
	}
}
