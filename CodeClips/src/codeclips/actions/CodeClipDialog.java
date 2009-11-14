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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.texteditor.ITextEditor;

import codeclips.Activator;

public class CodeClipDialog extends TitleAreaDialog{

	private String abbrev;
	private String expansion;
	private StyledText expansionText;
	private final ITextEditor textEditor;
	private Text abbrevText;
	private Button manageButton;
	private Button saveUpdateButton;
	private TemplateStore templateStore;

	public CodeClipDialog(ITextEditor textEditor) {
		this(textEditor, "");
	}

	public CodeClipDialog(ITextEditor textEditor, String expansion) {
		super(textEditor.getSite().getShell());
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
		setTitle("Code Clip");
		setMessage("Create/Update a Code Clip");
        Composite parentComposite = (Composite) super.createDialogArea(parent);
        
        GridLayout gridLayout = new GridLayout(1, false);
		parentComposite.setLayout(gridLayout);
		
		gridLayout.marginWidth = 10;
		gridLayout.marginHeight = 10;
		gridLayout.horizontalSpacing = 5;
		gridLayout.verticalSpacing = 5;
        
        Label abbrevLabel = new Label(parentComposite, SWT.NONE);
        abbrevLabel.setText("Clip Name:");
		GridData abbrevLabelGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		abbrevLabel.setLayoutData(abbrevLabelGridData);
		
        abbrevText = new Text(parentComposite, SWT.SINGLE | SWT.BORDER);
        abbrevText.setText(abbrev);
		GridData abbrevTextGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		abbrevText.setLayoutData(abbrevTextGridData);
		
		abbrevText.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent e) {
				Template existingTemplate = templateStore.findTemplate(abbrevText.getText());
				if (existingTemplate == null) {
					saveUpdateButton.setText("Update");					
				} else {
					saveUpdateButton.setText("Save");
				}
			}
		});
        
        Label expansionLabel = new Label(parentComposite, SWT.NONE);
        expansionLabel.setText("Clip Text:");
		GridData expansionLabelGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		expansionLabel.setLayoutData(expansionLabelGridData);
		
		expansionText = new StyledText(parentComposite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		expansionText.setText(expansion);
		expansionText.setFont(JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT));
		GridData styledTextGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		styledTextGridData.heightHint = expansionText.getLineHeight() * 8;
		expansionText.setLayoutData(styledTextGridData);
		
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
		manageButton = createButton(parent, IDialogConstants.CLIENT_ID, "Manage...", false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
		saveUpdateButton = createButton(parent, IDialogConstants.OK_ID, "Save", true);
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
