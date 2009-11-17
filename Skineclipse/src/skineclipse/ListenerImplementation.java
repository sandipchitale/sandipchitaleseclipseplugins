/**
 * 
 */
package skineclipse;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Slider;

final class ListenerImplementation implements Listener {
	public void handleEvent(Event event) {
		Slider slider = (Slider) event.widget;
		switch (event.detail) {
		case SWT.NONE:
		case SWT.DRAG:
		case SWT.HOME:
		case SWT.END:
		case SWT.ARROW_DOWN:
		case SWT.ARROW_UP:
		case SWT.PAGE_DOWN:
		case SWT.PAGE_UP:
			int level = slider.getSelection();
			SkinUtils.setColor(slider.getShell(),
					"",
					SkinUtils.getColor(slider.getDisplay(), level),
					(level < 128 ? slider.getDisplay().getSystemColor(SWT.COLOR_WHITE) : slider.getDisplay().getSystemColor(SWT.COLOR_BLACK)));
		default:
			break;
		}
	}
}