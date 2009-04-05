package moveresize;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

public class Mover extends Delta {
	public Mover(Shell shell) {
		super(shell);
		shell.setCursor(shell.getDisplay().getSystemCursor(SWT.CURSOR_SIZEALL));
	}
	
	private Shell getShell() {
		return (Shell) super.getControl();
	}

	private Point location = null;
	protected void process(int deltaX, int deltaY, STATE state, int x, int y) {
		Shell shell = getShell();
		
		if (shell.isDisposed()) {
			return;
		}
		switch (state) {
		case BEGIN:
			location = shell.getLocation();
			break;
		case IN_PROGRESS:
			move(location, deltaX, deltaY);
			break;
		case END:
			move(location, deltaX, deltaY);
			location = null;
			break;
		}
	}
	
	void move(Point location, int deltaX, int deltaY) {
		if (control == null || control.isDisposed()) {
			return;
		}
		control.setLocation(new Point(location.x + deltaX, location.y + deltaY));
	}
	
	protected void setCursor(Point xy) {
	}
}
