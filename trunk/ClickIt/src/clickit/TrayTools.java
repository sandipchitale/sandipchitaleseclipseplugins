package clickit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

public class TrayTools {
	public static void main(String[] args) {
		final Display display = new Display();
		final Image desktopImage = new Image(display, TrayTools.class.getResourceAsStream("desktop.png"));
		final Image desktopAreaImage = new Image(display, TrayTools.class.getResourceAsStream("desktoparea.png"));
		final Tray tray = display.getSystemTray();
		if (tray == null) {
			System.err.println("The system tray is not available! Exiting.");
		} else {
			final Shell shell = new Shell(display, SWT.NO_TRIM);
			shell.setBounds(0, 0, 0, 0);
			final TrayItem item = new TrayItem(tray, SWT.NONE);
			item.setImage(desktopImage);
			item.setData("DEFAULT_IMAGE", desktopImage);
			item.setToolTipText("Click It");
			item.addListener(SWT.MouseDown, new Listener() {
				public void handleEvent(Event event) {
					Image desktopImage = Util.getDesktopImage(shell.getDisplay());
					Util.processImage(shell, desktopImage);
				}
			});

			final Menu menu = new Menu(shell, SWT.POP_UP);
			item.addListener(SWT.MenuDetect, new Listener() {
				public void handleEvent(Event event) {
					menu.setVisible(true);
				}
			});

			MenuItem mi = new MenuItem(menu, SWT.PUSH);
			mi.setText("Desktop");
			mi.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					item.setImage(new Image(display, 16, 16));
					animate(display, new Runnable() {
						public void run() {
							item.setImage(desktopImage);
							Image desktopImage = Util.getDesktopImage(shell.getDisplay());
							Util.processImage(shell, desktopImage);
							desktopImage.dispose();
						}
					}, 5, item);
				}
			});
			mi.setImage(desktopImage);

			mi = new MenuItem(menu, SWT.PUSH);
			mi.setText("Desktop Area");
			mi.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					item.setImage(new Image(display, 16, 16));
					animate(display, new Runnable() {
						public void run() {
							item.setImage(desktopImage);
							Image image = Util.getDesktopAreaImage(shell);
							if (image != null) {
								Util.processImage(shell, image);
							}
							image.dispose();
						}
					}, 5, item);
				}
			});
			mi.setImage(desktopAreaImage);

			mi = new MenuItem(menu, SWT.PUSH);
			mi.setText("Quit");
			mi.addListener(SWT.Selection, new Listener() {
				public void handleEvent(Event event) {
					shell.close();
				}
			});
			shell.open();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			tray.dispose();
		}
		display.dispose();
		System.exit(0);
	}

	private static final void animate(final Display display, final Runnable runnable, final int countdown, final TrayItem item) {
		Image image = item.getImage();
		Rectangle bounds = image.getBounds();
		GC gc = new GC(image);
		gc.setBackground(display.getSystemColor(SWT.COLOR_BLACK));
		gc.fillRectangle(bounds);
		gc.setForeground(display.getSystemColor(SWT.COLOR_WHITE));
		gc.drawString("" + countdown, 4, -1, true);
		gc.dispose();
		item.setImage(image);
		if (countdown == 0) {
			runnable.run();
		} else {
			display.timerExec(1000, new Runnable() {
				public void run() {
					animate(display, runnable, countdown - 1, item);
				}
			});
		}
	}

}
