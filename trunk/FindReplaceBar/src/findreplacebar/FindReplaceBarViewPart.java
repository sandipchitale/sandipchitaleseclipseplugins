package findreplacebar;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.ISizeProvider;
import org.eclipse.ui.IViewLayout;
import org.eclipse.ui.IWorkbenchCommandConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.swt.IFocusService;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * This view implements the Find Replace bar.
 *
 * @author Sandip Chitale
 *
 */
@SuppressWarnings("restriction")
public class FindReplaceBarViewPart extends ViewPart implements IViewLayout, ISizeProvider{

	boolean wasHidden = true;
	private Combo findCombo;

//	private ToolItem allScope;
//	private ToolItem selectedLinesScope;

	private ToolItem previous;
	private ToolItem next;

	private ToolItem countOfTotal;
	private Text total;

	private ToolItem caseSensitive;
	private ToolItem regularExpression;
	private ToolItem wholeWord;

//	private Combo replaceCombo;
//	private ToolItem replaceFind;
//	private ToolItem replace;
//	private ToolItem replaceAll;

	private ToolItem close;

	private int incrementalOffset = -1;

	private IStatusLineManager statusLineManager;

	public FindReplaceBarViewPart() {
	}

	@Override
	protected void setSite(IWorkbenchPartSite site) {
		super.setSite(site);
		statusLineManager = ((PartSite)site).getActionBars().getStatusLineManager();

		IContextService contextService = (IContextService) site.getService(IContextService.class);
		contextService.activateContext("findreplacebar.context"); //$NON-NLS-1$

		IHandlerService handlerService = (IHandlerService) site.getService(IHandlerService.class);
		handlerService.activateHandler("findreplacebar.hide", new HideFindReplaceBarHandler()); //$NON-NLS-1$
		handlerService.activateHandler("findreplacebar.findPrevious", new FindPreviousHandler()); //$NON-NLS-1$
		handlerService.activateHandler("findreplacebar.findNext", new FindNextHandler()); //$NON-NLS-1$
		handlerService.activateHandler("findreplacebar.showFindReplaceDialog", new ShowFindReplaceDialogHandler()); //$NON-NLS-1$
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 12;
		gridLayout.makeColumnsEqualWidth = false;
		gridLayout.horizontalSpacing = 5;
		gridLayout.marginTop = 2;
		gridLayout.marginBottom = 2;
		gridLayout.marginHeight = 0;
		gridLayout.verticalSpacing = 0;
		composite.setLayout(gridLayout);

		Label findLabel = new Label(composite, SWT.RIGHT);
		findLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		findLabel.setText("Find:");

//		ToolBar scopeToolbar = new ToolBar(composite, SWT.FLAT);
//		scopeToolbar.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
//		allScope = new ToolItem(scopeToolbar, SWT.RADIO | SWT.FLAT);
//		allScope.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_FIND_SCOPE_ALL));
//		allScope.setSelection(true);
//		allScope.setToolTipText("Find in All Lines");
//
//		selectedLinesScope = new ToolItem(scopeToolbar, SWT.RADIO | SWT.FLAT);
//		selectedLinesScope.setEnabled(false);
//		selectedLinesScope.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_FIND_SCOPE_SELECTED_LINES));
//		selectedLinesScope.setToolTipText("Find in Selected Lines");

		findCombo = new Combo(composite, SWT.DROP_DOWN);
		findCombo.setText("            ");
		GridData findComboGridData =  new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		findComboGridData.widthHint = findCombo.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
	    findCombo.setLayoutData(findComboGridData);
	    findCombo.setText("");

	    findCombo.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
			}

