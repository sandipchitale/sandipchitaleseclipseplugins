package removetrailingwhitespace;

import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.ui.texteditor.IEditorStatusLine;
import org.eclipse.ui.texteditor.ITextEditor;

public class RemoveTrailingWhitespaceUtils {
	public static void removeTrailingWhitespaceUtils(ITextEditor textEditor) {
		if (textEditor.isEditable()) {
			IFindReplaceTarget findReplaceTarget = (IFindReplaceTarget) textEditor.getAdapter(IFindReplaceTarget.class);
			if (findReplaceTarget instanceof IFindReplaceTargetExtension3) {
				IFindReplaceTargetExtension3 findReplaceTargetExtension3 = (IFindReplaceTargetExtension3) findReplaceTarget;
				try {
					IFindReplaceTargetExtension findReplaceTargetExtension = (IFindReplaceTargetExtension) findReplaceTarget;
					findReplaceTargetExtension.beginSession();
					int offset = 0;
					boolean firstTime = true;
					while (offset != -1) {
						offset = 
							findReplaceTargetExtension3.findAndSelect(offset,
									"[\\t ]+$",
									true,
									false,
									false,
									true);
						if (offset == -1) {
							if (firstTime) {
								IEditorStatusLine editorStatusLine = (IEditorStatusLine) textEditor.getAdapter(IEditorStatusLine.class);
								if (editorStatusLine != null) {
									editorStatusLine.setMessage(true, "No trailing whitespaces found!", null);
								}
							}
							break;
						}
						firstTime = false;
						findReplaceTargetExtension3.replaceSelection("", false);
					}
				} finally {
					IFindReplaceTargetExtension findReplaceTargetExtension = (IFindReplaceTargetExtension) findReplaceTarget;
					findReplaceTargetExtension.endSession();
				}
			}
		}
	}
}
