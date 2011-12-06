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
			IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
			IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
			if (activePart instanceof IViewPart) {
				IViewPart viewPart = (IViewPart) activePart;
				if (activePage != null) {
					if (activePage.isPageZoomed()) {
						activePage.zoomOut();
						if (!MessageDialog.openConfirm(
								activeWorkbenchWindow.getShell(), "Close View",
								"Close " + viewPart.getTitle() + " View ?")) {
							return null;
						}
					}
					hideView(activePage, viewPart);
				}
			} else {
				if (activePage != null) {
					IViewReference[] viewReferences = activePage
							.getViewReferences();
					if (viewReferences.length > 0) {
						IViewPart viewPart = activePage
								.findView(viewReferences[0].getId());
						if (viewPart != null) {
							if (activePage.isPageZoomed()) {
								activePage.zoomOut();
								if (!MessageDialog.openConfirm(
										activeWorkbenchWindow.getShell(),
										"Close View",
										"Close " + viewPart.getTitle()
												+ "View ?")) {
									return null;
								}
							}
							hideView(activePage, viewPart, true);
						}
					} else {
						activeWorkbenchWindow.getShell().getDisplay().beep();
					}
				}
			}
		}
		return null;
	}

	private static void hideView(IWorkbenchPage activePage, IViewPart viewPart) {
		hideView(activePage, viewPart, false);
	}

	private static void hideView(IWorkbenchPage activePage, IViewPart viewPart,
			final boolean showMessage) {
		if (activePage == null || viewPart == null) {
			return;
		}
		final String viewTitle = viewPart.getTitle();
		activePage.hideView(viewPart);
		if (showMessage) {
			Activator.showMessage(activePage, "Closed " + viewTitle + " View.");
		}
	}

}
