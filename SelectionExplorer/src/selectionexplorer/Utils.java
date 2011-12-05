package selectionexplorer;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.internal.ui.JavaUIMessages;
import org.eclipse.jdt.internal.ui.dialogs.OpenTypeSelectionDialog;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public class Utils {

	static void openType(Shell shell, final String finalClassNameString)
	{
		// Get Clipboard
		Clipboard clipboard = new Clipboard(shell.getDisplay());
		// Put the paths string into the Clipboard
		clipboard.setContents(new Object[] { finalClassNameString },
				new Transfer[] { TextTransfer.getInstance() });
		OpenTypeSelectionDialog dialog = new OpenTypeSelectionDialog(shell,
				true, PlatformUI.getWorkbench().getProgressService(), null,
				IJavaSearchConstants.TYPE);
		dialog.setTitle(JavaUIMessages.OpenTypeAction_dialogTitle);
		dialog.setMessage(JavaUIMessages.OpenTypeAction_dialogMessage);
		dialog.setInitialPattern(finalClassNameString);

		int result = dialog.open();
		if (result != IDialogConstants.OK_ID)
			return;

		Object[] types = dialog.getResult();
		if (types != null && types.length > 0) {
			IType type = null;
			for (int i = 0; i < types.length; i++) {
				type = (IType) types[i];
				try {
					JavaUI.openInEditor(type, true, true);
				} catch (CoreException x) {
				}
			}
		}
	}
	
	static boolean isJavaTypeName(String typeName) {
		if (typeName == null) {
			return false;
		}
		boolean possibleJavaTypeName = true;
		String[] parts = typeName.split("[.$]");
		for (String part : parts) {
			for (int i = 0; i < part.length(); i++) {
				char ch = part.charAt(i);
				if (i == 0) {
					if (!Character.isJavaIdentifierStart(ch)) {
						possibleJavaTypeName = false;
						break;
					}
				} else {
					if (!Character.isJavaIdentifierPart(ch)) {
						possibleJavaTypeName = false;
						break;
					}
				}
			}
			if (!possibleJavaTypeName) {
				break;
			}
		}
		return possibleJavaTypeName;
	}
}
