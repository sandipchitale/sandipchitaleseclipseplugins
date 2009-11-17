package skineclipse;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

public class SkieclipseSliderInToolbar extends WorkbenchWindowControlContribution {

	private Slider slider;

	public SkieclipseSliderInToolbar() {
	}

	public SkieclipseSliderInToolbar(String id) {
		super(id);
	}

	@Override
	protected Control createControl(Composite parent) {
		if (slider == null) {
			slider = SkinUtils.getSlider(parent);
		}
		
		return slider;
	}
}
