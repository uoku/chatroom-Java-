package frame;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;



public class Login extends JFrame{
	private static int port = 5500;
	private String info="";
	private int waitport=0;
	JButton b1;
	JButton b2;
	JTextField text1,text2;
	String status="";
	String ownname="";
	public Login() {
		this.setTitle(" 登入");
		this.setLayout(new GridLayout(3,1));
		this.setSize(250,200);
		this.setLocation(550,200);
		JPanel panel =new JPanel();
		JPanel panel1 =new JPanel();
		JPanel panel2 =new JPanel();
		b1 = new JButton("登入");
		b2 = new JButton("創建");
		panel.add(b1);
		panel.add(b2);
		Label label1=new Label("帳號：");
		Label label2=new Label("密碼：");
		text1 = new JTextField(13);
		text2 = new JPasswordField(13);
		panel1.add(label1);
		panel1.add(text1);
		panel2.add(label2);
		panel2.add(text2);
		this.add(panel1);
		this.add(panel2);

		ByteBuffer rcvBuffer = ByteBuffer.allocateDirect(1024);
		Charset charset = Charset.forName("UTF-8");
		CharsetDecoder decoder = charset.newDecoder();
		
		b1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try (SocketChannel socketChannel = SocketChannel.open()) {
					if (socketChannel.isOpen()) {
						String buf;
						socketChannel.configureBlocking(true);
						socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 128 * 1024);
						socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 128 * 1024);
						socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
						socketChannel.setOption(StandardSocketOptions.SO_LINGER, 5);
						socketChannel.connect(new InetSocketAddress("127.0.0.1", port));
						if (socketChannel.isConnected()) {
							InetAddress ip = InetAddress.getLocalHost();
							ByteBuffer sendBuffer = ByteBuffer.wrap(String.valueOf("0"+text1.getText()+"*"+text2.getText()+"*"+ip+"*").getBytes());
							info=new String("0"+text1.getText()+"*"+text2.getText());
							socketChannel.write(sendBuffer);
							socketChannel.read(rcvBuffer);
							rcvBuffer.flip();
							CharBuffer charBuffer = decoder.decode(rcvBuffer);
							//System.out.println(charBuffer);
							buf=charBuffer.toString();
							System.out.println(buf);
							if(buf.equals("error")) {
					    		JOptionPane.showMessageDialog(null,"錯誤");
					    	}
					    	else { 
					    		String temp[]=buf.split("\\*");
					    		ownname=temp[1];
					    		waitport=Integer.valueOf(temp[0]);
					    		dispose();
					    		buf="correct";
					    	}
					    	status=buf;
					    	rcvBuffer.clear();
						} else {
							System.out.println("連線失敗!");
						}
					} else {
						System.out.println("socket channel 開啟失敗!");
					}
					socketChannel.close();
				} catch (IOException ex) {
					System.err.println(ex);
				}
				//-----------------------------------
				/*
				try {
					Socket client = new Socket();
					InetSocketAddress ad = new InetSocketAddress("127.0.0.1",port);
					client.connect(ad); 	
					BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
					InetAddress ip = InetAddress.getLocalHost();
					out.write(("0"+text1.getText()+"*"+text2.getText()+"*"+ip+"\r\n\r\n")); //sent account
					out.flush();
					
					
					info=new String("0"+text1.getText()+"*"+text2.getText()+"\r\n\r\n");
					// wait receive
					BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
					String buf = new String("");
					String message =null;
			    	while(((message=in.readLine())!=null)) {
			    		buf=buf+message;
			    	}
			    	//System.out.println(buf);
			    	 
			    	if(buf.equals("error")) {
			    		JOptionPane.showMessageDialog(null,"錯誤");
			    	}
			    	else {
			    		String temp[]=buf.split("\\*");
			    		ownname=temp[1];
			    		waitport=Integer.valueOf(temp[0]);
			    		dispose();
			    		buf="correct";
			    	}
			    	status=buf;
					client.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				*/
			}
		});
		 
		b2.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String name = JOptionPane.showInputDialog("請輸入使用者名稱:");
				if(name!=null) {
					try (SocketChannel socketChannel = SocketChannel.open()) {
						if (socketChannel.isOpen()) {
							String buf;
							socketChannel.configureBlocking(true);
							socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, 128 * 1024);
							socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, 128 * 1024);
							socketChannel.setOption(StandardSocketOptions.SO_KEEPALIVE, true);
							socketChannel.setOption(StandardSocketOptions.SO_LINGER, 5);
							socketChannel.connect(new InetSocketAddress("127.0.0.1", port));
							if (socketChannel.isConnected()) {
								ByteBuffer sendBuffer = ByteBuffer.wrap(("2"+text1.getText()+"*"+text2.getText()+"*"+name+"*").getBytes());
								socketChannel.write(sendBuffer);
								socketChannel.read(rcvBuffer);
								rcvBuffer.flip();
								CharBuffer charBuffer = decoder.decode(rcvBuffer);
								//System.out.println(charBuffer);
								buf=charBuffer.toString();
								if(buf.equals("account have been use")) {
									JOptionPane.showMessageDialog(null,"帳號或密碼已被使用");
								}
								if(buf.equals("creat success")) {
									JOptionPane.showMessageDialog(null,"成功");
								}
								rcvBuffer.clear();
							} else {
								System.out.println("連線失敗!");
							}
						} else {
						System.out.println("socket channel 開啟失敗!");
						}
						socketChannel.close();
					} catch (IOException ex) {
						System.err.println(ex);
					}
				}
				//-----------------------------
				/*
				try {
					String name = JOptionPane.showInputDialog("請輸入使用者名稱:");
					if(name!=null) {
						Socket client = new Socket();
						InetSocketAddress ad = new InetSocketAddress("127.0.0.1",port);
						client.connect(ad); 	 
						BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
						out.write(("2"+text1.getText()+"*"+text2.getText()+"*"+name+"\r\n\r\n")); //sent account
						out.flush();
					// 	wait receive
						BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
						String buf = new String("");
						String message =null;
						while(((message=in.readLine())!=null)) {
							buf=buf+message;
						}
						System.out.println(buf); 
						if(buf.equals("account have been use")) {
							JOptionPane.showMessageDialog(null,"帳號或密碼已被使用");
						}
						if(buf.equals("creat success")) {
							JOptionPane.showMessageDialog(null,"成功");
						}
						client.close();
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}	
				*/
			}
		});
		
		this.add(panel,BorderLayout.SOUTH);
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
		
	public boolean correct() {
		if(status.equals("correct")) {
    		return true;
    	}
		else
			return false;
	}
	public String getinfo() {
		return info;
	}
	public int getwaitPort() {
		return waitport;
	}
	public String getname() {
		return ownname;
	}

}