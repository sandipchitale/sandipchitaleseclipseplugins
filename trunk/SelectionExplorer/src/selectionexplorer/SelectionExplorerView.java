package selectionexplorer;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IMarkSelection;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;

public class SelectionExplorerView extends ViewPart {
	
	private FormToolkit toolkit;
	private ScrolledForm form;
	private FormText partInfo;
	private FormText contextInfo;
	private FormText selectionInfo;
	
	private ISelectionListener listener = new ISelectionListener() {
		@SuppressWarnings("rawtypes")
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (part.getSite().equals(getViewSite())) {
				return;
			}
			if (partInfo == null && selectionInfo == null) {
				return;
			}
			boolean update = false;
			final StringBuilder partInfoStringBuilder = new StringBuilder();
			final StringBuilder contextInfoStringBuilder = new StringBuilder();
			final StringBuilder selectionInfoStringBuilder = new StringBuilder();
			if (partInfo != null) {
				String viewOrEditor = part instanceof IViewPart? "View" : "Editor";
				partInfoStringBuilder.append("<form>");
				
				Class<? extends IWorkbenchPart> partClass = part.getClass();
				partInfoStringBuilder.append( 
						  "<p><b>" + viewOrEditor + ":</b> " + part.getTitle() + "</p>"
						+ "<p><b>ID:</b> " + part.getSite().getId() + "</p>"
						+ "<p><b>Class:</b> " + hyperlink("javatype", partClass.getName()) + "</p>"
						+ (Object.class.equals(partClass.getSuperclass()) ?
								"" :
								"<p><b>Superclass:</b> " + hyperlink("javatype", partClass.getSuperclass().getName()) + "</p>") 
						);
				Class[] ifaces = partClass.getInterfaces();
				if (ifaces.length > 0) {
					partInfoStringBuilder.append("<p><b>Interfaces:</b></p>");
					for (Class iface : ifaces) {
						partInfoStringBuilder.append("<li>" + hyperlink("javatype", iface.getName()) + "</li>");								
					}
				}
				
				String implementingBundle = getImplementingBundle(part);
				if (implementingBundle != null) {
					partInfoStringBuilder.append(
							(implementingBundle == null ? "" : "<p><b>Plug-in:</b> " +  implementingBundle + "</p>"));
				}
				partInfoStringBuilder.append("</form>");
				update = true;
			}

			if (selectionInfo != null) {
				selectionInfoStringBuilder.append("<form>");
				selectionInfoStringBuilder.append("<p><b>Selection Type:</b> ");
				if (selection instanceof ITextSelection) {
					selectionInfoStringBuilder.append("Text");
				} else if (selection instanceof ITreeSelection) {
					selectionInfoStringBuilder.append("Tree");
				} else if (selection instanceof IStructuredSelection) {
					selectionInfoStringBuilder.append("Structured");
				} else if (selection instanceof IMarkSelection) {
					selectionInfoStringBuilder.append("Mark");
				} else if (selection instanceof ISelection){
					selectionInfoStringBuilder.append(selection.getClass().getName());
				}
				selectionInfoStringBuilder.append("</p>");
				if (selection instanceof ITextSelection) {
					ITextSelection textSelection = (ITextSelection) selection;
					selectionInfoStringBuilder.append(
							"<p>"
							+ htmlEscape(textSelection.getText())
							+ "</p>");
				} else if (selection instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
					Iterator iterator = structuredSelection.iterator();
					int i = 0;
					while(iterator.hasNext()) {
						Object next = iterator.next();
						++i;
						Class nextClass = next.getClass();
						Class nextSuperclass = nextClass.getSuperclass();
						selectionInfoStringBuilder.append("<p>_____________________</p><p><b>Item:</b> " + i + "</p>");						
						selectionInfoStringBuilder.append("<p><b>Class:</b> " + hyperlink("javatype", nextClass.getName()) + "</p>");
						if (!Object.class.equals(nextSuperclass)) {							
							selectionInfoStringBuilder.append("<p><b>Superclass:</b> " +  hyperlink("javatype", nextSuperclass.getName()) + "</p>");
						}
						Class[] interfaces = nextClass.getInterfaces();
						if (interfaces.length > 0) {
							selectionInfoStringBuilder.append("<p><b>Interfaces:</b></p>");
							for (Class anInterface : interfaces) {
								selectionInfoStringBuilder.append("<li>" + hyperlink("javatype", anInterface.getName()) + "</li>");								
							}
						}
						String[] adapterTypes = Platform.getAdapterManager().computeAdapterTypes(nextClass);
						if (adapterTypes.length > 0) {
							selectionInfoStringBuilder.append("<p><b>Adapted Types:</b></p>");
							for (String adapterType : adapterTypes) {
								selectionInfoStringBuilder.append("<li>" + hyperlink("javatype", adapterType) + "</li>");								
							}
						}
						selectionInfoStringBuilder.append("<p>__</p>");
						selectionInfoStringBuilder.append(
								"<p>"
								+ htmlEscape(next.toString())
								+ "</p>"
								);
					}
				} else if (selection instanceof IMarkSelection) {
					IMarkSelection markSelection = (IMarkSelection) selection;
					try {
						selectionInfoStringBuilder.append(
								"<p>"
								+ htmlEscape(markSelection.getDocument().get(markSelection.getOffset(), markSelection.getLength()))
								+ "</p>"
								);
					} catch (BadLocationException e) {
					}
				}
				selectionInfoStringBuilder.append("</form>");
				update = true;
			}
			
			if (contextInfo != null) {
				IContextService contextService = (IContextService) getViewSite().getService(IContextService.class);
				Collection activeContextIds = contextService.getActiveContextIds();
				contextInfoStringBuilder.append("<form>");
				contextInfoStringBuilder.append("<p><b>Contexts:</b></p>");
				for (Object activeContextId : activeContextIds) {
					if (!((String)activeContextId).toLowerCase().endsWith("actionset")) {
						Context context = contextService.getContext((String) activeContextId);
						try {
							contextInfoStringBuilder.append("<p>" + context.getName() + " [" +activeContextId + "]</p>");
						} catch (NotDefinedException e) {
						}
					}
				}
				contextInfoStringBuilder.append("<p><b>ActionSet Contexts:</b></p>");
				for (Object activeContextId : activeContextIds) {
					if (((String)activeContextId).toLowerCase().endsWith("actionset")) {
						Context context = contextService.getContext((String) activeContextId);
						try {
							contextInfoStringBuilder.append("<p>" + context.getName() + " [" +activeContextId + "]</p>");
						} catch (NotDefinedException e) {
						}
					}
				}
				contextInfoStringBuilder.append("</form>");
			}
			if (update) {
				UIJob uiJob = new UIJob("") {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						if (partInfo == null && selectionInfo == null) {
							return Status.OK_STATUS;
						}
						if (partInfoStringBuilder.length() > 0) {
							partInfo.setText(partInfoStringBuilder.toString(), true, false);
						}
						if (contextInfoStringBuilder.length() > 0) {
							contextInfo.setText(contextInfoStringBuilder.toString(), true, false);
						}
						if (selectionInfoStringBuilder.length() > 0) {
							selectionInfo.setText(selectionInfoStringBuilder.toString(), true, false);
						}
						form.reflow(true);
						return Status.OK_STATUS;
					}
				};
				uiJob.setPriority(UIJob.INTERACTIVE);
				uiJob.schedule();
			}
		}
	};
	
	private static String hyperlink(String type, String text) {
		return "<a href=\"" + type + "://" + text + "\">" + text + "</a>";
	}
	
	private static String htmlEscape(String text) {
		return text.replaceAll(Pattern.quote("<"), Matcher.quoteReplacement("&lt;"))
				.replaceAll(Pattern.quote(">"), Matcher.quoteReplacement("&gt;"))
				.replaceAll(Pattern.quote("\n"), Matcher.quoteReplacement("<br/>"));
	}
	
	private String getImplementingBundle(IWorkbenchPart workbenchPart) {
		if (workbenchPart != null) {
			IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
			String extensionPointId = null;
			if (workbenchPart instanceof IViewPart) {
				extensionPointId = "org.eclipse.ui.views";
			} else {
				extensionPointId = "org.eclipse.ui.editors";			
			}
			IExtensionPoint extensionPoint = extensionRegistry.getExtensionPoint(extensionPointId);
			if (extensionPoint != null) {
				IExtension[] extensions = extensionPoint.getExtensions();
				for (IExtension extension : extensions) {
					IConfigurationElement[] configurationElements = extension.getConfigurationElements();
					for (IConfigurationElement configurationElement : configurationElements) {
						if (configurationElement.getName().equals("view") || configurationElement.getName().equals("editor")) {
							if (workbenchPart instanceof IViewPart &&
									configurationElement.getName().equals("view") && 
									configurationElement.getAttribute("id").equals(workbenchPart.getSite().getId())) {
								return(extension.getContributor().getName());
							} else if (workbenchPart instanceof IEditorPart &&
									configurationElement.getName().equals("editor") && 
									configurationElement.getAttribute("id").equals(workbenchPart.getSite().getId())) {
								return(extension.getContributor().getName());
							}
						}
					}
				}
			}
		}
		return null;
	}
	
	/**
	 * The constructor.
	 */
	public SelectionExplorerView() {
	}
	
	@Override
	public void init(IViewSite site) throws PartInitException {
		super.init(site);
		site.getWorkbenchWindow().getSelectionService().addSelectionListener(listener);
	}
	
	@Override
	public void dispose() {
		partInfo = null;
		selectionInfo = null;
		getSite().getWorkbenchWindow().getSelectionService().removeSelectionListener(listener);
		toolkit.dispose();
		super.dispose();
	}
	/**
	 * This is a callback that will allow us
	 * to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {
        toolkit = new FormToolkit(parent.getDisplay());
        form = toolkit.createScrolledForm(parent);
    	TableWrapLayout layout = new TableWrapLayout();
    	form.getBody().setLayout(layout);

    	Section partSection = new Section(form.getBody(), Section.TITLE_BAR | Section.TWISTIE);
    	partSection.setText("Part Information");
    	partInfo = toolkit.createFormText(partSection, true);
    	partSection.setClient(partInfo);
    	TableWrapData td = new TableWrapData();
    	td.colspan = 1;
    	td.grabHorizontal = true;
    	partSection.setLayoutData(td);
    	partSection.setExpanded(true);
    	partInfo.addHyperlinkListener(new IHyperlinkListener() {

			public void linkActivated(HyperlinkEvent e) {
				processHyperlink(e.getHref());
			}

			public void linkEntered(HyperlinkEvent e) {
				partInfo.setToolTipText(e.getHref().toString());
			}

			public void linkExited(HyperlinkEvent e) {
				partInfo.setToolTipText(null);				
			}
    		
    	});
    	
    	Section selectionSection = new Section(form.getBody(), Section.TITLE_BAR | Section.TWISTIE);
    	selectionSection.setText("Selection Information");
    	selectionInfo = toolkit.createFormText(selectionSection, true);
    	selectionSection.setClient(selectionInfo);
    	TableWrapData td2 = new TableWrapData();
    	td2.colspan = 1;
    	td2.grabHorizontal = true;
    	selectionSection.setLayoutData(td2);
    	selectionSection.setExpanded(true);
    	selectionInfo.addHyperlinkListener(new IHyperlinkListener() {

			public void linkActivated(HyperlinkEvent e) {
				processHyperlink(e.getHref());
			}

			public void linkEntered(HyperlinkEvent e) {
				selectionInfo.setToolTipText(e.getHref().toString());
			}

			public void linkExited(HyperlinkEvent e) {
				selectionInfo.setToolTipText(null);				
			}
    	});
    	
    	Section contextSection = new Section(form.getBody(), Section.TITLE_BAR | Section.TWISTIE);
    	contextSection.setText("Context Information");
    	contextInfo = toolkit.createFormText(contextSection, true);
    	contextSection.setClient(contextInfo);
    	TableWrapData td1 = new TableWrapData();
    	td1.colspan = 1;
    	td1.grabHorizontal = true;
    	contextSection.setLayoutData(td1);
    	contextSection.setExpanded(false);
    	contextInfo.addHyperlinkListener(new IHyperlinkListener() {

			public void linkActivated(HyperlinkEvent e) {
				processHyperlink(e.getHref());
			}

			public void linkEntered(HyperlinkEvent e) {
				contextInfo.setToolTipText(e.getHref().toString());
			}

			public void linkExited(HyperlinkEvent e) {
				contextInfo.setToolTipText(null);				
			}
    	});
    	
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
	}

	private void processHyperlink(Object object) {
		if (object instanceof String) {
			String href = (String) object;
			if (href.startsWith("javatype://")) {
				Utils.openType(getViewSite().getShell(), href.substring(11));
			} else if (href.startsWith("bundle://")) {
				
			}
		}
	}
}