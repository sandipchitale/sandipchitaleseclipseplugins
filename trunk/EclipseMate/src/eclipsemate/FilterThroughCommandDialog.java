package eclipsemate;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import org.eclipse.swt.widgets.Text;

public class FilterThroughCommandDialog extends Dialog {

	@SuppressWarnings("unused")
	private Map<String, String> environment;
	private Combo commandCombo;
	private String command;
	private static List<String> lastCommands = new LinkedList<String>();
	
	private Button noneButton;
	private Button selectionButton;
	private Button selectionLinesButton;
	private Button documentButton;
	private Button lineButton;
	private Button wordButton;
	
	private Filter.INPUT_TYPE inputType;
	private static Filter.INPUT_TYPE lastInputType;

	private Button discardButton;
	private Button outputToConsoleButton;
	private Text   consoleNameText;
	private Button replaceSelectionButton;
	private Button replaceSelectedLinesButton;
	private Button replaceLineButton;
	private Button replaceWordButton;
	private Button replaceDocumentButton;
	private Button insertAsTextButton;
	private Button insertAsTemplateButton;
	private Button showAsHTMLButton;
	private Button showAsToolTipButton;
	private Button createNewDocumentButton;

	private Filter.OUTPUT_TYPE outputType;
	private static Filter.OUTPUT_TYPE lastOutputType;

	private String consoleName;
	private static String lastConsoleName;

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
		
		commandCombo = new Combo(composite, SWT.NONE);
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

		noneButton = new Button(inputGroup, SWT.RADIO);
		noneButton.setText("None");
		selectionButton = new Button(inputGroup, SWT.RADIO);
		selectionButton.setText("Selection");
		selectionLinesButton = new Button(inputGroup, SWT.RADIO);
		selectionLinesButton.setText("Selection Lines");
		documentButton = new Button(inputGroup, SWT.RADIO);
		documentButton.setText("Document");
		lineButton = new Button(inputGroup, SWT.RADIO);
		lineButton.setText("Line");
		lineButton.setSelection(true);
		wordButton = new Button(inputGroup, SWT.RADIO);
		wordButton.setText("Word");
		
		Group outputGroup = new Group(composite, SWT.NONE);
		outputGroup.setText("Output");
		GridData outputGroupGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		outputGroupGridData.verticalSpan = 2;
		outputGroup.setLayoutData(outputGroupGridData);
		
		outputGroup.setLayout(new RowLayout(SWT.VERTICAL));
		
		discardButton = new Button(outputGroup, SWT.RADIO);
		discardButton.setText("Discard");
		replaceSelectionButton = new Button(outputGroup, SWT.RADIO);
		replaceSelectionButton.setText("Replace Selection");
		replaceSelectedLinesButton = new Button(outputGroup, SWT.RADIO);
		replaceSelectedLinesButton.setText("Replace Selected Lines");
		replaceLineButton = new Button(outputGroup, SWT.RADIO);
		replaceLineButton.setText("Replace Line");
		replaceWordButton = new Button(outputGroup, SWT.RADIO);
		replaceWordButton.setText("Replace Word");
		replaceDocumentButton = new Button(outputGroup, SWT.RADIO);
		replaceDocumentButton.setText("Replace Document");
		insertAsTextButton = new Button(outputGroup, SWT.RADIO);
		insertAsTextButton.setText("Insert as Text");
		insertAsTemplateButton = new Button(outputGroup, SWT.RADIO);
		insertAsTemplateButton.setText("Insert as Template");
		showAsHTMLButton = new Button(outputGroup, SWT.RADIO);
		showAsHTMLButton.setText("Show as HTML");
		showAsToolTipButton = new Button(outputGroup, SWT.RADIO);
		showAsToolTipButton.setText("Show as Tool Tip");
		createNewDocumentButton = new Button(outputGroup, SWT.RADIO);
		createNewDocumentButton.setText("Create New Document");
		outputToConsoleButton = new Button(outputGroup, SWT.RADIO);
		outputToConsoleButton.setText("Ouput to Console");
		outputToConsoleButton.setSelection(true);
		
		consoleNameText = new Text(outputGroup, SWT.BORDER);
		consoleNameText.setText(Filter.DEFAULT_CONSOLE_NAME);
		
