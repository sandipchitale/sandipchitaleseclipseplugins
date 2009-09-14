package eclipsemate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringBufferInputStream;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;
import java.util.WeakHashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.texteditor.ITextEditor;

@SuppressWarnings("deprecation")
public class Filter {
	static final String DEFAULT_CONSOLE_NAME = "Eclipse Mate";

	enum VARIABLES_NAMES {
		TM_BUNDLE_SUPPORT
		,TM_CURRENT_LINE
		,TM_CURRENT_WORD
		,TM_DIRECTORY
		,TM_FILEPATH
		,TM_LINE_NUMBER
		,TM_LINE_INDEX
		,TM_PROJECT_DIRECTORY
		,TM_SCOPE
		,TM_SELECTED_FILES
		,TM_SELECTED_FILE
		,TM_SELECTED_TEXT
		,TM_SOFT_TABS
		,TM_SUPPORT_PATH
		,TM_TAB_SIZE
		
		,TM_SELECTION_OFFSET
		,TM_SELECTION_LENGTH
		,TM_SELECTION_START_LINE_NUMBER
		,TM_SELECTION_END_LINE_NUMBER
		,TM_CARET_LINE_NUMBER
		,TM_CARET_LINE_TEXT
	};
	
	enum INPUT_TYPE  {
		NONE
		,SELECTION
		,SELECTED_LINES
		,LINE
		,WORD
		,DOCUMENT
	};
	
	enum OUTPUT_TYPE {
		DISCARD
		,REPLACE_SELECTION
		,REPLACE_SELECTED_LINES
		,REPLACE_LINE
		,REPLACE_WORD
		,REPLACE_DOCUMENT
		,INSERT_AS_TEXT
		,INSERT_AS_TEMPLATE
		,SHOW_AS_HTML
		,SHOW_AS_TOOLTIP
		,CREATE_A_NEW_DOCUMENT
		,OUTPUT_TO_CONSOLE
	}

	public static Map<String, String> computeEnvironment(IWorkbenchWindow workbenchWindow, IEditorPart editorPart) {
		Map<String, String> environment = new TreeMap<String, String>();
		
		if (editorPart instanceof ITextEditor) {
			ITextEditor abstractTextEditor = (ITextEditor) editorPart;
			IEditorInput editorInput = abstractTextEditor.getEditorInput();
			if (editorInput instanceof IFileEditorInput) {
				IFileEditorInput fileEditorInput = (IFileEditorInput) editorInput;
				IFile iFile = fileEditorInput.getFile();
				if (iFile != null) {
					environment.put(VARIABLES_NAMES.TM_SELECTED_FILE.name(), iFile.getLocation().toFile().getAbsolutePath());
					environment.put(VARIABLES_NAMES.TM_FILEPATH.name(), iFile.getLocation().toFile().getAbsolutePath());
					environment.put(VARIABLES_NAMES.TM_DIRECTORY.name(), iFile.getParent().getLocation().toFile().getAbsolutePath());
					environment.put(VARIABLES_NAMES.TM_PROJECT_DIRECTORY.name(), iFile.getProject().getLocation().toFile().getAbsolutePath());
					ISelectionProvider selectionProvider = abstractTextEditor.getSelectionProvider();
					ISelection selection = selectionProvider.getSelection();
					if (selection instanceof ITextSelection) {
						ITextSelection textSelection = (ITextSelection) selection;
						environment.put(VARIABLES_NAMES.TM_SELECTED_TEXT.name(), textSelection.getText());
						environment.put(VARIABLES_NAMES.TM_LINE_NUMBER.name(), String.valueOf(textSelection.getStartLine() + 1));
						environment.put(VARIABLES_NAMES.TM_SELECTION_OFFSET.name(), String.valueOf(textSelection.getOffset()));
						environment.put(VARIABLES_NAMES.TM_SELECTION_LENGTH.name(), String.valueOf(textSelection.getLength()));
						environment.put(VARIABLES_NAMES.TM_SELECTION_START_LINE_NUMBER.name(), String.valueOf(textSelection.getStartLine()));
						environment.put(VARIABLES_NAMES.TM_SELECTION_END_LINE_NUMBER.name(), String.valueOf(textSelection.getEndLine()));
						Object adapter = (Control) abstractTextEditor.getAdapter(Control.class);
						if (adapter instanceof Control) {
							Control control = (Control) adapter;
							if (control instanceof StyledText) {
								StyledText styledText = (StyledText) control;
								environment.put(VARIABLES_NAMES.TM_LINE_INDEX.name(), String.valueOf(textSelection.getOffset() - styledText.getOffsetAtLine(textSelection.getStartLine())));
								int caretOffset = styledText.getCaretOffset();
								int lineAtCaret = styledText.getLineAtOffset(caretOffset);
								environment.put(VARIABLES_NAMES.TM_CARET_LINE_NUMBER.name(), String.valueOf(lineAtCaret + 1));
								environment.put(VARIABLES_NAMES.TM_CARET_LINE_TEXT.name(), styledText.getLine(lineAtCaret));
							}
						}
					}
				}
			}
		} else {
			
		}
		return environment;
	}
	
