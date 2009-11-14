package codeclips.actions;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.texteditor.ITextEditor;

import codeclips.Activator;

public class ManageCodesClipsDialog extends TitleAreaDialog{

	private final ITextEditor textEditor;

	private Button newButton;
	private Button modifyButton;
	private Button deleteButton;
	private TemplateStore templateStore;

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
	protected Control createDialogArea(Composite parent) {
		setTitle("Manage Code Clips");
        Composite parentComposite = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = (GridLayout) parentComposite.getLayout();
		
		gridLayout.marginWidth = 10;
		gridLayout.marginHeight = 10;
		gridLayout.horizontalSpacing = 5;
		gridLayout.verticalSpacing = 5;
        
        Label abbrevLabel = new Label(parentComposite, SWT.NONE);
        abbrevLabel.setText("Clips:");
		GridData abbrevLabelGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		abbrevLabel.setLayoutData(abbrevLabelGridData);
		
		Table table = new Table (parentComposite, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
		table.setLinesVisible (true);
		table.setHeaderVisible (true);
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 200;
		table.setLayoutData(data);
		String[] titles = {"Abbrev", "Description"};
		for (int i=0; i<titles.length; i++) {
			TableColumn column = new TableColumn (table, SWT.NONE);
			column.setText(titles [i]);
		}
		
		Template[] templates = templateStore.getTemplates();
		for (Template template : templates) {
			TableItem item = new TableItem (table, SWT.NONE);
			item.setText (0, template.getName());
			item.setText (1, template.getDescription());
		}
		for (int i=0; i<titles.length; i++) {
			table.getColumn (i).pack ();
		}
		return parentComposite;
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
