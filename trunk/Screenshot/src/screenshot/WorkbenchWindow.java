package screenshot;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;

public class WorkbenchWindow implements IWorkbenchWindowPulldownDelegate2 {

	private IWorkbenchWindow window;

	public void dispose() {
		if (menu != null) {
			menu.dispose();
		}
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
		/* Take the screen shot */
		Shell shell = window.getShell();
		Image desktopImage = Util.getDesktopImage(shell.getDisplay(), shell.getBounds());
		Util.processImage(shell, desktopImage);
	}

	public void selectionChanged(IAction action, ISelection selection) {}

	private Menu menu;
	
	public Menu getMenu(Menu parent) {
		if (menu != null) {
			menu.dispose();
		}
		menu = new Menu(parent);
		buildMenu(menu);
		return menu;
	}

	public Menu getMenu(Control parent) {
		if (menu != null) {
			menu.dispose();
		}
		menu = new Menu(parent);
		buildMenu(menu);
		return menu;
	}
	
	private void buildMenu(Menu menu) {
		MenuItem perspectivesMenuItem = new MenuItem(menu, SWT.CASCADE);
		perspectivesMenuItem.setText("Perspectives");
		Menu perspectivesMenu = new Menu(menu);
		perspectivesMenuItem.setMenu(perspectivesMenu);
		fillPerspectivesMenu(perspectivesMenu);
		
		MenuItem viewsMenuItem = new MenuItem(menu, SWT.CASCADE);
		viewsMenuItem.setText("Views");
		Menu viewsMenu = new Menu(menu);
		viewsMenuItem.setMenu(viewsMenu);
		fillViewsMenu(viewsMenu);
	}
	
	private void fillPerspectivesMenu(Menu menu) {
		final IWorkbench workbench = window.getWorkbench();
		IPerspectiveRegistry perspectiveRegistry = workbench.getPerspectiveRegistry();
		// Get all perspectives
		IPerspectiveDescriptor[] perspectiveDescriptors = perspectiveRegistry
		.getPerspectives();

		// Sort alphabetically by label
		Arrays.sort(perspectiveDescriptors,
				new Comparator<IPerspectiveDescriptor>() {
			public int compare(IPerspectiveDescriptor pd1,
					IPerspectiveDescriptor pd2) {
				return pd1.getLabel().compareTo(pd2.getLabel());
			}
		});
		for (IPerspectiveDescriptor perspectiveDescriptor : perspectiveDescriptors) {
			final MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
			menuItem.setText(perspectiveDescriptor.getLabel());
			menuItem.setImage(perspectiveDescriptor.getImageDescriptor()
					.createImage());
			menuItem.setData(perspectiveDescriptor.getId());
			menuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					try {
						IPerspectiveDescriptor perspectiveDescriptorWithId = workbench
								.getPerspectiveRegistry()
								.findPerspectiveWithId(
										(String) e.widget.getData());
						if (perspectiveDescriptorWithId != null) {
							workbench.showPerspective(
									perspectiveDescriptorWithId.getId(),
									window);
							UIJob uiJob = new UIJob("") {
								public IStatus runInUIThread(IProgressMonitor monitor) {
									Shell shell = window.getShell();
									Image desktopImage = Util.getDesktopImage(shell.getDisplay(), shell.getBounds());
									Util.processImage(shell, desktopImage);
									return Status.OK_STATUS;
								}
							};
							uiJob.setSystem(true);
							uiJob.setPriority(UIJob.INTERACTIVE);
							uiJob.schedule(1000l);
						} else {
							// may be delete this menuItem ?
						}
					} catch (WorkbenchException we) {
					}
				}
			});
		}
	}
	
	private void fillViewsMenu(Menu menu) {
		IViewRegistry viewsRegistry = window.getWorkbench()
		.getViewRegistry();
		// Get all views
		IViewDescriptor[] viewDescriptors = viewsRegistry.getViews();

		// Sort alphabetically by label
		Arrays.sort(viewDescriptors, new Comparator<IViewDescriptor>() {
			public int compare(IViewDescriptor vd1, IViewDescriptor vd2) {
				return vd1.getLabel().compareTo(vd2.getLabel());
			}
		});

		// Configure the menu items for each View
		for (IViewDescriptor viewDescriptor : viewDescriptors) {
			MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
			menuItem.setText(viewDescriptor.getLabel());
			menuItem.setImage(viewDescriptor.getImageDescriptor()
					.createImage());
			menuItem.setData(viewDescriptor.getId());
			// Handle selection
			menuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					IWorkbench workbench = window.getWorkbench();
					try {
						IViewDescriptor viewWithId = workbench
						.getViewRegistry().find(
								(String) e.widget.getData());
						if (viewWithId != null) {
							final IWorkbenchPage activePage = window.getActivePage();
							final IViewPart view = activePage.showView(viewWithId
									.getId(), null,
									IWorkbenchPage.VIEW_CREATE);
							activePage.activate(view);
							final IViewReference viewReference = activePage.findViewReference(view.getViewSite().getId());
							if (viewReference != null) {
								((WorkbenchPage)activePage).detachView(viewReference);
								UIJob uiJob = new UIJob("") {
									public IStatus runInUIThread(IProgressMonitor monitor) {
										Shell shell = view.getViewSite().getShell();
										Image desktopImage = Util.getDesktopImage(shell.getDisplay(), shell.getBounds());
										Util.processImage(shell, desktopImage);
										((WorkbenchPage)activePage).attachView(viewReference);
										return Status.OK_STATUS;
									}
								};
								uiJob.setSystem(true);
								uiJob.setPriority(UIJob.INTERACTIVE);
								uiJob.schedule(500l);
							}
						} else {
							// may be delete this menuItem ?
						}
					} catch (PartInitException pie) {
					}
				}
			});
		}
	}

}
