package screenshot;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class WorkbenchWindow implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	public void dispose() {}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
		/* Take the screen shot */
		Shell shell = window.getShell();
		Image desktopImage = Util.getDesktopImage(shell.getDisplay(), shell.getBounds());
		Util.processImage(shell, desktopImage);
	}

	public void selectionChanged(IAction action, ISelection selection) {}

}
