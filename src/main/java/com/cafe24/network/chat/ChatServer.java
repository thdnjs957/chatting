package com.cafe24.network.chat;

import java.io.IOException;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ChatServer {

	public static final int SERVER_PORT = 7001;
	public static final String SERVER_IP = "192.168.1.28";

	public static void main(String[] args) {
		
		ServerSocket serverSocket = null;
		HashMap<String,Writer> map = new HashMap<String, Writer>();
		
		
		try {
			//1. 서버소켓 생성
			serverSocket = new ServerSocket();
			
			List<Writer> pw_list = new ArrayList<Writer>();
			
			serverSocket.setReuseAddress(true);
			
			//2. 바인딩(binding)
			serverSocket.bind(new InetSocketAddress("0.0.0.0", SERVER_PORT));
			log("chatting server starts at :"+ SERVER_PORT );

			while(true) {//다시 돌아가야함 thread에게 socket 전달하고  
				//3. accept, //accept 하면 socket이 하나 튀어나옴
				Socket socket = serverSocket.accept();
				Thread thread = new ChatServerThread(socket,pw_list,map);
				thread.start();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if( serverSocket != null && serverSocket.isClosed() == false ) {
					serverSocket.close();	
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void log(String log) {
		System.out.println("[server#"+ Thread.currentThread().getId() + "] " + log);
	}
	
}
