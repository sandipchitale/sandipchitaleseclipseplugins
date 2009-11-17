package skineclipse;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CBanner;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.CoolBar;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.part.PageBook;

public class SkinUtils {

	static Slider getSlider(Composite parent) {
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 4;
		parent.setLayout(layout);
		Slider slider = new Slider(parent, SWT.NONE);
		slider.setToolTipText("");
		slider.setMinimum(0);
		slider.setMaximum(255);
		slider.setIncrement(1);
		Display display = slider.getDisplay();
		Color widgetBackground = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
		slider.setSelection((widgetBackground .getRed() + widgetBackground.getGreen() + widgetBackground.getBlue())/3);

		GridData sliderGridData = new GridData(SWT.LEFT, SWT.CENTER, false,
				false);
		slider.setLayoutData(sliderGridData);

		slider.addListener(SWT.Selection, new ListenerImplementation());
		return slider;
	}
	
	private static Map<Integer, Color> colorMap = new HashMap<Integer, Color>();
	static Color getColor(Display display, int level) {
		Color color = colorMap.get(level);
		if (color == null) {
			color = new Color(display, level, level, level);
			colorMap.put(level, color);
		}
		return color;
	}
	
	static void setColor(Composite composite, String indent,
			Color color, Color foreGorund) {
		if (composite == null) {
			return;
		}
		try {
			if (!composite.isDisposed()) {
				composite.setRedraw(false);
				if (composite instanceof ToolBar
						|| composite instanceof Shell
						|| composite instanceof CLabel
						|| composite instanceof CTabFolder
						|| composite instanceof CoolBar
						|| composite instanceof CoolBar
						|| composite instanceof CBanner
						|| composite instanceof PageBook
						|| composite instanceof ViewForm
						|| composite instanceof Tree
						|| composite instanceof StyledText
						|| composite instanceof Table
						|| composite.getClass().getName().startsWith(
								"org.eclipse.jface")
						|| composite.getClass().getName().startsWith(
								"org.eclipse.ui.internal.layout")
						|| composite.getClass().getName().startsWith(
								"org.eclipse.ui.internal.FastViewBar")
						|| composite
								.getClass()
								.getName()
								.startsWith(
										"org.eclipse.ui.internal.progress.ProgressRegion")
						|| composite.getClass().getName().startsWith(
								"org.eclipse.swt.widgets.Canvas")
						|| composite.getClass() == Composite.class) {
					composite.setBackground(color);
					composite.setForeground(foreGorund);
				} else {
					// System.out.println(indent +
					// composite.getClass().getName());
				}
				Control[] controls = composite.getChildren();
				for (Control control : controls) {
					if (!(control instanceof Composite)) {
						// System.out.println(indent + "Control " +
						// control.getClass().getName());
					}
					if (control instanceof Composite) {
						setColor((Composite) control, indent + " ", color, foreGorund);
					} else if (control instanceof Label
							|| control instanceof CoolBar
							|| control instanceof Sash
							|| control instanceof ProgressBar
							|| control instanceof Text) {
						control.setBackground(color);
						control.setForeground(foreGorund);
					}

				}
			}
		} finally {
			composite.setRedraw(true);
		}
	}
}
