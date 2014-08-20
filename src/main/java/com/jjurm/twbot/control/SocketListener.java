package com.jjurm.twbot.control;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.jjurm.twbot.system.config.Config;

/**
 * Class for accepting connections and communicating through sockets.
 * 
 * @author JJurM
 */
public class SocketListener implements Runnable {
	private static final Logger LOG = LogManager.getLogger();

	private boolean running;

	/**
	 * <tt>Commander</tt> to pass input to.
	 */
	Commander commander;

	ServerSocket serverSocket;
	Thread serverSocketThread;
	List<OutputStream> outputs = new ArrayList<OutputStream>();

	/**
	 * Basic constructor
	 * 
	 * @param commander commander to pass input to
	 */
	public SocketListener(Commander commander) {
		this.commander = commander;
		serverSocketThread = new Thread(this);
	}

	/**
	 * Starts the thread.
	 */
	public synchronized void start() {
		if (running)
			return;
		running = true;
		serverSocketThread.start();
	}

	public synchronized void stop() {
		if (!running)
			return;
		running = false;
		try {
			if (serverSocket != null)
				serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void writeToAll(byte[] data) throws IOException {
		for (OutputStream output : outputs) {
			output.write(data);
			output.flush();
		}
	}

	@Override
	public void run() {
		Thread.currentThread().setName("ServerSocketThread");

		try {
			serverSocket = new ServerSocket(Config.socketPort, 3, null);
		} catch (BindException e) {
			LOG.error("Port " + Config.socketPort + "is already in use, terminating SocketListener");
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		try {
			while (running) {
				@SuppressWarnings("resource")
				Socket socket = serverSocket.accept();
				SocketThread thread = new SocketThread(commander, this, socket);
				thread.start();
			}
		} catch (IOException e) {
			// socket closed
		}

	}

	/**
	 * Each socket will be processed in separate thread.
	 * 
	 * @author JJurM
	 */
	static class SocketThread extends Thread {

		/**
		 * Index for naming threads
		 */
		static AtomicInteger atomicInteger = new AtomicInteger(1);

		Commander commander;
		SocketListener socketListener;
		Socket socket;

		/**
		 * Index of current socket
		 */
		int socketIndex;

		/**
		 * Basic constructor
		 * 
		 * @param socket
		 */
		public SocketThread(Commander commander, SocketListener socketListener, Socket socket) {
			this.commander = commander;
			this.socketListener = socketListener;
			this.socket = socket;

			socketIndex = atomicInteger.getAndIncrement();
		}

		@Override
		public void run() {
			setName("SocketThread-" + socketIndex);
			
			try (BufferedReader br = new BufferedReader(new InputStreamReader(
					socket.getInputStream()));
					OutputStream os = socket.getOutputStream();
					PrintWriter pw = new PrintWriter(os, true)) {

				socketListener.outputs.add(os);
				LOG.info("Accepted socket #" + socketIndex);
				
				loop: while (true) {

					try {
						commander.process(br, pw);
					} catch (StreamCloseRequest scr) {
						LOG.info("Closed socket #" + socketIndex);
						socketListener.outputs.remove(os);
						try {
							socket.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						break loop;
					}

				}
				

			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

}
