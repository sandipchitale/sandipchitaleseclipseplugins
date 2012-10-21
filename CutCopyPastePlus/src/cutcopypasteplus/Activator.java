package cutcopypasteplus;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "CutCopyPastePlus"; //$NON-NLS-1$

	public static final String IS_CUT_AND_COPY_HISTORY_ENABLED = "cutcopypasteplus.autoClipCutAndCopy";
	public static final String MAX_HISTORY_COUNT = "cutcopypasteplus.maxHistoryCount";
	public static final String PASTE_NEXT_DELAY = "cutcopypasteplus.pasteNextDelay";
	public static final String IS_QUICK_PASTE_CYCLES_THROUGH_HISTORY_ENABLED = "cutcopypasteplus.quickPasteCyclesThroughHistory";

	private static final boolean defaultIS_CUT_AND_COPY_HISTORY_ENABLED = true;
	private static final int defaultMAX_HISTORY_COUNT = 64;
	private static final int defaultPASTE_NEXT_DELAY = 1000;
	private static final boolean defaultIS_QUICK_PASTE_CYCLES_THROUGH_HISTORY_ENABLED = false;

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
		store.setDefault(IS_CUT_AND_COPY_HISTORY_ENABLED, defaultIS_CUT_AND_COPY_HISTORY_ENABLED);
		store.setDefault(MAX_HISTORY_COUNT, defaultMAX_HISTORY_COUNT);
		store.setDefault(PASTE_NEXT_DELAY, defaultPASTE_NEXT_DELAY);
		store.setDefault(IS_QUICK_PASTE_CYCLES_THROUGH_HISTORY_ENABLED, defaultIS_QUICK_PASTE_CYCLES_THROUGH_HISTORY_ENABLED);
	}

	public boolean isCutAndCopyHistoryEnabled() {
		return getPreferenceStore().getBoolean(IS_CUT_AND_COPY_HISTORY_ENABLED);
	}
	
	public int getMaxHistoryCount() {
		return getPreferenceStore().getInt(MAX_HISTORY_COUNT);
	}

	public int getPasteNextDelay() {
		return  getPreferenceStore().getInt(PASTE_NEXT_DELAY);
	}
	
	public boolean isQuickPasteCyclesThroughHistory() {
		return getPreferenceStore().getBoolean(IS_QUICK_PASTE_CYCLES_THROUGH_HISTORY_ENABLED);
	}
	
	void addExecutionListener() {
		// Add listener to monitor Cut and Copy commands
		ICommandService commandService = (ICommandService) PlatformUI.getWorkbench().getAdapter(ICommandService.class);
		if (commandService != null) {
			commandService.addExecutionListener(new IExecutionListener() {
				private long lastPrePasteMillis = System.currentTimeMillis();
				private String savedClipboardString = null;
				private int quickPasteOrdinal = 0;
				private String lastPastedString = null;
				public void notHandled(String commandId, NotHandledException exception) {
					clearAfter(commandId);
				}

				public void postExecuteFailure(String commandId, ExecutionException exception) {
					clearAfter(commandId);
				}

				public void preExecute(String commandId, ExecutionEvent event) {
					if (Activator.getDefault().isQuickPasteCyclesThroughHistory() && CutCopyHistory.getInstance().size() > 1) {
						// Is it a Paste command ?
						if (org.eclipse.ui.IWorkbenchCommandConstants.EDIT_PASTE.equals(commandId)) {
							// Yes
							try {
								// Reset
								savedClipboardString = null;
								long currentPrePasteMillis = System.currentTimeMillis();
								if ((currentPrePasteMillis - lastPrePasteMillis) < Activator.getDefault().getPasteNextDelay()) {
									// User has pasted quickly enough
									quickPasteOrdinal++;
									// This is second paste, so preselect the text pasted in first paste
									if (quickPasteOrdinal == 1) { // 0 == 1st, 1 == 2nd
										Control focusedControl = IsFocusedInTextPropertyTester.getFocusControl();
										if (focusedControl instanceof Text) {
											final Text text = (Text) focusedControl;
											final int caretOffset = text.getSelection().x;
											PlatformUI.getWorkbench().getDisplay().syncExec(new Runnable() {
												@Override
												public void run() {
													text.setSelection(caretOffset - lastPastedString.length(), caretOffset);
												}
											});
										} else if (focusedControl instanceof StyledText) {
											StyledText styledText = (StyledText) focusedControl;
											int caretOffset = styledText.getCaretOffset();
											styledText.setSelection(caretOffset - lastPastedString.length(), caretOffset);
										}
									}
									lastPastedString = null;
									Object contents = getClipboard().getContents(TextTransfer.getInstance());
									if (contents instanceof String) {
										savedClipboardString = (String) contents;
									}
									String nextTextToPaste = CutCopyHistory.getInstance().getNextTextToPaste();
									if (nextTextToPaste != null) {
										getClipboard().setContents(new Object[] {nextTextToPaste}, new Transfer[] {TextTransfer.getInstance()});
									}
								} else {
									quickPasteOrdinal = 0;
									Object contents = getClipboard().getContents(TextTransfer.getInstance());
									if (contents instanceof String) {
										lastPastedString = (String) contents;
									}
									// User is not pasting quickly enough
									CutCopyHistory.getInstance().reset();
									// Is history stale
									if (!CutCopyHistory.getInstance().isStale()) {
										// No - therefore the clipboard and first item of
										// CutCopyHistory are same so simply advance
										// past it
										CutCopyHistory.getInstance().getNextTextToPaste();
									}
								}
							} finally {
								// Remember the timestamp of this paste invocation
								lastPrePasteMillis = System.currentTimeMillis();
							}
						}
					}
				}

				public void postExecuteSuccess(String commandId, Object returnValue) {
					if (Activator.getDefault().isCutAndCopyHistoryEnabled()) {
						// Is it a Cut or Copy command
						if (org.eclipse.ui.IWorkbenchCommandConstants.EDIT_COPY.equals(commandId)
								|| org.eclipse.ui.IWorkbenchCommandConstants.EDIT_CUT.equals(commandId)) {
							Clipboard clipboard = getClipboard();
							if (clipboard != null) {
								Object contents = getClipboard().getContents(TextTransfer.getInstance());
								if (contents instanceof String) {
									if (Activator.getDefault().isCutAndCopyHistoryEnabled()) {
										CutCopyHistory.getInstance().setStale(false);
										CutCopyHistory.getInstance().add((String) contents);
									} else {
										CutCopyHistory.getInstance().setStale(true);
									}
								}
							}
						}
					}
					if (Activator.getDefault().isQuickPasteCyclesThroughHistory() && CutCopyHistory.getInstance().size() > 1) {
						// Is it a Paste command
						if (org.eclipse.ui.IWorkbenchCommandConstants.EDIT_PASTE.equals(commandId)) {
							try {
								// Select the just pasted text
								if (quickPasteOrdinal > 0) {
									Object contents = getClipboard().getContents(TextTransfer.getInstance());
									if (contents instanceof String) {
										final String string = (String) contents;
										Control focusedControl = IsFocusedInTextPropertyTester.getFocusControl();
										if (focusedControl instanceof Text) {
											final Text text = (Text) focusedControl;
											final int caretOffset = text.getSelection().x;
											PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
												@Override
												public void run() {
													text.setSelection(caretOffset - string.length(), caretOffset);
												}
											});
										} else if (focusedControl instanceof StyledText) {
											StyledText styledText = (StyledText) focusedControl;
											int caretOffset = styledText.getCaretOffset();
											styledText.setSelection(caretOffset - string.length(), caretOffset);
										}
									}
								}
								// Restore clipboard
								if (savedClipboardString != null) {
									getClipboard().setContents(new Object[] {savedClipboardString}, new Transfer[] {TextTransfer.getInstance()});
								}
							} finally {
								savedClipboardString = null;
							}
						}
					}
				}
				
				private void clearAfter(String commandId) {
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
