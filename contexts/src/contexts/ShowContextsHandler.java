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
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;

public class ShowContextsHandler extends AbstractHandler {

	@SuppressWarnings("unchecked")
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IContextService contextService = (IContextService) 
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(IContextService.class);
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
						if ("org.eclipse.ui.contexts.actionSet".equals(context.getId())) {
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
		Set<Context> keySet = contextToContextParents.keySet();
		for (Context context : keySet) {
			List<Context> contexts = contextToContextParents.get(context);
			int i = 0;
			for (Context context2 : contexts) {
				if (i > 0) {
					System.out.print(" > ");
				}
				try {
					System.out.print(context2.getName());
				} catch (NotDefinedException e) {
					System.out.print(context2.getId());
				}
				i++;
			}
			System.out.println();
		}
		return null;
	}

}
