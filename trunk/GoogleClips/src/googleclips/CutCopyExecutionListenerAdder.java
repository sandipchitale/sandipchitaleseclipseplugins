package googleclips;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

public class CutCopyExecutionListenerAdder implements IStartup {
	public void earlyStartup() {
		// Add listener to monitor Cut and Copy commands
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getAdapter(ICommandService.class);
		if (commandService != null) {
			commandService.addExecutionListener(new IExecutionListener() {
				public void notHandled(String commandId, NotHandledException exception) {
				}

				public void postExecuteFailure(String commandId, ExecutionException exception) {
				}

				public void postExecuteSuccess(String commandId, Object returnValue) {
					if (Activator.getDefault().isAutoClipCutCopy()) {
						if ("org.eclipse.ui.edit.copy".equals(commandId) || "org.eclipse.ui.edit.cut".equals(commandId)) {
							Clipboard clipboard = new Clipboard(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().getDisplay());
							Object contents = clipboard.getContents(TextTransfer.getInstance());
							if (contents instanceof String) {
								Activator.getDefault().copyGoogleClip((String) contents);
							}
						}
					}
				}

				public void preExecute(String commandId, ExecutionEvent event) {
				}

			});
		}
	}

}
