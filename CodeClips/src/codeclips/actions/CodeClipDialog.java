package codeclips.actions;

import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplatePersistenceData;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ListDialog;
import org.eclipse.ui.texteditor.ITextEditor;

import codeclips.Activator;

public class CodeClipDialog extends TitleAreaDialog {

	private final ITextEditor textEditor;

	private String abbrev;
	private String description;
	private String expansion;

	private Text abbrevText;
	private Text descriptionText;
	private StyledText expansionText;
		
	private final TemplatePersistenceData templatePersistenceData;

	private static final int MANAGE_ID = IDialogConstants.CLIENT_ID;
	private static final int CREATE_UPDATE_ID = MANAGE_ID+1;
	private static final int CANCEL_ID = CREATE_UPDATE_ID + 1;

	private Button createUpdateButton;

	public CodeClipDialog(Shell shell, ITextEditor textEditor, TemplatePersistenceData templatePersistenceData) {
		super(shell);
		this.textEditor = textEditor;
		this.templatePersistenceData = templatePersistenceData;
		Template template = templatePersistenceData.getTemplate();
		abbrev = template.getName();
		description = template.getDescription();
		expansion = template.getPattern();
		setHelpAvailable(false);
	}
	
	public CodeClipDialog(Shell shell, ITextEditor textEditor) {
		super(shell);
		this.textEditor = textEditor;
		this.templatePersistenceData = null;
		abbrev = "";
		description = "";
		expansion = "";
		setHelpAvailable(false);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		validateAbbrev();
		return contents;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		setTitle((templatePersistenceData == null ? "Create a new" : "Modify the") + " Code Clip");
        Composite parentComposite = (Composite) super.createDialogArea(parent);
        
        GridLayout gridLayout = (GridLayout) parentComposite.getLayout();
        gridLayout.numColumns = 2;
        gridLayout.makeColumnsEqualWidth = false;
		
        GridData layoutData = (GridData) parentComposite.getChildren()[0].getLayoutData();
        layoutData.horizontalSpan = 2;
		
		gridLayout.marginWidth = 10;
		gridLayout.marginHeight = 10;
		gridLayout.horizontalSpacing = 5;
		gridLayout.verticalSpacing = 5;
        
        Label abbrevLabel = new Label(parentComposite, SWT.NONE);
        abbrevLabel.setText("Abbreviation:");
		GridData abbrevLabelGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		abbrevLabel.setLayoutData(abbrevLabelGridData);
		
		Label descriptionLabel = new Label(parentComposite, SWT.NONE);
        descriptionLabel.setText("Description:");
		GridData descriptionLabelGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		descriptionLabel.setLayoutData(descriptionLabelGridData);
		
		abbrevText = new Text(parentComposite, SWT.SINGLE | SWT.BORDER);
        abbrevText.setText(abbrev);
        abbrevText.setEditable(templatePersistenceData == null);
		GridData abbrevTextGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		abbrevText.setLayoutData(abbrevTextGridData);
		
		abbrevText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				validateAbbrev();
			}
		});
		
        descriptionText = new Text(parentComposite, SWT.SINGLE | SWT.BORDER);
        descriptionText.setText(description);
		GridData descriptionTextGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		descriptionText.setLayoutData(descriptionTextGridData);
		
        Label expansionLabel = new Label(parentComposite, SWT.NONE);
        expansionLabel.setText("Expansion:");
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
		
		if (templatePersistenceData == null) {
			abbrevText.setFocus();
		} else {
			descriptionText.setFocus();
		}
		return parentComposite;
	}
	
	private void validateAbbrev() {
		setMessage("");
		createUpdateButton.setEnabled(true);
		if (templatePersistenceData == null) {
			String text = abbrevText.getText().trim();
			if ("".equals(text)) {
				setMessage("Must specify abbreviation.");
				createUpdateButton.setEnabled(false);
				return;
			}
			if (Activator.getDefault().getTemplateStore().findTemplate(text) != null) {
				setMessage("Code Clip with same abbreviation exists.");
				return;
			}
		}
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
		abbrev = abbrevText.getText().trim();
		description = descriptionText.getText().trim();
		expansion = expansionText.getText();
		super.okPressed();
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		if (getParentShell() == textEditor.getSite().getShell()) {
			Button manageButton = createButton(parent, MANAGE_ID, "Manage...", false);
			manageButton.addSelectionListener(new SelectionListener() {
				public void widgetSelected(SelectionEvent e) {
					new ManageCodesClipsDialog(getShell(), textEditor).open();
				}
				
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
			});
		}
		
		((GridLayout) parent.getLayout()).numColumns++;
		final Combo combo = new Combo(parent, SWT.READ_ONLY);
		combo.setItems (new String [] {
				"Insert Variables",
				"${cursor}",
				"${1}",
				"${2}",
				"${3}",
				"${4}",
				"${5}",
				"${6}",
				"${7}",
				"${8}",
				"${9}",
				"${clipboard}",
				"${user.home:sysprop}",
				"${date}",
				"${time}",
				"${user}",
				"${selection}",
				"${word_selection}",
				"${line_selection}",
				"${dollar}",
				"Variablize..."
				}
		);
		
		combo.select(0);
		combo.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				try {
					int selectionIndex = combo.getSelectionIndex();
					if (selectionIndex == (combo.getItemCount() - 1)) {
						String text = expansionText.getText();
						Map<String, Integer> frequencyMap = new TreeMap<String, Integer>();
						Scanner scanner = new Scanner(text);
						scanner.useDelimiter("\\W+");
						while(scanner.hasNext()) {
							String next = scanner.next();
							if (next.matches("[a-zA-Z]\\w*")) {
								Integer freq = frequencyMap.get(next);
								frequencyMap.put(next, (freq == null ? 1 : freq + 1));
							}
						}
						Set<String> variableCandidates = new TreeSet<String>();
						for (String word : frequencyMap.keySet()) {
							if (frequencyMap.get(word) > 1) {
								variableCandidates.add(word);
							}
						}
						if (variableCandidates.size() > 0) {
							ListDialog listDialog = new ListDialog(getShell());
							listDialog.setAddCancelButton(true);
							listDialog.setHelpAvailable(false);
							listDialog.setInput(variableCandidates);
							listDialog.setContentProvider(new ArrayContentProvider());
							listDialog.setLabelProvider(new LabelProvider());
							listDialog.setTitle("Variabalize");
							listDialog.setMessage("Select a word to variabalize");
							if (Window.OK == listDialog.open()) {
								Object[] results = listDialog.getResult();
								if (results.length > 0) {
									for (Object result : results) {
										if (result instanceof String) {
											String string = (String) result;
											text = text.replaceAll("\\b" + Pattern.quote(string) + "\\b", "\\$\\{" + string + "\\}");
										}
									}
									expansionText.setText(text);
								}
							}
						}
					} else if (selectionIndex > 0) {
						int caretOffset = expansionText.getCaretOffset();
						expansionText.replaceTextRange(caretOffset, 0, combo.getItem(selectionIndex));
					}
				} finally {
					combo.select(0);
				}
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		GridData gridData = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		combo.setLayoutData(gridData);
		
		createUpdateButton = createButton(parent, CREATE_UPDATE_ID, (templatePersistenceData == null ? "Create" : "Update"), false);
		createButton(parent, CANCEL_ID, IDialogConstants.CANCEL_LABEL, true);
	}
	
	@Override
	protected void buttonPressed(int buttonId) {
		if (buttonId == CREATE_UPDATE_ID) {
			okPressed();
			return;
		} else if (buttonId == CANCEL_ID) {
			cancelPressed();
			return;
		}
		super.buttonPressed(buttonId);
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
