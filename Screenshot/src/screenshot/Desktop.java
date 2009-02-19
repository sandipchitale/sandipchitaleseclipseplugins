package screenshot;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class Desktop implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	public void dispose() {

	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
        /* Take the screen shot */
        Shell shell = window.getShell();
		Display display = shell.getDisplay();
		GC gc = new GC(display);
        final Image image = new Image(display, display.getBounds());
        gc.copyArea(image, 0, 0);
        gc.dispose();
        
        Util.processImage(shell, image);
	}

	public void selectionChanged(IAction action, ISelection selection) {

	}

}
