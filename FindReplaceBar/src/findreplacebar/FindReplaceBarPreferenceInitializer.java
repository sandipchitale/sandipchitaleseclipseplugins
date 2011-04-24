package findreplacebar;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class FindReplaceBarPreferenceInitializer extends
		AbstractPreferenceInitializer {
	
	static final String OVERRIDE_FIND_REPLACE_DIALOG = "OVERRIDE_FIND_REPLACE_DIALOG"; //$NON-NLS-1$
	static final boolean defaultOVERRIDE_FIND_REPLACE_DIALOG = false;

	public FindReplaceBarPreferenceInitializer() {
	}

	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		prefs.setDefault(OVERRIDE_FIND_REPLACE_DIALOG, defaultOVERRIDE_FIND_REPLACE_DIALOG);
	}

}
