package featurespluginsfragments;

import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;

import org.eclipse.update.internal.configurator.FeatureEntry;
import org.osgi.framework.Bundle;

@SuppressWarnings("restriction")
class Utils {

	static void  printFeatureEntry(FeatureEntry featureEntry, StringBuilder stringBuffer, String indent, Map<String, FeatureEntry> featureIdToFeatureEntry, Map<String, SortedSet<String>> featureIdToIncludedFeatureIds) {
		Bundle[] bundles = featureEntry.getBundles();
		String featureId = getFeatureId(featureEntry);
		stringBuffer.append(indent + "Feature: " + featureId + "\n");
		indent += "  ";
		for (Bundle bundle : bundles) {
			printBundle(bundle, stringBuffer, indent);
		}
		SortedSet<String> includedFeatureIds = featureIdToIncludedFeatureIds.get(featureId);
		if (includedFeatureIds != null) {
			for (Iterator<String> iterator = includedFeatureIds.iterator(); iterator.hasNext();) {
				String includedFeatureId = iterator.next();
				FeatureEntry includedFeatureEntry = featureIdToFeatureEntry.get(includedFeatureId);
				if (includedFeatureEntry == null) {
					stringBuffer.append(indent + "Feature: " + includedFeatureId + "\n");
				} else {
					printFeatureEntry(includedFeatureEntry, stringBuffer, indent, featureIdToFeatureEntry, featureIdToIncludedFeatureIds);
				}
			}
		}
	}

	static void printBundle(Bundle bundle, StringBuilder stringBuffer, String indent) {
		stringBuffer.append(indent + (isFragment(bundle) ? "Fragment: " : "Plugin: ") + getBundleId(bundle) + "\n");
	}

	static boolean isFragment(Bundle bundle) {
		if (bundle.getHeaders().get("Fragment-Host") != null) {
			return true;
		}
		return false;
	}

	static String getFeatureId(FeatureEntry featureEntry) {
		return featureEntry.getIdentifier() + ":" + featureEntry.getVersion();
	}

	static String getBundleId(Bundle bundle) {
		return bundle.getSymbolicName() + ":" + bundle.getHeaders().get("Bundle-Version");
	}

}
