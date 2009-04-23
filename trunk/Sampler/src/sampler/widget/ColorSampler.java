package sampler.widget;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PreferencesUtil;

import sampler.SamplerPreferences;

public class ColorSampler extends CLabel {
	private Clipboard clipboard;
	private Image dropper;
	private Cursor dropperCursor;
	private Color lastColor;
	private String formatString;

	private MenuItem copyToClipboardMenuItem;
	
	private static Map<String, String> formatsMap = new LinkedHashMap<String, String>();
	static {
		formatsMap.put("#RRGGBB (HTML/CSS)", "#%1$02x%2$02x%3$02x");
		formatsMap.put("rgb(r,g,b) (CSS)", "rgb(%1$d, %2$d, %3$d)");
		formatsMap.put("new Color(r,g,b) (AWT)", "new Color(%1$3d, %2$3d, %3$3d)");
		
		String[] customFormats = SamplerPreferences.getCustomFormats();
		for (String customFormat : customFormats) {
			formatsMap.put(customFormat, customFormat);
		}
	}

	public ColorSampler(Composite parent) {
		super(parent, SWT.LEFT | SWT.BORDER);
	
		formatString = processedFormatString(formatsMap.values().iterator().next());

		ImageData dropperImageData = new ImageData(getClass().getResourceAsStream("dropper.png"));
		dropperCursor = new Cursor(parent.getDisplay(), dropperImageData, 3, 15);
		dropper = new Image(getDisplay(), dropperImageData);
		
		clipboard = new Clipboard(parent.getDisplay());

		setImage(dropper);
		setText("                          ");
		setToolTipText("Drag to sample color at mouse location on the desktop");

		class SamplerListener implements Listener {
			boolean dragging;

			public void handleEvent(Event event) {
				switch (event.type) {
				case SWT.MouseDown:
					if (event.button == 1 && event.stateMask == 0) {
						setCursor(dropperCursor);
						dragging = true;
						sampleColor(getDisplay().map(ColorSampler.this, null, new Point(event.x, event.y)));
					}
					break;
				case SWT.MouseMove:
					if (dragging) {
						sampleColor(getDisplay().map(ColorSampler.this, null, new Point(event.x, event.y)));
					}
					break;
				case SWT.MouseUp:
					if (dragging) {
						dragging = false;
						setCursor(getShell().getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
						sampleColor(getDisplay().map(ColorSampler.this, null, new Point(event.x, event.y)));
						refreshClipboard();
					}
					break;
				}
			}
		}

		Listener listener = new SamplerListener();
		addListener(SWT.MouseDown, listener);
		addListener(SWT.MouseMove, listener);
		addListener(SWT.MouseUp, listener);

		// Pop-up menu
		Menu menu = new Menu(getShell(), SWT.POP_UP);
		for(String displayFormat : formatsMap.keySet()) {
			final String format = formatsMap.get(displayFormat);
			MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
			menuItem.setText(displayFormat);
			menuItem.setSelection(format.equals(formatString));
			menuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					formatString = processedFormatString(format);
					refreshColorDisplay();
					refreshClipboard();
				}
			});
		}
		
		new MenuItem(menu, SWT.SEPARATOR);
		
		MenuItem customMenuItem =  new MenuItem(menu, SWT.PUSH);
		customMenuItem.setText("Custom...");
		customMenuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				showSamplerPreferences();
			}
		});

		new MenuItem(menu, SWT.SEPARATOR);
		
		copyToClipboardMenuItem = new MenuItem(menu, SWT.CHECK);
		copyToClipboardMenuItem.setText("Copy to Clipboard");
		copyToClipboardMenuItem.setSelection(true);
		copyToClipboardMenuItem.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				SamplerPreferences.setCopyToClipboard(copyToClipboardMenuItem.getSelection());
				refreshClipboard();
			}
		});
		
		setMenu(menu);
		
		lastColor =  getBackground();
	}
	
	private String processedFormatString(String unprocessedFormatString) {
		return unprocessedFormatString
		.replaceAll(Pattern.quote("%{r}"), Matcher.quoteReplacement("%1$"))
		.replaceAll(Pattern.quote("%{g}"), Matcher.quoteReplacement("%2$"))
		.replaceAll(Pattern.quote("%{b}"), Matcher.quoteReplacement("%3$"))
		.replaceAll(Pattern.quote("{r}"), Matcher.quoteReplacement("%1$d"))
		.replaceAll(Pattern.quote("{g}"), Matcher.quoteReplacement("%2$d"))
		.replaceAll(Pattern.quote("{b}"), Matcher.quoteReplacement("%3$d"))
		;
	}
	
	public void dispose() {
		super.dispose();
		if (!dropper.isDisposed()) {
			dropper.dispose();
		}
		if (!dropperCursor.isDisposed()) {
			dropperCursor.dispose();
		}
	}

	private void sampleColor(Point cursorAt) {
		Robot robot = getRobot();
		if (robot != null) {
			java.awt.Color pixelColor = robot.getPixelColor(cursorAt.x, cursorAt.y);
			Display display = getShell().getDisplay();
			Color newColor = new Color(display, pixelColor.getRed(), pixelColor.getGreen(), pixelColor.getBlue());
			showColor(display, newColor);
		}
	}

	private void refreshColorDisplay() {
		if (lastColor != null) {
			showColor(getDisplay(), lastColor);
		}
	}
	
	private void refreshClipboard() {
		if (SamplerPreferences.isCopyToClipboard() && lastColor != null) {
			copyToClipboard(getDisplay(), formatColor(lastColor));
		}
	}
	
	private void showColor(Display display, Color pixelColor) {
		String formattedColor = formatColor(pixelColor);
		setText(formattedColor);
		setToolTipText(formattedColor);
		setBackground(pixelColor);
		redraw();
		update();
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
	
	private void showSamplerPreferences() {
		String[] displayedIds = new String[] {"Sampler.page"};
		PreferenceDialog samplerPreferenceDialog = PreferencesUtil.createPreferenceDialogOn(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				displayedIds[0],
				displayedIds,
				null);
		samplerPreferenceDialog.open();
	}

}
