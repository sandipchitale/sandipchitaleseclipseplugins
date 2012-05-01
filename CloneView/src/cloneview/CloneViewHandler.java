package cloneview;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.eclipse.ui.internal.misc.StatusUtil;
import org.eclipse.ui.statushandlers.StatusManager;

@SuppressWarnings("restriction")
public class CloneViewHandler extends AbstractHandler {
	private static Map<String, Integer> viewIdToCloneOrdinal = new HashMap<String, Integer>();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow activeWorkbenchWindow = HandlerUtil
				.getActiveWorkbenchWindow(event);

		try {
			String viewId = "org.eclipse.rse.ui.view.systemView.clone";
			Integer viewOrdinal = viewIdToCloneOrdinal.get(viewId);
			if (viewOrdinal == null) {
				viewOrdinal = -1;
			}
			viewOrdinal = 1 + viewOrdinal;
			viewIdToCloneOrdinal.put(viewId, viewOrdinal);
			openView(viewId, viewId + "." + Activator.PLUGIN_ID + "."
					+ viewOrdinal, activeWorkbenchWindow, viewOrdinal);
		} catch (PartInitException e) {
			StatusUtil.handleStatus(
					e.getStatus(),
					WorkbenchMessages.ShowView_errorTitle
							+ ": " + e.getMessage(), //$NON-NLS-1$
					StatusManager.SHOW);
		}
		return null;
	}

	/**
	 * Opens the view with the given identifier.
	 * 
	 * @param viewId
	 *            The view to open; must not be <code>null</code>
	 * @param secondaryId
	 *            an optional secondary id; may be <code>null</code>
	 * @param viewOrdinal
	 * @throws PartInitException
	 *             If the part could not be initialized.
	 */
	private final void openView(final String viewId, final String secondaryId,
			final IWorkbenchWindow activeWorkbenchWindow, int cloneId)
			throws PartInitException {

		final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
		if (activePage == null) {
			return;
		}

		SystemViewPartClone viewPart = (SystemViewPartClone) activePage
				.showView(viewId, secondaryId, IWorkbenchPage.VIEW_ACTIVATE);
		viewPart.setCloneId(cloneId);

	}

}
