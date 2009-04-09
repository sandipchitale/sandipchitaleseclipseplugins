package moveresize;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.swt.graphics.Rectangle;

public class Main {
	public static void main(String[] args) {
		if (args.length == 0 || args.length > 2) {
			usage();
			System.exit(1);
		}

		for (int i = 0; i < args.length; i++) {
			String arg = args[i];
			if ("-server".equals(arg)) {
				i++;
				if (i >= args.length) {
					usage();
					System.exit(-1);
				}
				try {
					BlockingQueue<Request> queue = new ArrayBlockingQueue<Request>(2);
					new Server(Integer.parseInt(args[i]), queue).start();
					while(true) {
						try {
							Request request = queue.take();
							try {
								serve(request.input, new PrintWriter(request.socket.getOutputStream()));
							} finally {
								request.socket.close();
							}
						} catch (InterruptedException e) {
							System.err.println(e.getMessage());
						} catch (IOException e) {
							System.err.println(e.getMessage());
						}
					}
				} catch (NumberFormatException nfe) {
					usage();
					System.exit(-1);
				}
			} else {
				serve(args[i], new PrintWriter(System.out));
			}
		}
	
		System.exit(1);
	}
	
	
	private static void serve(String xywh, PrintWriter out) {
		String[] bounds = xywh.split(":");
		if (bounds.length != 4) {
			usage();
			return;
		}
		int x = Integer.MIN_VALUE;
		int y = Integer.MIN_VALUE;
		int width = Integer.MIN_VALUE;
		int height = Integer.MIN_VALUE;
		try {
			x = Integer.parseInt(bounds[0]);
			y = Integer.parseInt(bounds[1]);
			width = Integer.parseInt(bounds[2]);
			height = Integer.parseInt(bounds[3]);
			Rectangle rectangle = MoveResize.to(new Rectangle(x,y,width,height));
			if (rectangle != null) {
				out.println(rectangle.x + ":" + rectangle.y + ":" + rectangle.width + ":" + rectangle.height);
				out.flush();
				out.close();
			}
		} catch (NumberFormatException nfe) {
			usage();					
			return;
		}
	}
	
	private static void usage() {
		System.err.println("Usage: java -jar MoveResize.jar -server port | x:y:width:height");
	}
	
	private static class Request {
		final Socket socket;
		final String input;

		Request(Socket socket, String input) {
			this.socket = socket;
			this.input = input;
		}
	}
	
	private static class Server extends Thread {
		private final int port;
		private final BlockingQueue<Request> queue;

		Server(int port, BlockingQueue<Request> queue) {
			this.port = port;
			this.queue = queue;
		}
		
		public void run() {
			try {
				ServerSocket serverSocket = new ServerSocket(port, 1);
				while (true) {
					final Socket socket = serverSocket.accept();
					new Thread(new Runnable() {
						public void run() {
							serve(queue, socket);
						}
					}).start();
				}
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		}
	}
		
	private static void serve(BlockingQueue<Request> queue, Socket socket) {
		try {
			InputStream in = socket.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String input = br.readLine();
			if ("exit".equals(input)) {
				System.exit(0);
			}
			queue.put(new Request(socket, input));
		} catch (IOException e) {
			System.err.println(e.getMessage());
		} catch (InterruptedException e) {
			System.err.println(e.getMessage());
		}
	}
}
