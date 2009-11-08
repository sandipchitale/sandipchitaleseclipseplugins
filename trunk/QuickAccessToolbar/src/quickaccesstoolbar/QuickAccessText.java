package quickaccesstoolbar;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.commands.Command;
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
					quickAccessBox.setText("");
					quickAccessDialog = null;
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
			if (quickAccessDialog == null) {
				showQuickAccessDialog();
			}
			if (!quickAccessDialog.getShell().isDisposed()) {
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
		parent.setLayout(layout);
		quickAccessBox = new Text(parent, SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
		quickAccessBox.setText("                    ");
		quickAccessBox.addFocusListener(new FocusListener() {
			private boolean firstTime = true;
			public void focusLost(FocusEvent e) {
				quickAccessBox.removeKeyListener(keyListener);
				quickAccessBox.removeModifyListener(modifyListener);
			}
			
			public void focusGained(FocusEvent e) {
				if (firstTime) {
					firstTime = false;
					quickAccessBox.setText("");
					return;
				}
				quickAccessBox.addKeyListener(keyListener);
				quickAccessBox.addModifyListener(modifyListener);
				if (quickAccessBox.getText().trim().length() > 0) {
					showQuickAccessDialog();
				}
			}
		});
		
		GridData quickAccessBoxGridData = new GridData(SWT.LEFT, SWT.TOP, false, false);
		quickAccessBoxGridData.widthHint = quickAccessBox.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		quickAccessBox.setLayoutData(quickAccessBoxGridData);
		return quickAccessBox;
	}
	
	private void showQuickAccessDialog() {
		quickAccessDialog = new NoTitleQuickAccessDialog(getWorkbenchWindow(), null);
		Rectangle quickAccessBoxBounds = quickAccessBox.getBounds();
		Display display = quickAccessBox.getShell().getDisplay();
		Point quickAccessBoxLocationOnScreen = display.map(quickAccessBox, null, new Point(quickAccessBoxBounds.x, quickAccessBoxBounds.y));
		quickAccessDialog.getShell().setLocation(quickAccessBoxLocationOnScreen.x, quickAccessBoxLocationOnScreen.y + quickAccessBoxBounds.height + 2);
		quickAccessDialog.open();
	}
}
