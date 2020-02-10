package frame;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;


public class Server {

	private static int port=5500;
	private static ServerSocket s;
	
	public Server() throws Exception {
		/// account
		int acc_num=1;
		int friend_num[]=new int[100];
		for(int i=0;i<100;i++) {
			friend_num[i]=0;
		}
		int status[][]=new int[5][10];
		for(int i=0;i<5;i++) {
			for(int j=0;j<10;j++) {
				status[i][j]=0;
			}
		}
		//record port 
		int nuseportcount=5502;
		//
		friend_num[0]=0;
		String[][] account = new String[5][10];
		String[][] addr = new String[5][2];// 0 is port 1 is ip
		for(int i=0;i<5;i++) {
			addr[i][0]="5501";
			addr[i][1]="";
		}
		/// [0] is account ,[1] is password,[2] is name,after [3] is your friend info
		/// account 
		account[0][0]="acc1";account[0][1]="pass1";account[0][2]="Peter";
		
		///
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true) {
					for(int i=0;i<5;i++) {
						if(status[i][0]==1) {
							for(int k=0;k<5;k++) {
								for(int j=3;j<10;j++) {
									if(account[i][2].equals(account[k][j])) {
										status[k][j]=1;
									}
								}
							}
						}
					}
					try {
						Thread.currentThread().sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}).start();
		//----------------------------------------------------------------------
		ServerSocketChannel serverChannel;
		Selector selector;

