package moveresize;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

class MoveResize {
	static enum  MODE {MOVE, RESIZE};

	static final int BORDER_THICKNESS = 4;

	static Rectangle to(Rectangle bounds, final MODE mode) {
		final Rectangle[] rectangle = new Rectangle[1];
		Display display = new Display();
		final Shell shell = new Shell(display, SWT.NO_TRIM);
		shell.setBounds(display.getBounds());
		/* Take the screen shot */
        GC gc = new GC(display);
        final Image desktopImage = new Image(display, shell.getBounds());
        gc.copyArea(desktopImage, shell.getBounds().x, shell.getBounds().y);
        gc.dispose();
        final PaintListener paintListener = new ImagePainter(desktopImage, MODE.MOVE);
        shell.addPaintListener(paintListener);
        shell.setLayout(null);
		final Canvas canvas = new Canvas(shell, SWT.NONE);
		configure(display, canvas, bounds, mode);
		canvas.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					shell.close();
				} else if (e.keyCode == SWT.CR) {
					rectangle[0] = canvas.getBounds();
					shell.close();
				}
			}
		});
        shell.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					shell.close();
				} else if (e.keyCode == SWT.CR) {
					rectangle[0] = canvas.getBounds();
					shell.close();
				}
			}
		});
		shell.open();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
		return rectangle[0];
	}

	private static void configure(Display display, Control control, Rectangle bounds, MODE mode) {
		/* Take the screen shot */
        GC gc = new GC(display);
        final Image image = new Image(display, bounds);
        gc.copyArea(image, bounds.x, bounds.y);
        gc.dispose();
        final PaintListener paintListener = new ImagePainter(image, mode);
        control.addPaintListener(paintListener);
		control.setBounds(bounds);
		switch(mode) {
		case RESIZE:
			new Resizer(control).attach();
			break;
		case MOVE:			
	        new Mover(control).attach();
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
			if (e.widget instanceof Control) {
				Control control = (Control) e.widget;
				e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_BLACK));
				Rectangle bounds = control.getBounds();
				int times = (mode == MODE.RESIZE ? Resizer.BORDER_THICKNESS : 1);
				for (int i = 0; i < times; i++) {
					e.gc.drawRectangle(
							i,
							i,
							bounds.width -1 - (2*i),
							bounds.height -1 - (2*i));				
				}
			}
		}		
	};

}
