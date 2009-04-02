package reorder;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.internal.corext.dom.NodeFinder;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jdt.ui.SharedASTProvider;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

@SuppressWarnings("restriction")
public class SwapOperations {
	enum Bias {
		BACKWARD, FORWARD
	};

	@SuppressWarnings("unchecked")
	static void swap(JavaEditor editor, Bias bias) {
		if (!editor.isEditable()) {
			Display.getCurrent().beep();
			return;
		}

		ISourceViewer viewer = editor.getViewer();
		if (viewer == null)
			return;

		Point selectedRange = viewer.getSelectedRange();
		int caretAt = selectedRange.x;
		int length = selectedRange.y;

		ITypeRoot element = JavaUI.getEditorInputTypeRoot(editor
				.getEditorInput());
		if (element == null)
			return;

		CompilationUnit ast = SharedASTProvider.getAST(element,
				SharedASTProvider.WAIT_YES, null);
		if (ast == null)
			return;

		NodeFinder finder = new NodeFinder(caretAt, length);
		ast.accept(finder);
		ASTNode originalNode = finder.getCoveringNode();
		ASTNode node;

		List arguments = null;

		node = originalNode;
		while (node != null) {
			node = node.getParent();
		
			if (node instanceof ClassInstanceCreation) {
				arguments = ((ClassInstanceCreation) node).arguments();
			} else if (node instanceof MethodInvocation) {
				arguments = ((MethodInvocation) node).arguments();
			} else if (node instanceof MethodDeclaration) {
				arguments = ((MethodDeclaration) node).parameters();
			} else if (node instanceof ArrayCreation) {
				ArrayCreation arrayCreation = (ArrayCreation) node;
				ArrayInitializer initializer = arrayCreation.getInitializer();
				if (initializer != null) {
					arguments = initializer.expressions();
				}
			}

			// Something to reorder
			if (arguments != null && arguments.size() >= 2) {
				final int firstStart = ((ASTNode) arguments.get(0))
						.getStartPosition();
				final int lastEnd = ((ASTNode) arguments.get(arguments.size() - 1))
						.getStartPosition()
						+ ((ASTNode) arguments.get(arguments.size() - 1))
								.getLength();
				// Is the caret in the range
				if (firstStart <= caretAt && caretAt <= lastEnd) {
					int caretOffset = -1;
					List<String> tokens = new LinkedList<String>();
					int currentTokenIndex = -1;
					String currentToken = null;
					int previousEnd = -1;
	
					// Add items and intervening white spaces to a list
					// Also remember the item that surrounds the caret
					for (Object argument : arguments) {
						ASTNode expression = (ASTNode) argument;
						int startPosition = expression.getStartPosition();
						int endPosition = startPosition + expression.getLength();
						if (previousEnd != -1) {
							try {
								tokens.add(viewer.getDocument().get(previousEnd, (startPosition-previousEnd)));
							} catch (BadLocationException e) {
								Activator.getDefault().getLog().log(new Status(IStatus.ERROR
										,Activator.PLUGIN_ID
										,"Could not get whitespace token."));
								return;
							}
						}
						final String token = argument.toString();
						tokens.add(token);
	
						if (startPosition <= caretAt && caretAt <= endPosition) {
							currentTokenIndex = tokens.size() - 1;
							currentToken = token;
							caretOffset = caretAt - (int) startPosition;
						}
						previousEnd = endPosition;
					}
	
					if (tokens.size() > 0 && currentTokenIndex != -1) {
						String current = tokens.get(currentTokenIndex);
						switch (bias) {
						case BACKWARD:
							if (currentTokenIndex == 0) {
								Display.getCurrent().beep();
								return;
							}
							String previous = tokens.get(currentTokenIndex - 2);
							tokens.set(currentTokenIndex - 2, current);
							tokens.set(currentTokenIndex, previous);
							break;
						case FORWARD:
							if (currentTokenIndex == (tokens.size() - 1)) {
								Display.getCurrent().beep();
								return;
							}
							String next = tokens.get(currentTokenIndex + 2);
							tokens.set(currentTokenIndex + 2, current);
							tokens.set(currentTokenIndex, next);
							break;
						}
	
						// Build the insertion string
						final StringBuilder stringBuilder = new StringBuilder();
						int offset = 0;
						final int[] moveCaretOffset = new int[1];
						for (String token : tokens) {
							final String text = token;
							if (token == currentToken) {
								moveCaretOffset[0] = offset + caretOffset;
							}
							offset += text.length();
							stringBuilder.append(text);
						}
	
						// Now replace the old text with new text
						try {
							viewer.getDocument().replace(firstStart,
									(lastEnd - firstStart), stringBuilder.toString());
						} catch (BadLocationException e) {
							Activator.getDefault().getLog().log(new Status(IStatus.ERROR
									,Activator.PLUGIN_ID
									,"Could not swap items."));
							return;
						}
						viewer.setSelectedRange((int) firstStart
								+ moveCaretOffset[0], 0);
					}
				}
				break;
			}
		}
	}
}
