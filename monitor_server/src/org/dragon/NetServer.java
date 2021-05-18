package org.dragon;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class NetServer {
	
	private static final int THREAD = 3;
	
	public static void main(String[] args) {
		int port = 10000;  // 默认端口号
		try {
			port = Integer.parseInt(args[0]);
		} catch (Exception e) {
		//	e1.printStackTrace();
		}
		
		System.out.println("服务器启动中，占用端口为：" + port);
		
		ExecutorService threadPool = Executors.newFixedThreadPool(THREAD);
		Lock lock = new ReentrantLock();
		
		try (ServerSocket server = new ServerSocket(port)) {
			// 传输数据的管道
			Socket pipeline = server.accept();
			System.out.println("数据传输管道建立成功。。。");
			// 控制管道的中间人
			Socket middleman = server.accept();
			System.out.println("中间人建立成功。。。");
			System.out.println("系统启动。。。");
			// 下面是作为一个Http服务器的部分了。
			while (true) {
				Socket client = server.accept();
				System.out.println("一个请求进入系统。。。" + client);
				threadPool.submit(new UserConnection(client, 
						new PipelineServer(pipeline), 
						new MiddlemanServer(middleman),
						lock));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
}
