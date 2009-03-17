package googleclips.commands;

//import googleclips.Activator;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.IHandlerListener;

public class ClearCommand implements IHandler {


	public Object execute(ExecutionEvent event) throws ExecutionException {
//		Activator.getDefault().clearGoogleClips();
		return null;
	}

	public boolean isEnabled() {
		return false;
	}

	public boolean isHandled() {
		return false;
	}

	public void dispose() {}
	public void addHandlerListener(IHandlerListener handlerListener) {}
	public void removeHandlerListener(IHandlerListener handlerListener) {}

}
