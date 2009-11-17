package skineclipse;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.menus.IWorkbenchWidget;

public class SkieclipseSliderInStatusbar implements IWorkbenchWidget {
	private Slider slider;

	public void init(IWorkbenchWindow workbenchWindow) {
	}

	public void dispose() {
	}

	public void fill(Composite parent) {
		if (slider == null) {
			slider = SkinUtils.getSlider(parent);
		}
	}

	public void fill(Menu parent, int index) {

	}

	public void fill(ToolBar parent, int index) {

	}

	public void fill(CoolBar parent, int index) {

	}

}
