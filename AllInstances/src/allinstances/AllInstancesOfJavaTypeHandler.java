package allinstances;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.debug.core.IJavaClassObject;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIAllInstancesValue;
import org.eclipse.jdt.internal.debug.core.model.JDIClassType;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jdt.internal.ui.JavaUIMessages;
import org.eclipse.jdt.internal.ui.dialogs.OpenTypeSelectionDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

/**
 * This shows all instances of selected Java Type in the Expressions view.
 *
 * @author Sandip Chitale
 *
 */
@SuppressWarnings("restriction")
public class AllInstancesOfJavaTypeHandler extends AbstractHandler {

	public Object execute(final ExecutionEvent event) throws ExecutionException {
		final Shell shell = HandlerUtil.getActiveShell(event);
		if (shell != null) {
			// Is there a active debug context?
			IAdaptable debugContext = DebugUITools.getDebugContext();
			if (debugContext == null) {
				MessageDialog.openError(shell, "No Debug Session", "No debug session is active!");
				return null;
			}
			// Is there a active Java debug context?
			final IJavaDebugTarget target = (IJavaDebugTarget) debugContext.getAdapter(IJavaDebugTarget.class);
			if (target == null || target.isTerminated()) {
				MessageDialog.openError(shell, "No Java Debug Session",
						"No running Java debug session is selected in Debug view!");
				return null;
			}

			if (!target.supportsInstanceRetrieval()) {
				MessageDialog.openError(shell, "Unsupported Java Debug Session",
						"JVM running Java debug session does not support instances.");
				return null;
			}

			// Prompt the user for a Java Class
			SelectionDialog dialog = new OpenTypeSelectionDialog(shell, true, PlatformUI.getWorkbench()
					.getProgressService(), null, IJavaSearchConstants.CLASS);
			dialog.setTitle(JavaUIMessages.OpenTypeAction_dialogTitle);
			dialog.setMessage("Select Java Class to show instances of:");
			if (dialog.open() == IDialogConstants.OK_ID) {
				// Show the instances of Java Class
				final Object[] typesArray = dialog.getResult();
				if (typesArray != null && typesArray.length > 0 && typesArray[0] instanceof IType) {
					// Show the Expressions view
					IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
					IViewPart part = page.findView(IDebugUIConstants.ID_EXPRESSION_VIEW);
					if (part == null) {
						try {
							part = page.showView(IDebugUIConstants.ID_EXPRESSION_VIEW);
						} catch (PartInitException e) {
						}
					} else {
						page.bringToTop(part);
					}
					
					if (part == null) {
						return null;
					}
					final IType iType = (IType) typesArray[0];
					Job job = new Job("Computing Instances of " + iType.getElementName()) {

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							IJavaType[] types;
							try {
								List<IJavaClassObject> allClassObjects = new LinkedList<IJavaClassObject>();
								IJavaType[] javaLangClasses = target.getJavaTypes(Class.class.getName());
								for (IJavaType javaLangClassType : javaLangClasses) {
									if (javaLangClassType instanceof JDIClassType) {
										JDIClassType javaLangClass = (JDIClassType) javaLangClassType;
										IJavaObject[] javaLangClassInstances = javaLangClass.getInstances(Long.MAX_VALUE);
										for (IJavaObject javaLangClassInstance : javaLangClassInstances) {
											if (javaLangClassInstance instanceof IJavaClassObject) {
												IJavaClassObject javaLangClassInstanceClassObject = (IJavaClassObject) javaLangClassInstance;
												allClassObjects.add(javaLangClassInstanceClassObject);
											}
										}
									}
								}
								
								types = target.getJavaTypes(iType.getFullyQualifiedName());
								if (types == null || types.length == 0) {
									// If the type is not known the VM, open
									// a pop-up dialog with 0 instances
									PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
										public void run() {
											MessageDialog.openError(shell, "No Class", "No Class with name: " + iType.getFullyQualifiedName());
										}
									});
									
									return null;
								}
								
								for (IJavaType type : types) {
									if (monitor.isCanceled()) {
										return Status.CANCEL_STATUS;
									}
									if (type instanceof JDIClassType) {
										JDIClassType classType = (JDIClassType) type;
										Set<JDIClassType> typeAndSubTypes = new LinkedHashSet<JDIClassType>();
										typeAndSubTypes.add(classType);
										if (!Object.class.getName().equals(classType.getName())) {
											for (IJavaClassObject allClassObject : allClassObjects) {
												IJavaType instanceType = allClassObject.getInstanceType();
												if (instanceType instanceof JDIClassType) {
													JDIClassType jdiClassTypeSaved = (JDIClassType) instanceType;
													JDIClassType jdiClassType = jdiClassTypeSaved;
													boolean isSubClass = false;
													do {
														if (typeAndSubTypes.contains(jdiClassType)) {
															isSubClass = true;
															break;
														}
														jdiClassType = (JDIClassType)jdiClassType.getSuperclass();
													} while (jdiClassType != null);
													if (isSubClass) {
														typeAndSubTypes.add(jdiClassTypeSaved);
													}
												}										
											}
										}
										
										// TODO Topological sort - superclasses before subclasses
										
										for (JDIClassType typeOrSubType : typeAndSubTypes) {
											if (monitor.isCanceled()) {
												return Status.CANCEL_STATUS;
											}
											IJavaClassObject classObject = typeOrSubType.getClassObject();
											try {

												JDIAllInstancesValue aiv = new JDIAllInstancesValue(
														(JDIDebugTarget) typeOrSubType.getDebugTarget(), typeOrSubType);
												DebugPlugin
														.getDefault()
														.getExpressionManager()
														.addExpression(
																new JavaInspectExpression(typeOrSubType.getName() + " Instances (" + typeOrSubType.getInstanceCount() + ")", aiv));

												try {
													IThread suspendedThread = null;
													IThread[] threads = target.getThreads();
													for (IThread thread : threads) {
														if (thread.isSuspended()) {
															suspendedThread = thread;
															break;
														}
													}

													if (suspendedThread != null) {
														IJavaValue javaValue = classObject.sendMessage("getProtectionDomain", "()Ljava/security/ProtectionDomain;", null, (IJavaThread) suspendedThread, null);
														if (javaValue instanceof IJavaObject) {
															javaValue = ((IJavaObject) javaValue).sendMessage("getCodeSource", "()Ljava/security/CodeSource;", null, (IJavaThread) suspendedThread, null);
															if (javaValue instanceof IJavaObject) {
																IJavaObject classLoaderObject = typeOrSubType.getClassLoaderObject();
																DebugPlugin
																.getDefault()
																.getExpressionManager()
																.addExpression(
																		new JavaInspectExpression(
																				typeOrSubType.getName() + " CodeSource" + (classLoaderObject == null ? "" : "(ClassLoader " + classLoaderObject + ")"),
																				javaValue));
															}
														}
													}
												} catch (Exception e) {
												}

												// If no instances available - at least show the static fields
												if (aiv.getValues().length == 0) {
													IPreferenceStore preferenceStore = JDIDebugUIPlugin.getDefault().getPreferenceStore();
													if (preferenceStore.getBoolean(IDebugUIConstants.ID_EXPRESSION_VIEW + "." + IJDIPreferencesConstants.PREF_SHOW_STATIC_VARIABLES)) {
														// Static fields
														String[] declaredFieldNames = typeOrSubType.getDeclaredFieldNames();
														for (String fieldName : declaredFieldNames) {
															IJavaFieldVariable field = typeOrSubType.getField(fieldName);
															if (field != null && field.isStatic()) {
																DebugPlugin
																.getDefault()
																.getExpressionManager()
																.addExpression(
																		new JavaInspectExpression(
																				typeOrSubType.getName() + "." + field.getName(),
																				(IJavaValue) field.getValue()));
															}
														}
													}
												}

											} catch (DebugException e) {
												PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
													public void run() {
														MessageDialog.openError(shell, "Exception",
																"Exception while showing all instances of :" + iType.getElementName());
													}
												});
											}									
										}
									}
								}
							} catch (DebugException e) {
								PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
									public void run() {
										MessageDialog.openError(shell, "Exception", "Exception while trying to get all instances of :"
												+ iType.getElementName());
									}
								});
							}
							return Status.OK_STATUS;
						}
					};
					job.setPriority(Job.INTERACTIVE);
			        IWorkbenchSiteProgressService progressService = (IWorkbenchSiteProgressService) part.getSite().getService(IWorkbenchSiteProgressService.class);
			        progressService.schedule(job);
				}
			}
		}
		return null;
	}

}
