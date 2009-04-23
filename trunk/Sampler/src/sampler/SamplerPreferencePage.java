package sampler;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.ListEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;

public class SamplerPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public SamplerPreferencePage() {
	}

	public void init(IWorkbench workbench) {
		// Initialize the preference store we wish to use
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	protected void createFieldEditors() {
		ListEditor customFormatsListEditor = new CustomFormatListEditor(
				SamplerPreferences.CUSTOM_FORMATS, "Color Formats", getFieldEditorParent());
		addField(customFormatsListEditor);
		BooleanFieldEditor copyToClipboard = new BooleanFieldEditor(
				SamplerPreferences.COPY_TO_CLIPBOARD, "Copy to clipboard", getFieldEditorParent());
		addField(copyToClipboard);
	}
	
	public String getDescription() {
		return "Use {r}, {g}, {b} in format string to specify\n" +
				"red, green and blue component of the color.\n" +
				"You can use the format supported by\n" +
				"java.lang.String.format(String formatString, Object...values)";
	}
	
	private static class CustomFormatListEditor extends EntryModifiableListEditor {
		private final String item;

		CustomFormatListEditor(String key, String item, Composite parent) {
			super(key, "Custom " + item + " :", parent);
			this.item = item;
		}
		
		@Override
		protected String createList(String[] items) {
			return SamplerPreferences.createList(items); 
		}

		@Override
		protected String[] parseString(String stringList) {
			return SamplerPreferences.parseString(stringList);
		}

		@Override
		protected String getNewInputObject() {
			InputDialog commandDialog = new InputDialog(PlatformUI
					.getWorkbench().getActiveWorkbenchWindow().getShell(),"Custom " + item, "Enter custom " + item + " :", "", null);
			if (commandDialog.open() == InputDialog.OK) {
				return commandDialog.getValue();
			}
			return null;
		}
		
		@Override
		protected String getModifiedEntry(String original) {
			InputDialog commandDialog = new InputDialog(PlatformUI
					.getWorkbench().getActiveWorkbenchWindow().getShell(),"Edit Custom " + item, "Edit custom " + item + " :", original, null);
			if (commandDialog.open() == InputDialog.OK) {
				return commandDialog.getValue();
			}
			return null;
		}

		@Override
		protected void doFillIntoGrid(Composite parent, int numColumns) {
			super.doFillIntoGrid(parent, numColumns);
			List listControl = getListControl(parent);
			Composite buttonBoxControl = getButtonBoxControl(parent);
			Composite composite = new Composite(parent, SWT.NONE);
			GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
			gridData.horizontalSpan = 2;
			gridData.widthHint = 550;
			composite.setLayoutData(gridData);
			composite.setLayout(new GridLayout(2, false));
			listControl.setParent(composite);
			buttonBoxControl.setParent(composite);
		}
	}

}
