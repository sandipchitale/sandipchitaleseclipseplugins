package systemtoconsole;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.State;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class SystemToConsolePlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "SystemToConsole";

	// The shared instance
	private static SystemToConsolePlugin plugin;

	/**
	 * The constructor
	 */
	public SystemToConsolePlugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		UIJob job = new UIJob("") {
		    public IStatus runInUIThread(IProgressMonitor monitor) {
		        ICommandService commandService = (ICommandService) PlatformUI
		            .getWorkbench().getActiveWorkbenchWindow().getService(
		                ICommandService.class);
		        Command command = commandService.getCommand("SystemToConsole.out.command");
		        State state = command.getState(RegistryToggleState.STATE_ID);
				if(state != null) {
					state.setValue(Boolean.FALSE);
				}
				commandService.refreshElements(command.getId(), null);
		        command = commandService.getCommand("SystemToConsole.err.command");
		        state = command.getState(RegistryToggleState.STATE_ID);
				if(state != null) {
					state.setValue(Boolean.FALSE);
				}
				commandService.refreshElements(command.getId(), null);
		        return Status.OK_STATUS;
		    }
		 
		};
		job.setPriority(Job.INTERACTIVE);
		job.schedule();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static SystemToConsolePlugin getDefault() {
		return plugin;
	}

	private static final String SYSTEM_CONSOLE_NAME = System.class
			.getSimpleName();

	private static MessageConsole systemConsole;

	static MessageConsole getSystemConsole() {
		if (systemConsole == null) {
			ConsolePlugin plugin = ConsolePlugin.getDefault();
			IConsoleManager conMan = plugin.getConsoleManager();
			IConsole[] existing = conMan.getConsoles();
			for (int i = 0; i < existing.length; i++)
				if (SYSTEM_CONSOLE_NAME.equals(existing[i].getName()))
					return (MessageConsole) existing[i];
			systemConsole = new MessageConsole(SYSTEM_CONSOLE_NAME, null);
			conMan.addConsoles(new IConsole[] { systemConsole });
		}
		return systemConsole;
	}
}
