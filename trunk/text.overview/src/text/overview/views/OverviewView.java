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
 * This implements a view that shows the overview of last focused StyledText in
 * the parent workbench windows
 * 
 * @author Sandip Chitale
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

	private Listener focusListenerFilter;

	private CaretListener lastOverviewedStyledTextCaretListener;
	private ControlListener controlResizeListener;
	private DisposeListener disposeListener;
	private SelectionListener lastOverviewedStyledTextScrollBarSelectionListener;

	// private final ThreadLocal<Boolean> adjusting = new
	// ThreadLocal<Boolean>();

	/**
	 * The constructor.
	 */
	public OverviewView() {
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		composite.setLayout(null);

		overviewStyledText = new StyledText(composite, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL);
		overviewStyledText.setEditable(false);

		// overviewStyledText.addCaretListener(new CaretListener() {
		//
		// @Override
		// public void caretMoved(CaretEvent event) {
		// adjustTrackedStyledText();
		// }
		// });
		// overviewStyledText.getVerticalBar().addSelectionListener(new
		// SelectionAdapter() {
		// @Override
		// public void widgetSelected(SelectionEvent e) {
		// super.widgetSelected(e);
		// overviewStyledText.getDisplay().asyncExec(new Runnable() {
		//
		// @Override
		// public void run() {
		// adjustTrackedStyledText();
		// }
		// });
		// }
		// });

		controlResizeListener = new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				adjustSize();
			}
		};

		overviewStyledText.getParent().addControlListener(controlResizeListener);

		// listener to monitor if the carent movement changed the
		// lines visible in the tracked StyledText due to scrolling
		lastOverviewedStyledTextCaretListener = new CaretListener() {
			@Override
			public void caretMoved(CaretEvent event) {
				int topIndex = -1;
				try {
					if (lastOverviewedStyledText != null) {
						topIndex = lastOverviewedStyledText.getTopIndex();
						if (topIndex != lastTopIndex) {
							highlightViewport();
						}
					}
				} finally {
					lastTopIndex = topIndex;
				}
			}
		};
		
		lastOverviewedStyledTextScrollBarSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
			
			@Override
			public void widgetSelected(SelectionEvent event) {
				int topIndex = -1;
				try {
					if (lastOverviewedStyledText != null) {
						topIndex = lastOverviewedStyledText.getTopIndex();
						if (topIndex != lastTopIndex) {
							highlightViewport();
						}
					}
				} finally {
					lastTopIndex = topIndex;
				}
			}
		};

		// listener for dispose of tracked StyledText
		disposeListener = new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				lastOverviewedStyledText = null;
				lastTopIndex = -1;

				blank();
			}
		};

		focusListenerFilter = new Listener() {
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
				// Track focus
				display.addFilter(SWT.FocusIn, focusListenerFilter);

				// Check if there is focused StyledText
				// in this workbench window, if so track it
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

	protected void handleEvent(Event event) {
		if (event.type == SWT.FocusIn) {
			if (event.widget instanceof StyledText) {
				StyledText styledText = (StyledText) event.widget;
				if (styledText == overviewStyledText) {
					return;
				}
				if (styledText.getShell() == overviewStyledText.getShell()) {
					trackStyledText(styledText);
				}
			}
		}
	}

	@Override
	public void dispose() {

		overviewStyledText.getDisplay().removeFilter(SWT.FocusIn, focusListenerFilter);
		untrackLastOverviewedStyledText();
		lastOverviewedStyledText = null;

		lastFont.dispose();
		lastFont = null;

		focusListenerFilter = null;
		super.dispose();
	}

	private void trackStyledText(StyledText styledText) {
		untrackLastOverviewedStyledText();

		lastOverviewedStyledText = styledText;

		lastOverviewedStyledText.addCaretListener(lastOverviewedStyledTextCaretListener);
		lastOverviewedStyledText.addControlListener(controlResizeListener);
		lastOverviewedStyledText.addDisposeListener(disposeListener);
		((Scrollable) lastOverviewedStyledText).getVerticalBar().addSelectionListener(lastOverviewedStyledTextScrollBarSelectionListener);

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

	private void untrackLastOverviewedStyledText() {
		if (lastOverviewedStyledText != null) {
			lastOverviewedStyledText.removeCaretListener(lastOverviewedStyledTextCaretListener);
			lastOverviewedStyledText.removeControlListener(controlResizeListener);
			lastOverviewedStyledText.removeDisposeListener(disposeListener);
			((Scrollable) lastOverviewedStyledText).getVerticalBar().removeSelectionListener(lastOverviewedStyledTextScrollBarSelectionListener);
		}
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
		if (lastOverviewedStyledText != null) {
			// The index of the first (possibly only partially) visible line of
			// the widget
			int topIndex = JFaceTextUtil.getPartialTopIndex((StyledText) lastOverviewedStyledText);
			// The index of the last (possibly only partially) visible line of
			// the widget
			int bottomIndex = JFaceTextUtil.getPartialBottomIndex((StyledText) lastOverviewedStyledText);
			overviewStyledText.setLineBackground(topIndex, (bottomIndex - topIndex) + 1,
					overviewStyledText.getSelectionBackground());
			// if (adjusting.get() == null) {
			if (topIndex == 0) {
				overviewStyledText.setTopIndex(topIndex);
			} else {
				overviewStyledText.setTopIndex(Math.max(0, topIndex - 1));
			}
			// }
		}
	}

	// private void adjustTrackedStyledText() {
	// if (adjusting.get() != null) {
	// return;
	// }
	// try {
	// adjusting.set(lean.TRUE);
	// if (lastOverviewedStyledText != null) {
	// lastOverviewedStyledText.setCaretOffset(overviewStyledText.getCaretOffset());
	// lastOverviewedStyledText.setTopIndex(Math.max(0,
	// overviewStyledText.getTopIndex()));
	// }
	// } finally {
	// adjusting.set(null);
	// }
	// }

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