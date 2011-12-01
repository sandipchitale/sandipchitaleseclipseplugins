package closeviews;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class CloseActiveViewHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow activeWorkbenchWindow = HandlerUtil
				.getActiveWorkbenchWindow(event);
		if (activeWorkbenchWindow != null) {
			IWorkbenchPage activePage = activeWorkbenchWindow
					.getActivePage();
			IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
			if (activePart instanceof IViewPart) {
				IViewPart viewPart = (IViewPart) activePart;
				if (activePage != null) {
					if (activePage.isPageZoomed()) {
						activePage.zoomOut();
						if (!MessageDialog.openConfirm(
								activeWorkbenchWindow.getShell(),
								"Confirm Close Next View", "Close Next View?")) {
							return null;
						}
					}
					activePage.hideView(viewPart);
				}
			} else {
				if (activePage != null) {
					IViewReference[] viewReferences = activePage
							.getViewReferences();
					if (viewReferences.length > 0) {
						if (activePage.isPageZoomed()) {
							activePage.zoomOut();
							if (!MessageDialog.openConfirm(
									activeWorkbenchWindow.getShell(),
									"Confirm Close Next View",
									"Close Next View?")) {
								return null;
							}
						}
						activePage.hideView(viewReferences[0]);
					} else {
						activeWorkbenchWindow.getShell().getDisplay().beep();
					}
				}
			}
		}
		return null;
	}

}