			public void focusGained(FocusEvent e) {
				findCombo.setForeground(null);
				incrementalOffset = -1;
			}
		});

	    ToolBar previousNextToolbar = new ToolBar(composite, SWT.FLAT);
	    previousNextToolbar.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		previous = new ToolItem(previousNextToolbar, SWT.PUSH);
		previous.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_BACK));
		previous.setToolTipText("Find Previous");

		previous.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				findPrevious();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		next = new ToolItem(previousNextToolbar, SWT.PUSH);
		next.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_FORWARD));
		next.setToolTipText("Find Next");
		next.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				findNext();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		Label separator1 = new Label(composite, SWT.NONE);
		separator1.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		separator1.setText("|");
		separator1.setForeground(separator1.getDisplay().getSystemColor(SWT.COLOR_GRAY));

		ToolBar countOfTotalToolbar = new ToolBar(composite, SWT.FLAT);
		countOfTotalToolbar.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		countOfTotal = new ToolItem(countOfTotalToolbar, SWT.CHECK);
		countOfTotal.setSelection(true);
		countOfTotal.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_COUNT_OF_TOTAL));
		countOfTotal.setToolTipText("Show total matches");
		countOfTotal.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				showCountTotal();
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		total = new Text(composite,  SWT.SINGLE | SWT.RIGHT | SWT.BORDER);
		total.setText("      ");
		GridData totalGridData =  new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		totalGridData.widthHint = total.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		total.setLayoutData(totalGridData);
		total.setText("");
		total.setEditable(false);

		Label separator2 = new Label(composite, SWT.NONE);
		separator2.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		separator2.setText("|");
		separator2.setForeground(separator2.getDisplay().getSystemColor(SWT.COLOR_GRAY));

		ToolBar optionsToolbar = new ToolBar(composite, SWT.FLAT);
		optionsToolbar.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		caseSensitive = new ToolItem(optionsToolbar, SWT.CHECK | SWT.FLAT);
		caseSensitive.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_CASE_SENSITIVE));
		caseSensitive.setToolTipText("Case Sensitive");
		caseSensitive.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				find(true, true);
				showCountTotal();
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		wholeWord = new ToolItem(optionsToolbar, SWT.CHECK | SWT.FLAT);
		wholeWord.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_WHOLE_WORD));
		wholeWord.setToolTipText("Whole Word");
		wholeWord.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (wholeWord.getSelection()) {
					regularExpression.setSelection(false);
				}
				find(true, true);
				showCountTotal();
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		regularExpression = new ToolItem(optionsToolbar, SWT.CHECK | SWT.FLAT);
		regularExpression.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_REGULAR_EXRESSION));
		regularExpression.setToolTipText("Regular Expression");
		regularExpression.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				if (regularExpression.getSelection()) {
					wholeWord.setSelection(false);
				}
				find(true, true);
				showCountTotal();
				adjustRegularExpressionState();
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});

//		Label separator3 = new Label(composite, SWT.NONE);
//		separator3.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
//		separator3.setText("|");
//		separator3.setForeground(separator3.getDisplay().getSystemColor(SWT.COLOR_GRAY));

