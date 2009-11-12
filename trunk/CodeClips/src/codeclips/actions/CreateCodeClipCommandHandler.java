package codeclips.actions;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.texteditor.ITextEditor;

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
			ITextEditor textEditor = (ITextEditor) variable;
			System.out.println("Create Code Clip in " + textEditor.getEditorInput().getToolTipText());
		} else if (variable instanceof MultiPageEditorPart) {
			MultiPageEditorPart multiPageEditorPart = (MultiPageEditorPart) variable;
			Object selectedPage = multiPageEditorPart.getSelectedPage();
			if (selectedPage instanceof ITextEditor) {
				ITextEditor textEditor = (ITextEditor) selectedPage;
				System.out.println("Create Code Clip in " + textEditor.getEditorInput().getToolTipText());
			}
		}
		return null;
	}

}
