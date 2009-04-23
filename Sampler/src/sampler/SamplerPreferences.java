package sampler;

import java.util.regex.Pattern;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class SamplerPreferences extends AbstractPreferenceInitializer {

	public static final String COPY_TO_CLIPBOARD = Activator.PLUGIN_ID + ".COPY_TO_CLIPBOARD";
	public static final String CUSTOM_FORMATS = Activator.PLUGIN_ID + ".CUSTOM_FORMATS";

	public SamplerPreferences() {
	}

	public void initializeDefaultPreferences() {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		prefs.setDefault(CUSTOM_FORMATS, "");
		prefs.setDefault(COPY_TO_CLIPBOARD, true);
	}
	
	public static void setCopyToClipboard(boolean copyToClipboard) {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		prefs.setValue(COPY_TO_CLIPBOARD, copyToClipboard);
	}

	public static boolean isCopyToClipboard() {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		return prefs.getBoolean(COPY_TO_CLIPBOARD);
	}
	
	public static String[] getCustomFormats() {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		return parseString(prefs.getString(CUSTOM_FORMATS));
	}
	
	private static String SEPARATOR = "@@@@";
	
	static String createList(String[] items) {
		StringBuilder stringBuilder = new StringBuilder();
		for (String item : items) {
			if (stringBuilder.length() > 0) {
				stringBuilder.append(SEPARATOR);
			}
			stringBuilder.append(item);
		}
		return stringBuilder.toString();
	}
	
	static String[] parseString(String stringList) {
		if (stringList != null && stringList.length() > 0) {
			return stringList.split(Pattern.quote(SEPARATOR));
		}
		return new String[0];
	}

}
