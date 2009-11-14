package codeclips;

import java.io.IOException;

import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
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
	
	private static TemplateStore templateStore;
	
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
		
		templateStore = new TemplateStore(getPreferenceStore(), PreferenceInitializer.TEMPLATES_KEY);
		templateStore.load();
		Template[] templates = templateStore.getTemplates();
		for (Template template : templates) {
			System.out.println(template.getName() + " " + template.getPattern());
		}
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
		TemplateStore templateStore = Activator.getDefault().getTemplateStore();
		
		Template existingTemplate = templateStore.findTemplate(abbrev);
		if (existingTemplate == null) {
			existingTemplate = new Template(abbrev, description, "", expansion, true);
			TemplatePersistenceData templatePersistenceData = new TemplatePersistenceData(existingTemplate, true);
			templateStore.add(templatePersistenceData);
		} else {
			existingTemplate.setDescription(description);
			existingTemplate.setPattern(expansion);
		}
		
		try {
			templateStore.save();
		} catch (IOException e) {
		}
		Template[] templates = templateStore.getTemplates();
		for (Template template : templates) {
			System.out.println(template.getName() + " " + template.getPattern());
		}
	}

}
