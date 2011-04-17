package findreplacebar;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This takes care of the case when the active textEdior is an instance of
 * <code>IFindBarDecorated</code> or does adapt to
 * <code>IFindBarDecorated</code>.
 * 
 * @author schitale
 * 
 */
public class ShowFindBarHandler extends AbstractHandler {
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Object variable = HandlerUtil.getActivePart(event);
		if (variable instanceof ITextEditor) {
			ITextEditor textEditor = (ITextEditor) variable;
			if (textEditor instanceof AbstractTextEditor) {
				IWorkbenchPage activePage = textEditor.getSite()
						.getWorkbenchWindow().getActivePage();
				IViewPart viewPart;
				try {
					viewPart = activePage.showView("FindReplaceBar.view");
					activePage.activate(viewPart);
				} catch (PartInitException e) {
				}
			} else {
				IHandlerService handlerService = (IHandlerService) textEditor
						.getSite().getService(IHandlerService.class);
				try {
					handlerService.executeCommand(
							IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE,
							null);
				} catch (NotDefinedException e) {
				} catch (NotEnabledException e) {
				} catch (NotHandledException e) {
				}
			}
		}
		return null;
	}
}
