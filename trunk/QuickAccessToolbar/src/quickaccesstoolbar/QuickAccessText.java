package quickaccesstoolbar;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.quickaccess.QuickAccessDialog;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

@SuppressWarnings("restriction")
public class QuickAccessText extends WorkbenchWindowControlContribution {
	private Text quickAccessBox;
	
	private static Method handleSelectionMethod;
	private static Field tableField;
	
	static {
		try {
			handleSelectionMethod = QuickAccessDialog.class.getDeclaredMethod("handleSelection");
			handleSelectionMethod.setAccessible(true);
		} catch (SecurityException e) {
		} catch (NoSuchMethodException e) {
		}
		
		try {
			tableField = QuickAccessDialog.class.getDeclaredField("table");
			tableField.setAccessible(true);
		} catch (SecurityException e) {
		} catch (NoSuchFieldException e) {
		}
	}
	
	private class NoTitleQuickAccessDialog extends QuickAccessDialog {
		public NoTitleQuickAccessDialog(IWorkbenchWindow window,
				Command invokingCommand) {
			super(window, invokingCommand);
			setBlockOnOpen(false);
			try {
				Field takeFocusOnOpenField = PopupDialog.class.getDeclaredField("takeFocusOnOpen");
				takeFocusOnOpenField.setAccessible(true);
				takeFocusOnOpenField.set(this, Boolean.FALSE);
			} catch (SecurityException e) {
			} catch (NoSuchFieldException e) {
			} catch (IllegalArgumentException e) {
			} catch (IllegalAccessException e) {
			}
		}
		
		private Text titleControl;
		
		@Override
		protected Control createTitleControl(Composite parent) {
			// Set up dispose listener
			getShell().addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					quickAccessDialog = null;
					quickAccessBox.setText("");
				}
			});
			
			titleControl = (Text) super.createTitleControl(parent);
			return titleControl;
		}
		
		public Text getTitleControl() {
			return titleControl;
		}
		
		void handleSelection() {
			if (handleSelectionMethod != null) {
				try {
					handleSelectionMethod.invoke(this);
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				} catch (InvocationTargetException e) {
				}
			}
		}
		
		void nextSelection() {
			Table table = getTable();
			if (table != null) {
				int index = table.getSelectionIndex();
				if (index != -1 && table.getItemCount() > index + 1) {
					table.setSelection(index + 1);
				}
			}
		}
		
		void previousSelection() {
			Table table = getTable();
			if (table != null) {
				int index = table.getSelectionIndex();
				if (index != -1 && index >= 1) {
					table.setSelection(index - 1);
				}
			}
		}
		
		private Table getTable() {
			if (tableField != null) {
				try {
					return (Table) tableField.get(this);
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				}
			}
			return null;
		}
	}
	
	
	private NoTitleQuickAccessDialog quickAccessDialog;

	private ModifyListener modifyListener = new ModifyListener() {
		public void modifyText(ModifyEvent e) {
			if (quickAccessBox.getText().trim().length() > 0) {
				if (quickAccessDialog == null) {
					showQuickAccessDialog();
				}
				quickAccessDialog.getTitleControl().setText(quickAccessBox.getText());
			}
		}
	};
	
	private KeyListener keyListener = new KeyListener() {
		public void keyPressed(KeyEvent e) {
			if (e.keyCode == 0x0D) {
				if (quickAccessDialog != null) {
					quickAccessDialog.handleSelection();
				}
				return;
			} else if (e.keyCode == SWT.ARROW_DOWN) {
				if (quickAccessDialog != null) {
					quickAccessDialog.nextSelection();
				}
			} else if (e.keyCode == SWT.ARROW_UP) {
				if (quickAccessDialog != null) {
					quickAccessDialog.previousSelection();
				}
			} else if (e.character == 0x1B) {// ESC
				if (quickAccessDialog != null) {
					quickAccessDialog.close();
				}
			}
		}

		public void keyReleased(KeyEvent e) {}
	};
	
	public QuickAccessText() {
		this("");
	}

	public QuickAccessText(String id) {
		super(id);
	}

	@Override
	protected Control createControl(Composite parent) {
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight=2;
		parent.setLayout(layout);
		quickAccessBox = new Text(parent, SWT.SEARCH | SWT.ICON_SEARCH);
		quickAccessBox.setText("                                    ");
		quickAccessBox.addFocusListener(new FocusListener() {
			private boolean firstTime = true;
			private boolean secondTime = false;
			public void focusLost(FocusEvent e) {
				quickAccessBox.removeKeyListener(keyListener);
				quickAccessBox.removeModifyListener(modifyListener);
			}
			
			public void focusGained(FocusEvent e) {
				if (firstTime) {
					firstTime = false;
					secondTime = true;
					setQuickAccessBoxHelp();
					return;
				}
				if (secondTime) {
					secondTime = false;
					// Reset foreground for notmal use
					quickAccessBox.setForeground(null);
				}
				quickAccessBox.setText("");
				quickAccessBox.addKeyListener(keyListener);
				quickAccessBox.addModifyListener(modifyListener);
				if (quickAccessBox.getText().trim().length() > 0) {
					showQuickAccessDialog();
				}
			}
		});
		
		GridData quickAccessBoxGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		quickAccessBoxGridData.widthHint = quickAccessBox.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		quickAccessBox.setLayoutData(quickAccessBoxGridData);

		IHandlerService handlerService = (IHandlerService) getWorkbenchWindow().getService(IHandlerService.class);
		handlerService.activateHandler("QuickAccessToolbar.toolbar.command", new AbstractHandler() {
			public Object execute(ExecutionEvent event)
					throws ExecutionException {
				return quickAccessBox.setFocus();
			}
		});
		
		return quickAccessBox;
	}

	private void setQuickAccessBoxHelp() {
		quickAccessBox.setForeground(quickAccessBox.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
		if ("macosx".equals(Platform.getOS())) {			
			quickAccessBox.setText("Command+4 to activate");
		} else {
			quickAccessBox.setText("Control+4 to activate");
		}
	}
	
	private void showQuickAccessDialog() {
		ICommandService commandService = (ICommandService) getWorkbenchWindow().getService(ICommandService.class);
		quickAccessDialog = new NoTitleQuickAccessDialog(getWorkbenchWindow(), commandService.getCommand("org.eclipse.ui.window.quickAccess"));
		Rectangle quickAccessBoxBounds = quickAccessBox.getBounds();
		Display display = quickAccessBox.getShell().getDisplay();
		Point quickAccessBoxLocationOnScreen = display.map(quickAccessBox, null, new Point(quickAccessBoxBounds.x, quickAccessBoxBounds.y));
		quickAccessDialog.getShell().setLocation(quickAccessBoxLocationOnScreen.x, quickAccessBoxLocationOnScreen.y + quickAccessBoxBounds.height + 2);
		quickAccessDialog.open();
	}
}
