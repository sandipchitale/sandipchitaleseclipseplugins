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

public class CloseOtherViewsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
		if (activeWorkbenchWindow != null) {
			IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
			if (activePart instanceof IViewPart) {
				IViewPart activeViewPart = (IViewPart) activePart;
				IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
				if (activePage != null) {
					IViewReference[] viewReferences = activePage.getViewReferences();
					if (viewReferences.length > 0) {
						if (activePage.isPageZoomed()) {
							activePage.zoomOut();
							if (!MessageDialog.openConfirm(activeWorkbenchWindow.getShell(), "Confirm Other Views",
									"Close Other Views except " + activeViewPart.getTitle() + "?")) {
								return null;
							}
						}
						for (IViewReference viewReference : viewReferences) {
							if (activeViewPart.getSite().getId().equals(viewReference.getId())) {
								continue;
							}
							activePage.hideView(viewReference);
						}
					}
				}
			} else {
				activeWorkbenchWindow.getShell().getDisplay().beep();
			}
		}
		return null;
	}

}
