package cdgit;

import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowPulldownDelegate2;

/**
 * This launches the Command Line Shell with the selected folder or the folder
 * containing the selected file as the pwd.
 * 
 * @author Sandip V. Chitale
 * 
 */
public class CdgitWindowPulldownAction extends CdgitAction implements IWorkbenchWindowPulldownDelegate2 {

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

}