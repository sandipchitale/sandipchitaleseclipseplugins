package moveresize;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;

public class Resizer extends Delta {
	static final String RESIZING = Resizer.class.getName() + ".RESIZING";
	
	static final int BORDER_THICKNESS = 8;
	
	public Resizer(Control control) {
		super(control);
	}
	
	private Point location = null;
	private Point size = null;
	private int operation = SWT.CURSOR_ARROW;
	
	private KeyListener keyListener = new KeyListener() {
		boolean grow = true;
		public void keyPressed(KeyEvent e) {
		}

		public void keyReleased(KeyEvent e) {
			if (e.character == '.' || e.character == '-') {
				grow = false;
				e.doit = false;
				return;
			}
			if (e.character == '=' || e.character == '+' || e.character == 'o' || e.character == 'O') {
				grow = true;
				e.doit = false;
				return;
			}
			if (e.keyCode == SWT.ARROW_UP 
					|| e.keyCode == SWT.ARROW_DOWN
					|| e.keyCode == SWT.ARROW_LEFT
					|| e.keyCode == SWT.ARROW_RIGHT) {

				Control control = getControl();

				if (control.isDisposed()) {
					return;
				}

				e.doit = false;
				
				location = control.getLocation();
				size = control.getSize();

				int delta = 0;
				if ((e.stateMask & (SWT.SHIFT | SWT.CONTROL)) == 0) {
					delta = 10;
				} else if ((e.stateMask & SWT.SHIFT) != 0 ) {
					delta = 100;
				} else if ((e.stateMask & SWT.CONTROL) != 0) {
					delta = 1;
				}
				boolean moving = true;
				if ((e.stateMask & SWT.ALT) != 0) {
					moving = false;
				}
				try {
					switch(e.keyCode) { 
					case SWT.ARROW_UP:
						if (moving) {
							resize(location, size, SWT.CURSOR_SIZEALL, 0, -delta);
						} else {
							resize(location, size, (grow ? SWT.CURSOR_SIZEN : SWT.CURSOR_SIZES), 0, -delta);														
						}
						break;
					case SWT.ARROW_DOWN:
						if (moving) {
							resize(location, size, SWT.CURSOR_SIZEALL, 0, delta);
						} else {
							resize(location, size, (grow ? SWT.CURSOR_SIZES : SWT.CURSOR_SIZEN), 0, delta);						
						}
						break;
					case SWT.ARROW_LEFT:
						if (moving) {
							resize(location, size, SWT.CURSOR_SIZEALL, -delta, 0);
						} else {
							resize(location, size, (grow ? SWT.CURSOR_SIZEW : SWT.CURSOR_SIZEE), -delta, 0);						
						}
						break;
					case SWT.ARROW_RIGHT:
						if (moving) {
							resize(location, size, SWT.CURSOR_SIZEALL, delta, 0);
						} else {
							resize(location, size, (grow ? SWT.CURSOR_SIZEE : SWT.CURSOR_SIZEW), delta, 0);						
						}
						break;
					}
				} finally {
					location = null;
					size = null;
					control.setCursor(MoveResize.getCursor(SWT.CURSOR_ARROW));
				}
			}
		}
	};

	public void attach() {
		super.attach();
		control.addKeyListener(keyListener);
	}

	public void dettach() {
		super.dettach();
		control.removeKeyListener(keyListener);
	}

	protected void process(int deltaX, int deltaY, STATE state, int x, int y) {
		Control control = getControl();
		
		if (control.isDisposed()) {
			return;
		}
		switch (state) {
		case BEGIN:
			control.setData(RESIZING, Boolean.TRUE);
			operation = operation(control, x, y);
			location = control.getLocation();
			size = control.getSize();
			resize(location, size, operation, deltaX, deltaY);
			break;
		case IN_PROGRESS:
			control.setData(RESIZING, Boolean.TRUE);
			resize(location, size, operation, deltaX, deltaY);
			break;
		case END:
			control.setData(RESIZING, null);
			resize(location, size, operation, deltaX, deltaY);
			operation = SWT.CURSOR_ARROW;
			location = null;
			size = null;
			break;
		}
	}
	
