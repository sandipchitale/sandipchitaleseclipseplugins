package systemtoconsole;

import java.io.PrintStream;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.handlers.HandlerUtil;

public class TeeSystemOutAndErrHandler extends AbstractHandler {

	protected PrintStream savedOutPrintStream;
	protected PrintStream savedErrPrintStream;
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		Command command = event.getCommand();
		boolean wasRedirected = HandlerUtil.toggleCommandState(command);
		if (wasRedirected) {
			unredirectOut(savedOutPrintStream);
			savedOutPrintStream = null;
			unredirectErr(savedErrPrintStream);
			savedErrPrintStream = null;
		} else {
			savedOutPrintStream = redirectOut();
			savedErrPrintStream = redirectErr();
		}
		return null;
	}
	
	protected PrintStream redirectOut() {
		PrintStream savedPrintStream = System.out;
		// Do redirection
		MessageConsole systemConsole = SystemToConsolePlugin.getSystemConsole();
		final MessageConsoleStream systemConsoleStream = systemConsole.newMessageStream();
		System.setOut(new PrintStream(new TeeOutputStream(systemConsoleStream, savedPrintStream)));
		if (SystemToConsolePlugin.getDefault().isDebugging()) {
			System.out.println("System.out 'T'ed to System Console");
		}
		return savedPrintStream;
	}
	
	protected PrintStream redirectErr() {
		PrintStream savedPrintStream = System.err;
		// Do redirection
		MessageConsole systemConsole = SystemToConsolePlugin.getSystemConsole();
		final MessageConsoleStream systemConsoleStream = systemConsole.newMessageStream();
		System.setErr(new PrintStream(new TeeOutputStream(systemConsoleStream, savedPrintStream)));
		final Display display = PlatformUI.getWorkbench().getDisplay();
		if (display != null) {
			display.syncExec(new Runnable() {
				public void run() {
					systemConsoleStream.setColor(display.getSystemColor(SWT.COLOR_RED));
				}
			});
		}
		if (SystemToConsolePlugin.getDefault().isDebugging()) {
			System.err.println("System.err 'T' to System Console");
		}
		return savedPrintStream;
	}

	protected void unredirectOut(PrintStream stream) {
		System.setOut(stream);
		if (SystemToConsolePlugin.getDefault().isDebugging()) {
			System.out.println("System.out no longer 'T'ed to System Console");
		}
	}
	
	protected void unredirectErr(PrintStream stream) {
		System.setErr(stream);
		if (SystemToConsolePlugin.getDefault().isDebugging()) {
			System.err.println("System.err no longer 'T'ed to System Console");
		}
	}	
}
