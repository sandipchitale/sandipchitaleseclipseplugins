package sampler;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

public class ColorSampler extends WorkbenchWindowControlContribution {
	private Button sampler;
	private String formatString;
	private Color lastColor;
	private Cursor dropperCursor;

	private static Map<String, String> formatsMap = new LinkedHashMap<String, String>();
	static {
		formatsMap.put("#RRGGBB", "#%1$02x%2$02x%3$02x");
		formatsMap.put("rgb(r,g,b)", "rgb(%1$d, %2$d, %3$d)");
		formatsMap.put("new Color(r,g,b)", "new Color(%1$d, %1$d, %1$d)");
	}

	public ColorSampler() {
	}

	public ColorSampler(String id) {
		super(id);
	}

	protected Control createControl(Composite parent) {
		Font monospaced = new Font(parent.getDisplay(), "Monospace", 8, SWT.NORMAL);
		formatString = formatsMap.keySet().iterator().next();
		Image dropper = Activator.getDefault().getImageRegistry().get(Activator.DROPPER);
		Image colorchooser = Activator.getDefault().getImageRegistry().get(Activator.COLOR_CHOOSER);
		dropperCursor = new Cursor(parent.getDisplay(), 
				dropper.getImageData(),
				3,
				15);	

		final Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(4, false);
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 0;
		layout.marginTop = 0;
		layout.marginBottom = 0;
		layout.marginLeft= 0;
		layout.marginRight = 0;
		composite.setLayout(layout);
		
		final Button chooser = new Button(composite, SWT.PUSH | SWT.FLAT);
		GridData chooserGridData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		chooser.setLayoutData(chooserGridData);
		chooser.setImage(colorchooser);
		chooser.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				ColorDialog colorDialog = new ColorDialog(chooser.getShell());
				RGB rgb = colorDialog.open();
				if (rgb != null) {
					showColor(chooser.getDisplay(), new Color(chooser.getDisplay(), rgb), true);
				}
			}
		});

		sampler = new Button(composite, SWT.PUSH | SWT.FLAT);
		GridData samplerGridData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
		sampler.setLayoutData(samplerGridData);
		sampler.setImage(dropper);
		sampler.setToolTipText("Drag to sample color at mouse location on the desktop");
		class SamplerListener implements Listener {
			boolean dragging;

			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.MouseDown:
					sampler.setCursor(dropperCursor);
					dragging = true;
					sampleColor(sampler.getDisplay().map(sampler, null, new Point(event.x, event.y)));
					break;
				case SWT.MouseMove:
					if (dragging) {
						sampleColor(sampler.getDisplay().map(sampler, null, new Point(event.x, event.y)));
					}
					break;
				case SWT.MouseUp:
					dragging = false;
					sampler.setCursor(composite.getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
					sampleColor(sampler.getDisplay().map(sampler, null, new Point(event.x, event.y)), true);
					sampler.setBackground(null);
					break;
				}
			}
		}
		;

		Listener listener = new SamplerListener();
		sampler.addListener(SWT.MouseDown, listener);
		sampler.addListener(SWT.MouseMove, listener);
		sampler.addListener(SWT.MouseUp, listener);

		color = new Text(composite, SWT.BORDER | SWT.SINGLE);
		
//		Font font = color.getFont();
//		FontData[] fontData = font.getFontData();
//		for (int i = 0; i < fontData.length; i++) {
//			fontData[i].setHeight(fontData[i].getHeight() - 1);
//		}
//		Font newFont = new Font(parent.getDisplay(), fontData);
		color.setFont(monospaced);
		
		GridData colorGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		color.setLayoutData(colorGridData);
		color.setText("                      ");
		color.setEditable(false);
		color.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				color.setBackground(null);
			}
		});
		
		final CCombo formats = new CCombo(composite, SWT.BORDER);
//		font = formats.getFont();
//		fontData = font.getFontData();
//		for (int i = 0; i < fontData.length; i++) {
//			fontData[i].setHeight(fontData[i].getHeight() - 2);
//		}
//		newFont = new Font(parent.getDisplay(), fontData);
		formats.setFont(monospaced);
		GridData comboGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		formats.setLayoutData(comboGridData);
		formats.setEditable(false);
		for (String formatString : formatsMap.keySet()) {
			formats.add(formatString);
		}
		formats.select(0);
		formats.clearSelection();
		formats.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				formatString = formats.getItem(formats.getSelectionIndex());
				formats.clearSelection();
				showColor(e.display, lastColor);
			}
		});

		return composite;
	}

	private void sampleColor(Point cursorAt) {
		sampleColor(cursorAt, false);
	}
	
	private void sampleColor(Point cursorAt, boolean background) {
		Robot robot = getRobot();
		if (robot != null) {
			java.awt.Color pixelColor = robot.getPixelColor(cursorAt.x, cursorAt.y);
			Display display = sampler.getShell().getDisplay();
			Color newColor = new Color(display, pixelColor.getRed(), pixelColor.getGreen(), pixelColor.getBlue());
			sampler.setBackground(newColor);
			sampler.redraw();
			sampler.update();
			showColor(display, newColor, background);
		}
	}

	private void showColor(Display display, Color pixelColor) {
		showColor(display, pixelColor, false);
	}
	
	private void showColor(Display display, Color pixelColor, boolean background) {
		String formattedColor = String.format(formatsMap.get(formatString), pixelColor.getRed(), pixelColor.getGreen(), pixelColor.getBlue());
		color.setText(formattedColor);
		if (background) {
			color.setBackground(pixelColor);
		}
		color.redraw();
		color.update();
		new Clipboard(display).setContents(new Object[] { formattedColor }, new Transfer[] { TextTransfer.getInstance() });
		if (lastColor != pixelColor) {
			if (lastColor != null) {
				lastColor.dispose();
			}
		}
		lastColor = pixelColor;

	}

	private static Robot robot;
	private static boolean failedRobot;
	private Text color;

	public static Robot getRobot() {
		if (failedRobot) {
			return null;
		}
		if (robot == null) {
			try {
				robot = new Robot();
			} catch (AWTException e) {
				failedRobot = true;
				return null;
			}
		}
		return robot;
	}

}
