package codeclips.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.texteditor.ITextEditor;

import codeclips.Activator;

public class CreateCodeClipCommandHandler extends AbstractHandler {
	public static final String ID = "CodeClips.create.command";

	public CreateCodeClipCommandHandler() {
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEvaluationService evaluationService = (IEvaluationService) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IEvaluationService.class);
		if (evaluationService == null) {
			return null;
		}
		IEvaluationContext evaluationContext = evaluationService.getCurrentState();
		
		Object variable = evaluationContext.getVariable("activePart");
		if (variable instanceof ITextEditor) {
			createCodeClip((ITextEditor) variable);
		} else if (variable instanceof MultiPageEditorPart) {
			MultiPageEditorPart multiPageEditorPart = (MultiPageEditorPart) variable;
			Object selectedPage = multiPageEditorPart.getSelectedPage();
			if (selectedPage instanceof ITextEditor) {
				createCodeClip((ITextEditor) selectedPage);
			}
		}
		return null;
	}
	
	private static void createCodeClip(ITextEditor textEditor) {
		CodeClipDialog codeClipDialog = new CodeClipDialog(textEditor.getSite().getShell(), textEditor);
		ISelection selection = textEditor.getSelectionProvider().getSelection();
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection = (ITextSelection) selection;
			codeClipDialog.setExpansion(textSelection.getText());
		}
		
		if (Window.OK == codeClipDialog.open()) {			
			Activator.getDefault().persistTemplate(codeClipDialog.getAbbrev(), codeClipDialog.getDescription(), codeClipDialog.getExpansion());
		}
	}
}
