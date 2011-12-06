package closeviews;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "CloseViews"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	static void showMessage(IWorkbenchPage activePage, String message) {
		if (activePage != null) {
			IStatusLineManager statusLineManager = null;
			IWorkbenchPart activePart = activePage.getActivePart();
			if (activePart instanceof IViewPart) {
				IViewPart viewPart = (IViewPart) activePart;
				IViewSite viewSite = viewPart.getViewSite();
				statusLineManager = viewSite.getActionBars().getStatusLineManager();
			} else if (activePart instanceof IEditorPart) {
				IEditorPart editorPart = (IEditorPart) activePart;
				IEditorSite editorSite = editorPart.getEditorSite();
				statusLineManager = editorSite.getActionBars().getStatusLineManager();
			}
			if (statusLineManager != null) {
				statusLineManager.setMessage(message);
			}
		}
	}
}
