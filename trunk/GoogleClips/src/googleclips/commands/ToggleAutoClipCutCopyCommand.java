package googleclips.commands;

import googleclips.Activator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

public class ToggleAutoClipCutCopyCommand implements IHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Activator activator = Activator.getDefault();
		activator.setAutoClipCutCopy(!activator.isAutoClipCutCopy());
		return null;
	}

	public boolean isEnabled() {
		return true;
	}

	public boolean isHandled() {
		return true;
	}

	public void dispose() {
	}

	public void addHandlerListener(IHandlerListener handlerListener) {
	}

	public void removeHandlerListener(IHandlerListener handlerListener) {
	}

}
