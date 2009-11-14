package codeclips.actions;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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

public class CodeClipDialog extends TitleAreaDialog{

	private final ITextEditor textEditor;

	private String abbrev;
	private String description;
	private String expansion;

	private Text abbrevText;
	private Text descriptionText;
	private StyledText expansionText;
		
	private final Template template;

	public CodeClipDialog(Shell shell, ITextEditor textEditor, Template template) {
		super(shell);
		this.textEditor = textEditor;
		this.template = template;
		abbrev = template.getName();
		description = template.getDescription();
		expansion = template.getPattern();
	}
	
	public CodeClipDialog(Shell shell, ITextEditor textEditor) {
		super(shell);
		this.textEditor = textEditor;
		this.template = null;
		abbrev = "";
		description = "";
		expansion = "";
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle("Code Clip");
		setMessage((template == null ? "Create a new" : "Modify the") + " Code Clip");
        Composite parentComposite = (Composite) super.createDialogArea(parent);
        
        GridData layoutData = (GridData) parentComposite.getChildren()[0].getLayoutData();
        layoutData.horizontalSpan = 2;
        
        GridLayout gridLayout = new GridLayout(2, false);
		parentComposite.setLayout(gridLayout);
		
		gridLayout.marginWidth = 10;
		gridLayout.marginHeight = 10;
		gridLayout.horizontalSpacing = 5;
		gridLayout.verticalSpacing = 5;
        
        Label abbrevLabel = new Label(parentComposite, SWT.NONE);
        abbrevLabel.setText("Clip Name:");
		GridData abbrevLabelGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		abbrevLabel.setLayoutData(abbrevLabelGridData);
		
		Label descriptionLabel = new Label(parentComposite, SWT.NONE);
        descriptionLabel.setText("Description:");
		GridData descriptionLabelGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		descriptionLabel.setLayoutData(descriptionLabelGridData);
		
		abbrevText = new Text(parentComposite, SWT.SINGLE | SWT.BORDER);
        abbrevText.setText(abbrev);
        abbrevText.setEditable(template == null);
		GridData abbrevTextGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		abbrevText.setLayoutData(abbrevTextGridData);
		
		abbrevText.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				// TODO validate
			}
		});
		
        descriptionText = new Text(parentComposite, SWT.SINGLE | SWT.BORDER);
        descriptionText.setText(description);
		GridData descriptionTextGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		descriptionText.setLayoutData(descriptionTextGridData);
		
        Label expansionLabel = new Label(parentComposite, SWT.NONE);
        expansionLabel.setText("Clip Text:");
		GridData expansionLabelGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		expansionLabelGridData.horizontalSpan = 2;
		expansionLabel.setLayoutData(expansionLabelGridData);
		
		expansionText = new StyledText(parentComposite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		expansionText.setText(expansion);
		expansionText.setFont(JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT));
		GridData styledTextGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		styledTextGridData.heightHint = expansionText.getLineHeight() * 8;
		styledTextGridData.horizontalSpan = 2;
		expansionText.setLayoutData(styledTextGridData);
		
		if (template == null) {
			abbrevText.setFocus();
		} else {
			descriptionText.setFocus();
		}
		return parentComposite;
	}
	
	public String getAbbrev() {
		return abbrev;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getExpansion() {
		return expansion;
	}
	
	@Override
	protected void okPressed() {
		abbrev = abbrevText.getText();
		description = descriptionText.getText();
		expansion = expansionText.getText();
		super.okPressed();
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (getParentShell() == textEditor.getSite().getShell()) {
			Button manageButton = createButton(parent, IDialogConstants.CLIENT_ID, "Manage...", false);
			manageButton.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					new ManageCodesClipsDialog(getShell(), textEditor).open();
				}
				
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			});
		}
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		createButton(parent, IDialogConstants.OK_ID, (template == null ? "Create" : "Update"), true);
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