	public static void launch(String command, 
			Map<String, String> environment,
			FilterInputProvider filterInputProvider) {
		launch(command,
				environment,
				filterInputProvider,
				new EclipseConsolePrintStreamOutputConsumer());
	};
	
	public static void launch(String command,
			Map<String, String> environment,
			FilterInputProvider filterInputProvider,
			FilterOutputConsumer filterSTDOUTConsumerProvider) {
		launch(command,
				environment,
				filterInputProvider,
				filterSTDOUTConsumerProvider,
				new EclipseConsolePrintStreamOutputConsumer());
	};
	
	public static void launch(final String command,
			final Map<String, String> environment,
			final FilterInputProvider filterInputProvider,
			final FilterOutputConsumer filterSTDOUTConsumerProvider,
			final FilterOutputConsumer filterSTDERRConsumerProvider) {
		// Launch command on a separate thread.
		new Thread(new Runnable() {
			public void run() {
				Activator activator = Activator.getDefault();
				String[] commandArray = Utilities.parseParameters(command);
				try {
					
					ProcessBuilder processBuilder = new ProcessBuilder();
					processBuilder.command(Arrays.asList(commandArray));
					Map<String, String> inheritedEnvironment = processBuilder.environment();
					if (environment != null) {
						inheritedEnvironment.putAll(environment);
					}
					
					final Process process = processBuilder.start();
					filterSTDOUTConsumerProvider.consume(process.getInputStream());
					filterSTDERRConsumerProvider.consume(process.getErrorStream());
					
					new Thread(new Runnable() {
						public void run() {
							InputStream is = filterInputProvider.getInputStream();
							OutputStream os = process.getOutputStream();
							byte bytes[] = new byte[1024];
							while (true) {
								try {
									int readCount = is.read(bytes);
									if (readCount == -1) {
										break;
									}
									os.write(bytes, 0, readCount);
								} catch (IOException e) {
									// TODO
									break;
								}
							}
						}
					}).start();
					
					int status = process.waitFor();
					if (status == 0) {
						// Good
					} else {
						activator.getLog().log(
								new Status(IStatus.ERROR, activator.getBundle()
										.getSymbolicName(), "Process '"
										+ Arrays.asList(commandArray)
												.toString()
										+ "' exited with status: " + status));
					}
				} catch (InterruptedException ex) {
					activator.getLog().log(
							new Status(IStatus.ERROR, activator.getBundle()
									.getSymbolicName(),
									"Exception while executing '"
											+ Arrays.asList(commandArray)
													.toString() + "'", ex));
				} catch (IOException ioe) {
					activator.getLog().log(
							new Status(IStatus.ERROR, activator.getBundle()
									.getSymbolicName(),
									"Exception while executing '"
											+ Arrays.asList(commandArray)
													.toString() + "'", ioe));
				}

			}
		}, "Launching - " + command).start();
	}
	
	public interface FilterInputProvider {
		public InputStream getInputStream();
	};
	
	public interface FilterOutputConsumer {
		public void consume(InputStream input);
	}
	
	public static final FilterInputProvider EOF = new FilterInputProvider() {
		public InputStream getInputStream() {
			return new StringBufferInputStream("");
		}
	};
	
	public static class PrintStreamOutputConsumer implements FilterOutputConsumer
	{
		private PrintStream printStream;
		
