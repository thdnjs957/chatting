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
import java.util.List;

public class ChatServerThread extends Thread {

	private String nickname = null;
	private Socket socket = null;
	private List<Writer> pw_list = null;
    private BufferedReader br = null;
    private PrintWriter pw = null;
	public ChatServerThread(Socket socket, List<Writer> pw_list) {
		this.socket = socket;
		this.pw_list = pw_list;
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

				for (String t : tokens) {
					System.out.println("token 출력" + t);
				}

				if ("join".equals(tokens[0])) {
					doJoin(tokens[1], pw);

				} else if ("message".equals(tokens[0])) {

					doMessage(tokens[1]);

				} else if ("quit".equals(tokens[0])) {
					doQuit(pw);

				} 
     			 else { ChatServer.log("에러 : 알 수 없는 요청(" + tokens[0] + ")"); }
					 
			}

		} catch (SocketException e) {
			System.out.println("[server] sudden closed by client");
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
		// 다른 writer들에게 전송!
		broadcast(data);

		removeWriter(writer);
	}

}
