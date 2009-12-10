package keystrokes.startup;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.keys.BindingService;
import org.eclipse.ui.internal.keys.WorkbenchKeyboard.KeyDownFilter;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.progress.WorkbenchJob;

@SuppressWarnings("restriction")
public class Startup implements IStartup {

	public void earlyStartup() {
		WorkbenchJob workbenchJob = new WorkbenchJob("") {
			
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				IWorkbench workbench = PlatformUI.getWorkbench();
				Display display = workbench.getDisplay();
				Font LARGE_FONT = new Font(display, JFaceResources.TEXT_FONT,
						32, SWT.NORMAL);
				Color BLACK = display.getSystemColor(SWT.COLOR_BLACK);
				Color WHITE = display.getSystemColor(SWT.COLOR_WHITE);
				Shell shell = new Shell(display, SWT.TITLE | SWT.ON_TOP | SWT.NO_FOCUS);
				shell.setBackground(BLACK);
				GridLayout gridLayout = new GridLayout();
				gridLayout.marginWidth = 10;
				gridLayout.marginHeight = 10;
				shell.setLayout(gridLayout);

				final Label label = new Label(shell, SWT.CENTER | SWT.NO_BACKGROUND | SWT.NO_FOCUS);

				GridData labelGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
				label.setLayoutData(labelGridData);

				label.setFont(LARGE_FONT);
				label.setForeground(WHITE);
				label.setText("        ");
				Listener listener = new Listener() {
					public void handleEvent(Event event) {
						int accelerator = SWTKeySupport.convertEventToUnmodifiedAccelerator(event);
						label.setText(SWTKeySupport.convertAcceleratorToKeyStroke(accelerator).format());
					}
				};
				BindingService bindingService = (BindingService) workbench.getService(IBindingService.class);
				KeyDownFilter keyDownFilter = bindingService.getKeyboard().getKeyDownFilter();
				try {
					if (keyDownFilter != null) {
						display.removeFilter(SWT.KeyDown, keyDownFilter);
					}
				display.addFilter(SWT.KeyDown, listener);
				} finally {
					if (keyDownFilter != null) {
						display.addFilter(SWT.KeyDown, keyDownFilter);
					}
				}
				shell.setBounds(100, 100, 200, 90);
				shell.open();
				return Status.OK_STATUS;
			}
		};
		workbenchJob.setSystem(true);
		workbenchJob.setPriority(Job.INTERACTIVE);
		workbenchJob.schedule();
		
	}

}
