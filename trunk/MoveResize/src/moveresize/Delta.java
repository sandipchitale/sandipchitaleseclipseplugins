package moveresize;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

public class Delta {
	public enum STATE {BEGIN, IN_PROGRESS, END};
	
    public Delta(Control control) {
        this.control = control;
        attached = new AtomicBoolean(false);
    }
   
    protected void attach(Point displayRelativeStart) {
    	this.displayRelativeStart = displayRelativeStart;
    	attach();
    }
    
	public final void attach() {
        if (attached.compareAndSet(false, true)) {
        	control.addListener(SWT.MouseDown, listener);
    		control.addListener(SWT.MouseUp, listener);
    		control.addListener(SWT.MouseMove, listener);
        }
    }

    public final void dettach() {
        if (attached.compareAndSet(true, false)) {
        	control.removeListener(SWT.MouseDown, listener);
    		control.removeListener(SWT.MouseUp, listener);
    		control.removeListener(SWT.MouseMove, listener);
        }
    }
    
    protected final Control control;
    private AtomicBoolean attached;
    private Point displayRelativeStart;
    
    Listener listener = new Listener() {
        public void handleEvent(Event event) {
            if (control.isDisposed()) {
                return;
            }
            Point eventPoint = new Point(event.x, event.y);
            try {
            	Point displayRelativeCurrent = control.getDisplay().map(control, null, eventPoint);
            	switch (event.type) {
            	case SWT.MouseDown:
            		displayRelativeStart = control.getDisplay().map(control, null, eventPoint);
            		process(0, 0, STATE.BEGIN, event.x, event.y);
            		break;
            	case SWT.MouseMove:
            		if (displayRelativeStart != null) {
            			process(displayRelativeCurrent.x - displayRelativeStart.x,
            					displayRelativeCurrent.y - displayRelativeStart.y,
            					STATE.IN_PROGRESS,
            					event.x, event.y);
            		}
            		break;
            	case SWT.MouseUp:
            		if (displayRelativeStart != null) {
            			process(displayRelativeCurrent.x - displayRelativeStart.x,
            					displayRelativeCurrent.y - displayRelativeStart.y,
            					STATE.END,
            					event.x,
            					event.y);
            			displayRelativeStart = null;
            		}
            		break;
            	}
            } finally {
            	setCursor(eventPoint);
            }
        }
    };

    protected Control getControl() {
        return control;
    }
   
    protected void process(int deltaX, int deltaY, STATE state, int x, int y) {
    	System.out.println(
    			"(" + deltaX + "," + deltaY + ") state=" + state
    			+ " at " + "(" + x + "," + y + ")"
    			);
    }
    
    protected void setCursor(Point eventPoint) {}
}