	protected void setCursor(Point xy) {
		if (control == null || control.isDisposed()) {
			return;
		}
		control.setCursor(MoveResize.getCursor(operation(control, xy.x, xy.y)));
	}
	
	void resize(Point location, Point size, int operation, int deltaX, int deltaY) {
		if (control == null || control.isDisposed()) {
			return;
		}
		
		Rectangle bounds = new Rectangle(location.x, location.y, size.x, size.y);
		
		switch (operation) {
		case SWT.CURSOR_SIZESW:
			bounds.x += deltaX;
			bounds.width -= deltaX;
			bounds.height += deltaY;
			break;
		case SWT.CURSOR_SIZEW:
			bounds.x += deltaX;
			bounds.width -= deltaX;
			break;
		case SWT.CURSOR_SIZENW:
			bounds.x += deltaX;
			bounds.y += deltaY;
			bounds.width -= deltaX;
			bounds.height -= deltaY;
			break;
		case SWT.CURSOR_SIZEN:
			bounds.y += deltaY;
			bounds.height -= deltaY;
			break;		
		case SWT.CURSOR_SIZENE:
			bounds.y += deltaY;
			bounds.width += deltaX;
			bounds.height -= deltaY;
			break;
		case SWT.CURSOR_SIZESE:
			bounds.width += deltaX;
			bounds.height += deltaY;
			break;
		case SWT.CURSOR_SIZES:
			bounds.height += deltaY;
			break;
		case SWT.CURSOR_SIZEE:
			bounds.width += deltaX;
			break;
		case SWT.CURSOR_SIZEALL:
			bounds.x += deltaX;
			bounds.y += deltaY;
			break;
		}
		
		bounds.width = Math.max(2*BORDER_THICKNESS, bounds.width);
		bounds.height = Math.max(2*BORDER_THICKNESS, bounds.height);
		
		control.setBounds(bounds);
		control.setCursor(MoveResize.getCursor(operation));
		control.redraw();
		control.getShell().update();
	}

	private static int operation(Control control, int x, int y) {
		Point size = control.getSize();
		if (x >= 0 && x < BORDER_THICKNESS) {
			if (y >= 0 && y < BORDER_THICKNESS) {
				return SWT.CURSOR_SIZENW;				
			} else if (y >= BORDER_THICKNESS && y < (size.y - BORDER_THICKNESS)) {
				return SWT.CURSOR_SIZEW;
			} else if (y >= (size.y - BORDER_THICKNESS) && y <= size.y) {
				return SWT.CURSOR_SIZESW;
			}
		} else if (x >= BORDER_THICKNESS && x < (size.x - BORDER_THICKNESS)) {
			if (y >= 0 && y < BORDER_THICKNESS) {
				return SWT.CURSOR_SIZEN;				
			} else if (y >= BORDER_THICKNESS && y < (size.y - BORDER_THICKNESS)) {
				return SWT.CURSOR_SIZEALL;
			} else if (y >= (size.y - BORDER_THICKNESS) && y <= size.y) {
				return SWT.CURSOR_SIZES;
			}
		} else if (x >= (size.x - BORDER_THICKNESS) && x < size.x) {
			if (y >= 0 && y < BORDER_THICKNESS) {
				return SWT.CURSOR_SIZENE;				
			} else if (y >= BORDER_THICKNESS && y < (size.y - BORDER_THICKNESS)) {
				return SWT.CURSOR_SIZEE;
			} else if (y >= (size.y - BORDER_THICKNESS) && y <= size.y) {
				return SWT.CURSOR_SIZESE;
			}
		}
		return SWT.CURSOR_ARROW;
	}
	
	static boolean isResizing(Control control) {
		return control.getData(RESIZING) != null;
	}
}
