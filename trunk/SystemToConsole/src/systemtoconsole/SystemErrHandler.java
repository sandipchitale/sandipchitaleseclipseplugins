package systemtoconsole;

import java.io.PrintStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class SystemErrHandler extends AbstractSystemStreamHandler {

	@Override
	protected PrintStream redirect() {
		PrintStream savePrintStream = System.err;
		// Do redirection
		MessageConsole systemConsole = SystemToConsolePlugin.getSystemConsole();
		final MessageConsoleStream systemConsoleStream = systemConsole.newMessageStream();
		System.setErr(new PrintStream(systemConsoleStream));
		final Display display = PlatformUI.getWorkbench().getDisplay();
		if (display != null) {
			display.syncExec(new Runnable() {
				public void run() {
					systemConsoleStream.setColor(display.getSystemColor(SWT.COLOR_RED));
				}
			});
		}
		System.err.println("System.err redirected to System Console");
		return savePrintStream;
	}

	@Override
	protected void unredirect(PrintStream stream) {
		System.setErr(stream);
	}
	
}