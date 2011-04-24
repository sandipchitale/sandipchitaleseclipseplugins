package findreplacebar;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class FindReplaceBarPreferencePage extends FieldEditorPreferencePage
		implements IWorkbenchPreferencePage {
	
	static final String ID_FIND_REPLACE_BAR_PREFERENCE_PAGE = "FindReplaceBar.preferences.page"; //$NON-NLS-1$

	public FindReplaceBarPreferencePage() {
		super(FieldEditorPreferencePage.GRID);
	}

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		BooleanFieldEditor overrideFindReplaceDialogField = new BooleanFieldEditor(
				FindReplaceBarPreferenceInitializer.OVERRIDE_FIND_REPLACE_DIALOG,
				Messages.FindReplaceBarPreferencePage_OverrideFindReplaceFieldDescription, getFieldEditorParent());
		addField(overrideFindReplaceDialogField);
	}
}
