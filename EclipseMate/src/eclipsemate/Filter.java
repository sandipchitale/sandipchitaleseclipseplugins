package eclipsemate;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;

public class Filter {
	enum VARIABLES_NAMES {
		TM_BUNDLE_SUPPORT
		,TM_CURRENT_LINE
		,TM_CURRENT_WORD
		,TM_DIRECTORY
		,TM_FILEPATH
		,TM_LINE_INDEX
		,TM_LINE_NUMBER
		,TM_PROJECT_DIRECTORY
		,TM_SCOPE
		,TM_SELECTED_FILES
		,TM_SELECTED_FILE
		,TM_SELECTED_TEXT
		,TM_SOFT_TABS
		,TM_SUPPORT_PATH
		,TM_TAB_SIZE
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
	
	public static Map computeEnvironment(IWorkbenchWindow workbenchWindow, IEditorPart editorPart) {
		Map<String, String> environment = new HashMap<String, String>();
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
