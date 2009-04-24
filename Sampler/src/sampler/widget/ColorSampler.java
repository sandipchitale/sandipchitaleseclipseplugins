package sampler.widget;

import java.awt.AWTException;
import java.awt.Robot;
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

	private MenuItem copyToClipboardMenuItem;

	public ColorSampler(Composite parent) {
		super(parent, SWT.LEFT | SWT.BORDER);
		
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
		constructMenu(menu);
		
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
		
		setFormatString(SamplerPreferences.getCurrentFormat());
		lastColor =  getBackground();
		
		showColor(getDisplay(), lastColor);
	}
	
	private void constructMenu(Menu menu) {
		MenuItem[] items = menu.getItems();
		for (MenuItem menuItem : items) {
			if ((menuItem.getStyle() & SWT.RADIO) != 0) {
				menuItem.dispose();
			}
		}
		String currentFormat = SamplerPreferences.getCurrentFormat();
		String[] customFormats = SamplerPreferences.getCustomFormats();
		for(final String customFormat : customFormats) {
			String displayFormat = customFormat;
			if (customFormat.contains(SamplerPreferences.LABLE_VALUE_SEPARATOR)) {
				String[] splitDisplayFormat = customFormat.split(Pattern.quote(SamplerPreferences.LABLE_VALUE_SEPARATOR));
				displayFormat = splitDisplayFormat[0];
			}
			MenuItem menuItem = new MenuItem(menu, SWT.RADIO);
			menuItem.setText(displayFormat);
			menuItem.setSelection(customFormat.equals(currentFormat));
			menuItem.addSelectionListener(new SelectionAdapter() {
				public void widgetSelected(SelectionEvent e) {
					setCurrentFormat(customFormat);
					refreshColorDisplay();
					refreshClipboard();
				}
			});
		}
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
	
	private void setCurrentFormat(String currentFormat) {
		setFormatString(currentFormat);
		SamplerPreferences.setCurrentFormat(currentFormat);
	}

	private String formatString;

	private void setFormatString(String currentFormat) {
		if (currentFormat.contains(SamplerPreferences.LABLE_VALUE_SEPARATOR)) {
			currentFormat = currentFormat.split(Pattern.quote(SamplerPreferences.LABLE_VALUE_SEPARATOR))[1];
		}
		formatString = processedFormatString(currentFormat);
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
