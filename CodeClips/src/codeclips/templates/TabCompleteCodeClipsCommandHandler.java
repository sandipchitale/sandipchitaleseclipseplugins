package codeclips.templates;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditor;

public class TabCompleteCodeClipsCommandHandler extends AbstractHandler {
	private static Map<ITextViewer, Boolean> overrideNotHandled = new WeakHashMap<ITextViewer, Boolean>();

	static void setOverrideNotHandled(ITextViewer textViewer, boolean overrideNotHandled) {
		TabCompleteCodeClipsCommandHandler.overrideNotHandled.put(textViewer, overrideNotHandled);
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
		ITextEditor textEditor = null;
		if (activePart instanceof ITextEditor) {
			textEditor = (ITextEditor) activePart;
		} else if (activePart instanceof MultiPageEditorPart) {
			MultiPageEditorPart multiPageEditorPart = (MultiPageEditorPart) activePart;
			Object selectedPage = multiPageEditorPart.getSelectedPage();
			if (selectedPage instanceof ITextEditor) {
				textEditor = (ITextEditor) selectedPage;
			}
		}
		if (textEditor instanceof AbstractTextEditor) {
			AbstractTextEditor abstractTextEditor = (AbstractTextEditor) textEditor;
			Object adapter = abstractTextEditor.getAdapter(ITextOperationTarget.class);
			if (adapter instanceof ITextViewer) {
				ITextViewer textViewer = (ITextViewer) adapter;
				StyledText textWidget = textViewer.getTextWidget();
				CodeClipsContentAssistant contentAssistant = (CodeClipsContentAssistant) textWidget.getData("CodeClipsContentAssistant");
				if (contentAssistant == null) {
					contentAssistant = new CodeClipsContentAssistant();
					contentAssistant.install(textViewer);
				}
				contentAssistant.showPossibleCompletions();
			}
		}
		return null;
	}

	@Override
	public boolean isHandled() {
		IWorkbenchPart activePart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActivePart();
		ITextEditor textEditor = null;
		if (activePart instanceof ITextEditor) {
			textEditor = (ITextEditor) activePart;
		} else if (activePart instanceof MultiPageEditorPart) {
			MultiPageEditorPart multiPageEditorPart = (MultiPageEditorPart) activePart;
			Object selectedPage = multiPageEditorPart.getSelectedPage();
			if (selectedPage instanceof ITextEditor) {
				textEditor = (ITextEditor) selectedPage;
			}
		}
		if (textEditor instanceof AbstractTextEditor) {
			AbstractTextEditor abstractTextEditor = (AbstractTextEditor) textEditor;
			Object adapter = abstractTextEditor.getAdapter(ITextOperationTarget.class);
			if (adapter instanceof ITextViewer) {
				ITextViewer textViewer = (ITextViewer) adapter;

				Boolean overrideNotHandled = TabCompleteCodeClipsCommandHandler.overrideNotHandled.get(textViewer);
				if (overrideNotHandled != null && overrideNotHandled) {
					return false;
				}
				StyledText textWidget = textViewer.getTextWidget();
				int caretOffset = textWidget.getCaretOffset();
				if (caretOffset > 0) {
					return !Character.isWhitespace(textWidget.getText(caretOffset-1, caretOffset-1).charAt(0));
				}
			}
		}
		return false;
	}
}
