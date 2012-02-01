package cutcopypasteplus;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;

public class IsFocusedInTextPropertyTester extends PropertyTester {

	private static final String IS_FOCUSED_IN_TEXT = "isFocusedInText";

	public IsFocusedInTextPropertyTester() {
	}

	@Override
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		
		if (IS_FOCUSED_IN_TEXT.equals(property)) {
			return isFocusedInText();
		}
		return false;
	}

	static boolean isFocusedInText() {
		Display display = getDisplay();
		if (display != null) {
			Control focusControl = display.getFocusControl();
			return (focusControl != null && (focusControl instanceof Text || focusControl instanceof StyledText));				
		}
		return false;
	}

	static Control getFocusControl() {
		Display display = getDisplay();
		if (display != null) {
			return display.getFocusControl();
		}
		return null;
	}
	
	static private Display getDisplay() {
		Display display = PlatformUI.getWorkbench().getDisplay();
		return display;
	}

}
