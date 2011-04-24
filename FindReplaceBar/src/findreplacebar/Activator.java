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
	
	static final String ICON_CLOSE = "icons/close.png";  //$NON-NLS-1$
	static final String ICON_COUNT_OF_TOTAL = "icons/countOfTotal.png";  //$NON-NLS-1$
	static final String ICON_FIND_SCOPE_ALL = "icons/findScopeAll.png"; //$NON-NLS-1$
	static final String ICON_FIND_SCOPE_SELECTED_LINES = "icons/findScopeSelectedLines.png"; //$NON-NLS-1$
	static final String ICON_CASE_SENSITIVE = "icons/casesensitive.png"; //$NON-NLS-1$
	static final String ICON_REGULAR_EXRESSION = "icons/regularexpression.png"; //$NON-NLS-1$
	static final String ICON_WHOLE_WORD = "icons/wholeword.png"; //$NON-NLS-1$
	
	static final String ICON_FIND = "icons/find.png";  //$NON-NLS-1$
	static final String ICON_PREFERENCES = "icons/preferences.png";  //$NON-NLS-1$


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
		reg.put(ICON_CLOSE, imageDescriptorFromPlugin(PLUGIN_ID, ICON_CLOSE));
		reg.put(ICON_FIND_SCOPE_ALL, imageDescriptorFromPlugin(PLUGIN_ID, ICON_FIND_SCOPE_ALL));
		reg.put(ICON_FIND_SCOPE_SELECTED_LINES, imageDescriptorFromPlugin(PLUGIN_ID, ICON_FIND_SCOPE_SELECTED_LINES));
		
		reg.put(ICON_COUNT_OF_TOTAL, imageDescriptorFromPlugin(PLUGIN_ID, ICON_COUNT_OF_TOTAL));
		
		reg.put(ICON_CASE_SENSITIVE, imageDescriptorFromPlugin(PLUGIN_ID, ICON_CASE_SENSITIVE));
		reg.put(ICON_REGULAR_EXRESSION, imageDescriptorFromPlugin(PLUGIN_ID, ICON_REGULAR_EXRESSION));
		reg.put(ICON_WHOLE_WORD, imageDescriptorFromPlugin(PLUGIN_ID, ICON_WHOLE_WORD));
		
		reg.put(ICON_FIND, imageDescriptorFromPlugin(PLUGIN_ID, ICON_FIND));
		reg.put(ICON_PREFERENCES, imageDescriptorFromPlugin(PLUGIN_ID, ICON_PREFERENCES));
	}
	
	public boolean getOverrideFindReplaceDialog() {
		return getPreferenceStore().getBoolean(FindReplaceBarPreferenceInitializer.OVERRIDE_FIND_REPLACE_DIALOG);
	}
}
