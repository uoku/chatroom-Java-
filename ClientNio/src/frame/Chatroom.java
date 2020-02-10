package frame;

import java.awt.EventQueue;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Arrays;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;

import java.awt.BorderLayout;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JTextArea;
import java.awt.Font;
import java.awt.Frame;

import javax.swing.JScrollBar;
import javax.swing.ScrollPaneConstants;
import javax.swing.WindowConstants;

public class Chatroom  extends JFrame{

	public JFrame frame;
	public JTextField textField_1;
	public String name="";
	public boolean t=false;
	public SocketChannel socketChannel;
	int port_ff;
	public JTextArea textArea;
	public String friendname;
	public int ownport;
	public Chatroom(String name,String friendname,int po,int ownport){
		this.name=name;
		this.ownport=ownport;
		this.friendname=friendname;
		port_ff=po;
		frame = new JFrame();
	}
	public void initialize() {
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		frame.setTitle(name);
		
		textArea = new JTextArea();
		textArea.setEnabled(false);
		
		JScrollPane scrollPane_1 = new JScrollPane(textArea);
		scrollPane_1.setBounds(0, 0, 426, 221);
		frame.getContentPane().add(scrollPane_1);
		
		textField_1 = new JTextField();
		textField_1.setBounds(0, 231, 304, 32);
		frame.getContentPane().add(textField_1);
		textField_1.setColumns(10);
		
		JButton btnEnter = new JButton("Enter");
		btnEnter.setBounds(314, 231, 112, 32);
		frame.getContentPane().add(btnEnter);
		frame.setVisible(true);
		
		btnEnter.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				String text = textField_1.getText();
				text=name+":"+text+"\n";
				textArea.append(text);
				text=text+":"+Integer.toString(ownport)+":";
				try (SocketChannel socketChannel = SocketChannel.open()) {
					if (socketChannel.isOpen()) {
						String buf;
						socketChannel.configureBlocking(true);
						socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 128 * 1024);
						socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 128 * 1024);
						socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
						socketChannel.setOption(StandardSocketOptions.SO_LINGER, 5);
						socketChannel.connect(new InetSocketAddress("127.0.0.1", port_ff));
						if (socketChannel.isConnected()) {
							InetAddress ip = InetAddress.getLocalHost();
							ByteBuffer sendBuffer = ByteBuffer.wrap(String.valueOf(text).getBytes());
							socketChannel.write(sendBuffer);
						} else {
							System.out.println("連線失敗!");
						}
					} else {
						System.out.println("socket channel 開啟失敗!");
					}
					socketChannel.close();
				} catch (IOException ex) {
					textArea.append("對方已離線\n");
				}
				textField_1.setText("");
			}
		});
	}
}
