package org.dragon;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * 用于协调双方通信的中间人
 * */
public class MiddlemanServer {

	private DataOutputStream output;
	
	public MiddlemanServer(Socket middleman) throws IOException {
		this.output = new DataOutputStream(new BufferedOutputStream(middleman.getOutputStream()));
	}

	/**
	 * 建立一个连接
	 * @throws IOException 
	 * */ 
	public void open() throws IOException {
		output.writeChar('Y');
		output.flush();
	}
	
	/**
	 * 终止一个连接
	 * @throws IOException 
	 * */
	public void terminate() throws IOException {
		output.writeChar('N');
		output.flush();
	}

}
