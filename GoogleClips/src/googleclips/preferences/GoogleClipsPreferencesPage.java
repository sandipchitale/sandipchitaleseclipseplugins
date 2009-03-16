package googleclips.preferences;

import googleclips.Activator;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This page manages the Google Clips plug-in preferences .
 * 
 * @author Sandip V. Chitale
 * 
 */
public class GoogleClipsPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public GoogleClipsPreferencesPage() {
		super(FieldEditorPreferencePage.GRID);
	}

	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected void createFieldEditors() {
		StringFieldEditor googleIdFieldEditor = new StringFieldEditor(Activator.GOOGLE_ID, "Google ID:", getFieldEditorParent());
		addField(googleIdFieldEditor);

		StringFieldEditor spreadsheetNameFieldEditor = new StringFieldEditor(Activator.SPREADSHEET_NAME, "Spreadsheet Name:", getFieldEditorParent());
		addField(spreadsheetNameFieldEditor);

		StringFieldEditor worksheetNameFieldEditor = new StringFieldEditor(Activator.WORKSHEET_NAME, "Worksheet Name:", getFieldEditorParent());
		addField(worksheetNameFieldEditor);

		StringFieldEditor columnNameFieldEditor = new StringFieldEditor(Activator.COLUMN_NAME, "Column:", getFieldEditorParent());
		addField(columnNameFieldEditor);

		BooleanFieldEditor booleanFieldEditor = new BooleanFieldEditor(Activator.AUTO_CLIP_CUT_COPY, "AutoClip Cut and Copy", getFieldEditorParent());
		addField(booleanFieldEditor);

		IntegerFieldEditor maxClipsCountFieldEditor = new IntegerFieldEditor(Activator.MAX_CLIPS_COUNT, "Maximum number of clips to show:", getFieldEditorParent());
		addField(maxClipsCountFieldEditor);
	}

}
