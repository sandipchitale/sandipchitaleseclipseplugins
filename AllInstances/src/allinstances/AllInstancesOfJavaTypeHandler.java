package allinstances;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIAllInstancesValue;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceType;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jdt.internal.ui.JavaUIMessages;
import org.eclipse.jdt.internal.ui.dialogs.OpenTypeSelectionDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * This shows all instances of selected Java Type in the Expressions view.
 * 
 * @author Sandip Chitale
 *
 */
@SuppressWarnings("restriction")
public class AllInstancesOfJavaTypeHandler extends AbstractHandler {

	public Object execute(ExecutionEvent event) throws ExecutionException {
		Shell shell = HandlerUtil.getActiveShell(event);
		if (shell != null) {
			// Is there a active debug context?
			IAdaptable debugContext = DebugUITools.getDebugContext();
			if (debugContext == null) {
				MessageDialog.openError(shell, "No Debug Session", "No debug session is active!");
				return null;
			}
			// Is there a active Java debug context?
			IJavaDebugTarget target = (IJavaDebugTarget) debugContext.getAdapter(IJavaDebugTarget.class);
			if (target == null || target.isTerminated()) {
				MessageDialog.openError(shell, "No Java Debug Session", "No running Java debug session is selected in Debug view!");
				return null;
			}
			
			if (!target.supportsInstanceRetrieval()) {
				MessageDialog.openError(shell, "Unsupported Java Debug Session", "JVM running Java debug session does not support instances.");
				return null;
			}
			
			// Prompt the user for a Java Class
			SelectionDialog dialog = new OpenTypeSelectionDialog(shell, true, PlatformUI.getWorkbench()
					.getProgressService(), null, IJavaSearchConstants.CLASS);
			dialog.setTitle(JavaUIMessages.OpenTypeAction_dialogTitle);
			dialog.setMessage("Select Java Class to show instances of:");
			if (dialog.open() == IDialogConstants.OK_ID) {
				// Show the instances of Java Class
				Object[] typesArray = dialog.getResult();
				if (typesArray != null && typesArray.length > 0 && typesArray[0] instanceof IType) {
					IType iType = (IType) typesArray[0];
					IJavaType[] types;
					try {
						types = target.getJavaTypes(iType.getFullyQualifiedName());
						if (types == null || types.length == 0) {
							// If the type is not known the VM, open
							// a pop-up dialog with 0 instances
							MessageDialog.openError(shell, "No Instances", iType.getFullyQualifiedName());
							return null;
						}
						boolean activateExpressionsView = false;
						for (IJavaType type : types) {
							if (type instanceof JDIReferenceType) {
								JDIReferenceType rtype = (JDIReferenceType) type;
								try {
									JDIAllInstancesValue aiv = new JDIAllInstancesValue(
											(JDIDebugTarget) rtype.getDebugTarget(), rtype);
									DebugPlugin
									.getDefault()
									.getExpressionManager()
									.addExpression(
											new JavaInspectExpression("Instances Of " + rtype.getName(), aiv));

									activateExpressionsView = true;
								} catch (DebugException e) {
									MessageDialog.openError(shell, "Exception",
											"Exception while showing all instances of :" + iType.getElementName());
								}
							}
						}
						if (activateExpressionsView) {
							IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
							IViewPart part = page.findView(IDebugUIConstants.ID_EXPRESSION_VIEW);
							if (part == null) {
								try {
									page.showView(IDebugUIConstants.ID_EXPRESSION_VIEW);
								} catch (PartInitException e) {
								}
							} else {
								page.bringToTop(part);
							}
						}
					} catch (DebugException e) {
						MessageDialog.openError(shell, "Exception",
								"Exception while trying to get all instances of :" + iType.getElementName());
					}
				}
			}
		}
		return null;
	}

}