		try {
			serverChannel = ServerSocketChannel.open();
			selector = Selector.open();

			if (serverChannel.isOpen() && selector.isOpen()) {
				serverChannel.configureBlocking(false);
				serverChannel.setOption(StandardSocketOptions.SO_RCVBUF, 256 * 1024);
				serverChannel.setOption(StandardSocketOptions.SO_REUSEADDR, true);
				serverChannel.bind(new InetSocketAddress(port));
				serverChannel.register(selector, SelectionKey.OP_ACCEPT);
				String buf="";
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
							} if (key.isReadable()) {
								SocketChannel client = (SocketChannel) key.channel();
								ByteBuffer output = (ByteBuffer) key.attachment();
								client.read(output);
								// 
								buf = new String(Arrays.copyOfRange(output.array(), 0, output.limit()));
								//System.out.println(buf);
								SelectionKey clientKey = client.register(selector, SelectionKey.OP_WRITE);
							} if (key.isWritable()) {
								SocketChannel client = (SocketChannel) key.channel();
								//---------------------------
								int type;
								type=Integer.valueOf(buf.substring(0,1));
								String info ="";
								for(int i=0;i<buf.length();i++) {
									String temp=buf.substring(i,i+1);
									if(temp!=null) {
										info=info+temp;
									}
								}
								//System.out.println(info);
								info=info.substring(1);
								//System.out.println(type);
								//  0 sent account + password (login)
								if(type==0) {
									boolean check=false;
									///account + "0" +password = info
									String temp[] = info.split("\\*");
									String ac = temp[0];
									String pass = temp[1];
									String ip = temp[2];
									//System.out.println(ac+" "+pass);
									int host=-1;
									for(int i=0;i<acc_num;i++) {
										if(ac.equals(account[i][0])) {
											if(pass.equals(account[i][1])){
												if(status[i][0]!=1) {
													check=true;
													host=i;
												}
											}
											break;
										}
									}
									//System.out.println(host);
									//change status
									if(check==true) {
										addr[host][1]=ip;
										status[host][0]=1;
										ByteBuffer output = ByteBuffer.wrap(String.valueOf(addr[host][0]+"*"+account[host][2]+"*").getBytes());
										if (output != null) {
											client.write(output);
											output.clear();
										}
									}else {
										ByteBuffer output = ByteBuffer.wrap(String.valueOf("error").getBytes());
										if (output != null) {
											client.write(output);
											output.clear();
										}
									}
									//System.out.println("type0");
								}
								// 1 sent password 
								/*if(type==1) {
									boolean check=false;
									BufferedWriter out = new BufferedWriter(new OutputStreamWriter(s_c.getOutputStream()));
									for(int i=0;i<acc_num;i++) {
										if(info.equals(account[i][1])) 	
											check=true;
									}
									if(check==true) {
										out.write(("correct\r\n\r\n"));
										out.flush();
									}else {
										out.write(("error\r\n\r\n"));
										out.flush();
									}
									//System.out.println("type1");
								}
								*/
								// 2 create account +password 
								if(type==2) {
									boolean check=false;
									String mes[] = info.split("\\*");
									String ac = mes[0];
									String pass = mes[1];
									String name = mes[2];
									//System.out.print(ac+" "+pass);
									for(int i=0;i<acc_num;i++) {
										if(ac.equals(account[i][0]) || pass.equals(account[i][1]) || name.equals(account[i][2])) 	{
											check=true;
											break;
										}
									}
									if(check==true) {
										ByteBuffer output = ByteBuffer.wrap(String.valueOf("account have been use").getBytes());
										if (output != null) {
											client.write(output);
											output.clear();
										}
									}
									else {
										account[acc_num][0]=ac;
										account[acc_num][1]=pass;
										account[acc_num][2]=name;
										addr[acc_num][0]=Integer.toString(nuseportcount++);
										acc_num++;
										ByteBuffer output = ByteBuffer.wrap(String.valueOf("creat success").getBytes());
										if (output != null) {
											client.write(output);
											output.clear();
										}
									}
									//System.out.println("type2");
								}
								// 3 search
								if(type==3) {
									//System.out.println("type3");
									int host_num = -1;
									String temp[]=info.split("\\*");
									String ac =temp[0];
									String s_name =temp[1];
									for(int i=0;i<acc_num;i++) {
										if(ac.equals(account[i][0])) 	
											host_num=i;
									}
									if(host_num==-1) {
										ByteBuffer output = ByteBuffer.wrap(String.valueOf("no this person").getBytes());
										if (output != null) {
											client.write(output);
											output.clear();
										}
									}
									else if(friend_num[host_num]==0 || account[host_num][2].equals(s_name)) {
										ByteBuffer output = ByteBuffer.wrap(String.valueOf("no friend").getBytes());
										if (output != null) {
											client.write(output);
											output.clear();
										}
									}
									else {
										String friend_info="";
										for(int i=3;i<3+friend_num[host_num];i++) {
											if(account[host_num][i].equals(s_name)) {
												int tt=0;
												for(int j=0;j<acc_num;j++) {
													if(account[j][2].equals(s_name)) {
														tt=j;
													}
												}
												friend_info=addr[tt][0]+account[host_num][i]+status[host_num][i]+"*";
												break;
											}
										}
										ByteBuffer output = ByteBuffer.wrap(String.valueOf(friend_info).getBytes());
										if (output != null) {
											client.write(output);
											output.clear();
										}
									}
								}
								// 4 get friend info 
								if(type==4) {
									int host_num = -1;
									String temp[] = info.split("\\*");
									info=temp[0];
									for(int i=0;i<acc_num;i++) {
										if(info.equals(account[i][0])) 	
											host_num=i;
									}
									if(host_num==-1) {
										ByteBuffer output = ByteBuffer.wrap(String.valueOf("no this person").getBytes());
										if (output != null) {
											client.write(output);
											output.clear();
										}
									}
									else if(friend_num[host_num]==0) {
										ByteBuffer output = ByteBuffer.wrap(String.valueOf("no friend").getBytes());
										if (output != null) {
											client.write(output);
											output.clear();
										}
									}
									else {
										String friend_info="";
										for(int i=3;i<3+friend_num[host_num];i++) {
											int tt=0;
											for(int j=0;j<acc_num;j++) {
												if(account[j][2].equals(account[host_num][i])) {
													tt=j;
												}
											}
											friend_info = friend_info +addr[tt][0]+ account[host_num][i] +status[host_num][i]+"*";
										}
										ByteBuffer output = ByteBuffer.wrap(String.valueOf(friend_info).getBytes());
										if (output != null) {
											client.write(output);
											output.clear();
										}
									}
									//System.out.println("type4");
								}
								// 5 new friend
								if(type==5) {
									//System.out.println("type5");
									String temp[]=info.split("\\*");
									String ac = temp[0];
									String fac = temp[1];
									int host=-1,f_host=-1;
									for(int i=0;i<acc_num;i++) {
										if(ac.equals(account[i][0])) {
											host=i;
										}
										if(fac.equals(account[i][0])) {
											f_host=i;
										}
									}
									if(f_host==-1 || host==f_host) {
										// no this ac
										ByteBuffer output = ByteBuffer.wrap(String.valueOf("no this person").getBytes());
										if (output != null) {
											client.write(output);
											output.clear();
										}
									}
									else {
										//success
										//System.out.println(host+" "+f_host);
										account[host][3+friend_num[host]++]=account[f_host][2];
										account[f_host][3+friend_num[f_host]++]=account[host][2];
										/*for(int i=0;i<2;i++) {
											for(int j=0;j<5;j++) {
												System.out.print(account[i][j]);
											}
											System.out.print("\n");
										}
										*/
										ByteBuffer output = ByteBuffer.wrap(String.valueOf(account[f_host][2]+"*"+addr[f_host][0]+"*").getBytes());
										if (output != null) {
											client.write(output);
											output.clear();
										}
									}
								}
								if(type==6) {
									String temp[] = info.split("\\*");
									String ac=temp[0];
									int host =-1;
									for(int i=0;i<acc_num;i++) {
										if(account[i][0].equals(ac)) {
											host=i;
										}
									}
									status[host][0]=0;
									for(int i=0;i<acc_num;i++) {
										for(int j=3;j<3+friend_num[i];j++) {
											if(account[host][2].equals(account[i][j])) {
												status[i][j]=0;
											}
										}
									}
								}
								if(type==7) {
									// info = account
									//no need
								}
								if(type==8) {
									// connect request info=friend name
									String temp[] = info.split("\\*");
									info=temp[0];
									int host=0;
									for(int i=0;i<acc_num;i++) {
										if(account[i][0].equals(info)) {
											host=i;
										}
									}
									ByteBuffer output = ByteBuffer.wrap(String.valueOf(addr[host][0]+"*"+addr[host][1]+"*").getBytes());
									if (output != null) {
										client.write(output);
										output.clear();
									}
								}
								//-------------------------
								/*ByteBuffer output = ByteBuffer.wrap(String.valueOf("OK").getBytes());
								if (output != null) {
									client.write(output);
									output.clear();
								}
								*/
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
		//----------------------------------------------------------------------------------------------------
	}

}
