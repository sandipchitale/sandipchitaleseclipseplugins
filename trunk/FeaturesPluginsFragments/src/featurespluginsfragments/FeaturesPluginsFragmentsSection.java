package featurespluginsfragments;

import java.io.PrintWriter;
import java.util.Collection;
import java.util.Set;

import org.eclipse.ui.about.ISystemSummarySection;
import org.eclipse.update.internal.configurator.FeatureEntry;
import org.osgi.framework.Bundle;

@SuppressWarnings("restriction")
public class FeaturesPluginsFragmentsSection implements ISystemSummarySection {

	public FeaturesPluginsFragmentsSection() {
	}

	public void write(PrintWriter writer) {
		writer.println();
		Info info = new Info();
		// List to console
		Set<FeatureEntry> topLevelFeatureEntries = info.getTopLevelFeatureEntries();
		FeatureEntry[] featureEntryArray = topLevelFeatureEntries.toArray(new FeatureEntry[topLevelFeatureEntries.size()]);
		Collection<Bundle> orphanBundles = info.getOrphanBundles();

		StringBuilder stringBuilder = new StringBuilder();
		for (FeatureEntry featureEntry : featureEntryArray) {
			Utils.printFeatureEntry(featureEntry, stringBuilder, "", info.getFeatureIdToFeatureEntry(), info.getFeatureIdToIncludedFeatureIds());
		}
		for (Bundle bundle : orphanBundles) {
			Utils.printBundle(bundle, stringBuilder, "");
		}
		writer.println(stringBuilder.toString());
	}

}
