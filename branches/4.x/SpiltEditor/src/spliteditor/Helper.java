package spliteditor;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.internal.EditorSashContainer;
import org.eclipse.ui.internal.EditorStack;
import org.eclipse.ui.internal.ILayoutContainer;
import org.eclipse.ui.internal.LayoutPart;
import org.eclipse.ui.internal.PageLayout;
import org.eclipse.ui.internal.PartPane;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.PartStack;
import org.eclipse.ui.internal.WorkbenchPage;

@SuppressWarnings("restriction")
public class Helper {
	static void splitEditor(IEditorPart editorPart, String orientation) {
		PartPane currentEditorPartPane = ((PartSite) editorPart.getSite()).getPane();
		LayoutPart layoutPart = currentEditorPartPane.getPart();
		EditorSashContainer editorSashContainer = null;
		ILayoutContainer layoutPartContainer = layoutPart.getContainer();
		if (layoutPartContainer instanceof LayoutPart) {
			ILayoutContainer editorSashLayoutContainer = ((LayoutPart) layoutPartContainer).getContainer();
			if (editorSashLayoutContainer instanceof EditorSashContainer) {
				editorSashContainer = (EditorSashContainer) editorSashLayoutContainer;
				/*
				 * Create a new part stack (i.e. a workbook) to home the
				 * currentEditorPartPane which hold the active editor
				 */
				PartStack newPart = createStack(editorPart.getEditorSite().getPage(), editorSashContainer);
				editorSashContainer.stack(currentEditorPartPane, newPart);
				if (OrientationParameterValues.HORIZONTALLY.equals(orientation)) {
					editorSashContainer.add(newPart, PageLayout.BOTTOM, 0.5f, (LayoutPart) layoutPartContainer);
				} else if (OrientationParameterValues.VERTICALLY.equals(orientation)) {
					editorSashContainer.add(newPart, PageLayout.RIGHT, 0.5f, (LayoutPart) layoutPartContainer);
				}
			}
		}
	}
	/**
	* A method to create a part stack container (a new workbook)
	*
	* @param editorSashContainer the <code>EditorSashContainer</code> to set for the returned <code>PartStack</code>
	* @return a new part stack container
	*/
	private static PartStack createStack(IWorkbenchPage workbenchPage, EditorSashContainer editorSashContainer) {
		return EditorStack.newEditorWorkbook(editorSashContainer, (WorkbenchPage) workbenchPage);
	}
}
