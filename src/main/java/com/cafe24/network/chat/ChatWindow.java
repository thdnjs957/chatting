package com.cafe24.network.chat;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ChatWindow {

	private Frame frame;
	private Panel pannel;
	private Button buttonSend;
	private TextField textField;
	private TextArea textArea;
	private Socket socket;
	private int mode;
	
	public ChatWindow(String name, Socket socket,int mode) {
		frame = new Frame(name);
		pannel = new Panel();
		buttonSend = new Button("Send");
		textField = new TextField();
		textArea = new TextArea(30, 80);
		
		this.socket = socket;
		this.mode = mode;

		new ChatClientReceiveClass(socket).start();
	}

	public void show() {
		// Button
		buttonSend.setBackground(Color.GRAY);
		buttonSend.setForeground(Color.WHITE);
		buttonSend.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent actionEvent) {
				sendMessage();
			}
		});

		// Textfield
		textField.setColumns(80);
		textField.addKeyListener(new KeyAdapter() {

			@Override
			public void keyPressed(KeyEvent e) {
				char keyCode = e.getKeyChar();
				if (keyCode == KeyEvent.VK_ENTER) {
					sendMessage();
				}
				
				if(mode == 1 && keyCode == 27) {
					System.out.println("귓속말이 종료되었습니다.");
					mode = 0;
				}
				
			}
		});
		
		

		// Pannel
		pannel.setBackground(Color.LIGHT_GRAY);
		pannel.add(textField);
		pannel.add(buttonSend);
		frame.add(BorderLayout.SOUTH, pannel);

		// TextArea
		textArea.setEditable(false);
		frame.add(BorderLayout.CENTER, textArea);

		
		// Frame
		frame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				finish(); 
			}
			
	
		});
		

		
		frame.setVisible(true);
		frame.pack();
	}
	


	private void finish() {
		
		PrintWriter pw;
		try {
			pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
			String request = "quit";
			
			pw.println(request);
			System.exit(0);
		}
		catch (IOException e1) {
			e1.printStackTrace();
		}
	
	}

	String store_name = null;
	
	// 대화 보내기
	private void sendMessage() {
		PrintWriter pw;
		try {
			pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);
			
			String message = textField.getText(); 
			String whisper_start_mark = null;
			
			if(mode == 0) {//아직 whisper 연결 안함
				whisper_start_mark = message.substring(0,1); // -> / 
				
				if("/".equals(whisper_start_mark)) { // -> /소원
					mode = 1;//귓속말 모드 표시
					store_name = message.substring(1);
					pw.println("whisper :" + message); 
				}

				pw.println("message :" + message);//그냥 일반 채팅
				
			}
			
			else {//mode가 1이 돼서 whisper 연결 되면
				pw.println("whisper :" + message + " " + store_name); //   whisper :안녕 소원 store_name(소원)
				
				if("quit".contentEquals(message)) {
					pw.println("귓속말이 종료되었습니다.");
					mode = 0;
				}
								
			}
			
			textField.setText("");
			textField.requestFocus();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	
	
	private class ChatClientReceiveClass extends Thread {
		
		Socket socket = null;
		
		ChatClientReceiveClass(Socket socket) {
			this.socket = socket;
		}

		public void run() {
			try {
				BufferedReader br = new BufferedReader(
						new InputStreamReader(socket.getInputStream(), "utf-8"));
				while (true) {
					
					String message = br.readLine();
					textArea.append(message+"\n");
					
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

