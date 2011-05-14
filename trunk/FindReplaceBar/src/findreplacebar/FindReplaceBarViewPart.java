package findreplacebar;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
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
import org.eclipse.ui.progress.WorkbenchJob;
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
	private static final String EMPTY = ""; //$NON-NLS-1$

	private Composite composite;
	private GridLayout compositeGridLayout;

	private Combo findCombo;

//	private ToolItem allScope;
//	private ToolItem selectedLinesScope;

	private ToolItem previous;
	private ToolItem next;

	private ToolItem showMatchCounts;
	private Text precedingMatches;
	private Label plus;
	private GridData plusGridData;
	private GridData precedingMatchesGridData;
	private Text succeedingMatches;
	private GridData succeedingMatchesGridData;
	private Label sum;
	private GridData sumGridData;
	private Text allMatches;
	private GridData allMatchesGridData;

	private ToolItem caseSensitive;
	private ToolItem wholeWord;
	private ToolItem regularExpression;
	private Combo groupsCombo;
	private GridData groupsComboGridData;

//	private Combo replaceCombo;
//	private ToolItem replaceFind;
//	private ToolItem replace;
//	private ToolItem replaceAll;

	private ToolItem showFindReplaceDialog;
	private ToolItem showPreferences;
	private ToolItem close;

	private IStatusLineManager statusLineManager;

	private int incrementalOffset = -1;

	private static boolean showMatchCountsState = false;
	
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
		handlerService.activateHandler("findreplacebar.toggleWholeWordMode", new ToggleWholeWordModeHandler()); //$NON-NLS-1$
		handlerService.activateHandler("findreplacebar.toggleRegularExpressionMode", new ToggleRegularExpressionModeHandler()); //$NON-NLS-1$
		handlerService.activateHandler("findreplacebar.showFindReplaceDialog", new ShowFindReplaceDialogHandler()); //$NON-NLS-1$
	}

	@Override
	public void createPartControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		compositeGridLayout = new GridLayout();
		compositeGridLayout.numColumns = 18;
		compositeGridLayout.makeColumnsEqualWidth = false;
		compositeGridLayout.horizontalSpacing = 5;
		compositeGridLayout.marginTop = 2;
		compositeGridLayout.marginBottom = 2;
		compositeGridLayout.marginHeight = 0;
		compositeGridLayout.verticalSpacing = 0;
		composite.setLayout(compositeGridLayout);

		Label findLabel = new Label(composite, SWT.RIGHT);
		findLabel.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		findLabel.setText(Messages.FindReplaceBarViewPart_Find_Label);

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
		findCombo.setText("                        "); //$NON-NLS-1$
		GridData findComboGridData =  new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		findComboGridData.widthHint = findCombo.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
	    findCombo.setLayoutData(findComboGridData);
	    findCombo.setText(EMPTY);

	    findCombo.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
			}

			public void focusGained(FocusEvent e) {
				findCombo.setBackground(null);
				incrementalOffset = -1;
			}
		});

		Label separator1 = new Label(composite, SWT.NONE);
		separator1.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		separator1.setText("|"); //$NON-NLS-1$
		separator1.setForeground(separator1.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		
	    ToolBar previousToolbar = new ToolBar(composite, SWT.FLAT);
	    previousToolbar.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		previous = new ToolItem(previousToolbar, SWT.PUSH);
		previous.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_BACK));
		previous.setToolTipText(Messages.FindReplaceBarViewPart_Find_Previous_Tooltip);

		previous.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				findPrevious();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
	    ToolBar nextToolbar = new ToolBar(composite, SWT.FLAT);
		next = new ToolItem(nextToolbar, SWT.PUSH);
		next.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_FORWARD));
		next.setToolTipText(Messages.FindReplaceBarViewPart_Find_Next_Tooltip);
		next.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				findNext();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

	    ToolBar showMatchCountsToolbar = new ToolBar(composite, SWT.FLAT);
		showMatchCountsToolbar.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		showMatchCounts = new ToolItem(showMatchCountsToolbar, SWT.CHECK);
		showMatchCounts.setSelection(showMatchCountsState);
		showMatchCounts.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_COUNT_OF_TOTAL));
		showMatchCounts.setToolTipText(Messages.FindReplaceBarViewPart_Show_Matche_Counts_Tooltip);
		showMatchCounts.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				showMatchCountsState = showMatchCounts.getSelection();
				adjustMatchCountVisibility();
				showMatchCounts();
				findCombo.setFocus();
				findCombo.clearSelection();
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		precedingMatches = new Text(composite,  SWT.SINGLE | SWT.RIGHT | SWT.BORDER);
		precedingMatches.setText("        "); //$NON-NLS-1$
		precedingMatchesGridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		precedingMatchesGridData.widthHint = precedingMatches.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		precedingMatches.setLayoutData(precedingMatchesGridData);
		precedingMatches.setText(EMPTY);
		precedingMatches.setEditable(false);
		precedingMatches.setToolTipText(Messages.FindReplaceBarViewPart_Preceding_Matches_Tooltip);
		
		plus = new Label(composite, SWT.NONE);
		plusGridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		plus.setLayoutData(plusGridData);
		plus.setText(" + "); //$NON-NLS-1$
		plus.setForeground(plus.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		
		succeedingMatches = new Text(composite,  SWT.SINGLE | SWT.RIGHT | SWT.BORDER);
		succeedingMatches.setText("        "); //$NON-NLS-1$
		succeedingMatchesGridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		succeedingMatchesGridData.widthHint = succeedingMatches.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		succeedingMatches.setLayoutData(succeedingMatchesGridData);
		succeedingMatches.setText(EMPTY);
		succeedingMatches.setEditable(false);
		succeedingMatches.setToolTipText(Messages.FindReplaceBarViewPart_Succeeding_Matches_Tooltip);
		
		sum = new Label(composite, SWT.NONE);
		sumGridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		sum.setLayoutData(sumGridData);
		sum.setText(" = "); //$NON-NLS-1$
		sum.setForeground(sum.getDisplay().getSystemColor(SWT.COLOR_GRAY));

		allMatches = new Text(composite,  SWT.SINGLE | SWT.RIGHT | SWT.BORDER);
		allMatches.setText("        "); //$NON-NLS-1$
		allMatchesGridData = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		allMatchesGridData.widthHint = allMatches.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		allMatches.setLayoutData(allMatchesGridData);
		allMatches.setText(EMPTY);
		allMatches.setEditable(false);
		allMatches.setToolTipText(Messages.FindReplaceBarViewPart_Total_Matches_Tooltip);

		
		Label separator2 = new Label(composite, SWT.NONE);
		separator2.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		separator2.setText("|"); //$NON-NLS-1$
		separator2.setForeground(separator2.getDisplay().getSystemColor(SWT.COLOR_GRAY));

		ToolBar optionsToolbar = new ToolBar(composite, SWT.FLAT);
		optionsToolbar.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		caseSensitive = new ToolItem(optionsToolbar, SWT.CHECK | SWT.FLAT);
		caseSensitive.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_CASE_SENSITIVE));
		caseSensitive.setToolTipText(Messages.FindReplaceBarViewPart_Case_Sensitive_Tooltip);
		caseSensitive.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				find(true, true);
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		wholeWord = new ToolItem(optionsToolbar, SWT.CHECK | SWT.FLAT);
		wholeWord.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_WHOLE_WORD));
		wholeWord.setToolTipText(Messages.FindReplaceBarViewPart_Whole_Word_Tooltip);
		wholeWord.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				toggleWholeWordMode();
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});

		regularExpression = new ToolItem(optionsToolbar, SWT.CHECK | SWT.FLAT);
		regularExpression.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_REGULAR_EXRESSION));
		regularExpression.setToolTipText(Messages.FindReplaceBarViewPart_Regular_Expression_Tooltip);
		regularExpression.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				toggleRegularExpressionMode();
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		groupsCombo = new Combo(composite, SWT.READ_ONLY | SWT.DROP_DOWN | SWT.FLAT | SWT.NO_FOCUS);
		groupsCombo.setText("                        "); //$NON-NLS-1$
		groupsComboGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		groupsComboGridData.widthHint = groupsCombo.computeSize(SWT.DEFAULT, SWT.DEFAULT).x;
		groupsComboGridData.exclude = true;
		groupsCombo.setLayoutData(groupsComboGridData);
		groupsCombo.setText(EMPTY);
		groupsCombo.setItems(new String[0]);
		groupsCombo.setVisible(false);
		groupsCombo.setToolTipText(Messages.FindReplaceBarViewPart_Show_Matched_Groups_Tooltip);
		
