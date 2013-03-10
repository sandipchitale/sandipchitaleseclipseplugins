package openclosedprojects;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.OpenResourceAction;
import org.eclipse.ui.dialogs.ListSelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.internal.WorkbenchWindow;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;


@SuppressWarnings("restriction")
public class OpenClosedProjectsHandler extends AbstractHandler {
	private static class ClosedProjectsContentProvider extends BaseWorkbenchContentProvider {
		@Override
		public Object[] getChildren(Object element) {
			if (element instanceof IWorkspaceRoot) {
				IWorkspaceRoot workspaceRoot = (IWorkspaceRoot) element;
				Object[] objects = super.getChildren(workspaceRoot);
				List<IProject> projectsList = new LinkedList<IProject>();
				for (int i = 0; i < objects.length; i++) {
					if (objects[i] instanceof IProject) {
						IProject project = (IProject) objects[i];
						if (!project.isOpen()) {
							projectsList.add(project);
						}
					}
				}
				return projectsList.toArray(new Object[projectsList.size()]);
			}
			return new Object[0];
		}
	}
	
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IProject[] projects = workspaceRoot.getProjects();
		boolean atleastOneClosedProjects = false;
		for (IProject project : projects) {
			if (!project.isOpen()) {
				atleastOneClosedProjects = true;
				break;
			}
		}
		if (atleastOneClosedProjects) {
			ListSelectionDialog closedProjectsSelectionDialog = new ListSelectionDialog(HandlerUtil.getActiveShell(event), 
					ResourcesPlugin.getWorkspace().getRoot(), 
					new ClosedProjectsContentProvider(),
					new WorkbenchLabelProvider(), "Select the closed Projects to open:");
			closedProjectsSelectionDialog.setTitle("Select closed Projects");
			if (closedProjectsSelectionDialog.open() == Window.OK) {
				Object[] result = closedProjectsSelectionDialog.getResult();
				if (result.length > 0) {
					OpenResourceAction openResourceAction = new OpenResourceAction(activeWorkbenchWindow);
					openResourceAction.selectionChanged(new StructuredSelection(result));
					openResourceAction.run();
				}  else {
					showStatus(activeWorkbenchWindow, "No closed Proejcts selected for opening.");
				}
			}
		} else {
			showStatus(activeWorkbenchWindow, "No closed Proejcts to open.");
		}
		return null;
	}

	private void showStatus(IWorkbenchWindow activeWorkbenchWindow, String message) {
		if (activeWorkbenchWindow instanceof WorkbenchWindow) {
			WorkbenchWindow workbenchWindow = (WorkbenchWindow) activeWorkbenchWindow;
			workbenchWindow.getStatusLineManager().setMessage(message);
		}
	}

}