		outputToConsoleButton.addSelectionListener(new SelectionListener() {			
			public void widgetSelected(SelectionEvent e) {
				consoleNameText.setEnabled(outputToConsoleButton.getSelection());
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
//		RowData rowData = new RowData();
//		rowData.
		
		padding = new Label(composite, SWT.NONE);
		paddingGridData = new GridData(SWT.LEAD, SWT.TOP, false, false);
		padding.setLayoutData(paddingGridData);

		Button showEnvironmentButton = new Button(composite, SWT.PUSH);
		showEnvironmentButton.setText("Show Environment...");
		GridData showEnvironmentButtonGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		showEnvironmentButton.setLayoutData(showEnvironmentButtonGridData);
		showEnvironmentButton.addSelectionListener(new SelectionListener() {			
			public void widgetSelected(SelectionEvent e) {
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		
		return composite;
	}

	public void setEnvironment(Map<String, String> environment) {
		this.environment = environment;		
	}
	
	public String getCommand() {
		return command;
	}
	
	public Filter.INPUT_TYPE getInputType() {
		return inputType;
	}
	
	public Filter.OUTPUT_TYPE getOuputType() {
		return outputType;
	}
	
	public String getConsoleName() {
		return consoleName;
	}
	
	@Override
	protected void okPressed() {
		command = commandCombo.getText();
		lastCommands.add(0, command);
		
		if (noneButton.getSelection()) {
			inputType = Filter.INPUT_TYPE.NONE;
		} else if (selectionButton.getSelection()) {
			inputType = Filter.INPUT_TYPE.SELECTION;
		} else if (selectionLinesButton.getSelection()) {
			inputType = Filter.INPUT_TYPE.SELECTED_LINES;
		} else if (documentButton.getSelection()) {
			inputType = Filter.INPUT_TYPE.DOCUMENT;
		} else if (lineButton.getSelection()) {
			inputType = Filter.INPUT_TYPE.LINE;
		} else if (wordButton.getSelection()) {
			inputType = Filter.INPUT_TYPE.WORD;
		}
		
		lastInputType = inputType;
		
		if (discardButton.getSelection()) {
			outputType = Filter.OUTPUT_TYPE.DISCARD;
		} else if (outputToConsoleButton.getSelection()) {
			outputType = Filter.OUTPUT_TYPE.OUTPUT_TO_CONSOLE;
		} else if (replaceSelectionButton.getSelection()) {
			outputType = Filter.OUTPUT_TYPE.REPLACE_SELECTION;
		} else if (replaceSelectedLinesButton.getSelection()) {
			outputType = Filter.OUTPUT_TYPE.REPLACE_SELECTED_LINES;
		} else if (replaceLineButton.getSelection()) {
			outputType = Filter.OUTPUT_TYPE.REPLACE_LINE;
		} else if (replaceWordButton.getSelection()) {
			outputType = Filter.OUTPUT_TYPE.REPLACE_WORD;
		} else if (insertAsTextButton.getSelection()) {
			outputType = Filter.OUTPUT_TYPE.INSERT_AS_TEXT;
		} else if (insertAsTemplateButton.getSelection()) {
			outputType = Filter.OUTPUT_TYPE.INSERT_AS_TEMPLATE;
		} else if (showAsHTMLButton.getSelection()) {
			outputType = Filter.OUTPUT_TYPE.SHOW_AS_HTML;
		} else if (showAsToolTipButton.getSelection()) {
			outputType = Filter.OUTPUT_TYPE.SHOW_AS_TOOLTIP;
		} else if (createNewDocumentButton.getSelection()) {
			outputType = Filter.OUTPUT_TYPE.CREATE_A_NEW_DOCUMENT;
		}
		
		lastOutputType = outputType;
		
		consoleName = consoleNameText.getText();
		if (consoleName.trim().length() == 0)
		{
			consoleName = Filter.DEFAULT_CONSOLE_NAME;
		}
		
		lastConsoleName = consoleName;
		if (lastConsoleName.equals(Filter.DEFAULT_CONSOLE_NAME)) {
			lastConsoleName = "";
		}
		
		super.okPressed();
	}

}
