package com.cafe24.network.chat;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class ChatClientApp {
	
	public static void main(String[] args) {
		String name = null;
		Scanner scanner = new Scanner(System.in);
		int mode = 0;
		while( true ) {
			
			System.out.println("닉네임>>>");
			name = scanner.nextLine();
			
			if (name.isEmpty() == false ) {
				break;
			}
			
			System.out.println("대화명은 한글자 이상 입력해야 합니다.\n");
		}

		//1. 소켓 만들고 
		Socket socket = new Socket();
		
		//2. iostream
		try {
			socket.connect(new InetSocketAddress(ChatServer.SERVER_IP, ChatServer.SERVER_PORT));
			
			PrintWriter pw = new PrintWriter( 
					new OutputStreamWriter( socket.getOutputStream(), "utf-8"), true );
			
			pw.println("join "+name);
			pw.flush();
			
			new ChatWindow(name,socket,mode).show(); 
			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 
		
		scanner.close();

	}
	
	

}
