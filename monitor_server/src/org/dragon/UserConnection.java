package org.dragon;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.locks.Lock;

public class UserConnection implements Runnable {
	public Socket client;
	private InputStream input;
	private OutputStream output;
	
	private PipelineServer pipeline;
	private MiddlemanServer middleman;
	
	private Lock lock;  // 同步锁
	
	// 信道被占用时，直接返回该报文
	private byte[] errorResponse = ("HTTP/1.1 444\r\n" + 
			"Content-Type: text/html;charset=UTF-8\r\n" + 
			"Content-Length: 48\r\n" + 
			"Connection: close\r\n" + 
			"\r\n" + 
			"<h1>当前信道被占用，无法访问！</h1>").getBytes(StandardCharsets.UTF_8);
	
	// 信道被占用时，直接返回该报文
	private byte[] unknownResponse = ("HTTP/1.1 404\r\n" + 
			"Content-Type: text/html;charset=UTF-8\r\n" + 
			"Content-Length: 36\r\n" + 
			"Connection: close\r\n" + 
			"\r\n" + 
			"<h1>这里是未知的世界！</h1>").getBytes(StandardCharsets.UTF_8);
	
	public UserConnection(Socket client, PipelineServer pipeline, MiddlemanServer middleman, Lock lock) throws IOException {
		this.client = client;
		input = new BufferedInputStream(client.getInputStream());
		output = new BufferedOutputStream(client.getOutputStream());
		
		this.pipeline = pipeline;
		this.middleman = middleman;
		this.lock = lock;
	}
	
	@Override
	public void run() {
		execute();
	}
	
	/**
	 * 用于接力传输数据的方法
	 * */
	public void execute() {
		// 只读取请求头
		try {
			String[] params = getRequestLine(input);
			if (params.length != 3) {
				throw new Exception("错误请求！");
			}
			System.out.println("Request method: " + params[0] + 
					" Reqeust path: " + params[1] + 
					" Protocol version: " + params[2]);
			
			byte[] response = null;
			
			// 只处理对于根路径的访问，其它的拒绝掉
			if ("/".equals(params[1])) {
					System.out.println("准备获取同步锁");
					if (lock.tryLock()) {    // 尝试获取同步锁，获取不到则返回信道被占用。
						try {
							System.out.println("获取到同步锁");
							// 请求客户端建立连接
							middleman.open();
							// 开始数据接力
							pipeline.transfer(output);
					    } catch (Exception e) {
						    e.printStackTrace();
					    } finally {
							lock.unlock();     // 释放同步锁
							// 如果锁被释放了，说明用户结束了页面。
							middleman.terminate();
							Thread.sleep(2000);   // 暂停两秒
							pipeline.clear();
							System.out.println("释放同步锁");
							
						}
					} else {
						System.out.println("没有获取到同步锁，信道被占用");
						response = errorResponse;
						output.write(response);
						output.close();    // 正常关闭该连接
					}
			} else {
				response = unknownResponse;
				output.write(response);
				output.close();    // 正常关闭该连接
			}
			System.out.println("执行结束。。。");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (client != null) {
					client.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private String[] getRequestLine(InputStream in) throws IOException {
		// 只读取第一行，这是我们需要的全部内容
		StringBuilder requestBuilder = new StringBuilder();
		while (true) {
			int c = in.read();
			if (c == '\r' || c == '\n' || c == -1) break;
			requestBuilder.append((char)c);
		}
		String requestLine = requestBuilder.toString();
		return requestLine.split(" ");
	}
}
