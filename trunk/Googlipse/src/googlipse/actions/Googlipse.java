package googlipse.actions;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.browser.WebBrowserEditor;
import org.eclipse.ui.internal.browser.WebBrowserEditorInput;

@SuppressWarnings("restriction")
public class Googlipse implements IWorkbenchWindowPulldownDelegate2 {
	private static Map<String, String> urlsMap = new LinkedHashMap<String, String>();
	static {
		urlsMap.put("https://www.google.com/accounts/ManageAccount", "Account");
		urlsMap.put("http://code.google.com/u/sandipchitale/", "Code");
		urlsMap.put("http://docs.google.com", "Docs");
		urlsMap.put("http://sandipchitale.blogspot.com/", "Blog");
		urlsMap.put("http://mail.google.com/mail", "Gmail");
		urlsMap.put("http://www.google.com/calendar", "Calendar");
	}

	public void init(IWorkbenchWindow window) {}

	public void run(IAction action) {
		open(urlsMap.keySet().iterator().next());
	}

	private void open(String urlString) {
		try {
			WebBrowserEditor.open(
					new WebBrowserEditorInput(
							new URL(urlString),
							IWorkbenchBrowserSupport.PERSISTENT, "Googlipse") {
						@Override
						public boolean canReplaceInput(WebBrowserEditorInput input) {
							return input.getURL().getHost().toLowerCase().endsWith("google.com");
						}
						@Override
						public boolean isToolbarLocal() {
							return true;
						}
						@Override
						public boolean isStatusbarVisible() {
							return true;
						}
					});
		} catch (MalformedURLException e) {
		}
	}

	public void selectionChanged(IAction action, ISelection selection) {}

	private Menu googlipseMenu;
	private Menu googlipseMenuInMenu;
	public Menu getMenu(Menu parent) {
		if (googlipseMenuInMenu != null) {
			googlipseMenuInMenu.dispose();
		}
		googlipseMenuInMenu = new Menu(parent);
		fillMenu(googlipseMenuInMenu, 0);
		return googlipseMenuInMenu;
	}
	
	public Menu getMenu(Control parent) {
		if (googlipseMenu != null) {
			googlipseMenu.dispose();
		}
		googlipseMenu = new Menu(parent);
		fillMenu(googlipseMenu, 0);
		return googlipseMenu;
	}

	
	private void fillMenu(Menu menu, int start) {
		int i = 0;
		for (final String url : urlsMap.keySet()) {
			if (i >= start) {
				MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
				menuItem.setText(urlsMap.get(url));
				menuItem.addSelectionListener(new SelectionListener() {

					public void widgetDefaultSelected(SelectionEvent e) {
						widgetSelected(e);
					}

					public void widgetSelected(SelectionEvent e) {
						open(url);
					}
				});
			}
			i++;
		}
	}

	public void dispose() {
		if (googlipseMenuInMenu != null) {
			googlipseMenuInMenu.dispose();
		}
		if (googlipseMenu != null) {
			googlipseMenu.dispose();
		}
	}
}
