package eclipsemate.bundle.model.persistent;

import java.io.File;
import java.util.Collections;
import java.util.Set;

import eclipsemate.bundle.model.EMBundle;

public class EMBundleRegistry {
	private static Set<EMBundle> bundles;
	
	public static void addBundle(EMBundle bundle) {
		bundles.add(bundle);
	}
	
	public static void removeBundle(EMBundle bundle) {
		bundles.remove(bundle);
	}
	
	public static Set<EMBundle> getBundles() {
		return Collections.unmodifiableSet(bundles);
	}

	public static void removeAllBundles() {
		bundles.clear();
	}
	
	public static void loadBundles() {
		
	}
	
	public static void reloadBundles() {
		
	}
	
	public static void saveBundles() {
		
	}
	
	public static EMBundle parse(File bundleFolder) {
		return null;
	}
}
