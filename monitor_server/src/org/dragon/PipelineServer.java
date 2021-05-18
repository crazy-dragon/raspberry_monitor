package org.dragon;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class PipelineServer {
	
	private byte[] responseHeader = ("HTTP/1.0 200 OK\r\n" + 
			"Server: Motion/4.1.1\r\n" + 
			"Connection: close\r\n" + 
			"Max-Age: 0\r\n" + 
			"Expires: 0\r\n" + 
			"Cache-Control: no-cache, private\r\n" + 
			"Pragma: no-cache\r\n" + 
			"Content-Type: multipart/x-mixed-replace; boundary=BoundaryString" + 
			"\r\n\r\n")   // 注意拼接最后的两个分隔符。
			.getBytes(StandardCharsets.UTF_8);
		
	/**
	 * 内网客户端的输入
	 * */
	private DataInputStream input;
	
	public PipelineServer(Socket socket) throws IOException {
		input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
	}

	/**
	 * 同步方法
	 * 将内网服务的输出，转发给外网服务器作为其输入。
	 * @throws IOException 
	 * */
	synchronized public void transfer(OutputStream output) throws IOException {
		
		// 因为我客户端抓取的是HTTP的报文，所以报头被过滤了，只有数据体部分了
		// 但是我是直接响应客户端的请求，因此需要额外补充一个报文头
		output.write(responseHeader);
		
		// 持续传输数据（数据体部分）
		while (true) {
			byte[] data = new byte[5*1024];
		    input.readFully(data);  // 每次从内网穿透客户端读取数据大小为5KB
		    output.write(data);    // 转发数据包到外网客户端
		    output.flush();        // 手动刷新缓存
		}
	}
	
	/**
	 * 清空管道的方法
	 * 客户端输出端，清空输出缓存；服务端输入端，清空输入缓存。
	 * */
	public void clear() throws IOException {
		byte[] data = new byte[10*1024];  // 设置一个尽量大一点的数组，然后一次性读取，清空缓冲区，不知道是否可以成功。
		int len = input.read(data);           // 读取之后，不做处理，即丢弃，目的是为了清空管道。
		System.out.println("执行清空管道输入端的命令，读取大小为：" + len + " 字节");
	}
}
