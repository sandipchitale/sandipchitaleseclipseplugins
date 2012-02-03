package cutcopypasteplus;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.handlers.HandlerUtil;

public class PasteCommandHandler extends AbstractHandler {
	private static long lastPasteInMillis = System.currentTimeMillis();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		long currentTimeMillis = System.currentTimeMillis();
		try {
			Control focusedControl = IsFocusedInTextPropertyTester.getFocusControl();
			if (focusedControl instanceof Text || focusedControl instanceof StyledText) {
				String textToPaste = null;

				if ((currentTimeMillis - lastPasteInMillis) > Activator.getDefault().getPasteNextDelay()) {
					if (Activator.getDefault().isCutAndCopyHistoryEnabled()) {
						textToPaste = CutCopyHistory.getInstance().getFirstTextToPaste();
					} else {
						CutCopyHistory.getInstance().reset();
						Clipboard clipboard = Activator.getDefault().getClipboard();
						if (clipboard != null) {
							Object contents = clipboard.getContents(TextTransfer.getInstance());
							if (contents instanceof String) {
								textToPaste = (String) contents;
							}
						}
					}
				} else {
					textToPaste = CutCopyHistory.getInstance().getNextTextToPaste();
				}
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
		} finally {
			lastPasteInMillis = currentTimeMillis;
		}
		return null;
	}

}
