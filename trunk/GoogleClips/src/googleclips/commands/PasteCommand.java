package googleclips.commands;

import googleclips.Activator;

import java.util.List;

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
    private int candidateInsertIndex = 0;
    private long lastPasteTimeInMillis = -1L;

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window == null) {
			return null;
		}
        long currentTimeMillis = System.currentTimeMillis();
        if ((lastPasteTimeInMillis == -1)
                || (currentTimeMillis - lastPasteTimeInMillis) > 2000L) {
            candidateInsertIndex = 0;
        } else {
            candidateInsertIndex++;
        }
        lastPasteTimeInMillis = currentTimeMillis;
        List<String> googleClips = Activator.getDefault().getGoogleClips();        
		String textToInsert = googleClips.get(candidateInsertIndex % googleClips.size());
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
