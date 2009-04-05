package moveresize;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

class MoveResize {
	static enum  MODE {MOVE, RESIZE};

	static final int BORDER_THICKNESS = 4;

	static Rectangle to(Rectangle bounds, final MODE mode) {
		final Rectangle[] rectangle = new Rectangle[1];
		Display display = new Display();
		final Shell shell = new Shell(display, SWT.NO_TRIM);
		shell.setBounds(bounds);
		shell.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					shell.close();
				} else if (e.keyCode == SWT.CR) {
					Rectangle bounds = shell.getBounds(); 
					rectangle[0] = bounds;
					shell.close();
				}
			}
		});
		configureShell(display, shell, bounds, mode);
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
		return rectangle[0];
	}

	private static void configureShell(Display display, Shell shell, Rectangle bounds, MODE mode) {
		/* Take the screen shot */
        GC gc = new GC(display);
        final Image image = new Image(display, bounds);
        gc.copyArea(image, bounds.x, bounds.y);
        gc.dispose();
        final PaintListener paintListener = new ImagePainter(image, mode);
        shell.addPaintListener(paintListener);
		shell.setBounds(bounds);
		switch(mode) {
		case RESIZE:
			new Resizer(shell).attach();
			break;
		case MOVE:			
	        new Mover(shell).attach();
			break;
		}
	}
	
	private static class ImagePainter implements PaintListener {
		private final Image image;
		private final MODE mode;

		ImagePainter(Image image, MODE mode) {
			this.image = image;
			this.mode = mode;
		}
		public void paintControl(PaintEvent e) {
			e.gc.drawImage(image, 0, 0);
			switch (mode) {
			case RESIZE:
				if (e.widget instanceof Shell) {
					Shell shell = (Shell) e.widget;
					e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_BLACK));
					Rectangle bounds = shell.getBounds();
					for (int i = 0; i < Resizer.BORDER_THICKNESS; i++) {
						e.gc.drawRectangle(
								i,
								i,
								bounds.width -1 - (2*i),
								bounds.height -1 - (2*i));				
					}
				}
				break;
			}
		}		
	};

}
