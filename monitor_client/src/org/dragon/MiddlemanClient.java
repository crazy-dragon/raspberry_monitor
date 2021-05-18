package org.dragon;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;

public class MiddlemanClient implements Runnable {
	private DataInputStream input;
	private PipelineClient pipeline;
	private String urlStr;
	
	public MiddlemanClient(Socket middleman, PipelineClient pipeline, String urlStr) throws IOException {
		this.input = new DataInputStream(new BufferedInputStream(middleman.getInputStream()));
		this.pipeline = pipeline;
		this.urlStr = urlStr;
	}

	@Override
	public void run() {
		System.out.println("客户端启动了。。。");
		while (true) {         // 中间人需要一直保持同服务器端的通信。
			try {
				this.receive();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void receive() throws IOException {
		char ch = input.readChar();
		System.out.println("接收到服务端发送的命令：" + ch);
		if (ch == 'Y') {
			pipeline.setFlag(true);
			new Thread(()-> {
				try {
					execute();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).start();
		} else if (ch == 'N') {
			pipeline.setFlag(false);
		} else {
			// 不做处理
		}
	}
	
	public void execute() throws IOException {
		// 建立正常的连接请求，获取到内网服务器的响应数据
		URL url = new URL(urlStr);
		DataInputStream httpInput = new DataInputStream(new BufferedInputStream(url.openStream()));
	    pipeline.transfer(httpInput);
	}
}
