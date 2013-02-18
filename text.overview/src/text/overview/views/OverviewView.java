package text.overview.views;

import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.ui.ISizeProvider;
import org.eclipse.ui.IViewLayout;
import org.eclipse.ui.part.ViewPart;

/**
 */

public class OverviewView extends ViewPart implements IViewLayout, ISizeProvider {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "text.overview.views.OverviewView";

	private Composite composite;
	private StyledText overviewStyledText;
	private StyledText lastOverviewedStyledText;
	private Font lastFont;
	private String lastFontName;
	private int lastFontStyle;
	private double lastScale = 0.2d;
	private int lastTopIndex = -1;

	private Listener listener;

	private CaretListener caretListener;
	private ControlListener controlListener;
	private DisposeListener disposeListener;
	private SelectionListener selectionListener;

	/**
	 * The constructor.
	 */
	public OverviewView() {
	}

	@Override
	public void dispose() {
		if (lastOverviewedStyledText != null) {
			lastOverviewedStyledText.removeCaretListener(caretListener);
			lastOverviewedStyledText.removeControlListener(controlListener);
			lastOverviewedStyledText.removeDisposeListener(disposeListener);
			((Scrollable) lastOverviewedStyledText).getVerticalBar().removeSelectionListener(selectionListener);
		}
		overviewStyledText.getDisplay().removeFilter(SWT.FocusIn, listener);
		lastOverviewedStyledText = null;
		listener = null;
		super.dispose();
	}

	protected void handleEvent(Event event) {
		if (event.type == SWT.FocusIn) {
			if (event.widget instanceof StyledText) {
				StyledText styledText = (StyledText) event.widget;
				if (styledText == overviewStyledText) {
					return;
				}
				if (overviewStyledText.getShell() != styledText.getShell()) {
					return;
				}

				if (lastOverviewedStyledText != null) {
					lastOverviewedStyledText.removeCaretListener(caretListener);
					lastOverviewedStyledText.removeControlListener(controlListener);
					lastOverviewedStyledText.removeDisposeListener(disposeListener);
					((Scrollable) lastOverviewedStyledText).getVerticalBar().removeSelectionListener(selectionListener);
				}

				trackStyledText(styledText);
			}
		}
	}
	
	private void trackStyledText(StyledText styledText) {
		lastOverviewedStyledText = styledText;

		lastOverviewedStyledText.addCaretListener(caretListener);
		lastOverviewedStyledText.addControlListener(controlListener);
		lastOverviewedStyledText.addDisposeListener(disposeListener);
		((Scrollable) lastOverviewedStyledText).getVerticalBar().addSelectionListener(selectionListener);

		Font font = lastOverviewedStyledText.getFont();
		FontData[] fontData = font.getFontData();
		FontData fontDatum = fontData[0];
		if (fontDatum.getName().equals(lastFontName) && fontDatum.getStyle() == lastFontStyle) {
			// nothing to do
		} else {
			lastFontName = fontDatum.getName();
			lastFontStyle = fontDatum.getStyle();
			if (lastFont != null) {
				lastFont.dispose();
			}
			lastFont = new Font(overviewStyledText.getDisplay(), lastFontName, 1, lastFontStyle);
			lastScale = 1.0d / ((double) fontDatum.getHeight());
			overviewStyledText.setFont(lastFont);
		}
		overviewStyledText.setBackground(lastOverviewedStyledText.getBackground());
		overviewStyledText.setSelectionBackground(lastOverviewedStyledText.getSelectionBackground());
		overviewStyledText.setText(lastOverviewedStyledText.getText());
		overviewStyledText.setStyleRanges(lastOverviewedStyledText.getStyleRanges());
		overviewStyledText.setSelection(lastOverviewedStyledText.getSelection());
		adjustSize();
		highlightViewport();
	}

	private void blank() {
		overviewStyledText.setText("\n");
		adjustSize();
		highlightViewport();
	}

	private void adjustSize() {
		Point size = overviewStyledText.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int x = (int) (size.x * lastScale);
		Point parentSize = overviewStyledText.getParent().getSize();
		overviewStyledText.setSize(new Point(Math.max(parentSize.x, x), parentSize.y));
	}

	private void highlightViewport() {
		overviewStyledText.setLineBackground(0, overviewStyledText.getLineCount() - 1, null);
		lastTopIndex = -1;
		if (lastOverviewedStyledText != null) {
			// The index of the first (possibly only partially) visible line of
			// the widget
			int topIndex = JFaceTextUtil.getPartialTopIndex((StyledText) lastOverviewedStyledText);
			lastTopIndex = topIndex;
			// The index of the last (possibly only partially) visible line of
			// the widget
			int bottomIndex = JFaceTextUtil.getPartialBottomIndex((StyledText) lastOverviewedStyledText);
			overviewStyledText.setLineBackground(topIndex, (bottomIndex - topIndex) + 1, 
					overviewStyledText.getSelectionBackground());
			overviewStyledText.setTopIndex(Math.max(0, topIndex - 2));
		}
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(null);

		overviewStyledText = new StyledText(composite, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL) {
		};
		overviewStyledText.setEditable(false);

		controlListener = new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				adjustSize();
			}
		};
		
		caretListener = new CaretListener() {
			@Override
			public void caretMoved(CaretEvent event) {
				if (lastOverviewedStyledText != null) {
					int topIndex = lastOverviewedStyledText.getTopIndex();
					if (topIndex != lastTopIndex) {
						highlightViewport();
					}
				}
			}
		};

		overviewStyledText.getParent().addControlListener(controlListener);

		disposeListener = new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				lastOverviewedStyledText = null;
				blank();
			}
		};

		selectionListener = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				highlightViewport();
			}
		};

		listener = new Listener() {
			@Override
			public void handleEvent(final Event event) {
				event.display.asyncExec(new Runnable() {
					@Override
					public void run() {
						OverviewView.this.handleEvent(event);
					}
				});
			}
		};

		final Display display = parent.getDisplay();
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				display.addFilter(SWT.FocusIn, listener);
				Control focusControl = display.getFocusControl();
				if (focusControl instanceof StyledText) {
					StyledText styledText = (StyledText) focusControl;
					if (styledText.getShell() == overviewStyledText.getShell()) {
						trackStyledText(styledText);
					}
				}
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
	}

	@Override
	public int getSizeFlags(boolean width) {
		if (!width) {
			return SWT.MIN | SWT.MAX;
		}
		return 0;
	}

	@Override
	public int computePreferredSize(boolean width, int availableParallel, int availablePerpendicular,
			int preferredResult) {
		if (width) {
			return 120;
		}
		return 0;
	}

	@Override
	public boolean isCloseable() {
		return true;
	}

	@Override
	public void setCloseable(boolean closeable) {
	}

	@Override
	public boolean isMoveable() {
		return false;
	}

	@Override
	public void setMoveable(boolean moveable) {
	}

	@Override
	public boolean isStandalone() {
		return true;
	}

	@Override
	public boolean getShowTitle() {
		return true;
	}
}