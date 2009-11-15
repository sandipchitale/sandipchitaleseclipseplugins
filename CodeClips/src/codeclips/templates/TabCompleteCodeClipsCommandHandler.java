package codeclips.templates;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.progress.WorkbenchJob;
import org.eclipse.ui.texteditor.ITextEditor;

public class TabCompleteCodeClipsCommandHandler extends AbstractHandler {
	public static final String ID = "CodeClips.tabcomplete.command";

	public TabCompleteCodeClipsCommandHandler() {
	}

	public Object execute(ExecutionEvent event) throws ExecutionException {
		return null;
	}
	
	private static AtomicBoolean firstTime = new AtomicBoolean(true);

	@Override
	public boolean isHandled() {
		if (firstTime.compareAndSet(true, false)) {
			Job addEditorMonitorJobs = new WorkbenchJob("Monitor TextEditors.") {  //$NON-NLS-1$
				public IStatus runInUIThread(IProgressMonitor monitor) {
					IWorkbench workbench = PlatformUI.getWorkbench();
					if (workbench != null) {
						IWorkbenchWindow[] workbenchWindows = workbench.getWorkbenchWindows();
						for (IWorkbenchWindow workbenchWindow : workbenchWindows) {
							addWindowListeners(workbenchWindow);
						}
						workbench.addWindowListener(windowListener);
					}
					return Status.OK_STATUS;
				}

			};
			addEditorMonitorJobs.setSystem(true);
			addEditorMonitorJobs.schedule();
		}
		return false;
	}
	
	private static IPartListener partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart part) {
		}

		public void partBroughtToTop(IWorkbenchPart part) {
		}

		public void partClosed(IWorkbenchPart part) {
		}

		public void partDeactivated(IWorkbenchPart part) {
		}

		public void partOpened(IWorkbenchPart part) {
			if (part instanceof ITextEditor) {
				addAction((ITextEditor)part);
			} else if (part instanceof MultiPageEditorPart) {
				MultiPageEditorPart multiPageEditorPart = (MultiPageEditorPart) part;
				Object selectedPage = multiPageEditorPart.getSelectedPage();
				if (selectedPage instanceof ITextEditor) {
					addAction((ITextEditor)selectedPage);
				}
			}
		}	
	};
	
	private static IPageListener pageListener = new IPageListener() {
		
		public void pageOpened(IWorkbenchPage page) {
			addPartListener(page);
		}
		
		public void pageClosed(IWorkbenchPage page) {
			page.removePartListener(partListener);
		}
		
		public void pageActivated(IWorkbenchPage page) {
			
		}
	};

	private static IWindowListener windowListener = new IWindowListener() {
		
		public void windowOpened(IWorkbenchWindow window) {
			addWindowListeners(window);
		}
		
		public void windowDeactivated(IWorkbenchWindow window) {
			
		}
		
		public void windowClosed(IWorkbenchWindow window) {
			window.removePageListener(pageListener);
			IWorkbenchPage[] workbenchPages = window.getPages();
			for (IWorkbenchPage workbenchPage : workbenchPages) {
				workbenchPage.removePartListener(partListener);
			}
		}
		
		public void windowActivated(IWorkbenchWindow window) {
		}
	};
	
	private static void addWindowListeners(IWorkbenchWindow workbenchWindow) {
		IWorkbenchPage[] workbenchPages = workbenchWindow.getPages();
		for (IWorkbenchPage workbenchPage : workbenchPages) {
			addPartListener(workbenchPage);
		}
		workbenchWindow.addPageListener(pageListener);
	}
	
	private static void addPartListener(IWorkbenchPage workbenchPage) {
		IEditorReference[] editorReferences = workbenchPage.getEditorReferences();
		for (IEditorReference editorReference : editorReferences) {
			IEditorPart part = editorReference.getEditor(false);
			if (part instanceof ITextEditor) {
				addAction((ITextEditor)part);
			} else if (part instanceof MultiPageEditorPart) {
				MultiPageEditorPart multiPageEditorPart = (MultiPageEditorPart) part;
				Object selectedPage = multiPageEditorPart.getSelectedPage();
				if (selectedPage instanceof ITextEditor) {
					addAction((ITextEditor) selectedPage);
				}
			}
		}
		workbenchPage.addPartListener(partListener);		
	}

	private static void addAction(ITextEditor textEditor) {
		if (textEditor.isEditable()) {
			textEditor.setAction(ID, TabCompleteCodeClipsAction.create(textEditor));
		}
	}
}
