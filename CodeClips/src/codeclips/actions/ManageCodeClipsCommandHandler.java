package codeclips.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

public class ManageCodeClipsCommandHandler extends AbstractHandler {
	public static final String ID = "CodeClips.create.command";

	public ManageCodeClipsCommandHandler() {
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveWorkbenchWindow(event).getShell();
		manageCodeClips(shell, shell);
		return null;
	}

	private static void manageCodeClips(Shell parentShell, Shell shell) {
		ManageCodeClipsDialog manageCodeClipsDialog = new ManageCodeClipsDialog(parentShell, shell);
		manageCodeClipsDialog.open();
	}
}
