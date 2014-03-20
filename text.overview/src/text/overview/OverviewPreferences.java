package text.overview;

import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * The preferences initializer for Overview
 *
 * @author Sandip V. Chitale
 *
 */
public class OverviewPreferences extends AbstractPreferenceInitializer {

	static final String MIN_OVERVIEW_FONT_SIZE = "MIN_OVERVIEW_FONT_SIZE";
	static final String OVERVIEW_FONT_SIZE = "OVERVIEW_FONT_SIZE";
	static final String MAX_OVERVIEW_FONT_SIZE = "MAX_OVERVIEW_FONT_SIZE";
	
	private static int defaultMinOverviewFontSize = 1;
	private static int defaultOverviewFontSize = defaultMinOverviewFontSize;
	private static int defaultMaxOverviewFontSize = 13;

	/**
	 * The constructor
	 */
	public OverviewPreferences() {
	}


	@Override
	public void initializeDefaultPreferences() {
		if (Platform.getOS().equals(Platform.OS_MACOSX)) {
			defaultMinOverviewFontSize = 4;
		}
		defaultOverviewFontSize = defaultMinOverviewFontSize;
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		prefs.setDefault(MIN_OVERVIEW_FONT_SIZE, defaultMinOverviewFontSize);
		prefs.setDefault(OVERVIEW_FONT_SIZE, defaultOverviewFontSize);
		prefs.setDefault(MAX_OVERVIEW_FONT_SIZE, defaultMaxOverviewFontSize);
	}

}
