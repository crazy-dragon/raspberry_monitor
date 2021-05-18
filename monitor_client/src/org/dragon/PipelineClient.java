package org.dragon;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * 用于接力数据的管道类
 * 
 * 每次只能有一个线程获取到管道类，如果被使用就继续等待或者直接放弃。
 * */
public class PipelineClient {

	private boolean flag = false;  // 标志变量，为true 管道开启，false 管道关闭，默认关闭
	private OutputStream output;
	
	public PipelineClient(Socket socket) throws IOException {
		output = new BufferedOutputStream(socket.getOutputStream());
	}
	
	/**
	 * 同步方法
	 * 将内网服务的输出，转发给外网服务器作为其输入。
	 * @throws IOException 
	 * */
	public void transfer(InputStream in) throws IOException {
		DataInputStream  httpInput = new DataInputStream(in);
		
		// 持续传输数据
		while (flag) {
			byte[] data = new byte[5*1024];
		    httpInput.readFully(data);  // 每次读取数据大小为5KB
		    
		    output.write(data);    // 转发数据包
		    output.flush();        // 手动刷新缓存
		    
		    System.out.println("向服务端发送数据大小为：" + data.length + " 字节。");
		}
		
		// 如果管道传输退出的话，就关闭 inputStream 对象，in
		in.close();
	}
	
	/**
	 * 清空管道的方法
	 * 客户端输出端，清空输出缓存；服务端输入端，清空输入缓存。
	 * */
	public void clear() throws IOException {
		output.flush();
	}
	
	/**
	 * 设置flag变量的值
	 * */
	public void setFlag(boolean flag) {
		this.flag = flag;
		if (flag) {
			System.out.println("管道开启");
		} else {
			System.out.println("管道关闭");
			try {
				this.clear();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
