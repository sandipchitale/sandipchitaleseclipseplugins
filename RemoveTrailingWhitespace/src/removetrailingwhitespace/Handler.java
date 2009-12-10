package removetrailingwhitespace;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.ui.ISources;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

public class Handler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Object applicationContext = event.getApplicationContext();
		if (applicationContext instanceof IEvaluationContext) {
			IEvaluationContext evaluationContext = (IEvaluationContext) applicationContext;
			Object activePartVariable = evaluationContext.getVariable(ISources.ACTIVE_PART_NAME);
			ITextEditor textEditor = null;
			if (activePartVariable instanceof ITextEditor) {
				textEditor = (ITextEditor) activePartVariable;
			} else if (activePartVariable instanceof MultiPageEditorPart) {
				MultiPageEditorPart multiPageEditorPart = (MultiPageEditorPart) activePartVariable;
				Object selectedPage = multiPageEditorPart.getSelectedPage();
				if (selectedPage instanceof ITextEditor) {
					textEditor = (ITextEditor) selectedPage;
				}
			}
			if (textEditor != null) {
				RemoveTrailingWhitespaceUtils.removeTrailingWhitespaceUtils(textEditor);
			}
		}
		return null;
	}

}
