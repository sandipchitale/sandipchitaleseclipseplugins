package eclipsemate;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.ITextEditor;

public class ExecuteLineInsertingResultHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
		if (page == null) {
			Activator.beep(activeWorkbenchWindow);
			return null;
		}
		IEditorPart editor = page.getActiveEditor();
		if (editor == null) {
			Activator.beep(activeWorkbenchWindow);
			return null;
		}
		Map<String, String> environment = Filter.computeEnvironment(activeWorkbenchWindow, editor);
		Filter.StringOutputConsumer filterOutputConsumer = new Filter.StringOutputConsumer();
		Filter.launch(environment.get(Filter.VARIABLES_NAMES.TM_CARET_LINE_TEXT.name()), environment, Filter.EOF, filterOutputConsumer);
		try {
			String output = filterOutputConsumer.getOutput();
			if (editor instanceof ITextEditor) {
				ITextEditor abstractTextEditor = (ITextEditor) editor;
				if (abstractTextEditor.isEditable()) {
					Object adapter = (Control) abstractTextEditor.getAdapter(Control.class);
					if (adapter instanceof Control) {
						Control control = (Control) adapter;
						if (control instanceof StyledText) {
							StyledText styledText = (StyledText) control;
							int caretOffset = styledText.getCaretOffset();
							int lineAtCaret = styledText.getLineAtOffset(caretOffset);
							int startOffsetOfLineAtCaret = styledText.getOffsetAtLine(lineAtCaret);
							int length = styledText.getLine(lineAtCaret).length();
							styledText.replaceTextRange(startOffsetOfLineAtCaret, length, output);
						}
					}
				}
			}
		} catch (InterruptedException e) {
			// TODO
		}
		return null;
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled();
	}
	
	@Override
	public boolean isHandled() {
		return super.isHandled();
	}
}
