package eclipsemate;


import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class BundleEditorPreferencePage extends PreferencePage implements
		IWorkbenchPreferencePage {

	@SuppressWarnings("unused")
	private IWorkbench workbench;
	private Combo filterCombo;
	private Label itemTypeLabel;
	private Tree bundelTree;
	private Composite editorsPanel;

	public BundleEditorPreferencePage() {
	}

	public BundleEditorPreferencePage(String title) {
		super(title);
	}

	public BundleEditorPreferencePage(String title, ImageDescriptor image) {
		super(title, image);
	}

	@Override
	protected Control createContents(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		main.setLayout(new GridLayout(1, false));

		SashForm sash = new SashForm(main, SWT.BORDER | SWT.HORIZONTAL);
		sash.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Composite leftPanel = new Composite(sash, SWT.NONE);
		
		GridLayout leftPanelGridLayout = new GridLayout(1, false);
		leftPanel.setLayout(leftPanelGridLayout);
		
		filterCombo = new Combo(leftPanel, SWT.READ_ONLY);
		GridData filterComboGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		filterCombo.setLayoutData(filterComboGridData);
		filterCombo.add("Show All");
		filterCombo.select(0);
		
		bundelTree = new Tree(leftPanel, SWT.BORDER);
		GridData bundelTreeGridData = new GridData(GridData.FILL_BOTH);
		bundelTreeGridData.verticalSpan = 1;
		bundelTree.setLayoutData(bundelTreeGridData);
		
		Composite rightPanel = new Composite(sash, SWT.NONE);
		
		GridLayout rightPanelGridLayout = new GridLayout(3, false);
		rightPanel.setLayout(rightPanelGridLayout);
		
		itemTypeLabel = new Label(rightPanel, SWT.LEAD);
		itemTypeLabel.setText("No item selected");
		GridData itemTypeLabelGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		itemTypeLabelGridData.horizontalSpan = 3;
		itemTypeLabel.setLayoutData(itemTypeLabelGridData);
		
		editorsPanel = new Composite(rightPanel, SWT.BORDER);
		GridData editorsPanelGridData = new GridData(GridData.FILL_BOTH);
		editorsPanelGridData.horizontalSpan = 3;
		editorsPanel.setLayoutData(editorsPanelGridData);
		
		StackLayout editorStackLayout = new StackLayout();
		editorsPanel.setLayout(editorStackLayout);
		
		Label keybindingLabel = new Label(rightPanel, SWT.LEAD);
		keybindingLabel.setText("Activation:");
		GridData keybindingLabelGridData = new GridData(SWT.FILL, SWT.TOP, false, false);
		keybindingLabel.setLayoutData(keybindingLabelGridData);
		
		Combo keybindingTypeCombo = new Combo(rightPanel, SWT.READ_ONLY);
		keybindingTypeCombo.add("Key sequence");
		keybindingTypeCombo.add("Tab");
		keybindingTypeCombo.select(0);
		GridData keybindingTypeComboGridData = new GridData(SWT.LEAD, SWT.TOP, false, false);
		keybindingLabel.setLayoutData(keybindingTypeComboGridData);
		
		Text keybindingText = new Text(rightPanel, SWT.SINGLE | SWT.BORDER);
		GridData keybindingTextComboGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		keybindingText.setLayoutData(keybindingTextComboGridData);
		
		Label contextLabel = new Label(rightPanel, SWT.LEAD);
		contextLabel.setText("Activation:");
		GridData contextLabelGridData1 = new GridData(SWT.FILL, SWT.TOP, false, false);
		contextLabel.setLayoutData(contextLabelGridData1);
		
		Text contextText = new Text(rightPanel, SWT.SINGLE | SWT.BORDER);
		GridData contextTextComboGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
		contextTextComboGridData.horizontalSpan = 2;
		contextText.setLayoutData(contextTextComboGridData);
		
		// Set the sash position
		sash.setWeights(new int[] {30, 70});
		
		return main;
	}

	public void init(IWorkbench workbench) {
		this.workbench = workbench;

	}

}
