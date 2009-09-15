package eclipsemate;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import eclipsemate.Filter.FilterInputProvider;
import eclipsemate.Filter.INPUT_TYPE;
import eclipsemate.Filter.OUTPUT_TYPE;

public class FilterThroughCommandHandler extends AbstractHandler {
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow activeWorkbenchWindow = HandlerUtil.getActiveWorkbenchWindow(event);
		IWorkbenchPage page = activeWorkbenchWindow.getActivePage();
		if (page == null) {
			Activator.beep(activeWorkbenchWindow);
			return null;
		}
		IEditorPart editor = page.getActiveEditor();
		if (editor == null) {
			Activator.beep(activeWorkbenchWindow);
			return null;
		}
		
		Map<String, String> environment = Filter.computeEnvironment(activeWorkbenchWindow, editor);
		if (editor instanceof ITextEditor) {
			ITextEditor abstractTextEditor = (ITextEditor) editor;
			if (abstractTextEditor.isEditable()) {
				Object adapter = (Control) abstractTextEditor.getAdapter(Control.class);
				if (adapter instanceof Control) {
					Control control = (Control) adapter;
					if (control instanceof StyledText) {
						StyledText styledText = (StyledText) control;
						
						int caretOffset = styledText.getCaretOffset();
						int lineAtCaret = styledText.getLineAtOffset(caretOffset);
						String lineText = styledText.getLine(lineAtCaret);
						int lineLength = lineText.length();
						
						Point selectionRange = styledText.getSelection();
						int selectionStartOffsetLine = styledText.getLineAtOffset(selectionRange.x);
						int selectionEndOffsetLine = styledText.getLineAtOffset(selectionRange.y);

						int selectionStartOffsetLineStartOffset = styledText.getOffsetAtLine(selectionStartOffsetLine);
						int selectionEndOffsetLineEndOffset = 
							styledText.getOffsetAtLine(selectionEndOffsetLine) + styledText.getLine(selectionEndOffsetLine).length();
						
						FilterThroughCommandDialog filterThroughCommandDialog = new FilterThroughCommandDialog(activeWorkbenchWindow.getShell());
						filterThroughCommandDialog.setEnvironment(environment);
						if (filterThroughCommandDialog.open() == Window.OK) {
							INPUT_TYPE inputType = filterThroughCommandDialog.getInputType();
							FilterInputProvider filterInputProvider = Filter.EOF;

							switch (inputType) {
							case SELECTION:
								filterInputProvider = new Filter.StringInputProvider(styledText.getSelectionText());
								break;
							case SELECTED_LINES:
								filterInputProvider = new Filter.StringInputProvider(styledText.getText(selectionStartOffsetLineStartOffset,
										selectionEndOffsetLineEndOffset));
								break;
							case DOCUMENT:
								filterInputProvider = new Filter.StringInputProvider(styledText.getText());
								break;
							case LINE:
								filterInputProvider = new Filter.StringInputProvider(styledText.getLine(styledText.getLineAtOffset(styledText.getCaretOffset())));
								break;
							case WORD:
								// TODO
								filterInputProvider = Filter.EOF;
								break;
							}

							Filter.FilterOutputConsumer filterOutputConsumer = null;
							OUTPUT_TYPE ouputType = filterThroughCommandDialog.getOuputType();
							switch (ouputType) {
							case DISCARD:
								filterOutputConsumer = Filter.DISCARD;
								break;
							case OUTPUT_TO_CONSOLE:
								filterOutputConsumer = new Filter.EclipseConsolePrintStreamOutputConsumer(filterThroughCommandDialog.getConsoleName());
								break;
							default:
								filterOutputConsumer = new Filter.StringOutputConsumer();
								break;
							}
							
							Filter.launch(filterThroughCommandDialog.getCommand(), environment, filterInputProvider, filterOutputConsumer);
							
							try {
								switch (ouputType) {
								case DISCARD:
									break;
								case REPLACE_SELECTION:
									int start = Math.min(selectionRange.x, selectionRange.y);
									int end = Math.max(selectionRange.x, selectionRange.y);
									styledText.replaceTextRange(start, end - start, 
											((Filter.StringOutputConsumer)filterOutputConsumer).getOutput());
									break;
								case REPLACE_SELECTED_LINES:
									styledText.replaceTextRange(selectionStartOffsetLineStartOffset, 
											selectionEndOffsetLineEndOffset - selectionStartOffsetLineStartOffset, 
											((Filter.StringOutputConsumer)filterOutputConsumer).getOutput());
									break;
								case REPLACE_LINE:
									int startOffsetOfLineAtCaret = styledText.getOffsetAtLine(lineAtCaret);
									styledText.replaceTextRange(startOffsetOfLineAtCaret, lineLength, 
											((Filter.StringOutputConsumer)filterOutputConsumer).getOutput());
									break;
								case REPLACE_DOCUMENT:
									styledText.setText( 
											((Filter.StringOutputConsumer)filterOutputConsumer).getOutput());
									break;
								case INSERT_AS_TEXT:
									styledText.replaceTextRange(caretOffset, 0, 
											((Filter.StringOutputConsumer)filterOutputConsumer).getOutput());
									break;
								case SHOW_AS_HTML:
									File tempHmtlFile = null;
									try {
										tempHmtlFile = File.createTempFile(Activator.PLUGIN_ID, ".html");
									} catch (IOException e) {
										// TODO
									}
									if (tempHmtlFile != null) {
										String output = ((Filter.StringOutputConsumer)filterOutputConsumer).getOutput();
										tempHmtlFile.deleteOnExit();
										PrintWriter pw = null;
										try {
											pw = new PrintWriter(tempHmtlFile);
										} catch (FileNotFoundException e1) {
											e1.printStackTrace();
										}
										if (pw != null) {
											pw.println(output);
											pw.flush();
											pw.close();
											IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
											try {
												support.getExternalBrowser().openURL(tempHmtlFile.toURI().toURL());
											} catch (PartInitException e) {
												// TODO
											} catch (MalformedURLException e) {
												// TODO
											}
										}
									}
									break;
								case SHOW_AS_TOOLTIP:													
									DefaultInformationControl tooltip = new DefaultInformationControl(activeWorkbenchWindow.getShell(), "Type escape to dismiss.", null);
									tooltip.setInformation(((Filter.StringOutputConsumer)filterOutputConsumer).getOutput());
									Point p = tooltip.computeSizeHint();
									tooltip.setSize(p.x, p.y);

									Point locationAtOffset = styledText.getLocationAtOffset(caretOffset);
									locationAtOffset = styledText.toDisplay(locationAtOffset.x, locationAtOffset.y + styledText.getLineHeight(caretOffset) + 2);
									tooltip.setLocation(locationAtOffset);
									tooltip.setVisible(true);
									tooltip.setFocus();
									break;
								case CREATE_NEW_DOCUMENT:
									File file = Utilities.queryFile();
									IEditorInput input = Utilities.createNonExistingFileEditorInput(file, "Untitled.txt");
									String editorId = "org.eclipse.ui.DefaultTextEditor";
									try
									{
										IEditorPart part = page.openEditor(input, editorId);

										if (part instanceof ITextEditor)
										{
											ITextEditor textEditor = (ITextEditor) part;
											IDocumentProvider dp = textEditor.getDocumentProvider();
											IDocument doc = dp.getDocument(textEditor.getEditorInput());
											try
											{
												String fileContents = ((Filter.StringOutputConsumer)filterOutputConsumer).getOutput();
												if (fileContents != null)
												{
													doc.replace(0, 0, fileContents);
												}
											}
											catch (BadLocationException e)
											{
												// TODO
											}
										}

									}
									catch (PartInitException e)
									{
										// TODO
									}
									break;
								}
							} catch (InterruptedException e) {
								// TODO
							}
						}
					}
				}
			}
		}
		
		return null;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	@Override
	public boolean isHandled() {
		return true;
	}

}
