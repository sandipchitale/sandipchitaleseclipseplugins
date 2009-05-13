package featurespluginsfragments;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "FeaturesPluginsFragments";

	static final String FEATURE = "icons/feature.gif";
	static final String PLUGIN = "icons/plugin.gif";
	static final String FRAGMENT = "icons/fragment.gif";

	// The shared instance
	private static Activator plugin;

	private BundleContext context;
	
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
		this.context = context;
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
		context = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}
	
	BundleContext getBundleContext() {
		return context;
	}
	
    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
        reg.put(FEATURE, createImageDescriptor(FEATURE));
        reg.put(PLUGIN, createImageDescriptor(PLUGIN));
        reg.put(FRAGMENT, createImageDescriptor(FRAGMENT));
    }

    private ImageDescriptor createImageDescriptor(String id) {
        return imageDescriptorFromPlugin(PLUGIN_ID, id);
    }


}
