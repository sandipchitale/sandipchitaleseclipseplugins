package eclipsemate;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.handlers.HandlerUtil;

public class FilterThroughCommandHandler extends AbstractHandler {
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
		String editorId = editor.getSite().getId();
		if (editorId == null) {
			Activator.beep(activeWorkbenchWindow);
			return null;
		}
		Map<String, String> environment = Filter.computeEnvironment(activeWorkbenchWindow, editor);
		System.out.println(environment.toString().replaceAll(",", "\n"));
		//new FilterThroughCommandDialog(activeWorkbenchWindow.getShell()).open();
		return null;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	@Override
	public boolean isHandled() {
		return true;
	}
	

}
