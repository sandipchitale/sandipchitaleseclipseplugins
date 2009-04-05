package moveresize;

import org.eclipse.swt.graphics.Rectangle;

public class Main {
	public static void main(String[] args) {
		try {
			Thread.sleep(500L);
		} catch (InterruptedException e) {
		}
		boolean resize = true;
		if (args.length == 0 || args.length > 2) {
			usage();
			System.exit(1);
		}
		int x = Integer.MIN_VALUE;
		int y = Integer.MIN_VALUE;
		int width = Integer.MIN_VALUE;
		int height = Integer.MIN_VALUE;

		for (String arg : args) {
			if ("-resize".equals(arg)) {
				resize = true;
			} else if ("-move".equals(arg)) {
				resize = false;
			} else {
				String[] bounds = arg.split(":");
				if (bounds.length != 4) {
					usage();
					System.exit(-1);
				}
				try {
					x = Integer.parseInt(bounds[0]);
					y = Integer.parseInt(bounds[1]);
					width = Integer.parseInt(bounds[2]);
					height = Integer.parseInt(bounds[3]);
				} catch (NumberFormatException nfe) {
					usage();					
					System.exit(-1);
				}
			}
		}
		Rectangle rectangle = MoveResize.to(new Rectangle(x,y,width,height), (resize ? MoveResize.MODE.RESIZE : MoveResize.MODE.MOVE));
		if (rectangle != null) {
			System.out.println(rectangle.x + ":" + rectangle.y + ":" + rectangle.width + ":" + rectangle.height);
			System.exit(0);
		}
		System.exit(1);
	}
	
	private static void usage() {
		System.err.println("Usage: java -jar MoveResize.jar [ -resize | -move ] x:y:width:height");		
		System.err.println("Usage: -resize is default");		
	}
}
