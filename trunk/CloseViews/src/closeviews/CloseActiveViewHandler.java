package closeviews;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class CloseActiveViewHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart instanceof IViewPart) {
			IViewPart viewPart = (IViewPart) activePart;
			IWorkbenchPage activePage = viewPart.getSite().getWorkbenchWindow()
					.getActivePage();
			if (activePage != null) {
				activePage.hideView(viewPart);
			}
		}
		return null;
	}

}
