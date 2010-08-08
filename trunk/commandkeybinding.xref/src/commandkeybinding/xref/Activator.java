package commandkeybinding.xref;

import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "commandkeybinding.xref";

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
	
	public static final String ADD_KEYSTROKE = "/icons/addkeystroke.png"; //$NON-NLS-1$
	public static final String WINDOWS = "/icons/windows.png"; //$NON-NLS-1$
	public static final String LINUX = "/icons/linux.png"; //$NON-NLS-1$
	public static final String MAC = "/icons/apple.png"; //$NON-NLS-1$
	public static final String USER = "/icons/user.gif"; //$NON-NLS-1$
	
	protected void initializeImageRegistry(ImageRegistry reg) {
		reg.put(ADD_KEYSTROKE, imageDescriptorFromPlugin(PLUGIN_ID, ADD_KEYSTROKE));
		reg.put("win32", imageDescriptorFromPlugin(PLUGIN_ID, WINDOWS));
		reg.put("wpf", imageDescriptorFromPlugin(PLUGIN_ID, WINDOWS));
		reg.put("gtk", imageDescriptorFromPlugin(PLUGIN_ID, LINUX));
		reg.put("motif", imageDescriptorFromPlugin(PLUGIN_ID, LINUX));
		reg.put("cocoa", imageDescriptorFromPlugin(PLUGIN_ID, MAC));
		reg.put("carbon", imageDescriptorFromPlugin(PLUGIN_ID, MAC));
		reg.put("U", imageDescriptorFromPlugin(PLUGIN_ID, USER));
	}
	
	public Image getImage(String imageID) {
		return getImageRegistry().get(imageID);
	}

}
