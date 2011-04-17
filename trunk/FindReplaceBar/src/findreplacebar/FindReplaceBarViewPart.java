package findreplacebar;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.ISizeProvider;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

public class FindReplaceBarViewPart extends ViewPart implements ISizeProvider{

	private Combo findCombo;

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
		
		Label findLabel = new Label(composite, SWT.RIGHT);
		findLabel.setText("Find:");
		
		ToolBar scopeToolbar = new ToolBar(composite, SWT.FLAT);
		ToolItem allScope = new ToolItem(scopeToolbar, SWT.RADIO | SWT.FLAT);
		allScope.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_FIND_SCOPE_ALL));
		allScope.setSelection(true);
		ToolItem selectedLinesScope = new ToolItem(scopeToolbar, SWT.RADIO | SWT.FLAT);
		selectedLinesScope.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_FIND_SCOPE_SELECTED_LINES));
		
		findCombo = new Combo(composite, SWT.DROP_DOWN);

		GC gc = new GC(findCombo);
	    FontMetrics fm = gc.getFontMetrics();
	    int width = 10 * fm.getAverageCharWidth();
	    int height = fm.getHeight();
	    gc.dispose();
	    findCombo.setLayoutData(new RowData(findCombo.computeSize(width, height)));
				
	    ToolBar previousNextToolbar = new ToolBar(composite, SWT.FLAT);
		ToolItem previous = new ToolItem(previousNextToolbar, SWT.PUSH | SWT.FLAT);
		previous.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_BACK));
		ToolItem next = new ToolItem(previousNextToolbar, SWT.PUSH);
		next.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_TOOL_FORWARD));

//		Button countOfTotal = new Button(composite, SWT.TOGGLE | SWT.FLAT);
//		countOfTotal.setText("\u01A9");
//		
//		Label countOfTotalLabel = new Label(composite, SWT.NONE);
//		countOfTotalLabel.setText("  0 / 0 ");
		
//		Label separator1 = new Label(composite, SWT.RIGHT);
//		separator1.setText(" Scope:");
//		
		Label separator1 = new Label(composite, SWT.NONE);
		separator1.setText("|");
		separator1.setForeground(separator1.getDisplay().getSystemColor(SWT.COLOR_GRAY));
	    
		ToolBar optionsToolbar = new ToolBar(composite, SWT.FLAT);
		ToolItem caseSensitive = new ToolItem(optionsToolbar, SWT.CHECK | SWT.FLAT);
		caseSensitive.setText("a\u2260A");
		ToolItem regularExpression = new ToolItem(optionsToolbar, SWT.CHECK | SWT.FLAT);
		regularExpression.setText(".?+*");
		ToolItem wholeWord = new ToolItem(optionsToolbar, SWT.CHECK | SWT.FLAT);
		wholeWord.setText("\\<\\>");

		Label separator2 = new Label(composite, SWT.NONE);
		separator2.setText("|");
		separator2.setForeground(separator2.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		
		Label replaceLabel = new Label(composite, SWT.RIGHT);
		replaceLabel.setText("Replace with:");
		
		Combo replaceCombo = new Combo(composite, SWT.DROP_DOWN);
		
		gc = new GC(replaceCombo);
	    fm = gc.getFontMetrics();
	    width = 10 * fm.getAverageCharWidth();
	    height = fm.getHeight();
	    gc.dispose();
	    replaceCombo.setLayoutData(new RowData(replaceCombo.computeSize(width, height)));
	    
	    ToolBar replaceToolbar = new ToolBar(composite, SWT.FLAT);
		ToolItem replaceFind = new ToolItem(replaceToolbar, SWT.PUSH | SWT.FLAT);
		replaceFind.setText("Replace/Find");
//		replaceFind.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_REPLACE_FIND));
		ToolItem replace = new ToolItem(replaceToolbar, SWT.PUSH);
		replace.setText("Replace");
//		replace.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_REPLACE));
		ToolItem replaceAll = new ToolItem(replaceToolbar, SWT.PUSH);
		replaceAll.setText("Replace All");
//		replaceAll.setImage(Activator.getDefault().getImageRegistry().get(Activator.ICON_REPLACE_ALL));
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
