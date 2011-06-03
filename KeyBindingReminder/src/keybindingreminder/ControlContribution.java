package keybindingreminder;

import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

public class ControlContribution extends WorkbenchWindowControlContribution {

	public ControlContribution() {
	}

	public ControlContribution(String id) {
		super(id);
	}
	
	private static Map<String, Integer> commandIdInvocationCountMap = new LinkedHashMap<String, Integer>();
	private boolean active = true;

	@Override
	protected Control createControl(Composite parent) {
		final Text reminder = new Text(parent, SWT.SINGLE | SWT.READ_ONLY | SWT.BORDER);
		final IBindingService bindingService =
			(IBindingService) PlatformUI.getWorkbench().getService(IBindingService.class);
		final ICommandService commandService =
			(ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		
		final IExecutionListener executionListener = new IExecutionListener() {
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
						StringBuilder stringBuilder = new StringBuilder();
						for (TriggerSequence triggerSequence : activeBindingsFor) {
							if (stringBuilder.length() != 0) {
								stringBuilder.append(" or ");
							}
							stringBuilder.append(triggerSequence.toString());
						}
						Integer invocationCount = commandIdInvocationCountMap.get(commandId);
						if (invocationCount == null) {
							invocationCount = 1;
							commandIdInvocationCountMap.put(commandId, invocationCount);
						} else {
							commandIdInvocationCountMap.put(commandId, 1 + invocationCount);
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
							reminder.setBackground(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_RED));
						} else if (invocationCount > 1) {
							reminder.setBackground(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW));
						} else if (invocationCount > 0) {
							reminder.setBackground(PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW));
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
				reminder.setBackground(null);
				reminder.setForeground(null);
				if (active) {
					active = false;
					commandService.removeExecutionListener(executionListener);
					reminder.setText("");
					reminder.setToolTipText("Click to activate the\nKey binding reminder!");
				} else {
					active = true;
					commandService.addExecutionListener(executionListener);
					reminder.setToolTipText("");
				}
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
		
		
		return reminder;
	}

}
