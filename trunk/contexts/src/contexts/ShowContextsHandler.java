package contexts;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.contexts.IContextService;

public class ShowContextsHandler extends AbstractHandler {

	@SuppressWarnings("unchecked")
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow activeWorkbenchWindow = workbench
				.getActiveWorkbenchWindow();
		IContextService contextService = (IContextService) activeWorkbenchWindow
				.getService(IContextService.class);
		Collection activeContextIds = contextService.getActiveContextIds();
		Map<Context, List<Context>> contextToContextParents = new HashMap<Context, List<Context>>();
		for (Iterator iterator = activeContextIds.iterator(); iterator
				.hasNext();) {
			String activeContextId = (String) iterator.next();
			Context context = contextService.getContext(activeContextId);
			if (!context.isDefined()) {
				continue;
			}
			Context theContext = context;
			List<Context> parentContexts = new LinkedList<Context>();
			boolean actionSetContext = false;
			while (context != null) {
				parentContexts.add(context);
				try {
					activeContextId = context.getParentId();
					// No more parents
					if (activeContextId == null) {
						if ("org.eclipse.ui.contexts.actionSet".equals(context
								.getId())) {
							actionSetContext = true;
						}
						break;
					}
					context = contextService.getContext(activeContextId);
				} catch (NotDefinedException e1) {
					break;
				}
			}
			if (!actionSetContext) {
				Collections.reverse(parentContexts);
				contextToContextParents.put(theContext, parentContexts);
			}
		}
		MessageConsole console = findConsole("Contexts");
		MessageConsoleStream out = console.newMessageStream();

		Set<Context> keySet = contextToContextParents.keySet();
		for (Context context : keySet) {
			List<Context> contexts = contextToContextParents.get(context);
			int i = 0;
			for (Context context2 : contexts) {
				if (i > 0) {
					out.print(" > ");
				}
				try {
					out.print(context2.getName());
				} catch (NotDefinedException e) {
					out.print(context2.getId());
				}
				i++;
			}
			out.println();
		}
		IWorkbenchPage acrtivePage = activeWorkbenchWindow.getActivePage();
		String id = IConsoleConstants.ID_CONSOLE_VIEW;
		IConsoleView view;
		try {
			view = (IConsoleView) acrtivePage.showView(id);
			view.display(console);
		} catch (PartInitException e) {
		}
		return null;
	}

	private static MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

}
