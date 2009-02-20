package screenshot;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class DesktopArea implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	public void dispose() {}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
        /* Take the screen shot of rubberbanded area*/
        Shell shell = window.getShell();
        Util.processImage(shell, Util.getImage(shell));
	}

	public void selectionChanged(IAction action, ISelection selection) {}

}
