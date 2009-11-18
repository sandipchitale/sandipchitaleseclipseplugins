package eclipseinfo;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.internal.content.ContentTypeManager;
import org.eclipse.core.runtime.IBundleGroup;
import org.eclipse.core.runtime.IBundleGroupProvider;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorRegistry;
import org.eclipse.ui.IFileEditorMapping;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;
import org.osgi.framework.Bundle;

public class Info implements IWorkbenchWindowActionDelegate {

	public void dispose() {
	}

	public void init(IWorkbenchWindow window) {
	}

	public void run(IAction action) {
		PrintWriter out = new PrintWriter(System.out);
		// Fonts
		// System Colors
		
		// Features
		// Plugins
		bundles(out);
		// Extension points
		// Extensions		
		
		// Perspectives
		perspectives(out);
		// Views
		views(out);
		// Editors
		editors(out);
		
		contentTypes(out);
		out.flush(); 
	}

	public void selectionChanged(IAction action, ISelection selection) {
	}
	
	private static void bundles(PrintWriter out) {
		String header = "Bundles aka Plug-ins:";
		header(out, header);
		IBundleGroupProvider[] bundleGroupProviders = Platform.getBundleGroupProviders();
		for (IBundleGroupProvider bundleGroupProvider : bundleGroupProviders) {
			IBundleGroup[] bundleGroups = bundleGroupProvider.getBundleGroups();
			for (IBundleGroup bundleGroup : bundleGroups) {
				Bundle[] bundles = bundleGroup.getBundles();
				for (Bundle bundle : bundles) {
				}
			}
		}
		footer(out, header);
	}	

	private static void perspectives(PrintWriter out) {
		String header = "Perspectives";
		header(out, header);
		IPerspectiveRegistry perspectiveRegistry = PlatformUI.getWorkbench().getPerspectiveRegistry();
		IPerspectiveDescriptor[] perspectives = perspectiveRegistry.getPerspectives();
		SortedSet<IPerspectiveDescriptor> sortedPerspectiveDescriptors = new TreeSet<IPerspectiveDescriptor>(
				new Comparator<IPerspectiveDescriptor>() {
					public int compare(IPerspectiveDescriptor o1, IPerspectiveDescriptor o2) {
						return o1.getId().compareTo(o2.getId());
					}
				}
				);
		sortedPerspectiveDescriptors.addAll(Arrays.asList(perspectives));
		
		for (IPerspectiveDescriptor perspectiveDescriptor : sortedPerspectiveDescriptors) {
			out.println(
					perspectiveDescriptor.getId()
					+ " "
					+ perspectiveDescriptor.getLabel()
					+ " "
					+ perspectiveDescriptor.getDescription()
					);
		}

		footer(out, header);
	}

	private static void views(PrintWriter out) {
		String header = "Views";
		header(out, header);
		IViewRegistry viewRegistry = PlatformUI.getWorkbench().getViewRegistry();
		IViewDescriptor[] viewDescriptors = viewRegistry.getViews();
		SortedSet<IViewDescriptor> sortedViewDescriptors = new TreeSet<IViewDescriptor>(
				new Comparator<IViewDescriptor>() {
					public int compare(IViewDescriptor o1, IViewDescriptor o2) {
						return o1.getId().compareTo(o2.getId());
					}
				}
				);
		sortedViewDescriptors.addAll(Arrays.asList(viewDescriptors));
		
		for (IViewDescriptor viewDescriptor : sortedViewDescriptors) {
			out.println(
					viewDescriptor.getId()
					+ " "
					+ viewDescriptor.getLabel()
					+ " "
					+ viewDescriptor.getDescription()
					);
		}

		footer(out, header);
	}
	
	@SuppressWarnings("deprecation")
	private static void editors(PrintWriter out) {
		String header = "Editors";
		header(out, header);
		IEditorRegistry editorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
		IFileEditorMapping[] fileEditorMappings = editorRegistry.getFileEditorMappings();
		IEditorDescriptor defaultEditorDescriptor = editorRegistry.getDefaultEditor();
		out.println(defaultEditorDescriptor.getId()
				+ " "
				+ defaultEditorDescriptor.getLabel()
		);
		for (IFileEditorMapping fileEditorMapping : fileEditorMappings) {
			IEditorDescriptor[] editorDescriptors = fileEditorMapping.getEditors();
			SortedSet<IEditorDescriptor> sortedContentTypes = new TreeSet<IEditorDescriptor>(
					new Comparator<IEditorDescriptor>() {
						public int compare(IEditorDescriptor o1, IEditorDescriptor o2) {
							return o1.getId().compareTo(o2.getId());
						}
					}
					);
			sortedContentTypes.addAll(Arrays.asList(editorDescriptors));
			
			for (IEditorDescriptor editorDescriptor : sortedContentTypes) {
				out.println(
						fileEditorMapping.getExtension()
						+ " "
						+ fileEditorMapping.getLabel()
						+ " "						
						+ editorDescriptor.getId()
						+ " "
						+ editorDescriptor.getLabel()
						);
			}
		}
		
		footer(out, header);
	}
	
	private static void contentTypes(PrintWriter out) {
		String header = "Content Types";
		header(out, header);
		ContentTypeManager contentTypeManager = ContentTypeManager.getInstance();
		IContentType[] contentTypes = contentTypeManager.getAllContentTypes();
		SortedSet<IContentType> sortedContentTypes = new TreeSet<IContentType>(
				new Comparator<IContentType>() {

					public int compare(IContentType o1, IContentType o2) {
						return o1.getId().compareTo(o2.getId());
					}
				}
				);
		sortedContentTypes.addAll(Arrays.asList(contentTypes));
		
		for (IContentType contentType : sortedContentTypes) {
			IContentType baseContentType = contentType.getBaseType();
			out.println(
					contentType.getId()
					+ " "
					+ contentType.getName()
					+ (baseContentType == null ? "": " Base Type: " + baseContentType.getId())
					);
		}
		footer(out, header);
	}

	private static void header(PrintWriter out, String string) {
		out.println("====== " + string + " ======");
	}
	

	private static void footer(PrintWriter out, String string) {
		out.println("======");
	}
}
