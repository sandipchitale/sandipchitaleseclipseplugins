package codeclips.actions;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.texteditor.ITextEditor;

import codeclips.Activator;

public class ManageCodesClipsDialog extends TitleAreaDialog{

	private String abbrev;
	private String expansion;
	private StyledText expansionText;
	private final ITextEditor textEditor;
	private Text abbrevText;
	private Button newButton;
	private Button modifyButton;
	private Button deleteButton;
	private TemplateStore templateStore;

	public ManageCodesClipsDialog(Shell shell, ITextEditor textEditor) {
		super(shell);
		this.textEditor = textEditor;
		abbrev = "";
		this.expansion = expansion;
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
        
        GridLayout gridLayout = new GridLayout(1, false);
		parentComposite.setLayout(gridLayout);
		
		gridLayout.marginWidth = 10;
		gridLayout.marginHeight = 10;
		gridLayout.horizontalSpacing = 5;
		gridLayout.verticalSpacing = 5;
        
        Label abbrevLabel = new Label(parentComposite, SWT.NONE);
        abbrevLabel.setText("Clips:");
		GridData abbrevLabelGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		abbrevLabel.setLayoutData(abbrevLabelGridData);
		
		return parentComposite;
	}
	
	public String getExpansion() {
		return expansion;
	}
	
	public String getAbbrev() {
		return abbrev;
	}
	
	@Override
	protected void okPressed() {
		abbrev = abbrevText.getText();
		expansion = expansionText.getText();
		super.okPressed();
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		newButton = createButton(parent, IDialogConstants.CLIENT_ID, "Add...", false);
		modifyButton = createButton(parent, IDialogConstants.CLIENT_ID+1, "Modify...", true);
		deleteButton = createButton(parent, IDialogConstants.CLIENT_ID+2, "Delete", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CLOSE_LABEL, false);
	}
	
	void setExpansion(String expansion) {
		if (expansionText == null) {
			this.expansion = expansion;
		} else {
			expansionText.setText(expansion);
			expansion = null;
		}
	}	
}
