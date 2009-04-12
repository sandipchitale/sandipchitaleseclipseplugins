package moveresize;

import org.eclipse.swt.SWT;
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
