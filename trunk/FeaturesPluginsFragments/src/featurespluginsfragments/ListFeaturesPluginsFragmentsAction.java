package featurespluginsfragments;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.IDialogConstants;
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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.update.internal.configurator.FeatureEntry;
import org.osgi.framework.Bundle;

@SuppressWarnings("restriction")
public class ListFeaturesPluginsFragmentsAction implements IWorkbenchWindowActionDelegate {

	private IWorkbenchWindow window;

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
		this.window = window;
	}

	public void run(IAction action) {
		Info info = new Info();
		// List to console
		Set<FeatureEntry> topLevelFeatureEntries = info.getTopLevelFeatureEntries();
		FeatureEntry[] featureEntryArray = topLevelFeatureEntries.toArray(new FeatureEntry[topLevelFeatureEntries.size()]);
		Collection<Bundle> orphanBundles = info.getOrphanBundles();

		List<Object> objects = new LinkedList<Object>();
		objects.addAll(topLevelFeatureEntries);
		objects.addAll(orphanBundles);

		FESD fesd = new FESD(window.getShell(), new TreeLabelProvider(), new TreeContentProvider(info.getFeatureIdToFeatureEntry(), info.getFeatureIdToIncludedFeatureIds()));
		fesd.setHelpAvailable(false);
		fesd.setMessage("Features, Plug-ins and Fragments");
		fesd.setTitle("Features, Plug-ins and Fragments");
		fesd.setInput(objects.toArray());
		fesd.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}

	private static class FESD extends FilteredElementTreeSelectionDialog {

		FESD(Shell parent, ILabelProvider labelProvider, ITreeContentProvider contentProvider) {
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

		/*
		 * (non-Javadoc) Method declared on Dialog.
		 */
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL,
					true);
		}
	}

	private static class TreeLabelProvider implements ILabelProvider {

		public Image getImage(Object element) {
			if (element instanceof FeatureEntry) {
				return Activator.getDefault().getImageRegistry().get(Activator.FEATURE);
			} else if (element instanceof Bundle) {
				Bundle bundle = (Bundle) element;
				if (Utils.isFragment(bundle)) {
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
				return Utils.getFeatureId(featureEntry);
			} else if (element instanceof Bundle) {
				Bundle bundle = (Bundle) element;
				return Utils.getBundleId(bundle);
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

		private TreeContentProvider(Map<String, FeatureEntry> featureIdToFeatureEntry, Map<String, SortedSet<String>> featureIdToIncludedFeatureIds) {
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
						return bundle1.getSymbolicName().compareTo(bundle2.getSymbolicName());
					}
				});
				children.addAll(Arrays.asList(bundles));
				SortedSet<String> includedFeatureIdSet = featureIdToIncludedFeatureIds.get(Utils.getFeatureId(featureEntry));
				if (includedFeatureIdSet != null) {
					for (String featureId : includedFeatureIdSet) {
						FeatureEntry includedFeatureEntry = featureIdToFeatureEntry.get(featureId);
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
				if (featureIdToIncludedFeatureIds.get(Utils.getFeatureId(featureEntry)) != null) {
					return true;
				}
			}
			return false;
		}

		public Object[] getElements(Object inputElement) {
			return (Object[]) inputElement;
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

}