//		Label separator3 = new Label(composite, SWT.NONE);
//		separator3.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
//		separator3.setText("|"); //$NON-NLS-1$
//		separator3.setForeground(separator3.getDisplay().getSystemColor(SWT.COLOR_GRAY));

//		Label replaceLabel = new Label(composite, SWT.RIGHT);
//		replaceLabel.setText("Replace with:");
//
//		replaceCombo = new Combo(composite, SWT.DROP_DOWN);
//		replaceCombo.setText("            "); //$NON-NLS-1$
//		replaceCombo.setLayoutData(new RowData(replaceCombo.computeSize(SWT.DEFAULT, SWT.DEFAULT)));
//		replaceCombo.setText(EMPTY);
//
//	    ToolBar replaceToolbar = new ToolBar(composite, SWT.FLAT);
//		replaceFind = new ToolItem(replaceToolbar, SWT.PUSH | SWT.FLAT);
//		replaceFind.setText("Replace/Find");
//		replace = new ToolItem(replaceToolbar, SWT.PUSH);
//		replace.setText("Replace");
//		replaceAll = new ToolItem(replaceToolbar, SWT.PUSH);
//		replaceAll.setText("Replace All");
//
		Label separator4 = new Label(composite, SWT.RIGHT);
		separator4GridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		separator4.setLayoutData(separator4GridData);
		separator4.setText("|"); //$NON-NLS-1$
		separator4.setForeground(separator2.getDisplay().getSystemColor(SWT.COLOR_GRAY));

		ToolBar toolsToolbar = new ToolBar(composite, SWT.FLAT);
		toolsToolbar.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		showFindReplaceDialog = new ToolItem(toolsToolbar, SWT.PUSH);
		showFindReplaceDialog.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_FIND));
		showFindReplaceDialog.setToolTipText(Messages.FindReplaceBarViewPart_Show_Find_Replace_Dialog_Tooltip);

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
		showPreferences.setToolTipText(Messages.FindReplaceBarViewPart_Show_Preferences_Tooltip);

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
		separator5.setLayoutData(new GridData(SWT.END, SWT.CENTER, false, false));
		separator5.setText("|"); //$NON-NLS-1$
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
		
		adjustGroupsComboVisibility();
		adjustMatchCountVisibility();

		// Register as focus control
		IFocusService focusService = (IFocusService) getSite().getService(IFocusService.class);
		focusService.addFocusTracker(findCombo, "findreplacebar.findCombo"); //$NON-NLS-1$
	}
	
	private void adjustGroupsComboVisibility() {
		groupsCombo.setListVisible(regularExpression.getSelection());
		groupsComboGridData.exclude = !regularExpression.getSelection();
		groupsCombo.setVisible(regularExpression.getSelection());
		separator4GridData.grabExcessHorizontalSpace = !regularExpression.getSelection();
		
		composite.layout();
	}
	
	private void adjustMatchCountVisibility() {
		precedingMatchesGridData.exclude = !showMatchCounts.getSelection();
		plusGridData.exclude = !showMatchCounts.getSelection();
		succeedingMatchesGridData.exclude = !showMatchCounts.getSelection();
		sumGridData.exclude = !showMatchCounts.getSelection();
		allMatchesGridData.exclude = !showMatchCounts.getSelection();
		precedingMatches.setVisible(showMatchCounts.getSelection());
		plus.setVisible(showMatchCounts.getSelection());
		succeedingMatches.setVisible(showMatchCounts.getSelection());
		sum.setVisible(showMatchCounts.getSelection());
		allMatches.setVisible(showMatchCounts.getSelection());
		
		composite.layout();
	}
	
	private void toggleWholeWordMode() {
		if (wholeWord.isEnabled()) {
			if (wholeWord.getSelection()) {
				regularExpression.setSelection(false);
			}
			adjustGroupsComboVisibility();
			find(true, true);
			adjustRegularExpressionState();
		} else {
			beep();
		}
	}

	private void toggleRegularExpressionMode() {
		if (regularExpression.getSelection()) {
			wholeWord.setSelection(false);
		}
		adjustGroupsComboVisibility();
		find(true, true);
		adjustRegularExpressionState();
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

	private ModifyListener modifyListener = new ModifyListener() {
		private String lastText = EMPTY;

		public void modifyText(ModifyEvent e) {
			findCombo.setBackground(null);
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
		}
	};

	private GridData separator4GridData;


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
	
	private class ToggleWholeWordModeHandler extends AbstractHandler {
		public Object execute(ExecutionEvent event) throws ExecutionException {
			if (wholeWord.isEnabled()) {
				wholeWord.setSelection(!wholeWord.getSelection());
				toggleWholeWordMode();
			} else {
				beep();
			}
			return null;
		}
	}
	
	private class ToggleRegularExpressionModeHandler extends AbstractHandler {
		public Object execute(ExecutionEvent event) throws ExecutionException {
			regularExpression.setSelection(!regularExpression.getSelection());
			toggleRegularExpressionMode();
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
		precedingMatches.setText(EMPTY);
		succeedingMatches.setText(EMPTY);
		allMatches.setText(EMPTY);
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
			error(Messages.FindReplaceBarViewPart_No_Find_Target_Message, false);
			return;
		}
		
		IFindReplaceTarget findReplaceTarget = (IFindReplaceTarget) textEditor.getAdapter(IFindReplaceTarget.class);
		if (findReplaceTarget != null) {
			boolean foundOne = false;
			try {
				String findText = findCombo.getText();
				
				Pattern pattern = null;
				
				if (!wrapping) {
					groupsCombo.setText(EMPTY);
					groupsCombo.setItems(new String[0]);
				}
				
				if (regularExpression.getSelection()) {
					// Make sure it is a valid regexp
					int flags = 0;
					if (!caseSensitive.getSelection()) {
						flags |= Pattern.CASE_INSENSITIVE;
					}
					try {
						pattern = Pattern.compile(findText, flags);
					} catch (PatternSyntaxException e) {
						groupsCombo.setListVisible(false);
						error(Messages.FindReplaceBarViewPart_Illegal_Regular_Expression_Message, true);
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
					foundOne = true;
					findCombo.setBackground(null);
					selection = textWidget.getSelection();
					if (!forward) {
						incrementalOffset = selection.x;
					}
					if (pattern != null && regularExpression.getSelection()) {
						String selectionText = textWidget.getSelectionText();
						Matcher matcher = pattern.matcher(selectionText);
						if (matcher.matches()) {
							List<String> groupTexts = new LinkedList<String>();
							int groupCount = matcher.groupCount() + 1;
							groupTexts.add(0 + " " + matcher.group(0)); //$NON-NLS-1$
							for (int m = 1; m < groupCount; m++) {
								groupTexts.add(m + " " + matcher.group(m)); //$NON-NLS-1$
							}
							groupsCombo.setItems(groupTexts.toArray(new String[groupTexts.size()]));
							groupsCombo.select(0);
							groupsCombo.setVisibleItemCount(groupCount);
							groupsCombo.setListVisible(groupCount > 1);
						}
					}
					statusLineManager.setErrorMessage(EMPTY);
				} else {
					if (wrap) {
						if (!wrapping) {
							find(forward, incremental, wrap, true);
							return;
						}
					}
					if (!EMPTY.equals(findText)) {
						error(Messages.FindReplaceBarViewPart_String_Not_Found_Message, true);
					}
				}
			} finally {
				if (foundOne) {
					showMatchCounts();
				}
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
			findCombo.clearSelection();
		} finally {
			findCombo.addModifyListener(modifyListener);
		}
	}

	private void showMatchCounts() {
		try {
			if (!showMatchCounts.getSelection()) {
				return;
			}
		} finally {
			precedingMatches.setText(EMPTY);
			allMatches.setText(EMPTY);
			succeedingMatches.setText(EMPTY);
		}
		WorkbenchJob workbenchJob = new WorkbenchJob(EMPTY) {
			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				String patternString = findCombo.getText();
				boolean patternStringIsAWord = isWord(patternString);
				int totalMatches = 0;
				int previousMatches = 0;
				int nextMatches = 0;
				if (!EMPTY.equals(patternString)) {
					ISourceViewer sourceViewer = getSourceViewer();
					if (sourceViewer != null) {
						StyledText textWidget = sourceViewer.getTextWidget();
						int offset = textWidget.getSelectionRange().x;
						String text = textWidget.getText();
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
						// Make sure it is a valid regular expression
						Matcher matcher = null;
						try {
							Pattern pattern = Pattern.compile(patternString, flags);
							matcher = pattern.matcher(text);
						} catch (PatternSyntaxException e) {
							error(Messages.FindReplaceBarViewPart_Illegal_Regular_Expression_Message, true);
							return Status.CANCEL_STATUS;
						}
						if (matcher != null) {
							while (matcher.find()) {
								int end = matcher.end();
								if (offset > end) {
									previousMatches++;
								} else {
									nextMatches++;
								}
								++totalMatches;
							}
						}
					}
				}
				precedingMatches.setText(EMPTY+previousMatches);
				allMatches.setText(EMPTY+totalMatches);
				succeedingMatches.setText(EMPTY+nextMatches);
				return Status.OK_STATUS;
			}
		};
		workbenchJob.setPriority(Job.INTERACTIVE);
		workbenchJob.setSystem(true);
		workbenchJob.schedule();
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
		String[] displayedIds = new String[] {FindReplaceBarPreferencePage.ID_FIND_REPLACE_BAR_PREFERENCE_PAGE};
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

	private void error(String message, boolean errorBackground) {
		beep();
		statusLineManager.setErrorMessage(message);
		precedingMatches.setText(EMPTY);
		allMatches.setText(EMPTY);
		succeedingMatches.setText(EMPTY);
		if (errorBackground) {
			findCombo.setBackground(findCombo.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
		}
	}
	
	private void beep() {
		getViewSite().getShell().getDisplay().beep();
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
