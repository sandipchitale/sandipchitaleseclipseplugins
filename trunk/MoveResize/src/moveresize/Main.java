package moveresize;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

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
					server(Integer.parseInt(args[i]));
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
	
	private static void server(int port) {
		try {
			ServerSocket serverSocket = new ServerSocket(port, 1);
			while (true) {
				final Socket socket = serverSocket.accept();
				new Thread(new Runnable() {
					public void run() {
						try {
							serve(socket.getInputStream(), socket.getOutputStream());
						} catch (IOException e) {
							System.err.println(e.getMessage());
						}
					}
				}).start();
			}
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}
	}
	
	private static void serve(InputStream in, OutputStream out) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			try {
				String readLine = br.readLine();
				if ("exit".equals(readLine)) {
					System.exit(0);
				}
				serve(readLine, new PrintWriter(out));
			} catch (IOException e) {
				System.err.println(e.getMessage());
			}
		} finally {
			try {
				in.close();
			} catch (IOException e) {
			}
		}
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
		System.err.println("Usage: -resize is default");		
	}
}
