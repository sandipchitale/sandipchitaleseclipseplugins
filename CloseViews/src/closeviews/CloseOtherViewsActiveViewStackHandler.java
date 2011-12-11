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
import org.eclipse.ui.internal.ILayoutContainer;
import org.eclipse.ui.internal.LayoutPart;
import org.eclipse.ui.internal.PartPane;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.ViewPane;

@SuppressWarnings("restriction")
public class CloseOtherViewsActiveViewStackHandler extends AbstractHandler {

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
								activeWorkbenchWindow.getShell(),
								"Close other Views in tab",
								"Close other Views in tab containing " + viewPart.getTitle() + " View ?")) {
							return null;
						}
					}
					closeOtherViewsActiveViewStack(activePage, viewPart);
				}
			} else {
				if (activePage != null) {
					IViewReference[] viewReferences = activePage
							.getViewReferences();
					if (viewReferences.length > 0) {
						if (activePage.isPageZoomed()) {
							activePage.zoomOut();
						}
						IViewPart viewPart = activePage.findView(viewReferences[0].getId());
						if (viewPart != null) {
							if (!MessageDialog.openConfirm(
									activeWorkbenchWindow.getShell(),
									"Close other Views in Tab",
									"Close other Views in tab containing " + viewPart.getTitle() + " View ?")) {
								return null;
							}
							closeOtherViewsActiveViewStack(activePage, viewPart);
						}
					} else {
						activeWorkbenchWindow.getShell().getDisplay().beep();
					}
				}
			}
		}
		return null;
	}

	private static void closeOtherViewsActiveViewStack(IWorkbenchPage activePage,
			IViewPart viewPart) {
		
		PartPane currentViewPartPane = ((PartSite) viewPart
				.getSite()).getPane();
		LayoutPart layoutPart = currentViewPartPane.getPart();
		ILayoutContainer layoutPartContainer = layoutPart
				.getContainer();
		LayoutPart[] children = layoutPartContainer.getChildren();
		if (children.length > 0) {
			String viewPartId = viewPart.getViewSite().getId();
			for (LayoutPart childLayoutPart : children) {
				if (childLayoutPart instanceof ViewPane) {
					ViewPane viewPane = (ViewPane) childLayoutPart;
					String viewPaneId = viewPane.getID();
					if (viewPartId.equals(viewPaneId)) {
						continue;
					}
					viewPart = activePage.findView(viewPaneId);
					activePage.hideView(viewPart);
				}
			}
		}
	}

}
