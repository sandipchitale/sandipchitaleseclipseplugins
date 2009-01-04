package featurespluginsfragments;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.dialogs.FilteredTree;
import org.eclipse.update.internal.configurator.FeatureEntry;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

@SuppressWarnings("restriction")
public class ListFeaturesPluginsFragmentsAction implements
		IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
		Map<String, FeatureEntry> featureIdToFeatureEntry = new HashMap<String, FeatureEntry>();
		Map<String, SortedSet<String>> featureIdToIncludedFeatureIds = new HashMap<String, SortedSet<String>>();
		Set<String> featureIds = new TreeSet<String>();
		Map<String, Bundle> bundleIdsToBundles = new TreeMap<String, Bundle>();
		BundleContext bundleContext = Activator.getDefault().getBundleContext();
		Bundle[] bundles = bundleContext.getBundles();
		for (Bundle bundle : bundles) {
			bundleIdsToBundles.put(getBundleId(bundle), bundle);
		}
		
		IBundleGroupProvider[] bundleGroupProviders = Platform
				.getBundleGroupProviders();
		for (IBundleGroupProvider bundleGroupProvider : bundleGroupProviders) {
			IBundleGroup[] bundleGroups = bundleGroupProvider.getBundleGroups();
			for (IBundleGroup bundleGroup : bundleGroups) {
				FeatureEntry featureEntry = (FeatureEntry) bundleGroup;
				String featureId = getFeatureId(featureEntry);
				featureIdToFeatureEntry.put(featureId, featureEntry);
				featureIds.add(featureId);
			}
			for (IBundleGroup bundleGroup : bundleGroups) {
				FeatureEntry featureEntry = (FeatureEntry) bundleGroup;
				String featureId = getFeatureId(featureEntry);
				IncludedFeaturesParser includedFeaturesParser = new IncludedFeaturesParser(
						featureEntry);
				includedFeaturesParser.parse();
				List<String> includedFeatures = includedFeaturesParser
						.getIncludedFeatures();
				for (Iterator<String> iter = includedFeatures.iterator(); iter
						.hasNext();) {
					String includedFeatureId = iter.next();
					SortedSet<String> set = featureIdToIncludedFeatureIds
							.get(featureId);
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
					bundleIdsToBundles.remove(getBundleId(bundle));
				}
			}
		}
		
		Set<FeatureEntry> topLevelFeatureEntries = new LinkedHashSet<FeatureEntry>();		
		for (String featureId : featureIds) {
			FeatureEntry featureEntry = featureIdToFeatureEntry
					.get(featureId);
			topLevelFeatureEntries.add(featureEntry);
		}
		
		// List to console
		FeatureEntry[] featureEntryArray = topLevelFeatureEntries.toArray(new FeatureEntry[topLevelFeatureEntries.size()]);
		Collection<Bundle> orphanBundels = bundleIdsToBundles.values();
		try {
			final MessageConsole messageConsole = getMessageConsole();
			IConsoleView view = (IConsoleView) window.getActivePage().showView(IConsoleConstants.ID_CONSOLE_VIEW);
			view.display(messageConsole);
			MessageConsoleStream messageConsoleStream = messageConsole.newMessageStream();
			for (FeatureEntry featureEntry : featureEntryArray) {
				printFeatureEntry(featureEntry, messageConsoleStream, "", featureIdToFeatureEntry, featureIdToIncludedFeatureIds);
			}
			for (Bundle bundle : orphanBundels) {
				printBundle(bundle, messageConsoleStream, "");
			}
			messageConsoleStream.flush();
			messageConsoleStream.close();
		} catch (PartInitException e) {
		} catch (IOException e) {
		}
		
		List<Object> objects = new LinkedList<Object>();
		objects.addAll(topLevelFeatureEntries);
		objects.addAll(orphanBundels);
		
		FESD fesd =
			new FESD(window.getShell(),
				new TreeLabelProvider(),
				new TreeContentProvider(featureIdToFeatureEntry,
						featureIdToIncludedFeatureIds));
		fesd.setHelpAvailable(false);
		fesd.setMessage("Features, Plug-ins and Fragments");
		fesd.setTitle("Features, Plug-ins and Fragments");
		fesd.setInput(objects.toArray());
		fesd.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}
	
	private static class FESD extends FilteredElementTreeSelectionDialog {

		FESD(Shell parent, ILabelProvider labelProvider,
				ITreeContentProvider contentProvider) {
			super(parent, labelProvider, contentProvider);
		}
		
		protected TreeViewer createTreeViewer(Composite parent) {
			final TreeViewer treeViewer = super.createTreeViewer(parent);

			treeViewer.getTree().addPaintListener(new PaintListener() {
				private boolean expanding;
				public void paintControl(PaintEvent e) {
					if (!expanding) {
						expanding = true;
						try {
							treeViewer.expandAll();
						} finally {
							expanding = false;
						}
					}
				}
			});
			
			return treeViewer;
		}
		
	}
	
	private static class TreeLabelProvider implements ILabelProvider {

		public Image getImage(Object element) {
			if (element instanceof FeatureEntry) {
				return Activator.getDefault().getImageRegistry().get(Activator.FEATURE);
			} else if (element instanceof Bundle) {
				Bundle bundle = (Bundle) element;
				if (isFragment(bundle)) {
					return Activator.getDefault().getImageRegistry().get(Activator.FRAGMENT);
				} else {
					return Activator.getDefault().getImageRegistry().get(Activator.PLUGIN);					
				}
			}
			return null;
		}

		public String getText(Object element) {
			if (element instanceof FeatureEntry) {
				FeatureEntry featureEntry = (FeatureEntry) element;
				return getFeatureId(featureEntry);
			} else if (element instanceof Bundle) {
				Bundle bundle = (Bundle) element;
				return getBundleId(bundle);
			}
			return null;
		}

		public void addListener(ILabelProviderListener listener) {

		}

		public void dispose() {

		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {

		}

	}

	private static class TreeContentProvider implements ITreeContentProvider {
		private final Map<String, FeatureEntry> featureIdToFeatureEntry;
		private final Map<String, SortedSet<String>> featureIdToIncludedFeatureIds;

		private TreeContentProvider(
				Map<String, FeatureEntry> featureIdToFeatureEntry,
				Map<String, SortedSet<String>> featureIdToIncludedFeatureIds) {
			this.featureIdToFeatureEntry = featureIdToFeatureEntry;
			this.featureIdToIncludedFeatureIds = featureIdToIncludedFeatureIds;
		}

		public Object[] getChildren(Object parentElement) {
			if (parentElement instanceof FeatureEntry) {
				FeatureEntry featureEntry = (FeatureEntry) parentElement;
				List<Object> children = new LinkedList<Object>();
				Bundle[] bundles = featureEntry.getBundles();
				Arrays.sort(bundles, new Comparator<Bundle>() {
					public int compare(Bundle bundle1, Bundle bundle2) {
						return bundle1.getSymbolicName().compareTo(
								bundle2.getSymbolicName());
					}
				});
				children.addAll(Arrays.asList(bundles));
				SortedSet<String> includedFeatureIdSet = featureIdToIncludedFeatureIds
						.get(getFeatureId(featureEntry));
				if (includedFeatureIdSet != null) {
					for (String featureId : includedFeatureIdSet) {
						FeatureEntry includedFeatureEntry = featureIdToFeatureEntry
								.get(featureId);
						if (includedFeatureEntry != null) {
							children.add(includedFeatureEntry);
						}
					}
				}
				return children.toArray();
			}
			return new Object[0];
		}

		public Object getParent(Object element) {
			return null;
		}

		public boolean hasChildren(Object element) {
			if (element instanceof FeatureEntry) {
				FeatureEntry featureEntry = (FeatureEntry) element;
				if (featureEntry.getBundles().length > 0) {
					return true;
				}
				if (featureIdToIncludedFeatureIds
						.get(getFeatureId(featureEntry)) != null) {
					return true;
				}
			}
			return false;
		}

		public Object[] getElements(Object inputElement) {
			return (Object[]) inputElement;
		}

		public void dispose() {}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}

	}
	
	private static void printFeatureEntry(FeatureEntry featureEntry,
			MessageConsoleStream messageConsoleStream, String indent,
			Map<String, FeatureEntry> featureIdToFeatureEntry,
			Map<String, SortedSet<String>> featureIdToIncludedFeatureIds) {
		Bundle[] bundles = featureEntry.getBundles();
		String featureId = getFeatureId(featureEntry);
		messageConsoleStream.println(indent + "Feature: " + featureId);
		indent += "  ";
		for (Bundle bundle : bundles) {
			printBundle(bundle, messageConsoleStream, indent);
		}
		SortedSet<String> includedFeatureIds = featureIdToIncludedFeatureIds
				.get(featureId);
		if (includedFeatureIds != null) {
			for (Iterator<String> iterator = includedFeatureIds.iterator(); iterator
					.hasNext();) {
				String includedFeatureId = iterator.next();
				FeatureEntry includedFeatureEntry = featureIdToFeatureEntry
						.get(includedFeatureId);
				if (includedFeatureEntry == null) {
					messageConsoleStream.println(indent + "Feature: "
							+ includedFeatureId);
				} else {
					printFeatureEntry(includedFeatureEntry,
							messageConsoleStream, indent,
							featureIdToFeatureEntry,
							featureIdToIncludedFeatureIds);
				}
			}
		}
	}
	
	private static void printBundle(Bundle bundle, MessageConsoleStream messageConsoleStream, String indent) {
		messageConsoleStream.println(indent
				+ (isFragment(bundle) ? "Fragment: " : "Plugin: ")
				+ getBundleId(bundle));
	}
	
	private static MessageConsole messageConsole;
	
	private static MessageConsole getMessageConsole() {
		if (messageConsole == null) {
			messageConsole = new MessageConsole("Features, Plug-ins and Fragments", null);
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{messageConsole});			
		}
		return messageConsole;
	}
	
	private static boolean isFragment(Bundle bundle) {
		if (bundle.getHeaders().get("Fragment-Host") != null) {
			return true;
		}
		return false;
	}

	private static String getFeatureId(FeatureEntry featureEntry) {
		return featureEntry.getIdentifier() + ":" + featureEntry.getVersion();
	}

	private static String getBundleId(Bundle bundle) {
		return bundle.getSymbolicName() + ":"
				+ bundle.getHeaders().get("Bundle-Version");
	}
}