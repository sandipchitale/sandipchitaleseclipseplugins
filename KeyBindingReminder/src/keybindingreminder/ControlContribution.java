package keybindingreminder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;
import org.eclipse.ui.progress.UIJob;

public class ControlContribution extends WorkbenchWindowControlContribution {

	public ControlContribution() {
	}

	public ControlContribution(String id) {
		super(id);
	}

	private static Map<String, Integer> commandIdInvocationCountMap = new LinkedHashMap<String, Integer>();
	private boolean active = true;
	private Text reminder;
	private IExecutionListener executionListener;
	private ICommandService commandService;

	@Override
	protected Control createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout(2
				, false);
		gridLayout.marginTop = 1;
		gridLayout.marginBottom = 0;
		gridLayout.marginHeight = 0;

		composite.setLayout(gridLayout);
		
		reminder = new Text(composite, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		final IBindingService bindingService = (IBindingService) PlatformUI.getWorkbench().getService(
				IBindingService.class);
		commandService = (ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);

		executionListener = new IExecutionListener() {
			private int type;

			@Override
			public void preExecute(String commandId, ExecutionEvent event) {
				type = Integer.MIN_VALUE;
				Object trigger = event.getTrigger();
				if (trigger instanceof Event) {
					type = ((Event) trigger).type;
				}
				showInfo(commandId);
				type = Integer.MIN_VALUE;
			}

			@Override
			public void postExecuteSuccess(String commandId, Object returnValue) {
			}

			@Override
			public void postExecuteFailure(String commandId, ExecutionException exception) {
			}

			public void showInfo(String commandId) {
				reminder.setText("");
				reminder.setToolTipText(null);
				reminder.setBackground(null);
				if (type != SWT.KeyDown && type != SWT.KeyUp) {
					TriggerSequence[] activeBindingsFor = bindingService.getActiveBindingsFor(commandId);
					if (activeBindingsFor.length > 0) {
						Integer invocationCount = commandIdInvocationCountMap.get(commandId);
						if (invocationCount == null) {
							invocationCount = 1;
							commandIdInvocationCountMap.put(commandId, invocationCount);
						} else {
							invocationCount =  1 + invocationCount;
							commandIdInvocationCountMap.put(commandId, invocationCount);
						}
						if (invocationCount > 3) {
							// They have been reminded enough
							return;
						}
						final StringBuilder stringBuilder = new StringBuilder();
						for (TriggerSequence triggerSequence : activeBindingsFor) {
							if (stringBuilder.length() != 0) {
								stringBuilder.append(" or ");
							}
							stringBuilder.append(triggerSequence.toString());
						}
						
						reminder.setText(stringBuilder.toString());
						Command command = commandService.getCommand(commandId);
						if (command != null) {
							try {
								commandId = command.getName();
							} catch (NotDefinedException e) {
							}
						}						
						reminder.setToolTipText(commandId + " can be invoked with \n" + stringBuilder.toString());
						if (invocationCount > 2) {
							reminder.setText("");
							final String finalCommandId = commandId;
							final PopupDialog popUpDialog =
								new PopupDialog(reminder.getShell(),
										PopupDialog.HOVER_SHELLSTYLE | SWT.MODELESS,
										false,
										false,
										false,
										false,
										false,
										null,
										null) {
						        private Text text;

								protected Control createDialogArea(Composite parent) {
						            GridData gd = new GridData(GridData.FILL_BOTH);
						            text = new Text(parent, SWT.MULTI | SWT.READ_ONLY | SWT.BORDER);
						            text.setLayoutData(gd);

						            text.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_FOREGROUND));
						            text.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));

						            text.setText("\n" + finalCommandId + " can be invoked with:\n\n" + stringBuilder.toString() + "\n\nKey Binding Reminder turned off.\n");
						            return text;
						        }

								@Override
								protected Point getInitialLocation(Point initialSize) {
									Shell parent = getParentShell();
									Point parentSize, parentLocation;

									if (parent != null) {
										parentSize = parent.getSize();
										parentLocation = parent.getLocation();
									} else {
										Rectangle bounds = getShell().getDisplay().getBounds();
										parentSize = new Point(bounds.width, bounds.height);
										parentLocation = new Point(0, 0);
									}
									// We have to take parent location into account because SWT considers all
									// shell locations to be in display coordinates, even if the shell is parented.
									return new Point(parentSize.x - initialSize.x + parentLocation.x - 20,
											parentSize.y - initialSize.y + parentLocation.y - 20);
								}

								@Override
								protected Control getFocusControl() {
									return text;
								}

							};
							popUpDialog.open();
							UIJob hider = new UIJob("") {
								@Override
								public IStatus runInUIThread(IProgressMonitor monitor) {
									popUpDialog.close();
									return Status.OK_STATUS;
								}

							};
							hider.setSystem(true);
							hider.setPriority(UIJob.INTERACTIVE);
							hider.schedule(4000);
						} else if (invocationCount > 1) {
							reminder.setBackground(reminder.getDisplay().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
						} else if (invocationCount > 0) {
							reminder.setBackground(reminder.getDisplay()
									.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
						}
					}
				} else {
					commandIdInvocationCountMap.put(commandId, 0);
				}
			}

