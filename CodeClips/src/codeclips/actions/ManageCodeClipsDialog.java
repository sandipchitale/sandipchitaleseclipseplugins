package codeclips.actions;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.texteditor.ITextEditor;

import codeclips.Activator;

public class ManageCodeClipsDialog extends TitleAreaDialog{

	private final Shell contextShell;

	private static final int ADD_ID = IDialogConstants.CLIENT_ID;
	private static final int MODIFY_ID = ADD_ID+1;
	private static final int DELETE_ID = MODIFY_ID+1;

	private Button newButton;
	private Button modifyButton;
	private Button deleteButton;
	private TemplateStore templateStore;

	private TableViewer tableViewer;

	private static class CodeClipsProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return ((TemplateStore)inputElement).getTemplateData(false);
		}

		public void dispose() {
		}

		public void inputChanged(final Viewer viewer, Object oldInput, final Object newInput) {
			UIJob job = new UIJob(viewer.getControl().getDisplay(), "") {
				@Override
				public IStatus runInUIThread(IProgressMonitor monitor) {
					if (!viewer.getControl().isDisposed()) {
						viewer.refresh();
					}
					return Status.OK_STATUS;
				}
			};
			job.setSystem(true);
			job.schedule();
		}
	}

	private static class CodeClipLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			TemplatePersistenceData templatePersistenceData = (TemplatePersistenceData) element;
			Template template = templatePersistenceData.getTemplate();
			switch (columnIndex) {
			case 0:
				return template.getName();
			case 1:
				return template.getDescription();
			default:
				throw new RuntimeException("Should not happen");
			}

		}
	}

	public ManageCodeClipsDialog(Shell parentShell, Shell contextShell) {
		super(parentShell);
		this.contextShell = contextShell;
		templateStore = Activator.getDefault().getTemplateStore();
		setHelpAvailable(false);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);

		tableViewer.setInput(templateStore);
		adjustButtonState();

		return contents;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Manage Code Clips");
        Composite parentComposite = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) parentComposite.getLayout();

		gridLayout.marginWidth = 10;
		gridLayout.marginHeight = 10;
		gridLayout.horizontalSpacing = 5;
		gridLayout.verticalSpacing = 5;

        Label abbrevLabel = new Label(parentComposite, SWT.NONE);
        abbrevLabel.setText("Code Clips:");
		GridData abbrevLabelGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		abbrevLabel.setLayoutData(abbrevLabelGridData);

		tableViewer = new TableViewer(parentComposite);

		Table table = tableViewer.getTable();
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 200;
		table.setLayoutData(data);

	    TableLayout layout = new TableLayout();
	    layout.addColumnData(new ColumnWeightData(20, 75, true));
	    layout.addColumnData(new ColumnWeightData(80, 75, true));
		table.setLayout(layout);

		TableViewerColumn abbrevColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
	    abbrevColumn.getColumn().setText("Abbreviation");
	    TableViewerColumn descriptionColumn = new TableViewerColumn(tableViewer, SWT.LEFT);
	    descriptionColumn.getColumn().setText("Description");

	    table.setHeaderVisible(true);
		table.setLinesVisible(true);

	    tableViewer.setContentProvider(new CodeClipsProvider());
	    tableViewer.setLabelProvider(new CodeClipLabelProvider());

	    tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				adjustButtonState();
			}
		});

		return parentComposite;
	}

	protected void adjustButtonState() {
		modifyButton.setEnabled(true);
		deleteButton.setEnabled(true);
		ISelection selection = tableViewer.getSelection();
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structuredSelection = (IStructuredSelection) selection;
			if (structuredSelection.isEmpty()) {
				modifyButton.setEnabled(false);
				deleteButton.setEnabled(false);
				return;
			}
			if (structuredSelection.size() > 1) {
				modifyButton.setEnabled(false);
				return;
			}
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		newButton = createButton(parent, ADD_ID, "Add...", false);
		newButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				CodeClipDialog codeClipDialog = new CodeClipDialog(getShell(), contextShell);
				if (Window.OK == codeClipDialog.open()) {
					Activator.getDefault().persistTemplate(codeClipDialog.getAbbrev(), codeClipDialog.getDescription(), codeClipDialog.getExpansion());
					tableViewer.setInput(templateStore);
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		modifyButton = createButton(parent,MODIFY_ID, "Modify...", true);
		modifyButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = tableViewer.getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
					if (!structuredSelection.isEmpty()) {
						TemplatePersistenceData templatePersistenceData = (TemplatePersistenceData) structuredSelection.getFirstElement();
						CodeClipDialog codeClipDialog = new CodeClipDialog(getShell(), contextShell, templatePersistenceData);
						if (Window.OK == codeClipDialog.open()) {
							Template modifiedTemplate = new Template(codeClipDialog.getAbbrev(),
									codeClipDialog.getDescription(),
									codeClipDialog.getContentType(),
									codeClipDialog.getExpansion(),
									true);
							templatePersistenceData.setTemplate(modifiedTemplate);
							Activator.getDefault().persistTemplatePersistenceData(templatePersistenceData);
							tableViewer.setInput(templateStore);
						}
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		deleteButton = createButton(parent, DELETE_ID, "Delete", false);
		deleteButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				ISelection selection = tableViewer.getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection structuredSelection = (IStructuredSelection) selection;
					if (!structuredSelection.isEmpty()) {
						templateStore.delete((TemplatePersistenceData) structuredSelection.getFirstElement());
						try {
							templateStore.save();
						} catch (IOException e1) {
							Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e1.getMessage(), e1));
						}
						tableViewer.setInput(templateStore);
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, true);
	}

}
