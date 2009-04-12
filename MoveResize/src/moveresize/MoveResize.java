package moveresize;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

class MoveResize {
	static final int BORDER_THICKNESS = 4;

	static Rectangle to(Rectangle bounds) {
		final Rectangle[] rectangle = new Rectangle[1];
		Display display = new Display();
		initCursors(display);
		final Shell shell = new Shell(display, SWT.NO_TRIM);
		shell.setText(MoveResize.class.getSimpleName());
		shell.setBounds(display.getBounds());
		/* Take the screen shot */
        GC gc = new GC(display);
        final Image desktopImage = new Image(display, shell.getBounds());
        gc.copyArea(desktopImage, shell.getBounds().x, shell.getBounds().y);
        gc.dispose();
        final PaintListener paintListener = new ImagePainter(desktopImage);
        shell.addPaintListener(paintListener);
        shell.setLayout(null);
		final Canvas canvas = new Canvas(shell, SWT.NONE);
		configure(display, canvas, bounds);
		canvas.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					close(shell, canvas);
				} else if (e.keyCode == SWT.CR) {
					rectangle[0] = canvas.getBounds();
					close(shell, canvas);				}
			}
		});

		canvas.setToolTipText(
				"Drag borders to resize.\n" +
				"Drag area to move.\n" +
				"Type ENTER to accept bounds.\n" +
				"Type ESCAPE to cancel.");
		
        shell.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {}
			public void keyReleased(KeyEvent e) {
				if (e.keyCode == SWT.ESC) {
					close(shell, canvas);
				} else if (e.keyCode == SWT.CR) {
					rectangle[0] = canvas.getBounds();
					close(shell, canvas);
				}
			}
		});
        shell.addMouseListener(new MouseAdapter() {
        	public void mouseUp(MouseEvent e) {
				close(shell, canvas);
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

	private static void close(final Shell shell, Canvas canvas) {
		canvas.setCursor(CURSOR_ARROW);
		shell.setCursor(CURSOR_ARROW);
		shell.setVisible(false);
		shell.close();
	}
	
	private static void configure(Display display, Control control, Rectangle bounds) {
		/* Take the screen shot */
        GC gc = new GC(display);
        final Image image = new Image(display, bounds);
        gc.copyArea(image, bounds.x, bounds.y);
        gc.dispose();
        final PaintListener paintListener = new ImagePainter(image);
        control.addPaintListener(paintListener);
		control.setBounds(bounds);
		new Resizer(control).attach();
	}
	
	static Cursor getCursor(int c) {
		switch (c) {
		case SWT.CURSOR_ARROW:
			return CURSOR_ARROW;
		case SWT.CURSOR_SIZEN:
			return CURSOR_N;
		case SWT.CURSOR_SIZEE:
			return CURSOR_E;
		case SWT.CURSOR_SIZEW:
			return CURSOR_W;
		case SWT.CURSOR_SIZES:
			return CURSOR_S;
		case SWT.CURSOR_SIZENE:
			return CURSOR_NE;
		case SWT.CURSOR_SIZENW:
			return CURSOR_NW;
		case SWT.CURSOR_SIZESE:
			return CURSOR_SE;
		case SWT.CURSOR_SIZESW:
			return CURSOR_SW;
		case SWT.CURSOR_SIZEALL:
			return CURSOR_MOVE;
		}
		return CURSOR_ARROW;
	}
	
	private static Cursor CURSOR_ARROW = null;
	private static Cursor CURSOR_MOVE = null;
	private static Cursor CURSOR_N = null;
	private static Cursor CURSOR_E = null;
	private static Cursor CURSOR_W = null;
	private static Cursor CURSOR_S = null;
	private static Cursor CURSOR_NE = null;
	private static Cursor CURSOR_NW = null;
	private static Cursor CURSOR_SE = null;
	private static Cursor CURSOR_SW = null;
	
	private static void initCursors(Display display) {
		CURSOR_ARROW = display.getSystemCursor(SWT.CURSOR_ARROW);
		CURSOR_MOVE = new Cursor(display, new ImageData(MoveResize.class.getResourceAsStream("CURSOR_MOVE.png")), 7, 7);
		CURSOR_N = new Cursor(display, new ImageData(MoveResize.class.getResourceAsStream("CURSOR_N.png")), 7, 0);
		CURSOR_E = new Cursor(display, new ImageData(MoveResize.class.getResourceAsStream("CURSOR_E.png")), 15, 7);
		CURSOR_W = new Cursor(display, new ImageData(MoveResize.class.getResourceAsStream("CURSOR_W.png")), 0, 7);
		CURSOR_S = new Cursor(display, new ImageData(MoveResize.class.getResourceAsStream("CURSOR_S.png")), 7, 15);
		CURSOR_NE = new Cursor(display, new ImageData(MoveResize.class.getResourceAsStream("CURSOR_NE.png")), 15, 0);
		CURSOR_NW = new Cursor(display, new ImageData(MoveResize.class.getResourceAsStream("CURSOR_NW.png")), 0, 0);
		CURSOR_SE = new Cursor(display, new ImageData(MoveResize.class.getResourceAsStream("CURSOR_SE.png")), 15, 15);
		CURSOR_SW = new Cursor(display, new ImageData(MoveResize.class.getResourceAsStream("CURSOR_SW.png")), 0, 15);
	}
	
	private static class ImagePainter implements PaintListener {
		private final Image image;

		ImagePainter(Image image) {
			this.image = image;
		}
		public void paintControl(PaintEvent e) {
			e.gc.drawImage(image, 0, 0);
			if (e.widget instanceof Control) {
				Control control = (Control) e.widget;
				e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_BLACK));
				Rectangle bounds = control.getBounds();
				int times = 2;
				for (int i = 0; i < times; i++) {
					e.gc.drawRoundRectangle(
							i,
							i,
							bounds.width -1 - (2*i),
							bounds.height -1 - (2*i),
							BORDER_THICKNESS*2,
							BORDER_THICKNESS*2);				
				}
			}
		}		
	};

}
