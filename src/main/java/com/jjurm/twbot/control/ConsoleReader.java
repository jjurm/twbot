package com.jjurm.twbot.control;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Class for reading input from an <tt>InputStream</tt>. Passes the input to
 * given <tt>Commander</tt>.
 * 
 * @author JJurM
 */
public class ConsoleReader implements Runnable {

	private boolean running;

	/**
	 * <tt>Commander</tt> to pass input to.
	 */
	private Commander commander;

	BufferedReader br;
	PrintWriter pw;

	Thread consoleReader;

	/**
	 * Basic constructor.
	 * 
	 * @param commander commander to pass input to
	 */
	public ConsoleReader(Commander commander) {
		this.commander = commander;

		br = new BufferedReader(new InputStreamReader(System.in));
		pw = new PrintWriter(System.out, true);

		consoleReader = new Thread(this);
	}

	/**
	 * Starts the thread.
	 */
	public synchronized void start() {
		if (running)
			return;
		running = true;
		consoleReader.start();
	}

	/**
	 * Stops the reader and closes resources.
	 */
	public synchronized void stop() {
		if (!running)
			return;
		running = false;
		/*try {
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		pw.close();*/
	}

	@Override
	public void run() {
		Thread.currentThread().setName("ConsoleReader");

		while (running) {

			try {
				commander.process(br, pw);
			} catch (StreamCloseRequest e) {
				// Never close the console streams
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}

	}

}
