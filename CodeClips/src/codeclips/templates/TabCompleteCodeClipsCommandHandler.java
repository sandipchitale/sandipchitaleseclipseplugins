package codeclips.templates;

import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
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
import org.eclipse.ui.services.IEvaluationService;
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
						// process existing windows
						IWorkbenchWindow[] workbenchWindows = workbench.getWorkbenchWindows();
						for (IWorkbenchWindow workbenchWindow : workbenchWindows) {
							processWindow(workbenchWindow);
						}
						// process future windows
						workbench.addWindowListener(windowListener);
					}
					return Status.OK_STATUS;
				}

			};
			addEditorMonitorJobs.setSystem(true);
			addEditorMonitorJobs.schedule();
			
			IEvaluationService evaluationService = (IEvaluationService) PlatformUI.getWorkbench().getService(IEvaluationService.class);
			if (evaluationService != null) {
				Object variable = evaluationService.getCurrentState().getVariable("activePart");
				if (variable instanceof ITextEditor) {
					processITextEditor((ITextEditor) variable);
				} else if (variable instanceof MultiPageEditorPart) {
					MultiPageEditorPart multiPageEditorPart = (MultiPageEditorPart) variable;
					Object selectedPage = multiPageEditorPart.getSelectedPage();
					if (selectedPage instanceof ITextEditor) {
						processITextEditor((ITextEditor) selectedPage);
					}
				}
			}
		}
		return false;
	}
	
	private static IWindowListener windowListener = new IWindowListener() {
		
		public void windowOpened(IWorkbenchWindow window) {
			// process newly opened window
			processWindow(window);
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
	
	private static IPageListener pageListener = new IPageListener() {
		
		public void pageOpened(IWorkbenchPage page) {
			processPage(page);
		}
		
		public void pageClosed(IWorkbenchPage page) {
			page.removePartListener(partListener);
		}
		
		public void pageActivated(IWorkbenchPage page) {
			
		}
	};
	
	private static IPartListener partListener = new IPartListener() {
		public void partActivated(IWorkbenchPart part) {
		}

		public void partBroughtToTop(IWorkbenchPart part) {
		}

		public void partClosed(IWorkbenchPart part) {
			if (part instanceof MultiPageEditorPart) {
				((MultiPageEditorPart) part).removePageChangedListener(pageChangedListener);
			}
		}

		public void partDeactivated(IWorkbenchPart part) {
		}

		public void partOpened(IWorkbenchPart editorPart) {
			if (editorPart instanceof IEditorPart) {
				processPart((IEditorPart)editorPart);
			}
		}	
	};
	
	private static IPageChangedListener pageChangedListener = new IPageChangedListener() {
		public void pageChanged(PageChangedEvent event) {
			Object selectedPage = event.getSelectedPage();
			if (selectedPage instanceof ITextEditor) {
				processITextEditor((ITextEditor)selectedPage);
			}
		}
	};
	
	private static void processWindow(IWorkbenchWindow workbenchWindow) {
		// process existing pages
		IWorkbenchPage[] workbenchPages = workbenchWindow.getPages();
		for (IWorkbenchPage workbenchPage : workbenchPages) {
			processPage(workbenchPage);
		}
		// process future pages
		workbenchWindow.addPageListener(pageListener);
	}
	
	private static void processPage(IWorkbenchPage workbenchPage) {
		IEditorReference[] editorReferences = workbenchPage.getEditorReferences();
		for (IEditorReference editorReference : editorReferences) {
			processPart(editorReference.getEditor(false));
		}
		workbenchPage.addPartListener(partListener);		
	}
	
	private static void processPart(IEditorPart editorPart) {
		if (editorPart instanceof ITextEditor) {
			processITextEditor((ITextEditor)editorPart);
		} else if (editorPart instanceof MultiPageEditorPart) {
			processMultiPageEditorPart((MultiPageEditorPart) editorPart);
		}
	}
	
	private static void processITextEditor(ITextEditor textEditor) {
		addAction(textEditor);
	}
	
	private static void processMultiPageEditorPart(MultiPageEditorPart multiPageEditorPart) {
		Object selectedPage = multiPageEditorPart.getSelectedPage();
		if (selectedPage instanceof ITextEditor) {
			addAction((ITextEditor)selectedPage);
		}
		multiPageEditorPart.addPageChangedListener(pageChangedListener);
	}

	private static void addAction(ITextEditor textEditor) {
		if (textEditor.isEditable()) {
			if (textEditor.getAction(ID) == null) {
				textEditor.setAction(ID, TabCompleteCodeClipsAction.create(textEditor));
			}
		}
	}
}
