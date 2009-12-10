package keystrokes;

import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.bindings.keys.SWTKeySupport;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.keys.BindingService;
import org.eclipse.ui.keys.IBindingService;

@SuppressWarnings("restriction")
public class ToggleKeyStrokesView extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = getKeyStrokeViewShell();
		if (shell != null) {
			shell.setVisible(!shell.isVisible());
		}
		return null;
	}
	
	private static int WIDTH;
	private static int HEIGHT;
	private static int SHELL_STYLE;
	
	static {
		if (Platform.OS_MACOSX.equals(Platform.getOS())) {
			WIDTH = 200;
			HEIGHT = 90;
		} else if (Platform.OS_WIN32.equals(Platform.getOS())) {
			WIDTH = 200;
			HEIGHT = 90;
		} else if (Platform.OS_LINUX.equals(Platform.getOS())) {
			WIDTH = 200;
			HEIGHT = 80;
		} else if (Platform.OS_MACOSX.equals(Platform.getOS())) {
			WIDTH = 420;
			HEIGHT = 100;
		}
		SHELL_STYLE = SWT.TITLE | SWT.CLOSE | SWT.BORDER | SWT.ON_TOP;
	}
	
	private static Shell keyStrokeViewShell;
	private static Timer timer;

	public static Shell getKeyStrokeViewShell() {
		if (keyStrokeViewShell == null || keyStrokeViewShell.isDisposed()) {
			IWorkbench workbench = PlatformUI.getWorkbench();
			final Display display = workbench.getDisplay();
			Font LARGE_FONT = new Font(display, JFaceResources.TEXT_FONT,
					32, SWT.NORMAL);
			Color BLACK = display.getSystemColor(SWT.COLOR_BLACK);
			Color WHITE = display.getSystemColor(SWT.COLOR_WHITE);
			keyStrokeViewShell = new Shell(display, SHELL_STYLE);
			keyStrokeViewShell.setBackground(BLACK);
			GridLayout gridLayout = new GridLayout();
			gridLayout.marginWidth = 10;
			gridLayout.marginHeight = 10;
			keyStrokeViewShell.setLayout(gridLayout);
			
			final Label label = new Label(keyStrokeViewShell, SWT.CENTER | SWT.NO_BACKGROUND | SWT.NO_FOCUS);

			GridData labelGridData = new GridData(SWT.FILL, SWT.FILL, true, true);
			label.setLayoutData(labelGridData);

			label.setFont(LARGE_FONT);
			label.setBackground(BLACK);
			label.setForeground(WHITE);
			label.setText("");
			final Listener listener = new Listener() {
				public void handleEvent(Event event) {
					if (timer != null) {
						timer.cancel();
						timer = null;
					}
					if (keyStrokeViewShell.isVisible()) {
						int accelerator;
						if ((event.stateMask == SWT.NONE | event.stateMask == SWT.SHIFT) &&
								( 
								Character.isLetterOrDigit(event.character) ||
								event.character == '<' ||
								event.character == '>' ||
								event.character == '?' ||
								event.character == ':' ||
								event.character == '"' ||
								event.character == '{' ||
								event.character == '}' ||
								event.character == '|' ||
								event.character == '_' ||
								event.character == '+'
								)) {
							label.setText(""+event.character);
						} else {
							accelerator = SWTKeySupport.convertEventToUnmodifiedAccelerator(event);
							label.setText(SWTKeySupport.convertAcceleratorToKeyStroke(accelerator).format());
						}
						timer = new Timer(true);
						timer.schedule(new TimerTask() {
							@Override
							public void run() {
								timer = null;
								if (!display.isDisposed()) {
									display.asyncExec(new Runnable() {
										public void run() {
											if (!label.isDisposed()) {
												label.setText("");
											}
										}			
									});
								}
							}
						}, 1000);
					}
				}
			};
			IBindingService iBindingService = (IBindingService) workbench.getService(IBindingService.class);
			if (iBindingService instanceof BindingService) {
				BindingService bindingService = (BindingService) iBindingService;
				try {
					if ( bindingService.getKeyboard().getKeyDownFilter() != null) {
						display.removeFilter(SWT.KeyDown,  bindingService.getKeyboard().getKeyDownFilter());
					}
					display.addFilter(SWT.KeyDown, listener);
				} finally {
					if ( bindingService.getKeyboard().getKeyDownFilter() != null) {
						display.addFilter(SWT.KeyDown,  bindingService.getKeyboard().getKeyDownFilter());
					}
				}
			} else {
				display.addFilter(SWT.KeyDown, listener);
			}
			keyStrokeViewShell.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent e) {
					display.removeFilter(SWT.KeyDown, listener);
					keyStrokeViewShell = null;
				}
			});
			
			Rectangle displayBounds = display.getBounds();
			
			keyStrokeViewShell.setBounds(
					displayBounds.x+displayBounds.width-WIDTH-2,
					displayBounds.y+displayBounds.height-HEIGHT-2,
					WIDTH,
					HEIGHT);
		}
		return keyStrokeViewShell;
	}
}