		public PrintStreamOutputConsumer() {
		}
		
		public PrintStreamOutputConsumer(PrintStream printStream) {
			this.printStream = printStream;
		}
		
		public PrintStream getPrintStream() {
			return printStream;
		}
		
		protected void setPrintStream(PrintStream printStream) {
			this.printStream = printStream;
		}

		public void consume(final InputStream outputStream) {
			new Thread(new Runnable() {
				public void run() {
					BufferedReader br = new BufferedReader(new InputStreamReader(outputStream));
					String line = null;
					try {
						while ((line = br.readLine()) != null) {
							if (printStream != null) {
								printStream.println(line);
							}
						}
					} catch (IOException e) {
					}
				}
			}).start();
		}
	}
	
	public static class StringOutputConsumer implements FilterOutputConsumer{
		private BlockingQueue<String> outputQueue;
		
		public StringOutputConsumer() {
			this(new ArrayBlockingQueue<String>(1));
		}
		
		public StringOutputConsumer(BlockingQueue<String> outputQueue) {
			this.outputQueue = outputQueue;
		}

		public void consume(final InputStream outputStream) {
			new Thread(new Runnable() {
				public void run() {
					StringBuilder stringBuilder = new StringBuilder();
					BufferedReader br = new BufferedReader(new InputStreamReader(outputStream));
					String line = null;
					try {
						while ((line = br.readLine()) != null) {
							stringBuilder.append(line + "\n");
						}
					} catch (IOException e) {
					}
					outputQueue.add(stringBuilder.toString());
				}
			}).start();
		}
		
		public String getOutput() throws InterruptedException {
			return outputQueue.take();
		}
	}
	
	public static class EclipseConsolePrintStreamOutputConsumer implements FilterOutputConsumer {
		private final String consoleName;

		public EclipseConsolePrintStreamOutputConsumer() {
			this(DEFAULT_CONSOLE_NAME);
		}
		
		public EclipseConsolePrintStreamOutputConsumer(String consoleName) {
			this.consoleName = consoleName;
		}
		
		public void consume(InputStream outputStream) {
			MessageConsole messageConsole = getMessageConsole(consoleName);
			MessageConsoleWriter messageConsoleWriter =
				new MessageConsoleWriter(messageConsole, outputStream);
			new Thread(messageConsoleWriter).start();
		}
	}
	
//	public class NamedConsolePrintStreamOutputConsumer extends PrintStreamOutputConsumer {
//		
//	}
	
	public static final FilterOutputConsumer DISCARD = new PrintStreamOutputConsumer();
	
	public static final FilterOutputConsumer TO_SYSOUT = new PrintStreamOutputConsumer(System.out);
	
	public static final FilterOutputConsumer TO_SYSERR = new PrintStreamOutputConsumer(System.err);	
	
	private static Map<String, MessageConsole> nameToMessageConsole = new WeakHashMap<String, MessageConsole>();

	@SuppressWarnings("unused")
	private static MessageConsole getMessageConsole() {
		return getMessageConsole(DEFAULT_CONSOLE_NAME);
	}
	
	private static MessageConsole getMessageConsole(String name) {
		MessageConsole messageConsole = nameToMessageConsole.get(name);
		if (messageConsole == null) {
			messageConsole = new MessageConsole(name, null);
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[]{messageConsole});
			nameToMessageConsole.put(name, messageConsole);
		}
		return messageConsole;
	}
	
	private static class MessageConsoleWriter implements Runnable {		
		private final MessageConsole messageConsole;
		private final InputStream from;
		
		private MessageConsoleWriter(MessageConsole messageConsole, InputStream from) {
			this.messageConsole = messageConsole;
			this.from = from;
		}
		
		public void run() {
			final MessageConsoleStream messageConsoleStream = messageConsole.newMessageStream();
			BufferedReader reader = new BufferedReader(new InputStreamReader(from));
			String output = null;
			try {
				while ((output = reader.readLine()) != null) {
					messageConsoleStream.println(output);
				}
			} catch (IOException e) {
			} finally {
				try {
					reader.close();
				} catch (IOException e) {
				}
				try {
					messageConsoleStream.close();
				} catch (IOException e) {
				}
			}
		}		
	}

}
