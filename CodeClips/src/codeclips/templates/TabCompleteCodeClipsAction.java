package codeclips.templates;

import java.util.ResourceBundle;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IPaintPositionManager;
import org.eclipse.jface.text.IPainter;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.TextEditorAction;

public class TabCompleteCodeClipsAction extends TextEditorAction {
	
	public static IAction create(ITextEditor textEditor) {
		return new TabCompleteCodeClipsAction(ResourceBundle.getBundle(TabCompleteCodeClipsAction.class.getName()), "TabCompleteCodeClipsAction.", textEditor); //$NON-NLS-1$
	}
	
	private ITextViewer textViewer;
	private StyledText textWidget;

	private ContentAssistant contentAssistant;
	
	private boolean deactivated = false;
	
	private IPainter painter = new IPainter() {
		public void setPositionManager(IPaintPositionManager manager) {}
		
		public void paint(int reason) {
			switch (reason) {
			case IPainter.SELECTION:
			case IPainter.KEY_STROKE:
				adjustHandledState();
				break;
			}
		}
		
		public void dispose() {}
		
		public void deactivate(boolean redraw) {}
	};

	protected TabCompleteCodeClipsAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
		setActionDefinitionId(TabCompleteCodeClipsCommandHandler.ID);
		if (editor instanceof AbstractTextEditor) {
			AbstractTextEditor abstractTextEditor = (AbstractTextEditor) editor;
			Object adapter = abstractTextEditor.getAdapter(ITextOperationTarget.class);
			if (adapter instanceof ITextViewer) {
				textViewer = (ITextViewer) adapter;
				contentAssistant = new CodeClipsContentAssistant(this);
				contentAssistant.install(textViewer);
				textWidget = textViewer.getTextWidget();
				if (textViewer instanceof ITextViewerExtension2) {
					ITextViewerExtension2 textViewerExtension2 = (ITextViewerExtension2) textViewer;
					textViewerExtension2.addPainter(painter);
				}
			}
		}
	}
	
	@Override
	public void run() {
		if (contentAssistant != null) {
			contentAssistant.showPossibleCompletions();
		}
	}

	void adjustHandledState() {
		if (isDeactivated()) {
			deactivate();
			return;
		}
		if (textWidget != null) {
			if (textWidget.isDisposed()) {
				deactivate();
			}
			int caretOffset = textWidget.getCaretOffset();
			if (caretOffset > 0) {
				String text = textWidget.getText(caretOffset-1, caretOffset-1);
				if (!Character.isWhitespace(text.charAt(0))) {
					activate();
					return;
				}
			}
			deactivate();
		}
	}

	boolean isDeactivated() {
		return deactivated;
	}

	void setDeactivated(boolean deactivated) {
		this.deactivated = deactivated;
		adjustHandledState();
	}

	void activate() {
		getTextEditor().setAction(getActionDefinitionId(), this);
	}
	
	void deactivate() {
		getTextEditor().setAction(getActionDefinitionId(), null);
	}
}
