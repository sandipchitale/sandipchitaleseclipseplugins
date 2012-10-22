package cutcopypasteplus;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class ToggleQuickPasteCyclesThroughHistoryCommandHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Activator.getDefault().setQuickPasteCyclesThroughHistory(
				!Activator.getDefault().isQuickPasteCyclesThroughHistory());
		return null;
	}

}
