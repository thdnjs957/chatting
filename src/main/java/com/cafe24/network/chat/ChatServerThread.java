package com.cafe24.network.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashMap;
import java.util.List;

public class ChatServerThread extends Thread {

	private String nickname = null;
	private Socket socket = null;
	private List<Writer> pw_list = null;
    private BufferedReader br = null;
    private PrintWriter pw = null;
    HashMap<String,Writer> map = null;
    public ChatServerThread(Socket socket, List<Writer> pw_list,HashMap<String,Writer> map) {
		this.socket = socket;
		this.pw_list = pw_list;
		this.map = map;
	}

	@Override
	public void run() {
		try {
			InetSocketAddress inetRemoteSocketAddress = (InetSocketAddress) socket.getRemoteSocketAddress();
			String remoteHostAddress = inetRemoteSocketAddress.getAddress().getHostAddress();
			int remotePort = inetRemoteSocketAddress.getPort();
			ChatServer.log("connected by client[" + remoteHostAddress + ":" + remotePort + "]");

			// 스트림 얻기 이게 위에 소켓이랑 연결 
			br = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));

			pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"), true);

			while (true) {

				// 데이터 읽기 요청 처리
				String data = br.readLine();
				System.out.println("---" + data);// 처음 클라이언트가 보낸 것
				
				if (data == null) {
					ChatServer.log("클라이언트로 부터 연결 끊김");
					doQuit(pw);
					break;
				}

				
				// 프로토콜 분석
				String[] tokens = data.split(" "); // " "을 기준으로 처리
				String real_name=null;
				System.out.println("토큰 길이는" + tokens.length);
				
				for (String t : tokens) {
					System.out.println("token 출력 : " + t);
				}

				if ("join".equals(tokens[0])) {
					doJoin(tokens[1], pw);

				} else if ("message".equals(tokens[0])) {

					doMessage(tokens[1]);

				} else if ("quit".equals(tokens[0])) {
					doQuit(pw);
					
				} else if("whisper".equals(tokens[0])){ //  whisper :/소원              
														// whisper :안녕 소원 store_name(소원)
					if(tokens.length > 2) { //이미 연결돼서 store_name이 붙어 있는 것
						real_name = tokens[tokens.length-1]; 
						doWhisperChat(real_name,pw,tokens);
					}
					else { // 처음 연결 시도 하는
						real_name = tokens[1].substring(2); 
						doWhisper(real_name,pw);
					}
				}
     			else { ChatServer.log("에러 : 알 수 없는 요청(" + tokens[0] + ")"); }
			}
	
		} catch (SocketException e) {
			System.out.println("[server] 연결이 끊겼습니다.");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (socket != null && socket.isClosed() == false) {
					socket.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	private void doJoin(String nickname, Writer writer) {
		this.nickname = nickname;
		String data = nickname + "님이 입장하였습니다.";
		// 다른 writer들에게 전송!
		broadcast(data);

		addWriter(writer);
		map.put(nickname, pw);
		System.out.println(map);
	}
	
	private void doWhisper(String nickname, Writer writer) {
		this.nickname = nickname;
		String data = nickname + "님과 귓속말 시작합니다. 귓속말을 종료하고 싶으면 esc를 누르세요";
		
		PrintWriter pw = (PrintWriter) map.get(nickname);
		pw.println(data);
		pw.flush();
		
	}
	
	private void doWhisperChat(String nickname, Writer writer,String[] tokens) {
		String data = tokens[1];
		PrintWriter pw = (PrintWriter) map.get(nickname);
		pw.println(data);
		pw.flush();
		
	}
	
	
	private void addWriter(Writer writer) {
		synchronized (pw_list) {
			pw_list.add(writer);
		}
	}

	private void removeWriter(Writer writer) {
		synchronized (pw_list) {
			pw_list.remove(writer);
		}
	}

	private void broadcast(String data) {
		synchronized (pw_list) {
			for (Writer writer : pw_list) {
				PrintWriter printWriter = (PrintWriter) writer;
				printWriter.println(data);
				printWriter.flush();
			}
		}
	}

	private void doMessage(String data) {
		broadcast(this.nickname + data);
	}

	private void doQuit(Writer writer) {
		String data = nickname + "님이 퇴장하셨습니다.";

		broadcast(data);

		removeWriter(writer);
	}

}
