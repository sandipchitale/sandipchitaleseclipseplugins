package findreplacebar;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "FindReplaceBar"; //$NON-NLS-1$
	
	static final String ICON_COUNT_OF_TOTAL = "icons/countOfTotal.png"; 
	static final String ICON_FIND_NEXT = "icons/findNext.png";
	static final String ICON_FIND_PREVIOUS = "icons/findPrevious.png";
	static final String ICON_FIND_SCOPE_ALL = "icons/findScopeAll.png";
	static final String ICON_FIND_SCOPE_SELECTED_LINES = "icons/findScopeSelectedLines.png";
	static final String ICON_REPLACE_FIND = "icons/replaceFind.png";
	static final String ICON_REPLACE = "icons/replace.png";
	static final String ICON_REPLACE_ALL = "icons/replaceAll.png";

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
	
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);
		reg.put(ICON_FIND_PREVIOUS, imageDescriptorFromPlugin(PLUGIN_ID, ICON_FIND_PREVIOUS));
		reg.put(ICON_FIND_NEXT, imageDescriptorFromPlugin(PLUGIN_ID, ICON_FIND_NEXT));
		reg.put(ICON_FIND_SCOPE_ALL, imageDescriptorFromPlugin(PLUGIN_ID, ICON_FIND_SCOPE_ALL));
		reg.put(ICON_FIND_SCOPE_SELECTED_LINES, imageDescriptorFromPlugin(PLUGIN_ID, ICON_FIND_SCOPE_SELECTED_LINES));
		reg.put(ICON_REPLACE_FIND, imageDescriptorFromPlugin(PLUGIN_ID, ICON_REPLACE_FIND));
		reg.put(ICON_REPLACE, imageDescriptorFromPlugin(PLUGIN_ID, ICON_REPLACE));
		reg.put(ICON_REPLACE_ALL, imageDescriptorFromPlugin(PLUGIN_ID, ICON_REPLACE_ALL));
	}

}
