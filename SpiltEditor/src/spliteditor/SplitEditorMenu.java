package spliteditor;

import org.eclipse.jface.action.ContributionItem;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.EditorPane;
import org.eclipse.ui.internal.ILayoutContainer;
import org.eclipse.ui.internal.LayoutPart;
import org.eclipse.ui.internal.PartPane;
import org.eclipse.ui.internal.PartSite;

@SuppressWarnings("restriction")
public class SplitEditorMenu extends ContributionItem {
	private String orientation;

	public SplitEditorMenu(String orientation) {
		this.orientation = orientation;
	}

	@Override
	public void fill(Menu menu, int index) {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
			if (activePage != null) {
				final IEditorPart activeEditor = activePage.getActiveEditor();
				PartPane activeEditorPartPane = ((PartSite) activeEditor.getSite()).getPane();
				LayoutPart layoutPart = activeEditorPartPane.getPart();
				ILayoutContainer layoutPartContainer = layoutPart.getContainer();
				LayoutPart[] children = layoutPartContainer.getChildren();
				if (children.length > 1) {
					for (LayoutPart childLayoutPart : children) {
						if (childLayoutPart instanceof EditorPane) {
							EditorPane editorPane = (EditorPane) childLayoutPart;
							IEditorReference editorReference = editorPane.getEditorReference();
							IWorkbenchPart part = editorReference.getPart(true);
							if (part != activeEditor) {
								if (part instanceof IEditorPart) {
									final IEditorPart otherEditorPart = (IEditorPart) part;
									MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
									menuItem.setText(otherEditorPart.getTitle());
									menuItem.addSelectionListener(new SelectionListener() {
										@Override
										public void widgetSelected(SelectionEvent e) {
											Helper.splitEditor(otherEditorPart, orientation);
										}

										@Override
										public void widgetDefaultSelected(SelectionEvent e) {
											widgetSelected(e);
										}
									});
								}
							}
						}
					}
				}
			}
		}
	}
}
