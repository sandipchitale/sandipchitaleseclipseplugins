package text.overview.views;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class RefreshOverviewViewHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		if (activePage != null) {
			IViewPart view = activePage.findView(OverviewView.ID);
			if (view instanceof OverviewView) {
				OverviewView overviewView = (OverviewView) view;
				overviewView.refreshOverviewView();
			}
		}
		return null;
	}

}
