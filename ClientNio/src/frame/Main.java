package frame;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;
import java.util.Set;

import javax.swing.JOptionPane;

public class Main {

	public static void main(String[] args) {
		String input = "";
		Scanner scanner = new Scanner(System.in);
		input = scanner.nextLine();
		if (input.equals("client")) {
			ByteBuffer rcvBuffer = ByteBuffer.allocateDirect(1024);
			Charset charset = Charset.forName("UTF-8");
			CharsetDecoder decoder = charset.newDecoder();
			int port = 5500;
			Login user = new Login();
			String user_info = "";
			while (!user.correct()) {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			user_info = user.getinfo();
			int usedport = user.getwaitPort();
			int mid = user_info.indexOf('*');
			user_info = user_info.substring(1, mid);
			final String leave = user_info;
			// System.out.print(user_info);
			System.out.print(usedport);
			String ownname = user.getname();
			/// add new thread to wait connect
			///

			// list of friend
			Friendlist test = new Friendlist(user_info, ownname, usedport);
			test.frame.setVisible(true);
			test.frame.addWindowListener(new WindowListener() {

				public void windowClosed(WindowEvent e) {
					// TODO Auto-generated method stub
				}

				@Override
				public void windowOpened(WindowEvent e) {
					// TODO Auto-generated method stub

				}

				public void windowClosing(WindowEvent e) {
					// TODO Auto-generated method stub
					try (SocketChannel socketChannel = SocketChannel.open()) {
						if (socketChannel.isOpen()) {
							socketChannel.configureBlocking(true);
							socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 128 * 1024);
							socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 128 * 1024);
							socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
							socketChannel.setOption(StandardSocketOptions.SO_LINGER, 5);
							socketChannel.connect(new InetSocketAddress("127.0.0.1", port));
							if (socketChannel.isConnected()) {
								ByteBuffer sendBuffer = ByteBuffer.wrap(String.valueOf("6" + leave + "*").getBytes());
								socketChannel.write(sendBuffer);
								socketChannel.close();
							} else {
								System.out.println("連線失敗!");
							}
						} else {
							System.out.println("socket channel 開啟失敗!");
						}
					} catch (IOException ex) {
						System.err.println(ex);
					}
					/*
					 * try { Socket client = new Socket(); InetSocketAddress ad = new
					 * InetSocketAddress("127.0.0.1",5500); client.connect(ad); BufferedWriter
					 * out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
					 * out.write("6"+leave+"\r\n\r\n"); //sent account out.flush(); client.close();
					 * } catch (IOException e1) { e1.printStackTrace(); }
					 */
					// System.out.println("close");
				}

				@Override
				public void windowIconified(WindowEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void windowDeiconified(WindowEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void windowActivated(WindowEvent e) {
					// TODO Auto-generated method stub

				}

				@Override
				public void windowDeactivated(WindowEvent e) {
					// TODO Auto-generated method stub

				}

			});
		}
		else {
			try {
				new Server();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
