package codeclips.actions;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.text.templates.Template;
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
import org.eclipse.ui.texteditor.ITextEditor;

import codeclips.Activator;

public class ManageCodesClipsDialog extends TitleAreaDialog{

	private final ITextEditor textEditor;

	private Button newButton;
	private Button modifyButton;
	private Button deleteButton;
	private TemplateStore templateStore;

	private TableViewer tableViewer;
	
	private class CodeClipsProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			return ((TemplateStore)inputElement).getTemplates();
		}

		public void dispose() {
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}

	}

	private static class CodeClipLabelProvider extends LabelProvider implements ITableLabelProvider {
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		
		public String getColumnText(Object element, int columnIndex) {
			Template template = (Template) element;
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

	public ManageCodesClipsDialog(Shell shell, ITextEditor textEditor) {
		super(shell);
		this.textEditor = textEditor;
		templateStore = Activator.getDefault().getTemplateStore();
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
			}
		}
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		newButton = createButton(parent, IDialogConstants.CLIENT_ID, "Add...", false);
		newButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				CodeClipDialog codeClipDialog = new CodeClipDialog(getShell(), textEditor);
				if (Window.OK == codeClipDialog.open()) {			
					Activator.getDefault().persistTemplate(codeClipDialog.getAbbrev(), codeClipDialog.getDescription(), codeClipDialog.getExpansion());
				}
			} 
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		modifyButton = createButton(parent, IDialogConstants.CLIENT_ID+1, "Modify...", true);
		modifyButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				CodeClipDialog codeClipDialog = new CodeClipDialog(getShell(), textEditor, new Template("","","","",true));
				if (Window.OK == codeClipDialog.open()) {			
					Activator.getDefault().persistTemplate(codeClipDialog.getAbbrev(), codeClipDialog.getDescription(), codeClipDialog.getExpansion());
				}
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		deleteButton = createButton(parent, IDialogConstants.CLIENT_ID+2, "Delete", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
	}
	
}
