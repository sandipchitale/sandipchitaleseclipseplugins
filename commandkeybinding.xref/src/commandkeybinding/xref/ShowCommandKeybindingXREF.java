package commandkeybinding.xref;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

public class ShowCommandKeybindingXREF extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		CommandKeybindingXREFDialog commandKeybindingXREFDialog = new CommandKeybindingXREFDialog();
		commandKeybindingXREFDialog.open();
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
