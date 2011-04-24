package findreplacebar;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;

/**
 * This shows the Find Replace Bar view.
 * 
 * @author Sandip Chitale
 * 
 */
public class ShowFindReplaceBarHandler extends AbstractHandler {
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Object variable = HandlerUtil.getActivePart(event);
		
		if (variable instanceof IWorkbenchPart) {
			IWorkbenchPart workbenchPart = (IWorkbenchPart) variable;
			Object adapter = workbenchPart.getAdapter(IFindReplaceTarget.class);
			if (adapter != null) {
				if (Activator.getDefault().getOverrideFindReplaceDialog() && workbenchPart instanceof IEditorPart) {
					showFindReplaceBar(workbenchPart.getSite().getWorkbenchWindow());
				} else {
					showFindReplaceDialog(workbenchPart);
				}
			}
		}
		
		return null;
	}
	private static void showFindReplaceBar(IWorkbenchWindow workbenchWindow) {
		IWorkbenchPage activePage = workbenchWindow.getActivePage();
		IViewPart viewPart;
		try {
			viewPart = activePage.showView("FindReplaceBar.view"); //$NON-NLS-1$
			activePage.activate(viewPart);
			((FindReplaceBarViewPart)viewPart).startFind();
		} catch (PartInitException e) {
		}
	}
	
	private static void showFindReplaceDialog(IWorkbenchPart workbenchPart) {
		IHandlerService handlerService = (IHandlerService) workbenchPart.getSite().getService(IHandlerService.class);
		try {
			handlerService.executeCommand(
					IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE,
					null);
		} catch (NotDefinedException e) {
		} catch (NotEnabledException e) {
		} catch (NotHandledException e) {
		} catch (ExecutionException e) {
		}
	}
}
