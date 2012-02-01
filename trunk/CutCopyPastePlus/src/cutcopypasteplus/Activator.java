package cutcopypasteplus;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "CutCopyPastePlus"; //$NON-NLS-1$

	public static final String ENHANCED_CUT_COPY_PASTE = "cutcopypasteplus.autoClipCutAndCopy";
	public static final String MAX_HISTORY_COUNT = "cutcopypasteplus.maxHistoryCount";
	public static final String PASTE_NEXT_DELAY = "cutcopypasteplus.pasteNextDelay";

	private static final boolean defaultENHANCED_CUT_COPY_PASTE = true;
	private static final int defaultMAX_HISTORY_COUNT = 64;
	private static final int defaultPASTE_NEXT_DELAY = 1000;

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		addExecutionListener();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	@Override
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(ENHANCED_CUT_COPY_PASTE, defaultENHANCED_CUT_COPY_PASTE);
		store.setDefault(MAX_HISTORY_COUNT, defaultMAX_HISTORY_COUNT);
		store.setDefault(PASTE_NEXT_DELAY, defaultPASTE_NEXT_DELAY);
	}

	public boolean isEnhancedCutCopyPaste() {
		return getPreferenceStore().getBoolean(ENHANCED_CUT_COPY_PASTE);
	}
	
	public int getMaxHistoryCount() {
		return getPreferenceStore().getInt(MAX_HISTORY_COUNT);
	}

	public int getPasteNextDelay() {
		return  getPreferenceStore().getInt(PASTE_NEXT_DELAY);
	}
	
	void addExecutionListener() {
		// Add listener to monitor Cut and Copy commands
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getAdapter(ICommandService.class);
		if (commandService != null) {
			commandService.addExecutionListener(new IExecutionListener() {

				public void notHandled(String commandId, NotHandledException exception) {
				}

				public void postExecuteFailure(String commandId, ExecutionException exception) {
				}

				public void preExecute(String commandId, ExecutionEvent event) {
					if (Activator.getDefault().isEnhancedCutCopyPaste()) {
						// Is it a Paste command
						if (org.eclipse.ui.IWorkbenchCommandConstants.EDIT_PASTE.equals(commandId)) {
						}
					}
				}

				public void postExecuteSuccess(String commandId, Object returnValue) {
					if (Activator.getDefault().isEnhancedCutCopyPaste()) {
						// Is it a Cut or Copy command
						if (org.eclipse.ui.IWorkbenchCommandConstants.EDIT_COPY.equals(commandId)
								|| org.eclipse.ui.IWorkbenchCommandConstants.EDIT_CUT.equals(commandId)) {
							Clipboard clipboard = getClipboard();
							if (clipboard != null) {
								Object contents = clipboard.getContents(TextTransfer.getInstance());
								if (contents instanceof String) {
									CutCopyHistory.getInstance().add((String) contents);
								}
							}
						} else if (org.eclipse.ui.IWorkbenchCommandConstants.EDIT_PASTE.equals(commandId)) {
						}
					}
				}

			});
		}
	}

	Clipboard getClipboard() {
		Display display = PlatformUI.getWorkbench().getDisplay();
		if (display != null) {
			return new Clipboard(display);
		}
		return null;
	}

}
