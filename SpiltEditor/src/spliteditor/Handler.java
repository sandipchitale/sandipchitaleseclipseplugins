/**
 * Copyright (c) 2011. Sandip Chitale.
 * The following code is based on article
 * 
 * http://eclipse.dzone.com/tips/programmatically-split-editor-
 * 
 * By: Dimitri Missoh
 * 
 * It has been modified to suite our purpose.
 */
package spliteditor;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.EditorSashContainer;
import org.eclipse.ui.internal.EditorStack;
import org.eclipse.ui.internal.ILayoutContainer;
import org.eclipse.ui.internal.LayoutPart;
import org.eclipse.ui.internal.PageLayout;
import org.eclipse.ui.internal.PartPane;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.PartStack;
import org.eclipse.ui.internal.WorkbenchPage;

/**
 * This is command handler for splitting the active editor
 * Horizontally or Vertically based on how it was invoked.
 * 
 * @author Sandip Chitale
 */
@SuppressWarnings("restriction")
public class Handler extends AbstractHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		IEditorPart activeEditor = HandlerUtil.getActiveEditor(event);
		if (activeEditor != null) {
			final IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
			if (activeWorkbenchWindow != null) {
				final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
				final String activeEditorId = HandlerUtil.getActiveEditorId(event);

				activeWorkbenchWindow.getActivePage().addPartListener(new IPartListener() {
					@Override
					public void partOpened(IWorkbenchPart part) {}
					
					@Override
					public void partDeactivated(IWorkbenchPart part) {}
					
					@Override
					public void partClosed(IWorkbenchPart part) {}
					
					@Override
					public void partBroughtToTop(IWorkbenchPart part) {}
					
					@Override
					public void partActivated(IWorkbenchPart part) {
						activePage.removePartListener(this);
						if (part instanceof IEditorPart) {
							IEditorPart editorPart = (IEditorPart) part;
							if (activeEditorId.equals(editorPart.getSite().getId())) {
								Helper.splitEditor(editorPart, event.getParameter(OrientationParameterValues.ORIENTATION));
							}
						}
						
					}
				});

				IHandlerService handlerService = (IHandlerService) activeEditor.getEditorSite().getService(IHandlerService.class);
				try {
					// Duplicate the editor
					handlerService.executeCommand(IWorkbenchCommandConstants.WINDOW_NEW_EDITOR, null);
					// Then handle the splitting in the PartListener above
				} catch (NotDefinedException e) {
				} catch (NotEnabledException e) {
				} catch (NotHandledException e) {
				}
			}
		}
		return null;
	}
}
