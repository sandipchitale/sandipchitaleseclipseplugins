package eclipsemate;

import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

public class FilterThroughCommandDialog extends Dialog {

	@SuppressWarnings("unused")
	private Map<String, String> environment;

	protected FilterThroughCommandDialog(Shell parentShell) {
		super(parentShell);
	}
	
	@Override
	protected boolean isResizable() {
		return true;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout compositeGridLayout = new GridLayout(3, false);
		composite.setLayout(compositeGridLayout);

		Label commandLabel = new Label(composite, SWT.TRAIL);
		commandLabel.setText("Command:");
		GridData commandLabelGridData = new GridData(SWT.LEAD, SWT.TOP, false, false);
		commandLabel.setLayoutData(commandLabelGridData);
		
		Combo commandCombo = new Combo(composite, SWT.NONE);
		GridData commandComboGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		commandComboGridData.horizontalSpan = 2;
		commandCombo.setLayoutData(commandComboGridData);
		
		
		Label padding = new Label(composite, SWT.NONE);
		GridData paddingGridData = new GridData(SWT.LEAD, SWT.TOP, false, false);
		padding.setLayoutData(paddingGridData);
		
		Group inputGroup = new Group(composite, SWT.NONE);
		inputGroup.setText("Input");
		GridData inputGroupGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		inputGroup.setLayoutData(inputGroupGridData);
		
		inputGroup.setLayout(new RowLayout(SWT.VERTICAL));

		Button noneButton = new Button(inputGroup, SWT.RADIO);
		noneButton.setText("None");
		Button selectionButton = new Button(inputGroup, SWT.RADIO);
		selectionButton.setText("Selection");
		Button selectionLinesButton = new Button(inputGroup, SWT.RADIO);
		selectionLinesButton.setText("Selection Lines");
		Button documentButton = new Button(inputGroup, SWT.RADIO);
		documentButton.setText("Document");
		Button lineButton = new Button(inputGroup, SWT.RADIO);
		lineButton.setText("Line");
		Button wordButton = new Button(inputGroup, SWT.RADIO);
		wordButton.setText("Word");
		
		Group outputGroup = new Group(composite, SWT.NONE);
		outputGroup.setText("Output");
		GridData outputGroupGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		outputGroupGridData.verticalSpan = 2;
		outputGroup.setLayoutData(outputGroupGridData);
		
		outputGroup.setLayout(new RowLayout(SWT.VERTICAL));
		
		Button discardButton = new Button(outputGroup, SWT.RADIO);
		discardButton.setText("Discard");
		Button replaceSelectionButton = new Button(outputGroup, SWT.RADIO);
		replaceSelectionButton.setText("Replace Selection");
		Button replaceSelectedLinesButton = new Button(outputGroup, SWT.RADIO);
		replaceSelectedLinesButton.setText("Replace Selected Lines");
		Button replaceLineButton = new Button(outputGroup, SWT.RADIO);
		replaceLineButton.setText("Replace Line");
		Button replaceWordButton = new Button(outputGroup, SWT.RADIO);
		replaceWordButton.setText("Replace Word");
		Button replaceDocumentButton = new Button(outputGroup, SWT.RADIO);
		replaceDocumentButton.setText("Replace Document");
		Button insertAsTextButton = new Button(outputGroup, SWT.RADIO);
		insertAsTextButton.setText("Insert as Text");
		Button insertAsTemplateButton = new Button(outputGroup, SWT.RADIO);
		insertAsTemplateButton.setText("Insert as Template");
		Button showAsHTMLButton = new Button(outputGroup, SWT.RADIO);
		showAsHTMLButton.setText("Show as HTML");
		Button showAsToolTipButton = new Button(outputGroup, SWT.RADIO);
		showAsToolTipButton.setText("Show as Tool Tip");
		Button createNewDocumentButton = new Button(outputGroup, SWT.RADIO);
		createNewDocumentButton.setText("Create New Document");
		Button outputToConsoleDocumentButton1 = new Button(outputGroup, SWT.RADIO);
		outputToConsoleDocumentButton1.setText("Ouput to Console");
		
		padding = new Label(composite, SWT.NONE);
		paddingGridData = new GridData(SWT.LEAD, SWT.TOP, false, false);
		padding.setLayoutData(paddingGridData);

		Button showEnvironmentButton = new Button(composite, SWT.PUSH);
		showEnvironmentButton.setText("Show Environment...");
		GridData showEnvironmentButtonGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		showEnvironmentButton.setLayoutData(showEnvironmentButtonGridData);
		
		return composite;
	}

	public void setEnvironment(Map<String, String> environment) {
		this.environment = environment;		
	}

}
