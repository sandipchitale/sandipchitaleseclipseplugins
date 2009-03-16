package googleclips.commands;

import googleclips.Activator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

public class PasteCommand implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}
		String textToInsert = Activator.getDefault().getGoogleClip();
		if (textToInsert != null) {
			IEditorPart activeEditor = window.getActivePage().getActiveEditor();
			if (activeEditor instanceof ITextEditor) {
				ITextEditor editor = (ITextEditor) activeEditor;
				Object adapter = (Control) editor.getAdapter(Control.class);
				if (adapter instanceof Control) {
					Control control = (Control) adapter;
					if (control instanceof StyledText) {
						StyledText styledText = (StyledText) control;
						if (!styledText.getEditable()) {
							window.getShell().getDisplay().beep();
							return null;
						}
						int caretOffset = styledText.getCaretOffset();
						styledText.insert(textToInsert);
						styledText.setSelection(caretOffset + textToInsert.length(), caretOffset);
					}
				}
			}
		}
		return null;
	}

	public boolean isEnabled() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return false;
		}

		IEditorPart activeEditor = window.getActivePage().getActiveEditor();
		if (activeEditor instanceof ITextEditor) {
			ITextEditor editor = (ITextEditor) activeEditor;
			Object adapter = (Control) editor.getAdapter(Control.class);
			if (adapter instanceof Control) {
				return true;
			}
		}
		return false;
	}

	public boolean isHandled() {
		return isEnabled();
	}

	public void dispose() {
	}

	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	public void removeHandlerListener(IHandlerListener handlerListener) {

	}

}
