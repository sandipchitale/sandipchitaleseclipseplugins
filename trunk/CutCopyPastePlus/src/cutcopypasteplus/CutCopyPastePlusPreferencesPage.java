package cutcopypasteplus;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This page manages the Cut Copy Paste Plus plug-in preferences .
 *
 * @author Sandip V. Chitale
 *
 */
public class CutCopyPastePlusPreferencesPage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

    public CutCopyPastePlusPreferencesPage() {
        super(FieldEditorPreferencePage.GRID);
    }

    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
    }

    @Override
    protected void createFieldEditors() {
        BooleanFieldEditor booleanENHANCED_CUT_COPY_PASTEFieldEditor = new BooleanFieldEditor(
                Activator.IS_CUT_AND_COPY_HISTORY_ENABLED, "Enable Cut and Copy History",
                getFieldEditorParent());
        addField(booleanENHANCED_CUT_COPY_PASTEFieldEditor);

        IntegerFieldEditor integerMAX_HISTORY_COUNTFieldEditor = new IntegerFieldEditor(
                Activator.MAX_HISTORY_COUNT, "Maximum History Count:",
                getFieldEditorParent());
        addField(integerMAX_HISTORY_COUNTFieldEditor);
        
        IntegerFieldEditor integerPASTE_NEXT_DELAYFieldEditor = new IntegerFieldEditor(
        		Activator.PASTE_NEXT_DELAY, "Paste next delay (milliseconds):",
        		getFieldEditorParent());
        addField(integerPASTE_NEXT_DELAYFieldEditor);

        BooleanFieldEditor booleanQUICK_PASTE_CYCLES_THROUGH_HISTORYFieldEditor = new BooleanFieldEditor(
                Activator.IS_QUICK_PASTE_CYCLES_THROUGH_HISTORY_ENABLED, "Quick Paste cycles through History",
                getFieldEditorParent());
        addField(booleanQUICK_PASTE_CYCLES_THROUGH_HISTORYFieldEditor);
    }

}
