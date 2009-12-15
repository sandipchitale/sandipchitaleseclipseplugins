package systemtoconsole;

import java.io.PrintStream;

import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

public class SystemOutHandler extends AbstractSystemStreamHandler {

	@Override
	protected PrintStream redirect() {
		PrintStream savePrintStream = System.out;
		// Do redirection
		MessageConsole systemConsole = SystemToConsolePlugin.getSystemConsole();
		final MessageConsoleStream systemConsoleStream = systemConsole.newMessageStream();
		System.setOut(new PrintStream(systemConsoleStream));
		System.out.println("System.out redirected to System Console");
		return savePrintStream;
	}

	@Override
	protected void unredirect(PrintStream stream) {
		System.setOut(stream);
	}
	
}
