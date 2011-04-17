package findreplacebar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.ISizeProvider;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class FindReplaceBarViewPart extends ViewPart implements ISizeProvider{

	private Combo findCombo;
	private ToolItem allScope;
	private ToolItem selectedLinesScope;
	private ToolItem previous;
	private ToolItem next;
	private ToolItem caseSensitive;
	private ToolItem regularExpression;
	private ToolItem wholeWord;
	private Combo replaceCombo;
	private ToolItem replaceFind;
	private ToolItem replace;
	private ToolItem replaceAll;
	private ToolItem countOfTotal;
	private Text count;
	private Text total;
	private ToolItem close;

	public FindReplaceBarViewPart() {
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		RowLayout rowLayout = new RowLayout();
		rowLayout.center = true;
		rowLayout.spacing = 5;
		rowLayout.marginTop = 0;
		rowLayout.marginBottom = 0;
		rowLayout.wrap = false;
		composite.setLayout(rowLayout);
		
		ToolBar closeToolBar = new ToolBar(composite, SWT.FLAT);
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
		
		Label findLabel = new Label(composite, SWT.RIGHT);
		findLabel.setText("Find:");
		
		ToolBar scopeToolbar = new ToolBar(composite, SWT.FLAT);
		allScope = new ToolItem(scopeToolbar, SWT.RADIO | SWT.FLAT);
		allScope.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_FIND_SCOPE_ALL));
		allScope.setSelection(true);
		allScope.setToolTipText("Find in All Lines");
		selectedLinesScope = new ToolItem(scopeToolbar, SWT.RADIO | SWT.FLAT);
		selectedLinesScope.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_FIND_SCOPE_SELECTED_LINES));
		selectedLinesScope.setToolTipText("Find in Selected Lines");
		
		findCombo = new Combo(composite, SWT.DROP_DOWN);

		GC gc = new GC(findCombo);
	    FontMetrics fm = gc.getFontMetrics();
	    int width = 10 * fm.getAverageCharWidth();
	    int height = fm.getHeight();
	    gc.dispose();
	    findCombo.setLayoutData(new RowData(findCombo.computeSize(width, height)));
				
	    ToolBar previousNextToolbar = new ToolBar(composite, SWT.FLAT);
		previous = new ToolItem(previousNextToolbar, SWT.PUSH);
		previous.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_BACK));
		previous.setToolTipText("Find Previous");
		next = new ToolItem(previousNextToolbar, SWT.PUSH);
		next.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_FORWARD));
		next.setToolTipText("Find Next");
		
		Label separator1 = new Label(composite, SWT.NONE);
		separator1.setText("|");
		separator1.setForeground(separator1.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		
		ToolBar countOfTotalToolbar = new ToolBar(composite, SWT.FLAT);
		countOfTotal = new ToolItem(countOfTotalToolbar, SWT.CHECK);
		countOfTotal.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_COUNT_OF_TOTAL));
		countOfTotal.setToolTipText("Show count of total");

		count = new Text(composite, SWT.SINGLE | SWT.RIGHT | SWT.BORDER);
		count.setText("      0");
		count.setEditable(false);
		
		Label of = new Label(composite, SWT.CENTER);
		of.setText(" of ");
		
		total = new Text(composite,  SWT.SINGLE | SWT.RIGHT | SWT.BORDER);
		total.setText("      0");
		total.setEditable(false);

		Label separator2 = new Label(composite, SWT.NONE);
		separator2.setText("|");
		separator2.setForeground(separator2.getDisplay().getSystemColor(SWT.COLOR_GRAY));
	    
		ToolBar optionsToolbar = new ToolBar(composite, SWT.FLAT);
		caseSensitive = new ToolItem(optionsToolbar, SWT.CHECK | SWT.FLAT);
		caseSensitive.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_CASE_SENSITIVE));
		caseSensitive.setToolTipText("Case Sensitive");
		regularExpression = new ToolItem(optionsToolbar, SWT.CHECK | SWT.FLAT);
		regularExpression.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_REGULAR_EXRESSION));
		regularExpression.setToolTipText("Regular Expression");
		wholeWord = new ToolItem(optionsToolbar, SWT.CHECK | SWT.FLAT);
		wholeWord.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_WHOLE_WORD));
		wholeWord.setToolTipText("Whole Word");

		Label separator3 = new Label(composite, SWT.NONE);
		separator3.setText("|");
		separator3.setForeground(separator3.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		
		Label replaceLabel = new Label(composite, SWT.RIGHT);
		replaceLabel.setText("Replace with:");
		
		replaceCombo = new Combo(composite, SWT.DROP_DOWN);
		
		gc = new GC(replaceCombo);
	    fm = gc.getFontMetrics();
	    width = 10 * fm.getAverageCharWidth();
	    height = fm.getHeight();
	    gc.dispose();
	    replaceCombo.setLayoutData(new RowData(replaceCombo.computeSize(width, height)));
	    
	    ToolBar replaceToolbar = new ToolBar(composite, SWT.FLAT);
		replaceFind = new ToolItem(replaceToolbar, SWT.PUSH | SWT.FLAT);
		replaceFind.setText("Replace/Find");
		replace = new ToolItem(replaceToolbar, SWT.PUSH);
		replace.setText("Replace");
		replaceAll = new ToolItem(replaceToolbar, SWT.PUSH);
		replaceAll.setText("Replace All");
	}

	@Override
	public void setFocus() {
		findCombo.setFocus();
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
			return 22;
		}
		return 0;
	}

}
