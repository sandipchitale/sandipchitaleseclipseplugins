package codeclips;

import java.io.IOException;
import java.util.UUID;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "CodeClips";

	// The shared instance
	private static Activator plugin;
	
	private static class CodeClipsTemplateStore extends TemplateStore {

		public CodeClipsTemplateStore(IPreferenceStore store, String key) {
			super(store, key);
		}
		
		protected void internalAdd(TemplatePersistenceData data) {
			super.internalAdd(data);
		}
	}
	
	private static CodeClipsTemplateStore templateStore;
	
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
		
		templateStore = new CodeClipsTemplateStore(getPreferenceStore(), PreferenceInitializer.TEMPLATES_KEY);
		templateStore.load();
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
	
	public TemplateStore getTemplateStore() {
		return templateStore;
	}
	
	public void persistTemplate(String abbrev, String description, String expansion) {
		// fake
		Template template = new Template(abbrev, description+" ", "", expansion, true);
		TemplatePersistenceData templatePersistenceData = new TemplatePersistenceData(template, true, UUID.randomUUID().toString());
		persistTemplatePersistenceData(templatePersistenceData);
		// fake
		template = new Template(abbrev, description, "", expansion, true);
		templatePersistenceData.setTemplate(template);
	}

	public void persistTemplatePersistenceData(TemplatePersistenceData templatePersistenceData) {
		templateStore.internalAdd(templatePersistenceData);
		try {
			templateStore.save();
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}
	}
	
	public static final String CODECLIP = "/icons/codeclip.gif"; //$NON-NLS-1$
	
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		reg.put(CODECLIP, imageDescriptorFromPlugin(PLUGIN_ID, CODECLIP));
	}
	
	public Image getImage(String imageID) {
		return getImageRegistry().get(imageID);
	}

}
