package eclipseinfo;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.core.internal.content.ContentTypeManager;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

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
		// Extension points
		// Extensions		
		
		// Perspectives
		// Views
		// Editors
		contentTypes(out);
		out.flush(); 
	}

	public void selectionChanged(IAction action, ISelection selection) {
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
