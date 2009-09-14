package eclipsemate;

import org.eclipse.swt.custom.StyledText;

public class TextEditorUtilities {
	
	public static String getLineAtCaret(StyledText styledText) {
		int caretAt = styledText.getCaretOffset();
		int lineAtCaret = styledText.getLineAtOffset(caretAt);
		return styledText.getLine(lineAtCaret);
	}
	
	public static String getWordAtCaret(StyledText styledText) {
		int caretAt = styledText.getCaretOffset();
		
		styledText.getWordWrap();
		return null;
	}
}
