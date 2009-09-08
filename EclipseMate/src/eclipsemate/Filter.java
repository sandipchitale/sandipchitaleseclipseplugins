package eclipsemate;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

public class Filter {
	enum VARIABLES_NAMES {
		TM_BUNDLE_SUPPORT
		,TM_CURRENT_LINE
		,TM_CURRENT_WORD
		,TM_DIRECTORY
		,TM_FILEPATH
		,TM_LINE_NUMBER
		,TM_LINE_INDEX
		,TM_PROJECT_DIRECTORY
		,TM_SCOPE
		,TM_SELECTED_FILES
		,TM_SELECTED_FILE
		,TM_SELECTED_TEXT
		,TM_SOFT_TABS
		,TM_SUPPORT_PATH
		,TM_TAB_SIZE
		
		,TM_SELECTION_OFFSET
		,TM_SELECTION_LENGTH
		,TM_SELECTION_START_LINE_NUMBER
		,TM_SELECTION_END_LINE_NUMBER
	};
	
	enum INPUT_TYPE  {
		NONE
		,SELECTION
		,SELECTED_LINES
		,LINE
		,WORD
		,DOCUMENT
	};
	
	enum OUTPUT_TYPE {
		DISCARD
		,REPLACE_SELECTION
		,REPLACE_SELECTED_LINES
		,REPLACE_LINE
		,REPLACE_WORD
		,REPLACE_DOCUMENT
		,INSERT_AS_TEMPLATE
		,SHOW_AS_HTML
		,SHOW_AS_TOOLTIP
		,CREATE_A_NEW_DOCUMENT
		,OUTPUT_TO_CONSOLE
	};
	
	public static Map<String, String> computeEnvironment(IWorkbenchWindow workbenchWindow, IEditorPart editorPart) {
		Map<String, String> environment = new HashMap<String, String>();
		
		if (editorPart instanceof ITextEditor) {
			ITextEditor abstractTextEditor = (ITextEditor) editorPart;
			IEditorInput editorInput = abstractTextEditor.getEditorInput();
			if (editorInput instanceof IFileEditorInput) {
				IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
				IFile iFile = fileEditorInput.getFile();
				if (iFile != null) {
					environment.put(VARIABLES_NAMES.TM_SELECTED_FILE.name(), iFile.getLocation().toFile().getAbsolutePath());
					environment.put(VARIABLES_NAMES.TM_FILEPATH.name(), iFile.getLocation().toFile().getAbsolutePath());
					environment.put(VARIABLES_NAMES.TM_DIRECTORY.name(), iFile.getParent().getLocation().toFile().getAbsolutePath());
					environment.put(VARIABLES_NAMES.TM_PROJECT_DIRECTORY.name(), iFile.getProject().getLocation().toFile().getAbsolutePath());
					ISelectionProvider selectionProvider = abstractTextEditor.getSelectionProvider();
					ISelection selection = selectionProvider.getSelection();
					if (selection instanceof ITextSelection) {
						ITextSelection textSelection = (ITextSelection) selection;
						environment.put(VARIABLES_NAMES.TM_SELECTED_TEXT.name(), textSelection.getText());
						environment.put(VARIABLES_NAMES.TM_LINE_NUMBER.name(), String.valueOf(textSelection.getStartLine() + 1));
						environment.put(VARIABLES_NAMES.TM_SELECTION_OFFSET.name(), String.valueOf(textSelection.getOffset()));
						environment.put(VARIABLES_NAMES.TM_SELECTION_LENGTH.name(), String.valueOf(textSelection.getLength()));
						environment.put(VARIABLES_NAMES.TM_SELECTION_START_LINE_NUMBER.name(), String.valueOf(textSelection.getStartLine()));
						environment.put(VARIABLES_NAMES.TM_SELECTION_END_LINE_NUMBER.name(), String.valueOf(textSelection.getEndLine()));
					}
				}
			}
		} else {
			
		}
		return environment;
	}
	
	public static void launch(String command, 
			INPUT_TYPE input,
			FilterInputProvider filterInputProvider,
			Map<String, String> environment,
			OUTPUT_TYPE output,
			FilterOutputConsumerProvider filterOutputConsumerProvider) {
	};
	
	public interface FilterInputProvider {
		public InputStream getInputStream();
	};
	
	public interface FilterOutputConsumerProvider {
		public void consumeOutputStream(OutputStream outputStream);
		public void consumeErrorStream(OutputStream errorStream);
	}

}
