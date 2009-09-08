package eclipsemate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.progress.UIJob;

/**
 * A simple external process launcher.
 * 
 * @author Sandip V. Chitale
 * 
 */
public class CommandLauncher {
	
	private static MessageConsole messageConsole;
	private static boolean firstTime = true;

	public static void launch(final String command, final Map<String, String> environment) {
		// Launch command on a separate thread.
		new Thread(new Runnable() {
			public void run() {
				Activator activator = Activator.getDefault();
				String[] commandArray = Utilities.parseParameters(command);
				try {
					
					ProcessBuilder processBuilder = new ProcessBuilder();
					processBuilder.command(Arrays.asList(commandArray));
					Map<String, String> inheritedEnvironment = processBuilder.environment();
					if (environment != null) {
						inheritedEnvironment.putAll(environment);
					}
					
					final Process process = processBuilder.start();
					final MessageConsole messageConsole = getMessageConsole();
					MessageConsoleStream newMessageStream = messageConsole.newMessageStream();
					if (firstTime) {
						firstTime = false;
					} else {
						newMessageStream.println();
						newMessageStream.println();
						newMessageStream.println();
					}
					messageConsole.newMessageStream().println(command);
					UIJob uiJob = new UIJob("") {
						@Override
						public IStatus runInUIThread(IProgressMonitor monitor) {
							IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
							if (activeWorkbenchWindow != null) {
								IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
								if (activePage != null) {
									IConsoleView view;
									try {
										view = (IConsoleView) activePage.showView(IConsoleConstants.ID_CONSOLE_VIEW);
										view.display(messageConsole);
										new Thread(new MessageConsoleWriter(messageConsole, process.getInputStream())).start();
										new Thread(new MessageConsoleWriter(messageConsole, process.getErrorStream())).start();
									} catch (PartInitException e) {
										return Status.CANCEL_STATUS;
									}
								}
							}
							return Status.OK_STATUS;
						}
					};
					uiJob.schedule();

					
					int status = process.waitFor();
					if (status == 0) {
						// Good
					} else {
						activator.getLog().log(
								new Status(IStatus.ERROR, activator.getBundle()
										.getSymbolicName(), "Process '"
										+ Arrays.asList(commandArray)
												.toString()
										+ "' exited with status: " + status));
					}
				} catch (InterruptedException ex) {
					activator.getLog().log(
							new Status(IStatus.ERROR, activator.getBundle()
									.getSymbolicName(),
									"Exception while executing '"
											+ Arrays.asList(commandArray)
													.toString() + "'", ex));
				} catch (IOException ioe) {
					activator.getLog().log(
							new Status(IStatus.ERROR, activator.getBundle()
									.getSymbolicName(),
									"Exception while executing '"
											+ Arrays.asList(commandArray)
													.toString() + "'", ioe));
				}

			}
		}, "Launching - " + command).start();
	}
	
	private static MessageConsole getMessageConsole() {
		if (messageConsole == null) {
			messageConsole = new MessageConsole("Path Tools Console", null);
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{messageConsole});			
		}
		return messageConsole;
	}
	
	private static class MessageConsoleWriter implements Runnable {		
		private final MessageConsole messageConsole;
		private final InputStream from;
		
		private MessageConsoleWriter(MessageConsole messageConsole, InputStream from) {
			this.messageConsole = messageConsole;
			this.from = from;
		}
		
		public void run() {
			final MessageConsoleStream messageConsoleStream = messageConsole.newMessageStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(from));
			String output = null;
			try {
				while ((output = reader.readLine()) != null) {
					messageConsoleStream.println(output);
				}
			} catch (IOException e) {
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
				}
				try {
					messageConsoleStream.close();
				} catch (IOException e) {
				}
			}
		}		
	}

}
