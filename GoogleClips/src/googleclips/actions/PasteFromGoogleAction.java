package googleclips.actions;

import googleclips.Activator;

import java.net.URL;
import java.util.List;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 
 * @author Sandip V. Chitale
 *
 */
public class PasteFromGoogleAction implements IWorkbenchWindowPulldownDelegate2 {

	private IWorkbenchWindow window;
    private int candidateInsertIndex = 0;
    private long lastPasteTimeInMillis = -1L;
    
	public void dispose() {
		if (googleClipsInEditMenu != null) {
			googleClipsInEditMenu.dispose();
		}
		if (googleClipsMenu != null) {
			googleClipsMenu.dispose();
		}
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
        long currentTimeMillis = System.currentTimeMillis();
        if ((lastPasteTimeInMillis == -1)
                || (currentTimeMillis - lastPasteTimeInMillis) > 2000L) {
            candidateInsertIndex = 0;
        } else {
            candidateInsertIndex++;
        }
        lastPasteTimeInMillis = currentTimeMillis;
        List<String> googleClips = Activator.getDefault().getGoogleClips();        
		String textToInsert = googleClips.get(candidateInsertIndex % googleClips.size());
		if (textToInsert != null) {
			IEditorPart activeEditor = window.getActivePage().getActiveEditor();
			if (activeEditor instanceof ITextEditor) {
				ITextEditor editor = (ITextEditor) activeEditor;
				Object adapter = (Control) editor.getAdapter(Control.class);
				if (adapter instanceof Control) {
					Control control = (Control) adapter;
					if (control instanceof StyledText) {
						StyledText styledText = (StyledText) control;
						if (!styledText.getEditable()) {
							window.getShell().getDisplay().beep();
							return;
						}
						int caretOffset = styledText.getCaretOffset();
						styledText.insert(textToInsert);
						styledText.setSelection(caretOffset + textToInsert.length(), caretOffset);
					}
				}
			}
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(false);
		if (window != null && window.getActivePage() != null) {
			IEditorPart activeEditor = window.getActivePage().getActiveEditor();
			if (activeEditor instanceof ITextEditor) {
				ITextEditor editor = (ITextEditor) activeEditor;
				Object adapter = (Control) editor.getAdapter(Control.class);
				if (adapter instanceof Control) {
					action.setEnabled(true);
				}
			}
		}
	}

	private Menu googleClipsInEditMenu;

	public Menu getMenu(Menu parent) {
		if (googleClipsInEditMenu == null) {
			googleClipsInEditMenu = new Menu(parent);
			googleClipsInEditMenu.addMenuListener(new MenuListener() {
				public void menuHidden(MenuEvent e) {}
				public void menuShown(MenuEvent e) {
					for (MenuItem item : googleClipsInEditMenu.getItems()) {
						item.dispose();
					}
					fillMenu(googleClipsInEditMenu);
				}
			});
		}
		return googleClipsInEditMenu;
	}

	private Menu googleClipsMenu;
	public Menu getMenu(Control parent) {
		if (googleClipsMenu != null) {
			googleClipsMenu.dispose();
		}
		googleClipsMenu = new Menu(parent);

		fillMenu(googleClipsMenu);

		return googleClipsMenu;
	}

	private void fillMenu(Menu menu) {
		List<String> googleClips = Activator.getDefault().getGoogleClips();
		int clipsCount = 0;
		for (String googleClip : googleClips) {
			MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
			menuItem.setText(Activator.abbreviate(googleClip));
			menuItem.setData(Activator.PLUGIN_ID, googleClip);
			menuItem.addSelectionListener(new SelectionListener() {

				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}

				public void widgetSelected(SelectionEvent e) {
					Object data = e.widget.getData(Activator.PLUGIN_ID);
					if (data instanceof String) {
						String textToInsert = (String) data;
						if (textToInsert != null) {
							IEditorPart activeEditor = window.getActivePage().getActiveEditor();
							if (activeEditor instanceof ITextEditor) {
								ITextEditor editor = (ITextEditor) activeEditor;
								Object adapter = (Control) editor.getAdapter(Control.class);
								if (adapter instanceof Control) {
									Control control = (Control) adapter;
									if (control instanceof StyledText) {
										StyledText styledText = (StyledText) control;
										if (!styledText.getEditable()) {
											window.getShell().getDisplay().beep();
											return;
										}
										int caretOffset = styledText.getCaretOffset();
										styledText.insert(textToInsert);
										styledText.setSelection(caretOffset + textToInsert.length(), caretOffset);
									}
								}
							}
						}
					}
				}

			});
			clipsCount++;
			if (clipsCount > Activator.getDefault().getMaxClipsCount()) {
				break;
			}
		}
		
//		new MenuItem(menu, SWT.SEPARATOR);
//		MenuItem clearClips = new MenuItem(menu, SWT.PUSH);
//		clearClips.setText("Clear");
//		clearClips.addSelectionListener(new SelectionListener() {
//			public void widgetDefaultSelected(SelectionEvent e) {
//				widgetSelected(e);
//			}
//
//			public void widgetSelected(SelectionEvent e) {
//				Activator.getDefault().clearGoogleClips();
//			}
//		});
		new MenuItem(menu, SWT.SEPARATOR);
		MenuItem gotoSpreadsheet = new MenuItem(menu, SWT.PUSH);
		gotoSpreadsheet.setText("Browse " + Activator.getDefault().getSpreadsheetName() + " spreadsheet");
		gotoSpreadsheet.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				Activator activator = Activator.getDefault();
				activator.getGoogleClips();
				URL listFeedUrl = activator.getListFeedUrl();
				if (listFeedUrl != null) {
					String externalForm = listFeedUrl.toExternalForm().substring("http://spreadsheets.google.com/feeds/list/".length());
					externalForm = "http://spreadsheets.google.com/ccc?key=" + externalForm.substring(0, externalForm.indexOf("/"));
					Program.launch(externalForm);
				}
			}
		});
		
		new MenuItem(menu, SWT.SEPARATOR);
		MenuItem toggleAutoClipCutCopy = new MenuItem(menu, SWT.CHECK);
		toggleAutoClipCutCopy.setText("Auto clip Cut and Copy");
		toggleAutoClipCutCopy.setSelection(Activator.getDefault().isAutoClipCutCopy());
		toggleAutoClipCutCopy.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				Activator activator = Activator.getDefault();
				activator.setAutoClipCutCopy(!activator.isAutoClipCutCopy());
			}
		});
		MenuItem preferences = new MenuItem(menu, SWT.PUSH);
		preferences.setText("Preferences...");
		preferences.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				String[] displayedIds = new String[] {"GoogleClips.page"};
				PreferenceDialog pathToolsPreferenceDialog = PreferencesUtil.createPreferenceDialogOn(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						displayedIds[0],
						displayedIds,
						null);
				pathToolsPreferenceDialog.open();
			}
		});
	}

}
