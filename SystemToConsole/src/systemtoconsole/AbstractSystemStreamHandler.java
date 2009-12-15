package systemtoconsole;

import java.io.PrintStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

public abstract class AbstractSystemStreamHandler extends AbstractHandler {
	
	protected PrintStream savedPrintStream;
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Command command = event.getCommand();
		boolean wasRedirected = HandlerUtil.toggleCommandState(command);
		if (wasRedirected) {
			unredirect(savedPrintStream);
			savedPrintStream = null;
		} else {
			savedPrintStream = redirect();
		}
		return null;
	}

	protected abstract PrintStream redirect();
	protected abstract void unredirect(PrintStream stream);
}
