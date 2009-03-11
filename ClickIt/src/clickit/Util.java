package clickit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.printing.PrintDialog;
import org.eclipse.swt.printing.Printer;
import org.eclipse.swt.printing.PrinterData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Dialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tracker;

public class Util {
	public static Image getDesktopImage(Display display) {
		return Desktop.getDesktopImage(display);
	}

	public static Image getDesktopAreaImage(Shell parentShell) {
		Display display = parentShell.getDisplay();
		return Desktop.getDesktopAreaImage(display);
	}

	public static Image getShellImage(Shell shell) {
		Display display = shell.getDisplay();
		return Desktop.getDesktopAreaImage(display, shell.getBounds());
	}

	public static Image getControlImage(Control control) {
		Display display = control.getDisplay();
		return Desktop.getDesktopAreaImage(display, display.map(control, null, control.getBounds()));
	}

	public static void processImage(Shell shell, Image image) {
		if (shell.getMinimized()) {
			shell.setMinimized(false);
		}
		shell.setActive();
		new ImageDialog(shell, image).open();
	}

	private static class ImageDialog extends Dialog {

		private final Image image;

		protected ImageDialog(Shell shell, Image image) {
			super(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);
			this.image = image;
		}
		
		void open() {
			final Shell shell = new Shell(getParent(), getStyle());
			shell.setLayout(new GridLayout(2, false));
			Display display = shell.getDisplay();
			ScrolledComposite scroller = new ScrolledComposite(shell, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			GridData scrollerGridData = new GridData();
			scrollerGridData.verticalSpan = 4;
			scrollerGridData.widthHint = display.getBounds().width / 4;
			scrollerGridData.heightHint = display.getBounds().height / 4;
			scroller.setLayoutData(scrollerGridData);

			final Canvas canvas = new Canvas(scroller, SWT.NONE);
			scroller.setContent(canvas);
			Rectangle bounds = image.getBounds();
			canvas.setSize(bounds.width, bounds.height);
			canvas.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					Rectangle bounds = image.getBounds();
					e.gc.drawImage(image, 0, 0, bounds.width, bounds.height, 0, 0, bounds.width, bounds.height);
					e.gc.drawRectangle(0, 0, bounds.width - 1, bounds.height - 1);
				}
			});

			Button saveToFileButton = new Button(shell, SWT.PUSH);
			saveToFileButton.setText("Save to File...");
			GridData saveToFileButtonGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
			saveToFileButton.setLayoutData(saveToFileButtonGridData);

			saveToFileButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}

				public void widgetSelected(SelectionEvent e) {
					saveToFile(shell, image);
				}
			});

			Button copyToClipboardButton = new Button(shell, SWT.PUSH);
			copyToClipboardButton.setText("Copy to Clipboard...");
			GridData copyToClipboardButtonGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
			copyToClipboardButton.setLayoutData(copyToClipboardButtonGridData);
			copyToClipboardButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}

				public void widgetSelected(SelectionEvent e) {
					copyToClipboard(shell, image);
				}
			});

			Button printButton = new Button(shell, SWT.PUSH);
			printButton.setText("Print...");
			GridData printButtonGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
			printButton.setLayoutData(printButtonGridData);
			printButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}
				
				public void widgetSelected(SelectionEvent e) {
					print(shell, image);
				}
			});
			
			Button closeButton = new Button(shell, SWT.PUSH);
			closeButton.setText("Close");
			GridData closeButtonGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
			closeButton.setLayoutData(closeButtonGridData);
			closeButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}

				public void widgetSelected(SelectionEvent e) {
					shell.close();
				}
			});
			
			shell.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					if (image != null && !image.isDisposed()) {
						image.dispose();
					}
				}
			});
			
			shell.pack();
			shell.open();
			shell.forceActive();
			shell.forceFocus();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			};
		}

		private static void saveToFile(Shell shell, Image image) {
			FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
			fileDialog.setOverwrite(true);
			fileDialog.setFilterExtensions(new String[] { "*.png", "*.gif", "*.jpg", "*.bmp", "*.ico" });
			fileDialog.setFilterIndex(0);
			fileDialog.setFilterPath(System.getProperty("java.io.tmpdir", System.getProperty("usre.home")));
			fileDialog.setFileName("screenshot.png");
			final String fileName = fileDialog.open();
			if (fileName != null) {
				final ImageLoader imageLoader = new ImageLoader();
				imageLoader.data = new ImageData[] { image.getImageData() };
				shell.getDisplay().asyncExec(new Runnable() {
					public void run() {
						int format = SWT.IMAGE_PNG;
						String lowerCaseFileName = fileName.toLowerCase();
						if (lowerCaseFileName.endsWith(".gif")) {
							format = SWT.IMAGE_GIF;
						} else if (lowerCaseFileName.endsWith(".jpg")) {
							format = SWT.IMAGE_JPEG;
						} else if (lowerCaseFileName.endsWith(".bmp")) {
							format = SWT.IMAGE_BMP;
						} else if (lowerCaseFileName.endsWith(".ico")) {
							format = SWT.IMAGE_ICO;
						}
						imageLoader.save(fileName, format);
					}

				});
			}
		}

		private static void copyToClipboard(Shell shell, Image image) {
			ImageTransfer imageTransfer = ImageTransfer.getInstance();
			TextTransfer textTransfer = TextTransfer.getInstance();
			new Clipboard(shell.getDisplay()).setContents(new Object[] { image.getImageData(), "screenshot" }, new Transfer[] { imageTransfer, textTransfer });
		}
		
		private static void print(Shell shell, final Image image) {
			PrintDialog printDialog = new PrintDialog(shell);
			final PrinterData printerData = printDialog.open();
			if (printerData == null) return;
			final Printer printer = new Printer(printerData);
			final ImageData imageData = image.getImageData();
			final Image printerImage = new Image(printer, imageData);
			Thread printThread = new Thread(new Runnable() {
				public void run() {
					// Determine the bounds of the entire area of the printer
					Rectangle trim = printer.computeTrim(0, 0, 0, 0);

					// Start the print job
					if (printer.startJob("Screenshot")) {
						if (printer.startPage()) {
							GC gc = new GC(printer);
							// Draw the image
							gc.drawImage(printerImage, 0, 0, imageData.width,
									imageData.height,
									-trim.x, -trim.y, 
									imageData.width, 
									imageData.height);

							// Clean up
							printer.endPage();
							gc.dispose();
						}
					}
					// Dispose the image
					printerImage.dispose();
					// End the job and dispose the printer
					printer.endJob();
					printer.dispose();

				}
			}, "Printing Screenshot");
			printThread.start();
		}
	}

	/**
	 * This class provides utility methods to get bounds or image of rectangular
	 * area of the desktop in a interactive fashion.
	 * 
	 * @author Sandip V. chitale
	 * 
	 */
	public static class Desktop {

		/**
		 * Return image of selected area of the desktop.
		 * 
		 * @param display
		 *            display corresponding to the desktop
		 * @return Image of the selected reactangular area of the desktop
		 */
		public static Image getDesktopAreaImage(Display display) {
			Rectangle bounds = getBounds(display);
			if (bounds != null) {
				return getDesktopAreaImage(display, bounds);
			}
			return null;
		}

		public static Image getDesktopImage(Display display) {
			return getDesktopAreaImage(display, display.getBounds());
		}

		public static Image getDesktopAreaImage(Display display, Rectangle bounds) {
			GC gc = new GC(display);
			Image image = new Image(display, new Rectangle(0, 0, bounds.width, bounds.height));
			gc.copyArea(image, bounds.x, bounds.y);
			gc.dispose();
			return image;
		}

		/**
		 * Return the bounds of selected area of the desktop.
		 * 
		 * @param display
		 *            display corresponding to the desktop
		 * @return
		 */
		public static Rectangle getBounds(Display display) {
			return getRectangle(display);
		}

		private static Rectangle getRectangle(final Display display) {
			final Rectangle[] rectangle = new Rectangle[1];

			// Create transparent shell
			final Shell shell = new Shell(display, SWT.NO_TRIM | SWT.ON_TOP);
			shell.setAlpha(1);

			// Make the shell same size as the display
			shell.setBounds(display.getBounds());

			// Listen to mouse down events
			shell.addListener(SWT.MouseDown, new Listener() {
				public void handleEvent(Event event) {
					switch (event.type) {
					case SWT.MouseDown:
						// Remove listener
						shell.removeListener(SWT.MouseDown, this);
						final Display display = shell.getDisplay();
						// Create a tracker i.e. rubber-band to select the area
						// of desktop
						Tracker tracker = new Tracker(display, SWT.RESIZE);
						tracker.setCursor(display.getSystemCursor(SWT.CURSOR_SIZESE));
						tracker.setStippled(true);
						tracker.setRectangles(new Rectangle[] { new Rectangle(event.x, event.y, 0, 0) });
						// Show the tracker
						if (tracker.open()) {
							rectangle[0] = tracker.getRectangles()[0];
							shell.setBounds(0, 0, 0, 0);
							display.asyncExec(new Runnable() {
								public void run() {
									display.update();
									// Close shell
									shell.close();
								}
							});
						}
						break;
					}
				}
			});
			// Set the cross-hair cursor
			shell.setCursor(display.getSystemCursor(SWT.CURSOR_CROSS));
			// Show the shell
			shell.open();

			shell.forceActive();
			shell.forceFocus();

			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			return rectangle[0];
		}
	}
}