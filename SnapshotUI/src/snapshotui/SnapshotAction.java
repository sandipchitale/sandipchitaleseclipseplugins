package snapshotui;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.internal.ViewSite;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.registry.ViewDescriptor;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.views.IViewDescriptor;

public class SnapshotAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	@Override
	public void dispose() {}

	@Override
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	@Override
	public void run(IAction action) {
		if (window != null) {
			IWorkbench workbench = window.getWorkbench();
			
			IWorkbenchPage activePage = window.getActivePage();
			final IPerspectiveDescriptor currentPerspective = activePage.getPerspective();
			
			final IPerspectiveRegistry perspectiveRegistry = workbench.getPerspectiveRegistry();
			final List<IPerspectiveDescriptor> perspectives = 
				new LinkedList<IPerspectiveDescriptor>(Arrays.asList(perspectiveRegistry.getPerspectives()));

			// Move current perspective to the last position so that we land up back
			// where we were.
			perspectives.remove(currentPerspective);
			perspectives.add(currentPerspective);
			
			final List<IViewDescriptor> viewDescriptors =
				new LinkedList<IViewDescriptor>(Arrays.asList(workbench.getViewRegistry().getViews()));

			final File finalDirectory  = getSnapshotDirectory();
			if (finalDirectory == null) {
				return;
			}

			final UIJob switchPersectiveUIJob = new UIJob("Switching Perspective") {

				@SuppressWarnings({ "restriction", "deprecation" })
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					if (perspectives == null || perspectives.size() == 0) {
						return Status.OK_STATUS;
					}
					
					IPerspectiveDescriptor perspectiveDescriptor = perspectives.get(0);
					IWorkbenchPage page = window.getActivePage();
					page.setPerspective(perspectiveDescriptor);
					
					return Status.OK_STATUS;
				}
				
			};
			switchPersectiveUIJob.setSystem(true);
			switchPersectiveUIJob.setPriority(UIJob.INTERACTIVE);
			
			final UIJob snapshotUIJob = new UIJob("Snapshooting Perspective") {

				@SuppressWarnings("restriction")
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					WorkbenchPage page = (WorkbenchPage) window.getActivePage();
					IPerspectiveDescriptor perspective = page.getPerspective();
					
					Shell shell = window.getShell();
					Rectangle bounds = shell.getBounds();
					Display display = shell.getDisplay();
					Image image = new Image(display, bounds.width, bounds.height);
					GC gc = new GC(display);
					gc.copyArea(image, bounds.x, bounds.y);
					gc.dispose();
					ImageLoader imageLoader = new ImageLoader();
					imageLoader.data = new ImageData[] {image.getImageData()};
					String imageFileName = perspective.getId();
					String imagePath = new File(finalDirectory, 
							imageFileName + ".png").getAbsolutePath();
					System.out.println("Saving " + imagePath);
					imageLoader.save(imagePath, SWT.IMAGE_PNG);
					image.dispose();
					return Status.OK_STATUS;
				}
				
			};
			snapshotUIJob.setSystem(true);
			snapshotUIJob.setPriority(UIJob.INTERACTIVE);

			final UIJob ensureViewUIJob = new UIJob("Snapshooting View") {

				@SuppressWarnings("restriction")
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					if (viewDescriptors.size() > 0) {
						IViewDescriptor viewDescriptor = viewDescriptors.get(0);
						String viewId = viewDescriptor.getId();
						
						IWorkbenchPage page = window.getActivePage();
						
						IViewPart view = page.findView(viewId);
						boolean viewWasFound = (view != null);
						if (!viewWasFound) {
							try {
								view = page.showView(viewId, null, IWorkbenchPage.VIEW_ACTIVATE);
							} catch (PartInitException e) {
								// TODO
							}
						}
					}
					return Status.OK_STATUS;
				}
				
			};
			ensureViewUIJob.setSystem(true);
			ensureViewUIJob.setPriority(UIJob.INTERACTIVE);
			
			final UIJob detachViewUIJob = new UIJob("Snapshooting View") {

				@SuppressWarnings("restriction")
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					if (viewDescriptors.size() > 0) {
						IViewDescriptor viewDescriptor = viewDescriptors.get(0);
						String viewId = viewDescriptor.getId();
						
						IWorkbenchPage page = window.getActivePage();
						
						IViewPart view = page.findView(viewId);
						boolean viewWasFound = (view != null);
						if (!viewWasFound) {
							try {
								view = page.showView(viewId, null, IWorkbenchPage.VIEW_ACTIVATE);
							} catch (PartInitException e) {
								// TODO
							}
						}
						
						IViewReference viewReference = page.findViewReference(viewId);
						if (viewReference == null) {
							return Status.OK_STATUS;
						}
						
						((WorkbenchPage)page).detachView(viewReference);
						
					}
					return Status.OK_STATUS;
				}
				
			};
			detachViewUIJob.setSystem(true);
			detachViewUIJob.setPriority(UIJob.INTERACTIVE);
			
			
			final UIJob attachViewUIJob = new UIJob("Snapshooting View") {

				@SuppressWarnings("restriction")
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					if (viewDescriptors.size() > 0) {
						IViewDescriptor viewDescriptor = viewDescriptors.get(0);
						String viewId = viewDescriptor.getId();
						
						IWorkbenchPage page = window.getActivePage();
						
						IViewPart view = page.findView(viewId);
						boolean viewWasFound = (view != null);
						if (!viewWasFound) {
							try {
								view = page.showView(viewId, null, IWorkbenchPage.VIEW_ACTIVATE);
							} catch (PartInitException e) {
								// TODO
							}
						}
						
						IViewReference viewReference = page.findViewReference(viewId);
						if (viewReference == null) {
							return Status.OK_STATUS;
						}
						
						((WorkbenchPage)page).attachView(viewReference);
						
					}
					return Status.OK_STATUS;
				}
				
			};
			attachViewUIJob.setSystem(true);
			attachViewUIJob.setPriority(UIJob.INTERACTIVE);
			
			final UIJob snapshotViewJob = new UIJob("Snapshooting View") {

				@SuppressWarnings("restriction")
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					if (viewDescriptors.size() > 0) {
						IViewDescriptor viewDescriptor = viewDescriptors.get(0);
						String viewId = viewDescriptor.getId();
						
						IWorkbenchPage page = window.getActivePage();
						
						IViewPart view = page.findView(viewId);
						if (view == null) {
							return Status.OK_STATUS;
						}

						IViewReference viewReference = page.findViewReference(viewId);
						if (viewReference == null) {
							return Status.OK_STATUS;
						}
						
						try {
							Shell shell = view.getViewSite().getShell();
							Rectangle bounds = shell.getBounds();
							Display display = shell.getShell().getDisplay();
							Image image = new Image(display, bounds.width, bounds.height);
							GC gc = new GC(display);
							gc.copyArea(image, bounds.x, bounds.y);
							gc.dispose();
							ImageLoader imageLoader = new ImageLoader();
							imageLoader.data = new ImageData[] {image.getImageData()};
							String imagePath = new File(finalDirectory, viewReference.getId() + ".png").getAbsolutePath();
							System.out.println("Saving " + imagePath);
							imageLoader.save(imagePath, SWT.IMAGE_PNG);
							image.dispose();
						} catch (IllegalArgumentException iae) {
						}
					}
					return Status.OK_STATUS;
				}
				
			};
			snapshotViewJob.setSystem(true);
			snapshotViewJob.setPriority(UIJob.INTERACTIVE);

			
			final UIJob resetPerspectiveUIJob = new UIJob("Resetting perspective") {

				@SuppressWarnings({ "restriction", "deprecation" })
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					// Reset the current perspective
					window.getActivePage().resetPerspective();
					return Status.OK_STATUS;
				}
				
			};
			resetPerspectiveUIJob.setSystem(true);
			resetPerspectiveUIJob.setPriority(UIJob.INTERACTIVE);
			
			Job job = new Job("") {
				@Override
				protected IStatus run(IProgressMonitor monitor) {
					while (perspectives.size() > 0) {
						switchPersectiveUIJob.schedule(1000L);
						try {
							switchPersectiveUIJob.join();
						} catch (InterruptedException e) {
							// TODO
						}
						
						snapshotUIJob.schedule(1000L);
						try {
							snapshotUIJob.join();
						} catch (InterruptedException e) {
							// TODO
						}

						if (perspectives.size() > 0) {
							perspectives.remove(0);
						}
					}
					
					while (viewDescriptors.size() > 0) {
						ensureViewUIJob.schedule(1000L);
						try {
							ensureViewUIJob.join();
						} catch (InterruptedException e) {
							// TODO
						}
						
						detachViewUIJob.schedule(1000L);
						try {
							detachViewUIJob.join();
						} catch (InterruptedException e) {
							// TODO
						}
						
						snapshotViewJob.schedule(1000L);
						try {
							snapshotViewJob.join();
						} catch (InterruptedException e) {
							// TODO
						}
						
						attachViewUIJob.schedule(1000L);
						try {
							attachViewUIJob.join();
						} catch (InterruptedException e) {
							// TODO
						}
						
						if (viewDescriptors.size() > 0) {
							viewDescriptors.remove(0);
						}
					}
					String[] exploreFolderCommand = null;
					if (Platform.OS_MACOSX.equals(Platform.getOS())) {
						exploreFolderCommand = new String[] {
								"/usr/bin/open",
								"-a",
								"/System/Library/CoreServices/Finder.app",
								finalDirectory.getAbsolutePath()
								
						};
					} else if (Platform.OS_WIN32.equals(Platform.getOS())) {
						exploreFolderCommand = new String[] {
								"cmd",
								"/C",
								"start",
								"explorer",
								"/select",
								",",
								"/e",
								",",
								finalDirectory.getAbsolutePath()
								
						};
					} else if (Platform.OS_LINUX.equals(Platform.getOS())) {
						exploreFolderCommand = new String[] {
								"/usr/bin/nautilus",
								finalDirectory.getAbsolutePath()
								
						};
					}
					if (exploreFolderCommand != null) {
						try {
							Process exec = Runtime.getRuntime().exec(exploreFolderCommand);
							exec.waitFor();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					
					resetPerspectiveUIJob.schedule(1000L);
					return Status.OK_STATUS;
				}
			};
			job.setSystem(true);
			job.schedule();
		}
	}
	
	@Override
	public void selectionChanged(IAction action, ISelection selection) {}

	private static int generation = 0;
	private File getSnapshotDirectory() {
		File directory = null;
		while (true) {
			directory = new File(System.getProperty("java.io.tmpdir"), "snapshot" + generation);
			if (!directory.exists()) {
				if (directory.mkdir()) {
					break;
				}
			}
			generation++;
		}
		return directory;
	}

}
