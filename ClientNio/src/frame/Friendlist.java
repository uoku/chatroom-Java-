package frame;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import javax.swing.SwingConstants;

import javax.swing.ScrollPaneConstants;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class Friendlist extends JFrame {

	public JFrame frame;
	JButton search;
	JButton refresh;
	JButton newfriend;
	JTextField searchbox;
	String info;
	String friend_info[];
	JPanel panel_3;
	String ownname = "";
	int num_friend=0;
	int goin = 0;
	static int port = 5500;
	ByteBuffer rcvBuffer = ByteBuffer.allocateDirect(1024);
	Charset charset = Charset.forName("UTF-8");
	CharsetDecoder decoder = charset.newDecoder();
	ArrayList<Chatroom> friend_w = new ArrayList<Chatroom>();
	int useport = 0;

	public Friendlist(String info, String name, int useport) {
		this.info = info;
		this.useport = useport;
		ownname = name;
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		initialize();
		setButton();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		// new thread to listen from other client
		new Thread(new Runnable() {

			@Override
			public void run() {
				ServerSocketChannel serverChannel;
				Selector selector;

				try {
					serverChannel = ServerSocketChannel.open();
					selector = Selector.open();

					if (serverChannel.isOpen() && selector.isOpen()) {
						serverChannel.configureBlocking(false);
						serverChannel.setOption(StandardSocketOptions.SO_RCVBUF, 256 * 1024);
						serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
						serverChannel.bind(new InetSocketAddress(useport));
						serverChannel.register(selector, SelectionKey.OP_ACCEPT);
						String buf = "";
						while (true) {
							selector.select();
							Set<SelectionKey> readyKeys = selector.selectedKeys();
							Iterator<SelectionKey> iterator = readyKeys.iterator();
							while (iterator.hasNext()) {
								SelectionKey key = iterator.next();
								iterator.remove();
								try {
									if (key.isAcceptable()) {
										ServerSocketChannel server = (ServerSocketChannel) key.channel();
										SocketChannel client = server.accept();
										client.configureBlocking(false);
										SelectionKey clientKey = client.register(selector, SelectionKey.OP_READ);
										ByteBuffer buffer = ByteBuffer.allocate(1024);
										clientKey.attach(buffer);
									}
									if (key.isReadable()) {
										SocketChannel client = (SocketChannel) key.channel();
										ByteBuffer output = (ByteBuffer) key.attachment();
										client.read(output);
										//
										buf = new String(Arrays.copyOfRange(output.array(), 0, output.limit()));
										// System.out.println(buf);
										SelectionKey clientKey = client.register(selector, SelectionKey.OP_WRITE);
									}
									if (key.isWritable()) {
										SocketChannel client = (SocketChannel) key.channel();
										// ---------------------------
										String temp[] = buf.split(":");
										String name_from = temp[0];
										String msg = temp[1];
										String from_port=temp[2];
										boolean have = false;
										for (int i = 0; i < friend_w.size(); i++) {
											// System.out.println(friend_w.get(i).friendname+" "+name_from);
											if (friend_w.get(i).friendname.equals(name_from)) {
												have=true;
												String m = friend_w.get(i).name;
												String u = friend_w.get(i).friendname;
												int p = friend_w.get(i).port_ff;
												if (friend_w.get(i).frame.isVisible() == false) {
													Chatroom x = new Chatroom(m, u, p,useport);
													friend_w.set(i, x);
													friend_w.get(i).initialize();
												}
												friend_w.get(i).textArea.append(name_from + ":" + msg);
												friend_w.get(i).textArea.revalidate();
											}
										}
										if(have==false) {
											Chatroom x = new Chatroom(ownname, name_from, Integer.valueOf(from_port),useport);
											friend_w.add(x);
											friend_w.get(friend_w.size()-1).initialize();
											friend_w.get(friend_w.size()-1).textArea.append(name_from + ":" + msg);
											friend_w.get(friend_w.size()-1).textArea.revalidate();
										}
										// System.out.println(buf);
										key.cancel();
									}
								} catch (IOException e) {
									key.cancel();
									try {
										key.channel().close();
									} catch (IOException ex) {
									}
								}
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		}).start();
		//
		//
		friend_info = new String[10];
		for (int i = 0; i < 10; i++) {
			friend_info[i] = "";
		}
		//
		frame = new JFrame();
		frame.setTitle(ownname);
		frame.setBounds(100, 100, 400, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.X_AXIS));

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 365, 0 };
		gbl_panel.rowHeights = new int[] { 50, 50, 500 };
		gbl_panel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 1.0, 1.0, 1.0 };
		panel.setLayout(gbl_panel);

		// search in panel1
		searchbox = new JTextField(15);
		search = new JButton("搜尋");
		refresh = new JButton("重整");
		newfriend = new JButton("新增好友");
		//

		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 0;
		gbc_panel_1.gridy = 0;
		panel.add(panel_1, gbc_panel_1);
		panel_1.add(searchbox);
		panel_1.add(search);
		panel_1.add(refresh);
		panel_1.add(newfriend);

		panel_3 = new JPanel();
		GridBagConstraints gbc_panel_3 = new GridBagConstraints();
		gbc_panel_3.fill = GridBagConstraints.BOTH;
		gbc_panel_3.gridx = 0;
		gbc_panel_3.gridy = 2;
		panel.add(panel_3, gbc_panel_3);
		panel_3.setLayout(new GridLayout(5, 1));
	}

	public void setButton() {
		search.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String buf = new String("");
				/*
				 * try { Socket client = new Socket(); InetSocketAddress ad = new
				 * InetSocketAddress("192.168.56.1",port); client.connect(ad); BufferedWriter
				 * out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
				 * out.write("3"+info+"*"+searchbox.getText()+"\r\n\r\n"); //sent account
				 * out.flush(); // wait receive BufferedReader in = new BufferedReader(new
				 * InputStreamReader(client.getInputStream())); String message =null;
				 * while(((message=in.readLine())!=null)) { buf=buf+message; }
				 * System.out.println(buf); client.close(); } catch (IOException e1) { // TODO
				 * Auto-generated catch block e1.printStackTrace(); }
				 */
				try (SocketChannel socketChannel = SocketChannel.open()) {
					if (socketChannel.isOpen()) {
						socketChannel.configureBlocking(true);
						socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 128 * 1024);
						socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 128 * 1024);
						socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
						socketChannel.setOption(StandardSocketOptions.SO_LINGER, 5);
						socketChannel.connect(new InetSocketAddress("********", port)); // change to your ip
						if (socketChannel.isConnected()) {
							ByteBuffer sendBuffer = ByteBuffer
									.wrap(String.valueOf("3" + info + "*" + searchbox.getText() + "*").getBytes());
							socketChannel.write(sendBuffer);
							socketChannel.read(rcvBuffer);
							rcvBuffer.flip();
							CharBuffer charBuffer = decoder.decode(rcvBuffer);
							// System.out.println(charBuffer);
							buf = charBuffer.toString();
							rcvBuffer.clear();
						} else {
							System.out.println("連線失敗!");
						}
					} else {
						System.out.println("socket channel 開啟失敗!");
					}
				} catch (IOException ex) {
					System.err.println(ex);
				}
				// --------------------------
				// save friend_info to matrix
				for (int i = 0; i < 10; i++) {
					friend_info[i] = "";
				}
				if (!(buf.equals("no this person")) && !(buf.equals("no friend"))) {
					String temp[] = buf.split("\\*");
					num_friend = temp.length;
					for (int i = 0; i < temp.length; i++) {
						friend_info[i] = temp[i];
					}
					for (int i = 0; i < temp.length; i++) {
						int port_ff = Integer.valueOf(friend_info[i].substring(0, 4));
						boolean ttt = false;
						// System.out.println(port_ff);
						for (int k = 0; k < friend_w.size(); k++) {
							if (friend_w.get(k).port_ff == port_ff) {
								ttt = true;
							}
						}
						if (ttt == false) {
							Chatroom tt = new Chatroom(ownname,
							friend_info[i].substring(4, friend_info[i].length() - 1), port_ff,useport);
							friend_w.add(tt);
						}
					}
				} else {
					System.out.println("friend_info error");
				}
				// create friend box
				panel_3.removeAll();
				panel_3.repaint();
				for (int i = 0; i < num_friend; i++) {
					String name = friend_info[i].substring(4, friend_info[i].length() - 1);
					int online = Integer.valueOf(friend_info[i].substring(friend_info[i].length() - 1));
					JButton label = new JButton(name);
					JPanel mpanel = new JPanel();
					JPanel panel = new JPanel();
					JLabel label1 = new JLabel();
					ImageIcon image = new ImageIcon(
							"C:\\Users\\baumi\\eclipse-workspace\\Client\\src\\frame\\button.png");
					label1.setIcon(image);
					if (online == 0) {
						label.setEnabled(false);
					} else {
						final int n = i;
						label.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								if (friend_w.get(n).frame.isVisible() == false) {
								// here to add connect to
									String m = friend_w.get(n).name;
									String u = friend_w.get(n).friendname;
									int p = friend_w.get(n).port_ff;
									Chatroom x = new Chatroom(m, u, p,useport);
									friend_w.set(n, x);
									friend_w.get(n).initialize();
								}
							}
						});
					}
					panel.add(label);
					panel.setSize(360, 50);

					mpanel.add(label1);
					mpanel.add(panel);
					panel_3.add(mpanel);
				}
				frame.revalidate();
				panel_3.revalidate();
			}
		});

		refresh.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String buf = new String("");
				/*
				 * try { Socket client = new Socket(); InetSocketAddress ad = new
				 * InetSocketAddress("192.168.56.1",port); client.connect(ad); BufferedWriter
				 * out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
				 * out.write("4"+info+"\r\n\r\n"); //sent account out.flush(); // wait receive
				 * BufferedReader in = new BufferedReader(new
				 * InputStreamReader(client.getInputStream())); String message =null;
				 * while(((message=in.readLine())!=null)) { buf=buf+message; }
				 * System.out.println(buf); client.close(); } catch (IOException e1) { // TODO
				 * Auto-generated catch block e1.printStackTrace(); }
				 */
				//
				try (SocketChannel socketChannel = SocketChannel.open()) {
					if (socketChannel.isOpen()) {
						socketChannel.configureBlocking(true);
						socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 128 * 1024);
						socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 128 * 1024);
						socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
						socketChannel.setOption(StandardSocketOptions.SO_LINGER, 5);
						socketChannel.connect(new InetSocketAddress("*********", port));// change to tour ip
						if (socketChannel.isConnected()) {
							ByteBuffer sendBuffer = ByteBuffer.wrap(String.valueOf("4" + info + "*").getBytes());
							socketChannel.write(sendBuffer);
							socketChannel.read(rcvBuffer);
							rcvBuffer.flip();
							CharBuffer charBuffer = decoder.decode(rcvBuffer);
							System.out.println(charBuffer);
							buf = charBuffer.toString();
							rcvBuffer.clear();
						} else {
							System.out.println("連線失敗!");
						}
					} else {
						System.out.println("socket channel 開啟失敗!");
					}
				} catch (IOException ex) {
					System.err.println(ex);
				}
				// save friend_info to matrix
				if (!(buf.equals("no this person")) && !(buf.equals("no friend"))) {
					String temp[] = buf.split("\\*");
					num_friend = temp.length;
					for (int i = 0; i < temp.length; i++) {
						friend_info[i] = temp[i];
					}
					for (int i = 0; i < temp.length; i++) {
						int port_ff = Integer.valueOf(friend_info[i].substring(0, 4));
						boolean ttt = false;
						// System.out.println(port_ff);
						for (int k = 0; k < friend_w.size(); k++) {
							if (friend_w.get(k).port_ff == port_ff) {
								ttt = true;
							}
						}
						if (ttt == false) {
							Chatroom tt = new Chatroom(ownname,
							friend_info[i].substring(4, friend_info[i].length() - 1), port_ff,useport);
							friend_w.add(tt);
						}
					}
				} else {
					System.out.println("friend_info error");
				}
				// create friend box
				panel_3.removeAll();
				for (int i = 0; i < num_friend; i++) {
					String name = friend_info[i].substring(4, friend_info[i].length() - 1);
					int online = Integer.valueOf(friend_info[i].substring(friend_info[i].length() - 1));
					JButton label = new JButton(name);
					JPanel mpanel = new JPanel();
					JPanel panel = new JPanel();
					JLabel label1 = new JLabel();
					ImageIcon image = new ImageIcon(
							"C:\\Users\\baumi\\eclipse-workspace\\Client\\src\\frame\\button.png");
					label1.setIcon(image);
					if (online == 0) {
						label.setEnabled(false);
					} else {
						final int n = i;
						label.addActionListener(new ActionListener() {

							@Override
							public void actionPerformed(ActionEvent e) {
								// here to add connect to
								if (friend_w.get(n).frame.isVisible() == false) {
									String m = friend_w.get(n).name;
									String u = friend_w.get(n).friendname;
									int p = friend_w.get(n).port_ff;
									Chatroom x = new Chatroom(m, u, p,useport);
									friend_w.set(n, x);
									friend_w.get(n).initialize();
								}
							}
						});
					}
					panel.add(label);
					panel.setSize(360, 50);

					mpanel.add(label1);
					mpanel.add(panel);
					panel_3.add(mpanel);
				}
				frame.revalidate();
			}
		});
		newfriend.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String buf = "";
				/*
				 * try { Socket client = new Socket(); InetSocketAddress ad = new
				 * InetSocketAddress("192.168.56.1",port); client.connect(ad); BufferedWriter
				 * out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
				 * String friend_ac = JOptionPane.showInputDialog("請輸入好友帳號:");
				 * out.write("5"+info+"*"+friend_ac+"\r\n\r\n"); //new friend out.flush();
				 * BufferedReader in = new BufferedReader(new
				 * InputStreamReader(client.getInputStream())); String message =null;
				 * while(((message=in.readLine())!=null)) { buf=buf+message; }
				 * System.out.println(buf); in.close(); out.close(); client.close();
				 * if(buf.equals("no this person")) { JOptionPane.showMessageDialog(frame,
				 * "error:無此人"); } else if(buf.equals("success")){ refresh.doClick(); } } catch
				 * (IOException e1) { e1.printStackTrace(); }
				 */
				//
				try (SocketChannel socketChannel = SocketChannel.open()) {
					if (socketChannel.isOpen()) {
						socketChannel.configureBlocking(true);
						socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 128 * 1024);
						socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 128 * 1024);
						socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
						socketChannel.setOption(StandardSocketOptions.SO_LINGER, 5);
						socketChannel.connect(new InetSocketAddress("*********", port));// change to your ip
						if (socketChannel.isConnected()) {
							String friend_ac = JOptionPane.showInputDialog("請輸入好友帳號:");
							ByteBuffer sendBuffer = ByteBuffer
									.wrap(String.valueOf("5" + info + "*" + friend_ac + "*").getBytes());
							socketChannel.write(sendBuffer);
							socketChannel.read(rcvBuffer);
							rcvBuffer.flip();
							CharBuffer charBuffer = decoder.decode(rcvBuffer);
							// System.out.println(charBuffer);
							buf = charBuffer.toString();
							rcvBuffer.clear();
							if (buf.equals("no this person")) {
								JOptionPane.showMessageDialog(frame, "error:無此人");
							} else {
								String temp[] = buf.split("\\*");
								String fac = temp[0];
								String pp = temp[1];
								System.out.print(fac + " " + pp);
								Chatroom tt = new Chatroom(ownname, fac, Integer.valueOf(pp),useport);
								friend_w.add(tt);
							}
						} else {
							System.out.println("連線失敗!");
						}
					} else {
						System.out.println("socket channel 開啟失敗!");
					}
				} catch (IOException ex) {
					System.err.println(ex);
				}

			}
		});

	}

}
