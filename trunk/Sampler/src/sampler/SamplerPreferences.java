package sampler;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

public class SamplerPreferences extends AbstractPreferenceInitializer {
	public static String LABLE_VALUE_SEPARATOR = "|||";

	private static List<String> formatsList = new LinkedList<String>();
	static {
		formatsList.add("#RRGGBB (HTML/CSS)" + LABLE_VALUE_SEPARATOR + "#%1$02x%2$02x%3$02x");
		formatsList.add("rgb(r,g,b) (CSS)" + LABLE_VALUE_SEPARATOR + "rgb(%1$d, %2$d, %3$d)");
		formatsList.add("new Color(r,g,b) (AWT)" + LABLE_VALUE_SEPARATOR + "new Color(%1$3d, %2$3d, %3$3d)");
	}

	public static final String COPY_TO_CLIPBOARD = Activator.PLUGIN_ID + ".COPY_TO_CLIPBOARD";
	public static final String CUSTOM_FORMATS = Activator.PLUGIN_ID + ".CUSTOM_FORMATS";
	public static final String CURRENT_FORMAT = Activator.PLUGIN_ID + ".CURRENT_FORMAT";

	public SamplerPreferences() {
	}

	public void initializeDefaultPreferences() {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		String[] formatsArray = formatsList.toArray(new String[0]);
		String formats = createList(formatsArray);
		prefs.setDefault(CUSTOM_FORMATS, formats);
		prefs.setDefault(CURRENT_FORMAT, formatsArray[0]);
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
	
	public static String getCurrentFormat() {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		String currentFormat = prefs.getString(CURRENT_FORMAT);
		if ("".equals(currentFormat)) {
			String[] customFormats = getCustomFormats();
			if (customFormats.length > 0) {
				currentFormat = customFormats[0];
			} else {
				currentFormat = "Red,Green,BLUE"  + SamplerPreferences.LABLE_VALUE_SEPARATOR + "{r}, {g}, {b}";
			}
		}
		return currentFormat;
	}
	
	public static void setCurrentFormat(String currentFormat) {
		IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
		prefs.setValue(CURRENT_FORMAT, currentFormat);
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
