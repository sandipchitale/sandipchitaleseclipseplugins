package editorfindbar.impl;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.text.IFindReplaceTarget;
import org.eclipse.jface.text.IFindReplaceTargetExtension;
import org.eclipse.jface.text.IFindReplaceTargetExtension3;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.texteditor.ITextEditor;

import editorfindbar.api.IFindBarDecorator;

public class FindBarDecorator implements IFindBarDecorator {
	private static String ID = "org.eclipse.ui.edit.find.bar";

	private final ITextEditor textEditor;
	private ISourceViewer sourceViewer;
	private final IStatusLineManager statusLineManager;

	public FindBarDecorator(ITextEditor textEditor, IStatusLineManager statusLineManager) {
		this.textEditor = textEditor;
		this.statusLineManager = statusLineManager;
	}

	public Composite createFindBarComposite(Composite parent) {
		composite = new Composite(parent, SWT.BORDER);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginTop = 0;
		gridLayout.marginLeft = 0;
		gridLayout.marginBottom = 0;
		gridLayout.marginRight = 0;
		composite.setLayout(gridLayout);

		Composite content = new Composite(composite, SWT.NONE);
		content.setLayout(new FillLayout());
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return content;
	}
	
	public void createFindBar(ISourceViewer sourceViewer) {
		this.sourceViewer = sourceViewer;
		findBar = new Composite(composite, SWT.BORDER);
		findBarGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		findBarGridData.exclude = true;
		findBar.setLayoutData(findBarGridData);

		GridLayout gridLayout = new GridLayout(10, false);
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginTop = 0;
		gridLayout.marginLeft = 5;
		gridLayout.marginBottom = 0;
		gridLayout.marginRight = 5;
		findBar.setLayout(gridLayout);

		Button close = new Button(findBar, SWT.PUSH);
		// close.setText("X");
		close.setToolTipText("Hide Find bar");
		close.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_TOOL_DELETE));
		close.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		close.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				hideFindBar();
			}

			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});

		Label findLabel = new Label(findBar, SWT.NONE);
		findLabel.setText("Find:");
		findLabel
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));

		combo = new Combo(findBar, SWT.DROP_DOWN | SWT.ICON_SEARCH);
		combo.setText("                    ");
		Point size = combo.computeSize(SWT.DEFAULT, SWT.DEFAULT);
		GridData comboGridData = new GridData(SWT.LEFT, SWT.CENTER, false,
				false);
		comboGridData.widthHint = size.x;
		combo.setLayoutData(comboGridData);
		combo.setText("");
		combo.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {}

			public void focusGained(FocusEvent e) {
				combo.setForeground(null);
			}
		});

		combo.addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent e) {}

			public void keyPressed(KeyEvent e) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.LF) {
					if ((e.stateMask & SWT.CTRL) == 0) {
						find(true);
					} else {
						find(false);
					}
				}
			}
		});

		previous = new Button(findBar, SWT.PUSH);
		previous.setEnabled(false);
		// previous.setText("Previous");
		previous.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_TOOL_BACK));
		previous
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		previous.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				find(false);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		next = new Button(findBar, SWT.PUSH);
		next.setEnabled(false);
		// next.setText("Next");
		next.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_TOOL_FORWARD));
		next.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		next.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				find(true);
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		caseSensitive = new Button(findBar, SWT.CHECK);
		caseSensitive.setText("Case Sensitive");
		caseSensitive.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false,
				false));

		wholeWord = new Button(findBar, SWT.CHECK);
		wholeWord.setText("Whole Word");
		wholeWord
				.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false));
		wholeWord.setEnabled(false);

		IFindReplaceTarget findReplaceTarget = (IFindReplaceTarget) textEditor.getAdapter(IFindReplaceTarget.class);
		if (findReplaceTarget instanceof IFindReplaceTargetExtension3) {
			regularExpression = new Button(findBar, SWT.CHECK);
			regularExpression.setText("Regular Expression");
			regularExpression.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
					false, false));
		}

		Label streach = new Label(findBar, SWT.PUSH);
		streach.setLayoutData(findBarGridData);

		Button showFindReplaceDialog = new Button(findBar, SWT.PUSH);
		showFindReplaceDialog.setText("Find/Replace...");
		showFindReplaceDialog.setToolTipText("Show Find/Replace Dialog");
		showFindReplaceDialog.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER,
				false, false));
		showFindReplaceDialog.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				IWorkbenchPartSite site = textEditor.getSite();
				IHandlerService handlerService = (IHandlerService) site
						.getService(IHandlerService.class);
				if (handlerService != null) {
					try {
						handlerService.executeCommand(
								"org.eclipse.ui.edit.findReplace", null);
					} catch (ExecutionException e1) {
					} catch (NotDefinedException e1) {
					} catch (NotEnabledException e1) {
					} catch (NotHandledException e1) {
					}
				}
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		textEditor.setAction(ID, new ShowFindBarAction());
	}

	private Combo combo;
	private Button caseSensitive;
	private Button wholeWord;
	private Button regularExpression;
	private int incrementalOffset = -1;
	private Button next;
	private Button previous;
	private Composite composite;
	private TraverseListener traverseListener = new TraverseListener() {
		public void keyTraversed(TraverseEvent e) {
			if (e.character == SWT.ESC) {
				hideFindBar();
			}
		}
	};
	private Composite findBar;
	private GridData findBarGridData;

	private ModifyListener modifyListener = new ModifyListener() {
		private String lastText = "";

		public void modifyText(ModifyEvent e) {
			combo.setForeground(null);
			boolean wrap = true;
			String text = combo.getText();
			if (lastText.startsWith(text)) {
				wrap = false;
			}
			lastText = text;
			previous.setEnabled(!"".equals(text));
			next.setEnabled(!"".equals(text));
			wholeWord.setEnabled((!"".equals(text)) && (isWord(text)));
			if ("".equals(text)) {
				ISelectionProvider selectionProvider = textEditor.getSelectionProvider();
				ISelection selection = selectionProvider.getSelection();
				if (selection instanceof TextSelection) {
					ITextSelection textSelection = (ITextSelection) selection;
					selectionProvider.setSelection(
							new TextSelection(textSelection.getOffset(), 0));
				}
			} else {
				find(true, true, wrap);
			}
		}
	};

	private class ShowFindBarAction extends Action {
		private ShowFindBarAction() {
			setActionDefinitionId(ID);
		}

		public void run() {
			showFindBar();
		}
	}

	private void hideFindBar() {
		if (findBarGridData.exclude == false) {
			findBarGridData.exclude = true;
			composite.layout();
			incrementalOffset = -1;
			combo.removeModifyListener(modifyListener);
			findBar.removeTraverseListener(traverseListener);
		}
		textEditor.setFocus();
	}

	private void showFindBar() {
		if (findBarGridData.exclude) {
			findBarGridData.exclude = false;
			composite.layout();
			findBar.addTraverseListener(traverseListener);
			combo.addModifyListener(modifyListener);
		}
		ISelection selection = sourceViewer.getSelectionProvider()
				.getSelection();
		if (selection instanceof ITextSelection) {
			ITextSelection textSelection = (ITextSelection) selection;
			String text = textSelection.getText();
			if (text.indexOf("\n") == -1 && text.indexOf("\r") == -1) {
				combo.setText(text);
			}
		}
		boolean comboHasFocus = combo.isFocusControl();
		if (!comboHasFocus) {
			combo.setFocus();
			incrementalOffset = -1;
		}
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

	private void find(boolean forward, boolean incremental, boolean wrap,
			boolean wrapping) {
		IFindReplaceTarget findReplaceTarget = (IFindReplaceTarget) textEditor.getAdapter(IFindReplaceTarget.class);
		if (findReplaceTarget != null) {
			try {
				if (findReplaceTarget instanceof IFindReplaceTargetExtension) {
					IFindReplaceTargetExtension findReplaceTargetExtension = (IFindReplaceTargetExtension) findReplaceTarget;
					findReplaceTargetExtension.beginSession();
				}
				String findText = combo.getText();
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
					combo.setForeground(null);
					if (!forward) {
						selection = textWidget.getSelection();
						incrementalOffset = selection.x;
					}
					statusLineManager.setMessage("");
				} else {
					if (wrap) {
						if (!wrapping) {
							find(forward, incremental, wrap, true);
							return;
						}
					}
					combo.setForeground(combo.getDisplay().getSystemColor(
							SWT.COLOR_RED));
					textWidget.getDisplay().beep();
					statusLineManager.setMessage("String Not found.");
				}
			} finally {
				if (findReplaceTarget instanceof IFindReplaceTargetExtension) {
					IFindReplaceTargetExtension findReplaceTargetExtension = (IFindReplaceTargetExtension) findReplaceTarget;
					findReplaceTargetExtension.endSession();
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
	 * @since 3.0
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

}
