package cutcopypasteplus;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;

public class PasteFromHistoryMenuCommandHandler extends AbstractHandler {

	class ShowNewLineLabelProvider extends LabelProvider {
		@Override
		public String getText(Object element) {
			String text = super.getText(element);
			return text.replace("\n", "\\n");
		}
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Control focusedControl = IsFocusedInTextPropertyTester.getFocusControl();
		if (focusedControl instanceof Text || focusedControl instanceof StyledText) {
			CutCopyHistory cutCopyHistory = CutCopyHistory.getInstance();
			if (cutCopyHistory.size() > 0) {
				Shell activeShell = HandlerUtil.getActiveShell(event);

				ElementListSelectionDialog elementListSelectionDialog = new ElementListSelectionDialog(activeShell, new ShowNewLineLabelProvider());
				elementListSelectionDialog.setTitle("Paste from history");
				elementListSelectionDialog.setMessage("Select entry to paste:");
				elementListSelectionDialog.setElements(cutCopyHistory.getHistory());
				if (elementListSelectionDialog.open() == Window.OK) {
					Object[] result = elementListSelectionDialog.getResult();
					if (result.length > 0) {
						String textToPaste = (String) result[0];
						if (textToPaste == null) {
							HandlerUtil.getActiveShell(event).getDisplay().beep();
							return null;
						}
						if (focusedControl instanceof StyledText) {
							StyledText styledText = (StyledText) focusedControl;
							if (!styledText.getEditable()) {
								HandlerUtil.getActiveShell(event).getDisplay().beep();
								return null;
							}
							int caretOffset = styledText.getCaretOffset();
							styledText.insert(textToPaste);
							styledText.setSelection(caretOffset + textToPaste.length(), caretOffset);
						} else if (focusedControl instanceof Text) {
							final Text text = (Text) focusedControl;
							if (!text.getEditable()) {
								HandlerUtil.getActiveShell(event).getDisplay().beep();
								return null;
							}
							final String finalTextToPaste = textToPaste;
							final int caretOffset = text.getSelection().x;
							text.insert(textToPaste);
							HandlerUtil.getActiveShell(event).getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									text.setSelection(caretOffset + finalTextToPaste.length(), caretOffset);
								}
							});
						}
					}
				}
			}
		}
		return null;
	}
}
