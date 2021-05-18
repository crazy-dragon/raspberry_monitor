package org.dragon;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetClient {
	
	private final static String REMOTE_HOST = "i.love.you";
	private final static String LOCAL_HOST = "192.168.187.144";
	private final static int REMOTE_PORT = 10000;
	private final static int LOCAL_PORT = 8081;
	private final static String URL = "http://" + LOCAL_HOST + ":" + LOCAL_PORT;
	
	public static void main(String[] args) {
		try {
			// 建立用于协调双方的中间人
			PipelineClient pipeline = new PipelineClient(new Socket(REMOTE_HOST, REMOTE_PORT));
			MiddlemanClient middleman = new MiddlemanClient(new Socket(REMOTE_HOST, REMOTE_PORT), pipeline, URL);			
			
			Thread client = new Thread(middleman);
			client.start();   // 启动中间人线程。
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