//		Label replaceLabel = new Label(composite, SWT.RIGHT);
//		replaceLabel.setText("Replace with:");
//
//		replaceCombo = new Combo(composite, SWT.DROP_DOWN);
//		replaceCombo.setText("            ");
//		replaceCombo.setLayoutData(new RowData(replaceCombo.computeSize(SWT.DEFAULT, SWT.DEFAULT)));
//		replaceCombo.setText("");
//
//	    ToolBar replaceToolbar = new ToolBar(composite, SWT.FLAT);
//		replaceFind = new ToolItem(replaceToolbar, SWT.PUSH | SWT.FLAT);
//		replaceFind.setText("Replace/Find");
//		replace = new ToolItem(replaceToolbar, SWT.PUSH);
//		replace.setText("Replace");
//		replaceAll = new ToolItem(replaceToolbar, SWT.PUSH);
//		replaceAll.setText("Replace All");
//
		Label separator4 = new Label(composite, SWT.NONE);
		separator4.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, false));

		ToolBar toolsToolbar = new ToolBar(composite, SWT.FLAT);
		toolsToolbar.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		showFindReplaceDialog = new ToolItem(toolsToolbar, SWT.PUSH);
		showFindReplaceDialog.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_FIND));
		showFindReplaceDialog.setToolTipText("Find/Replace...");

		showFindReplaceDialog.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showFindReplaceDialog();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});
		showPreferences = new ToolItem(toolsToolbar, SWT.PUSH);
		showPreferences.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_PREFERENCES));
		showPreferences.setToolTipText("Preferences...");

		showPreferences.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				showFindReplaceBarPreferences();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		Label separator5 = new Label(composite, SWT.NONE);
		separator5.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		separator5.setText("|");
		separator5.setForeground(separator5.getDisplay().getSystemColor(SWT.COLOR_GRAY));

		ToolBar closeToolBar = new ToolBar(composite, SWT.FLAT | SWT.RIGHT);
		closeToolBar.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		close = new ToolItem(closeToolBar, SWT.PUSH);
		close.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_CLOSE));
		close.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				getViewSite().getWorkbenchWindow().getActivePage().hideView(FindReplaceBarViewPart.this);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		findCombo.addModifyListener(modifyListener);

		// Register as focus control
		IFocusService focusService = (IFocusService) getSite().getService(IFocusService.class);
		focusService.addFocusTracker(findCombo, "findreplacebar.findCombo");
	}

	@Override
	public void setFocus() {
		findCombo.setFocus();
	}

	@Override
	public void dispose() {
		super.dispose();
	}

	public void startFind() {
		ISourceViewer sourceViewer = getSourceViewer();
		if (sourceViewer != null) {
			ISelection selection = sourceViewer.getSelectionProvider()
			.getSelection();
			if (selection instanceof ITextSelection) {
				ITextSelection textSelection = (ITextSelection) selection;
				String text = textSelection.getText();
				if (text.indexOf("\n") == -1 && text.indexOf("\r") == -1) { //$NON-NLS-1$ //$NON-NLS-2$
					setFindText(text);
				}
			}
		}

		adjustEnablement();
		boolean comboHasFocus = findCombo.isFocusControl();
		if (!comboHasFocus) {
			findCombo.setFocus();
			incrementalOffset = -1;
		}
	}

	@Override
	public int getSizeFlags(boolean width) {
		if (width) {
			return SWT.MIN | SWT.MAX;
		}
		return 0;
	}

	@Override
	public int computePreferredSize(boolean width, int availableParallel,
			int availablePerpendicular, int preferredResult) {
		if (!width) {
			return 26;
		}
		return 0;
	}

	private static final String EMPTY = ""; //$NON-NLS-1$

	private ModifyListener modifyListener = new ModifyListener() {
		private String lastText = EMPTY; //$NON-NLS-1$

		public void modifyText(ModifyEvent e) {
			findCombo.setForeground(null);
			boolean wrap = true;
			String text = findCombo.getText();
			if (lastText.startsWith(text)) {
				wrap = false;
			}
			lastText = text;
			adjustEnablement();
			adjustRegularExpressionState();
			if (EMPTY.equals(text)) {
				ITextEditor textEditor = getTextEditor();
				if (textEditor != null) {
					ISelectionProvider selectionProvider = textEditor.getSelectionProvider();
					ISelection selection = selectionProvider.getSelection();
					if (selection instanceof TextSelection) {
						ITextSelection textSelection = (ITextSelection) selection;
						selectionProvider.setSelection(
								new TextSelection(textSelection.getOffset(), 0));
					}
				}
			} else {
				find(true, true, wrap);
			}
			showCountTotal();
		}
	};

	private ToolItem showFindReplaceDialog;
	private ToolItem showPreferences;

	private class HideFindReplaceBarHandler extends AbstractHandler {
		public Object execute(ExecutionEvent event) throws ExecutionException {
			hideFindBar();
			return null;
		}
	}

	private class FindPreviousHandler extends AbstractHandler {
		public Object execute(ExecutionEvent event) throws ExecutionException {
			findPrevious();
			return null;
		}
	}

	private class FindNextHandler extends AbstractHandler {
		public Object execute(ExecutionEvent event) throws ExecutionException {
			findNext();
			return null;
		}
	}

	private class ShowFindReplaceDialogHandler extends AbstractHandler {
		public Object execute(ExecutionEvent event) throws ExecutionException {
			showFindReplaceDialog();
			return null;
		}
	}

	private void adjustEnablement() {
		String text = findCombo.getText();
		previous.setEnabled(!EMPTY.equals(text));
		next.setEnabled(!EMPTY.equals(text));
		//count.setText(EMPTY);
		total.setText(EMPTY);
		wholeWord.setEnabled((!EMPTY.equals(text)) && (isWord(text)));
	}

	private void adjustRegularExpressionState() {
		if (regularExpression == null) {
			return;
		}
		String findText = findCombo.getText();
		if (EMPTY.equals(findText)) {
			return;
		}
		if (regularExpression.getSelection()) {
			wholeWord.setSelection(false);
			try {
				Pattern.compile(findText);
			} catch (PatternSyntaxException pse) {
			}
		}
	}

	private void hideFindBar() {
		getViewSite().getWorkbenchWindow().getActivePage().hideView(FindReplaceBarViewPart.this);
	}

	private void findPrevious() {
		find(false);
		setFindText(findCombo.getText());
	}

	private void findNext() {
		find(true);
		setFindText(findCombo.getText());
	}

	private void find(boolean forward) {
		find(forward, false);
	}

	private void find(boolean forward, boolean incremental) {
		find(forward, incremental, true, false);
	}

	private void find(boolean forward, boolean incremental, boolean wrap) {
		find(forward, incremental, wrap, false);
	}

	private void find(boolean forward, boolean incremental, boolean wrap, boolean wrapping) {
		ITextEditor textEditor = getTextEditor();
		if (textEditor == null) {
			getViewSite().getShell().getDisplay().beep();
			statusLineManager.setMessage("No find target!");
			return;
		}

		IFindReplaceTarget findReplaceTarget = (IFindReplaceTarget) textEditor.getAdapter(IFindReplaceTarget.class);
		if (findReplaceTarget != null) {
			try {
				String findText = findCombo.getText();

				if (regularExpression.getSelection()) {
					// Make sure it is a valid regexp
					try {
						Pattern.compile(findText);
					} catch (PatternSyntaxException e) {
						getViewSite().getShell().getDisplay().beep();
						findCombo.setForeground(findCombo.getDisplay().getSystemColor(SWT.COLOR_RED));
						statusLineManager.setMessage("Illegal regular expression");
						return;
					}
				}

				if (findReplaceTarget instanceof IFindReplaceTargetExtension) {
					IFindReplaceTargetExtension findReplaceTargetExtension = (IFindReplaceTargetExtension) findReplaceTarget;
					findReplaceTargetExtension.beginSession();
				}

				ISourceViewer sourceViewer = getSourceViewer();
				StyledText textWidget = sourceViewer.getTextWidget();
				int offset = textWidget.getCaretOffset();
				Point selection = textWidget.getSelection();
				if (wrapping) {
					if (forward) {
						offset = 0;
					} else {
						offset = sourceViewer.getDocument().getLength() - 1;
					}
				} else {
					if (forward) {
						if (incremental) {
							if (incrementalOffset == -1) {
								incrementalOffset = offset;
							} else {
								offset = incrementalOffset;
							}
						} else {
							incrementalOffset = selection.x;
						}
					} else {
						incrementalOffset = selection.x;
						if (selection.x != offset) {
							offset = selection.x;
						}
					}
				}
				int newOffset = -1;
				if (findReplaceTarget instanceof IFindReplaceTargetExtension3) {
					newOffset = ((IFindReplaceTargetExtension3) findReplaceTarget)
							.findAndSelect(offset, findText, forward,
									caseSensitive.getSelection(), wholeWord
											.getEnabled()
											&& wholeWord.getSelection(),
									regularExpression.getSelection());
				} else {
					newOffset = findReplaceTarget.findAndSelect(offset,
							findText, forward, caseSensitive.getSelection(),
							wholeWord.getEnabled() && wholeWord.getSelection());
				}

				if (newOffset != -1) {
					findCombo.setForeground(null);
					if (!forward) {
						selection = textWidget.getSelection();
						incrementalOffset = selection.x;
					}
					statusLineManager.setMessage(EMPTY);
				} else {
					if (wrap) {
						if (!wrapping) {
							find(forward, incremental, wrap, true);
							return;
						}
					}
					findCombo.setForeground(findCombo.getDisplay().getSystemColor(SWT.COLOR_RED));
					textWidget.getDisplay().beep();
					statusLineManager.setMessage("String not found");
				}
			} finally {
				if (findReplaceTarget instanceof IFindReplaceTargetExtension) {
					IFindReplaceTargetExtension findReplaceTargetExtension = (IFindReplaceTargetExtension) findReplaceTarget;
					findReplaceTargetExtension.endSession();
				}
			}
		}
	}

	private void setFindText(String findText) {
		String[] items = findCombo.getItems();
		Set<String> itemSet = new LinkedHashSet<String>();
		itemSet.add(findText);
		itemSet.addAll(Arrays.asList(items));
		try {
			findCombo.removeModifyListener(modifyListener);
			findCombo.setItems(itemSet.toArray(new String[0]));
			findCombo.select(0);
		} finally {
			findCombo.addModifyListener(modifyListener);
		}
	}

	private void showCountTotal() {
		if (!countOfTotal.getSelection()) {
			total.setText(EMPTY);
			return;
		}
		String patternString = findCombo.getText();
		boolean patternStringIsAWord = isWord(patternString);
		int totalMatches = 0;
		if (!EMPTY.equals(patternString)) {
			ISourceViewer sourceViewer = getSourceViewer();
			if (sourceViewer != null) {
				String text = sourceViewer.getDocument().get();
				int flags = 0;
				if (!caseSensitive.getSelection()) {
					flags |= Pattern.CASE_INSENSITIVE;
				}
				if (!regularExpression.getSelection()) {
					patternString = Pattern.quote(patternString);
				}
				if (patternStringIsAWord && wholeWord.getSelection()) {
					patternString = "\\b" + patternString + "\\b"; //$NON-NLS-1$ //$NON-NLS-2$
				}
				// Make sure it is a valid regexp
				try {
					Pattern pattern = Pattern.compile(patternString, flags);
					Matcher matcher = pattern.matcher(text);
					if (matcher.find(0)) {
						totalMatches = 1;
						while (matcher.find()) {
							++totalMatches;
						}
					}
				} catch (PatternSyntaxException e) {
					getViewSite().getShell().getDisplay().beep();
					findCombo.setForeground(findCombo.getDisplay().getSystemColor(SWT.COLOR_RED));
					statusLineManager.setMessage("Illegal regular expression");
					return;
				}
			}
		}
		total.setText(String.valueOf(totalMatches));
	}

	private void showFindReplaceDialog() {
		ITextEditor textEditor = getTextEditor();
		if (textEditor != null) {
			IWorkbenchPartSite site = textEditor.getSite();
			site.getWorkbenchWindow().getActivePage().activate(textEditor);
			IHandlerService handlerService = (IHandlerService) site.getService(IHandlerService.class);
			if (handlerService != null) {
				try {
					handlerService.executeCommand(IWorkbenchCommandConstants.EDIT_FIND_AND_REPLACE, null);
				} catch (ExecutionException e1) {
				} catch (NotDefinedException e1) {
				} catch (NotEnabledException e1) {
				} catch (NotHandledException e1) {
				}
			}
		}
	}

	/**
	 * Tests whether each character in the given string is a letter.
	 *
	 * @param str
	 *            the string to check
	 * @return <code>true</code> if the given string is a word
	 */
	private boolean isWord(String str) {
		if (str == null || str.length() == 0)
			return false;

		for (int i = 0; i < str.length(); i++) {
			if (!Character.isJavaIdentifierPart(str.charAt(i)))
				return false;
		}
		return true;
	}

	private void showFindReplaceBarPreferences() {
		String[] displayedIds = new String[] {"FindReplaceBar.preferences.page"};
		PreferenceDialog preferenceDialog = PreferencesUtil.createPreferenceDialogOn(
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				displayedIds[0],
				displayedIds,
				null);
		preferenceDialog.open();
	}

	/**
	 * @return the active textEditor
	 */
	private ITextEditor getTextEditor() {
		IEditorPart activeEditor = getSite().getWorkbenchWindow().getActivePage().getActiveEditor();
		if (activeEditor instanceof ITextEditor) {
			return (ITextEditor) activeEditor;
		} else if (activeEditor instanceof MultiPageEditorPart) {
			MultiPageEditorPart multiPageEditorPart = (MultiPageEditorPart) activeEditor;
			Object activePage = multiPageEditorPart.getSelectedPage();
			if (activePage instanceof ITextEditor) {
				return (ITextEditor) activePage;
			}
		}
		return null;
	}

	/**
	 * @return the sourceView of the active textEditor
	 */
	private ISourceViewer getSourceViewer() {
		ITextEditor textEditor = getTextEditor();
		if (textEditor != null) {
			return (ISourceViewer) textEditor.getAdapter(ITextOperationTarget.class);
		}
		return null;
	}

	@Override
	public boolean isCloseable() {
		return false;
	}

	@Override
	public void setCloseable(boolean closeable) {}

	@Override
	public boolean isMoveable() {
		return false;
	}

	@Override
	public void setMoveable(boolean moveable) {}

	@Override
	public boolean isStandalone() {
		return true;
	}

	@Override
	public boolean getShowTitle() {
		return false;
	}
}