			@Override
			public void notHandled(String commandId, NotHandledException exception) {
			}
		};

		commandService.addExecutionListener(executionListener);
		reminder.addMouseListener(new MouseListener() {

			@Override
			public void mouseUp(MouseEvent e) {
				toggleActivation();
			}

			@Override
			public void mouseDown(MouseEvent e) {
			}

			@Override
			public void mouseDoubleClick(MouseEvent e) {
			}
		});

		reminder.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				commandService.removeExecutionListener(executionListener);
			}
		});
		reminder.setText("                               ");

//		Button statisticsButton = new Button(composite, SWT.PUSH | SWT.FLAT | SWT.NO_FOCUS);
//		statisticsButton.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_INFO_TSK));
//		statisticsButton.setText("\u2211");
//		
//		statisticsButton.addSelectionListener(new SelectionListener() {
//			
//			@Override
//			public void widgetSelected(SelectionEvent e) {
//				showStatistics();
//			}
//
//			@Override
//			public void widgetDefaultSelected(SelectionEvent e) {}
//		});
		return composite;
	}

	private void toggleActivation() {
		reminder.setBackground(null);
		reminder.setForeground(null);
		if (active) {
			active = false;
			commandService.removeExecutionListener(executionListener);
			reminder.setText("");
			reminder.setToolTipText("Click to activate the\nKey binding reminder!");
			commandIdInvocationCountMap.clear();
		} else {
			active = true;
			commandService.addExecutionListener(executionListener);
			reminder.setToolTipText("");
		}
	}
	
//	private void showStatistics() {
//		try {
//			File statisticsFile = File.createTempFile("command-usage-statistics", ".html");
//			statisticsFile.deleteOnExit();
//			
//			FileOutputStream fos = null; 
//			try {
//				fos = new FileOutputStream(statisticsFile);
//				
//				PrintWriter pw = new PrintWriter(fos, true);
//				
//				pw.println("<html>");
//				pw.println("<head>");
//				pw.println("<title>Eclipse Command Usage Statistics (" + new Date() + ")</title>");
//				pw.println("</head>");
//				pw.println("<body>");
//				pw.println("<h1>Eclipse Command Usage Statistics (" + new Date() + ")</h1>");
//				pw.println("<table style='border-collapse:collapse;width:100%;border: 1px dotted black;'><th><td>Command ID</td><td>Command Name</td><td>Invocation Count</td></th>");
//				pw.println("</table>");
//				pw.println("</body>");
//				pw.println("</html>");
//			} finally {
//				if (fos != null) {
//					fos.close();
//				}
//			}
//			
//			if (statisticsFile.isFile()) {
//				try {
//					PlatformUI.getWorkbench().getBrowserSupport().createBrowser(
//							IWorkbenchBrowserSupport.AS_EDITOR
//							,"ECUS"
//							,"ECUS"
//							,"Eclipse Command Usage Statistics"
//							).openURL(statisticsFile.toURL());
//				} catch (PartInitException e) {
//				}
//			}
//		} catch (IOException e) {
//		}
//	}
}
