/**
 * 
 */
package featurespluginsfragments;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.Platform;
import org.eclipse.update.internal.configurator.FeatureEntry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

class Info {

	private Set<FeatureEntry> topLevelFeatureEntries;
	private Collection<Bundle> orphanBundles;
	private Map<String, FeatureEntry> featureIdToFeatureEntry;
	private Map<String, SortedSet<String>> featureIdToIncludedFeatureIds;

	Info() {
		load();
	}

	private void load() {
		featureIdToFeatureEntry = new HashMap<String, FeatureEntry>();
		featureIdToIncludedFeatureIds = new HashMap<String, SortedSet<String>>();
		Set<String> featureIds = new TreeSet<String>();
		Map<String, Bundle> bundleIdsToBundles = new TreeMap<String, Bundle>();
		BundleContext bundleContext = Activator.getDefault().getBundleContext();
		Bundle[] bundles = bundleContext.getBundles();
		for (Bundle bundle : bundles) {
			bundleIdsToBundles.put(Utils.getBundleId(bundle), bundle);
		}

		IBundleGroupProvider[] bundleGroupProviders = Platform.getBundleGroupProviders();
		for (IBundleGroupProvider bundleGroupProvider : bundleGroupProviders) {
			IBundleGroup[] bundleGroups = bundleGroupProvider.getBundleGroups();
			for (IBundleGroup bundleGroup : bundleGroups) {
				FeatureEntry featureEntry = (FeatureEntry) bundleGroup;
				String featureId = Utils.getFeatureId(featureEntry);
				featureIdToFeatureEntry.put(featureId, featureEntry);
				featureIds.add(featureId);
			}
			for (IBundleGroup bundleGroup : bundleGroups) {
				FeatureEntry featureEntry = (FeatureEntry) bundleGroup;
				String featureId = Utils.getFeatureId(featureEntry);
				IncludedFeaturesParser includedFeaturesParser = new IncludedFeaturesParser(featureEntry);
				includedFeaturesParser.parse();
				List<String> includedFeatures = includedFeaturesParser.getIncludedFeatures();
				for (Iterator<String> iter = includedFeatures.iterator(); iter.hasNext();) {
					String includedFeatureId = iter.next();
					SortedSet<String> set = featureIdToIncludedFeatureIds.get(featureId);
					if (set == null) {
						set = new TreeSet<String>();
						featureIdToIncludedFeatureIds.put(featureId, set);
					}
					set.add(includedFeatureId);
					featureIds.remove(includedFeatureId);
				}
				Bundle[] featuresBundles = bundleGroup.getBundles();
				for (Bundle bundle : featuresBundles) {
					// Remove bundles owned by this feature
					bundleIdsToBundles.remove(Utils.getBundleId(bundle));
				}
			}
		}

		topLevelFeatureEntries = new LinkedHashSet<FeatureEntry>();
		for (String featureId : featureIds) {
			FeatureEntry featureEntry = featureIdToFeatureEntry.get(featureId);
			topLevelFeatureEntries.add(featureEntry);
		}

		// List to console
		FeatureEntry[] featureEntryArray = topLevelFeatureEntries.toArray(new FeatureEntry[topLevelFeatureEntries.size()]);
		orphanBundles = bundleIdsToBundles.values();
	}

	Map<String, FeatureEntry> getFeatureIdToFeatureEntry() {
		return featureIdToFeatureEntry;
	}

	Map<String, SortedSet<String>> getFeatureIdToIncludedFeatureIds() {
		return featureIdToIncludedFeatureIds;
	}

	Set<FeatureEntry> getTopLevelFeatureEntries() {
		return topLevelFeatureEntries;
	}

	Collection<Bundle> getOrphanBundles() {
		return orphanBundles;
	}

}