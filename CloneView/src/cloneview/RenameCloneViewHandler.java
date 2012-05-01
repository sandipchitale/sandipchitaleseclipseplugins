package cloneview;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class RenameCloneViewHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		if (activePart instanceof SystemViewPartClone) {
			SystemViewPartClone systemViewPartClone = (SystemViewPartClone) activePart;

			IWorkbenchWindow activeWorkbenchWindow = HandlerUtil
					.getActiveWorkbenchWindow(event);
			InputDialog inputDialog = new InputDialog(
					activeWorkbenchWindow.getShell(), "Rename", "Name:",
					systemViewPartClone.getPartName(), null);
			if (inputDialog.open() == InputDialog.OK) {
				systemViewPartClone.setPartName(inputDialog.getValue());
			}
		}
		return null;
	}

}
