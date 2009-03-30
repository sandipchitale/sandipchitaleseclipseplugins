package sampler;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

public class ColorSampler extends WorkbenchWindowControlContribution {
	private CLabel sampler;
	private Cursor dropperCursor;
	private Color lastColor;
	private String formatString;
	
	private boolean copyToClipboard = true;

	private static Map<String, String> formatsMap = new LinkedHashMap<String, String>();
	static {
		formatsMap.put("HTML/CSS #RRGGBB", "#%1$02x%2$02x%3$02x");
		formatsMap.put("CSS rgb(r,g,b)", "rgb(%1$d, %2$d, %3$d)");
		formatsMap.put("AWT new Color(r,g,b)", "new Color(%1$d, %2$d, %3$d)");
	}

	public ColorSampler() {
	}

	public ColorSampler(String id) {
		super(id);
	}

	protected Control createControl(Composite parent) {
		formatString = formatsMap.values().iterator().next();

		// Fonts
		Font textFont = null;
		FontDescriptor textFontDescriptor = JFaceResources.getTextFontDescriptor();
		if (textFontDescriptor != null) {
			FontData[] fontData = textFontDescriptor.getFontData();
			if (fontData != null) {
				for (FontData fontDatum : fontData) {
					fontDatum.setHeight(fontDatum.getHeight()-1);
				}
				textFont = new Font(parent.getDisplay(), fontData);
			}
		}

		Image dropper = Activator.getDefault().getImageRegistry().get(Activator.DROPPER);
		dropperCursor = new Cursor(parent.getDisplay(), dropper.getImageData(), 3, 15);
		
		clipboard = new Clipboard(parent.getDisplay());

		sampler = new CLabel(parent, SWT.LEFT | SWT.BORDER);

		if (textFont != null) {
			sampler.setFont(textFont);
		}
		
		sampler.setImage(dropper);
		sampler.setText("                          ");
		sampler.setToolTipText("Drag to sample color at mouse location on the desktop");
		
		GridData samplerGridData = new GridData(SWT.LEFT, SWT.TOP, false, false);
		sampler.setLayoutData(samplerGridData);

		class SamplerListener implements Listener {
			boolean dragging;

			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.MouseDown:
					if (event.button == 1 && event.stateMask == 0) {
						sampler.setCursor(dropperCursor);
						dragging = true;
						sampleColor(sampler.getDisplay().map(sampler, null, new Point(event.x, event.y)));
					}
					break;
				case SWT.MouseMove:
					if (dragging) {
						sampleColor(sampler.getDisplay().map(sampler, null, new Point(event.x, event.y)));
					}
					break;
				case SWT.MouseUp:
					if (dragging) {
						dragging = false;
						sampler.setCursor(sampler.getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
						sampleColor(sampler.getDisplay().map(sampler, null, new Point(event.x, event.y)));
						refreshClipboard();
					}
					break;
				}
			}
		}

		Listener listener = new SamplerListener();
		sampler.addListener(SWT.MouseDown, listener);
		sampler.addListener(SWT.MouseMove, listener);
		sampler.addListener(SWT.MouseUp, listener);

		// Pop-up menu
		Menu menu = new Menu(sampler.getShell(), SWT.POP_UP);
		for(String displayFormat : formatsMap.keySet()) {
			final String format = formatsMap.get(displayFormat);
			MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
			menuItem.setText(displayFormat);
			menuItem.setSelection(format.equals(formatString));
			menuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					formatString = format;
					refreshColorDisplay();
					refreshClipboard();
				}
			});
		}
		
		new MenuItem(menu, SWT.SEPARATOR);
		
		final MenuItem copyToClipboardMenuItem = new MenuItem(menu, SWT.CHECK);
		copyToClipboardMenuItem.setText("Copy to Clipboard");
		copyToClipboardMenuItem.setSelection(copyToClipboard);
		copyToClipboardMenuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				copyToClipboard = copyToClipboardMenuItem.getSelection();
				refreshClipboard();
			}
		});
		
		sampler.setMenu(menu);
		
		lastColor =  sampler.getBackground();
		
		return sampler;
	}

	private void sampleColor(Point cursorAt) {
		Robot robot = getRobot();
		if (robot != null) {
			java.awt.Color pixelColor = robot.getPixelColor(cursorAt.x, cursorAt.y);
			Display display = sampler.getShell().getDisplay();
			Color newColor = new Color(display, pixelColor.getRed(), pixelColor.getGreen(), pixelColor.getBlue());
			showColor(display, newColor);
		}
	}

	private void refreshColorDisplay() {
		if (lastColor != null) {
			showColor(sampler.getDisplay(), lastColor);
		}
	}
	
	private void refreshClipboard() {
		if (copyToClipboard && lastColor != null) {
			copyToClipboard(sampler.getDisplay(), formatColor(lastColor));
		}
	}
	
	private void showColor(Display display, Color pixelColor) {
		String formattedColor = formatColor(pixelColor);
		sampler.setText(formattedColor);
		sampler.setToolTipText(formattedColor);
		sampler.setBackground(pixelColor);
		sampler.redraw();
		sampler.update();
		if (lastColor != pixelColor) {
			if (lastColor != null) {
				lastColor.dispose();
			}
		}
		lastColor = pixelColor;

	}

	private void copyToClipboard(Display display, String formattedColor) {
		clipboard.setContents(new Object[] { formattedColor }, new Transfer[] { TextTransfer.getInstance() });
	}

	private String formatColor(Color pixelColor) {
		return String.format(formatString, pixelColor.getRed(), pixelColor.getGreen(), pixelColor.getBlue());
	}

	private static Robot robot;
	private static boolean failedRobot;
	private Clipboard clipboard;

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
