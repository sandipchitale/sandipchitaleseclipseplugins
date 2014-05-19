package text.overview.views;

import java.lang.ref.WeakReference;

import org.eclipse.jface.text.JFaceTextUtil;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.TextChangeListener;
import org.eclipse.swt.custom.TextChangedEvent;
import org.eclipse.swt.custom.TextChangingEvent;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.ControlListener;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.ui.ISizeProvider;
import org.eclipse.ui.IViewLayout;
import org.eclipse.ui.part.ViewPart;

import text.overview.Activator;

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

	private StyledText overviewStyledText;
	private Cursor overviewStyledTextCrosshairCursor;
	private DefaultToolTip overviewStyledTextToolTip;

	private StyledText lastOverviewedStyledText;
	private Font lastFont;
	private String lastFontName;
	private int lastFontStyle;
	private double lastScale = 0.2d;
	private int lastTopIndex = -1;
	private int lastLineAtOffset = -1;

	private Listener focusListenerFilter;

	private ControlListener parentResizeListener;

	private TextChangeListener lastOverviewedStyledTextTextChangeListener;
	private CaretListener lastOverviewedStyledTextCaretListener;
	private SelectionListener lastOverviewedStyledTextScrollBarSelectionListener;
	private ControlListener lastOverviewedStyledTextResizeListener;
	private DisposeListener lastOverviewedStyledTextDisposeListener;

	private final ThreadLocal<Boolean> suspendLastOverviewedStyledText = new ThreadLocal<Boolean>();

	private Scale scale;

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
		final Display display = parent.getDisplay();
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new org.eclipse.swt.layout.GridLayout(1, true));

		final Composite composite = new Composite(container, SWT.NONE);
		composite.setLayout(null);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		lastOverviewedStyledTextTextChangeListener = new TextChangeListener() {

			@Override
			public void textSet(TextChangedEvent event) {
			}

			@Override
			public void textChanging(TextChangingEvent event) {
				blank();
			}

			@Override
			public void textChanged(TextChangedEvent event) {
				blank();
			}
		};

		// listener to monitor if the carent movement changed the
		// lines visible in the tracked StyledText due to scrolling
		lastOverviewedStyledTextCaretListener = new CaretListener() {
			@Override
			public void caretMoved(CaretEvent event) {
				if (suspendLastOverviewedStyledText.get() != null) {
					return;
				}
				int topIndex = -1;
				int lineAtOffset = -1;
				try {
					if (lastOverviewedStyledText != null) {
						topIndex = lastOverviewedStyledText.getTopIndex();
						int caretOffset = lastOverviewedStyledText.getCaretOffset();
						lineAtOffset = lastOverviewedStyledText.getLineAtOffset(caretOffset);
						if (topIndex != lastTopIndex || lineAtOffset != lastLineAtOffset) {
							highlightViewport(topIndex != lastTopIndex);
						}
					}
				} finally {
					lastTopIndex = topIndex;
					lastLineAtOffset = lineAtOffset;
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
				if (suspendLastOverviewedStyledText.get() != null) {
					return;
				}
				int topIndex = -1;
				int lineAtOffset = -1;
				try {
					if (lastOverviewedStyledText != null) {
						topIndex = lastOverviewedStyledText.getTopIndex();
						int caretOffset = lastOverviewedStyledText.getCaretOffset();
						lineAtOffset = lastOverviewedStyledText.getLineAtOffset(caretOffset);
						if (topIndex != lastTopIndex || lineAtOffset != lastLineAtOffset) {
							highlightViewport(topIndex != lastTopIndex);
						}
					}
				} finally {
					lastTopIndex = topIndex;
					lastLineAtOffset = lineAtOffset;
				}
			}
		};

		lastOverviewedStyledTextResizeListener = new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				adjustSize();
			}
		};

		// listener for dispose of tracked StyledText
		lastOverviewedStyledTextDisposeListener = new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				blank();
			}
		};

		focusListenerFilter = new Listener() {
			@Override
			public void handleEvent(final Event event) {
				OverviewView.this.handleEvent(event);
			}
		};
		
		display.syncExec(new Runnable() {
			@Override
			public void run() {
				// Track focus
				display.addFilter(SWT.FocusIn, focusListenerFilter);
			}
		});

		scale = new Scale(container, SWT.BORDER);
		scale.setMinimum(Activator.getDefault().getMinOverviewFontSize());
		scale.setSelection(Activator.getDefault().getOverviewFontSize());
		scale.setMaximum(Activator.getDefault().getMaxOverviewFontSize());
		scale.setIncrement(1);
		scale.setPageIncrement(1);
		scale.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		
		scale.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				Activator.getDefault().setOverviewFontSize(scale.getSelection());
				StyledText styledText = lastOverviewedStyledText;
				blank();
				lastFontName = null;
				lastFontStyle = 0;
				reconfigureOverviewStyledText(composite);
				lastOverviewedStyledText = null;
				trackStyledText(styledText);
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
				
			}
		});
		
		reconfigureOverviewStyledText(composite);
		
	}
	
	private void reconfigureOverviewStyledText(Composite composite) {
		final Display display = composite.getDisplay();

		boolean layout = false;
		if (overviewStyledText != null) {
			overviewStyledTextToolTip.hide();
			overviewStyledText.setToolTipText(null);
			removeListenersLastOverviewedStyledText();
			overviewStyledTextCrosshairCursor.dispose();
			overviewStyledText.dispose();
			layout = true;
		}
		overviewStyledText = new StyledText(composite, SWT.MULTI | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
		overviewStyledText.setEditable(false);

		overviewStyledTextCrosshairCursor = new Cursor(display, SWT.CURSOR_CROSS);
		overviewStyledText.setCursor(overviewStyledTextCrosshairCursor);

		overviewStyledTextToolTip = new DefaultToolTip(overviewStyledText, DefaultToolTip.RECREATE, true);

		overviewStyledText.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseDoubleClick(MouseEvent e) {
				if (lastOverviewedStyledText != null) {
					overviewStyledTextToolTip.hide();
					lastOverviewedStyledText.setFocus();
				}
			}
		});

		overviewStyledText.addMouseTrackListener(new MouseTrackAdapter() {
			@Override
			public void mouseHover(MouseEvent e) {
				int lineIndex = overviewStyledText.getLineIndex(e.y);
				int fromLine = Math.max(0, lineIndex - 2);
				int toLine = Math.min(overviewStyledText.getLineCount() - 1, lineIndex + 2);
				int width = (int)(Math.log10(toLine+1) + 2);
				StringBuilder tooltip = new StringBuilder();
				for (int i = fromLine; i <= toLine; i++) {
					if (i > fromLine) {
						tooltip.append("\n");
					}
					tooltip.append(String.format("%" + width + "d ", i+1));
					String line = overviewStyledText.getLine(i);
					if (line != null) {
						tooltip.append((i == lineIndex ? "\u00bb\t" : " \t") +
								line.substring(0, Math.min(line.length(), 80)));
					}
				}
				overviewStyledTextToolTip.setText(tooltip.toString());
				overviewStyledTextToolTip.setPopupDelay(500);
				overviewStyledTextToolTip.setHideDelay(3000);
				overviewStyledTextToolTip.show(new Point(0, e.y + 10));
			}

			@Override
			public void mouseExit(MouseEvent e) {
				overviewStyledTextToolTip.setText(null);
			}
		});

		overviewStyledText.addCaretListener(new CaretListener() {

			@Override
			public void caretMoved(CaretEvent event) {
				if (suspendLastOverviewedStyledText.get() != null) {
					return;
				}
				adjustTrackedStyledText();
			}
		});

		parentResizeListener = new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e) {
				adjustSize();
			}
		};

		overviewStyledText.getParent().addControlListener(parentResizeListener);
		
		display.syncExec(new Runnable() {
			@Override
			public void run() {
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
		
		if (layout) {
			composite.layout(true);
		}
	}

	private WeakReference<StyledText> lastNonOverviewStyledText = null;
	
	void refreshOverviewView() {
		if (lastNonOverviewStyledText != null) {
			StyledText st = lastNonOverviewStyledText.get();
			if (st == null || st.isDisposed()) {
				lastNonOverviewStyledText = null;
				return;
			}
			trackStyledText(st);
		}
	}
	
	protected void handleEvent(Event event) {
		if (event.type == SWT.FocusIn) {
			if (event.widget instanceof StyledText) {
				StyledText styledText = (StyledText) event.widget;
				if (styledText.isDisposed()) {
					return;
				}
				if (styledText == overviewStyledText) {
					refreshOverviewView();
					return;
				}
				
				if (styledText.getShell() != overviewStyledText.getShell()) {
					return;
				}
				
				lastNonOverviewStyledText = new WeakReference<StyledText>(styledText);
				trackStyledText(styledText);
			}
		}
	}

	@Override
	public void dispose() {
		overviewStyledTextCrosshairCursor.dispose();
		getViewSite().getWorkbenchWindow().getShell().getDisplay().removeFilter(SWT.FocusIn, focusListenerFilter);

		removeListenersLastOverviewedStyledText();
		lastOverviewedStyledText = null;

		lastFont.dispose();
		lastFont = null;
		lastFontName = null;

		focusListenerFilter = null;

		super.dispose();
	}

	private void trackStyledText(StyledText styledText) {
		if (lastOverviewedStyledText == styledText) {
			return;
		}

		removeListenersLastOverviewedStyledText();

		try {
			suspendLastOverviewedStyledText.set(Boolean.TRUE);
			lastOverviewedStyledText = styledText;
			addListenersLastOverviewedStyledText();
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
				lastFont = new Font(overviewStyledText.getDisplay(), lastFontName, Activator.getDefault().getOverviewFontSize(), lastFontStyle);
				lastScale = ((double) Activator.getDefault().getOverviewFontSize()) / ((double) fontDatum.getHeight());
				overviewStyledText.setFont(lastFont);
				overviewStyledTextToolTip.setFont(font);
			}
			overviewStyledText.setForeground(lastOverviewedStyledText.getForeground());
			overviewStyledText.setBackground(lastOverviewedStyledText.getBackground());
			overviewStyledTextToolTip.setForegroundColor(lastOverviewedStyledText.getForeground());
			overviewStyledTextToolTip.setBackgroundColor(lastOverviewedStyledText.getBackground());
			overviewStyledText.setSelectionForeground(lastOverviewedStyledText.getSelectionForeground());
			overviewStyledText.setSelectionBackground(lastOverviewedStyledText.getSelectionBackground());
			overviewStyledText.setText(lastOverviewedStyledText.getText());
			overviewStyledText.setStyleRanges(lastOverviewedStyledText.getStyleRanges());
			overviewStyledText.setSelection(lastOverviewedStyledText.getSelection());
			scale.setToolTipText("Overview font size: " + Activator.getDefault().getOverviewFontSize());
			adjustSize();
			highlightViewport(true);
		} finally {
			suspendLastOverviewedStyledText.set(null);
		}
	}

	private void removeListenersLastOverviewedStyledText() {
		if (lastOverviewedStyledText != null) {
			lastOverviewedStyledText.getContent().removeTextChangeListener(lastOverviewedStyledTextTextChangeListener);
			lastOverviewedStyledText.removeCaretListener(lastOverviewedStyledTextCaretListener);
			lastOverviewedStyledText.removeControlListener(lastOverviewedStyledTextResizeListener);
			lastOverviewedStyledText.removeDisposeListener(lastOverviewedStyledTextDisposeListener);
			((Scrollable) lastOverviewedStyledText).getVerticalBar().removeSelectionListener(lastOverviewedStyledTextScrollBarSelectionListener);
		}
	}

	private void addListenersLastOverviewedStyledText() {
		if (lastOverviewedStyledText != null) {
			lastOverviewedStyledText.getContent().addTextChangeListener(lastOverviewedStyledTextTextChangeListener);
			lastOverviewedStyledText.addCaretListener(lastOverviewedStyledTextCaretListener);
			lastOverviewedStyledText.addControlListener(lastOverviewedStyledTextResizeListener);
			lastOverviewedStyledText.addDisposeListener(lastOverviewedStyledTextDisposeListener);
			((Scrollable) lastOverviewedStyledText).getVerticalBar().addSelectionListener(lastOverviewedStyledTextScrollBarSelectionListener);
		}
	}

	private void blank() {
		removeListenersLastOverviewedStyledText();
		lastOverviewedStyledText = null;
		lastTopIndex = -1;
		lastLineAtOffset = -1;
		overviewStyledText.setForeground(null);
		overviewStyledText.setBackground(null);
		overviewStyledTextToolTip.setForegroundColor(null);
		overviewStyledTextToolTip.setBackgroundColor(null);
		overviewStyledText.setSelectionForeground(null);
		overviewStyledText.setSelectionBackground(null);
		overviewStyledText.setText("");
		overviewStyledText.setStyleRanges(new StyleRange[0]);
		adjustSize();
		unhighlightViewport();
	}

	private void adjustSize() {
		Point size = overviewStyledText.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		int x = (int) (size.x * lastScale);
		Point parentSize = overviewStyledText.getParent().getSize();
		overviewStyledText.setSize(new Point(Math.max(parentSize.x, x), parentSize.y));
	}

	private void unhighlightViewport() {
		overviewStyledText.setLineBackground(0, overviewStyledText.getLineCount() - 1, null);
	}

	private void highlightViewport(boolean topIndexChanged) {
		if (topIndexChanged) {
			unhighlightViewport();
		}
		if (lastOverviewedStyledText != null) {
			// The index of the first (possibly only partially) visible line of
			// the widget
			int topIndex = JFaceTextUtil.getPartialTopIndex((StyledText) lastOverviewedStyledText);
			// The index of the last (possibly only partially) visible line of
			// the widget
			int bottomIndex = JFaceTextUtil.getPartialBottomIndex((StyledText) lastOverviewedStyledText);

			int caretOffset = lastOverviewedStyledText.getCaretOffset();
			int lineAtOffset = lastOverviewedStyledText.getLineAtOffset(caretOffset);

			overviewStyledText.setLineBackground(topIndex, (bottomIndex - topIndex) +
					(bottomIndex >= (overviewStyledText.getLineCount() - 1)? 0 : 1),
					overviewStyledText.getSelectionBackground());
			overviewStyledText.setLineBackground(lineAtOffset, 1, null);
			if (suspendLastOverviewedStyledText.get() == null) {
				if (topIndex == 0) {
					overviewStyledText.setTopIndex(topIndex);
				} else {
					overviewStyledText.setTopIndex(Math.max(0, topIndex - 1));
				}
			}
		}
	}

	private void adjustTrackedStyledText() {
		if (lastOverviewedStyledText != null) {
			try {
				suspendLastOverviewedStyledText.set(Boolean.TRUE);
				int topIndex = JFaceTextUtil.getPartialTopIndex((StyledText) lastOverviewedStyledText);
				// The index of the last (possibly only partially) visible line of
				// the widget
				int bottomIndex = JFaceTextUtil.getPartialBottomIndex((StyledText) lastOverviewedStyledText);
				int visibleLinesCount = bottomIndex - topIndex;
				int caretOffset = overviewStyledText.getCaretOffset();
				int lineAtOffset = overviewStyledText.getLineAtOffset(caretOffset);
				lastOverviewedStyledText.setTopIndex(Math.max(0, (lineAtOffset - (visibleLinesCount/2))));
				lastOverviewedStyledText.setCaretOffset(caretOffset);
				highlightViewport(true);
			} finally {
				suspendLastOverviewedStyledText.set(null);
			}
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		overviewStyledText.setFocus();
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
			return 140;
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
		return true;
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