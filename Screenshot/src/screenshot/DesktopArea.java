package screenshot;

import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.graphics.Image;
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
		final int[] countdownFrom = new int[1];
		countdownFrom[0] = 5;
		UIJob uiJob = new UIJob("") {
			public IStatus runInUIThread(IProgressMonitor monitor) {
				if (countdownFrom[0] >= 0) {
					if (countdownFrom[0] > 0) {
						((WorkbenchWindow) window).getStatusLineManager().setMessage("Screenshot in " + countdownFrom[0] + " seconds...");
					} else {
						((WorkbenchWindow) window).getStatusLineManager().setMessage("");
					}
					countdownFrom[0]--;
					schedule(1000);
					return Status.OK_STATUS;
				}
				Shell shell = window.getShell();
				if (shell == null || shell.isDisposed()) {
					return Status.CANCEL_STATUS;
				}
				shell.getDisplay().beep();
				/* Take the screen shot */
				Image image = Util.getImage(shell);
				Util.processImage(shell, image);
				return Status.OK_STATUS;
			}			
		};
		uiJob.setPriority(UIJob.INTERACTIVE);
		uiJob.setSystem(true);
		uiJob.schedule();
	}

	public void selectionChanged(IAction action, ISelection selection) {}

}
