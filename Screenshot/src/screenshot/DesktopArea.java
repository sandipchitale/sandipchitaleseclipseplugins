package screenshot;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.progress.UIJob;

public class DesktopArea implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	public void dispose() {}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
		((WorkbenchWindow) window).getStatusLineManager().setMessage("Screenshot in 5 seconds...");
		UIJob uiJob = new UIJob("Click!") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				/* Take the screen shot */
				Shell shell = window.getShell();
				if (shell == null || shell.isDisposed()) {
					return Status.CANCEL_STATUS;
				}
				Util.processImage(shell, Util.getImage(shell));
				((WorkbenchWindow) window).getStatusLineManager().setMessage("Screenshot in 5 seconds...Done.");
				return Status.OK_STATUS;
			}			
		};
		uiJob.setPriority(UIJob.INTERACTIVE);
		uiJob.schedule(5000);
	}

	public void selectionChanged(IAction action, ISelection selection) {}

}
