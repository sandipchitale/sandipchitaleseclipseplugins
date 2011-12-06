package closeviews;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

public class CloseAllViewsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow activeWorkbenchWindow = HandlerUtil
				.getActiveWorkbenchWindow(event);
		if (activeWorkbenchWindow != null) {
			IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
			if (activePage != null) {
				IViewReference[] viewReferences = activePage
						.getViewReferences();
				if (viewReferences.length > 0) {
					if (activePage.isPageZoomed()) {
						activePage.zoomOut();
						if (!MessageDialog.openConfirm(activeWorkbenchWindow.getShell(), "Confirm Close All Views", "Close All Views?")){
							return null;
						}
					} 
					for (IViewReference viewReference : viewReferences) {
						activePage.hideView(viewReference);
					}
				} else {
					IHandlerService handlerService = (IHandlerService) activeWorkbenchWindow
							.getService(IHandlerService.class);
					if (handlerService != null) {
						try {
							Activator.showMessage(activePage, "Reset the perspective to get back original set of views.");
							handlerService.executeCommand(IWorkbenchCommandConstants.WINDOW_RESET_PERSPECTIVE, null);
							Activator.showMessage(activePage, "");
						} catch (NotDefinedException e) {
							activeWorkbenchWindow.getShell().getDisplay().beep();
						} catch (NotEnabledException e) {
							activeWorkbenchWindow.getShell().getDisplay().beep();
						} catch (NotHandledException e) {
							activeWorkbenchWindow.getShell().getDisplay().beep();
						}
					}
				}
			}
		}
		return null;
	}

}
