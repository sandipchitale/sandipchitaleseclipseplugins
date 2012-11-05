package allinstances;

import java.lang.annotation.Annotation;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaClassObject;
import org.eclipse.jdt.debug.core.IJavaClassType;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaFieldVariable;
import org.eclipse.jdt.debug.core.IJavaInterfaceType;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.internal.debug.core.logicalstructures.JDIAllInstancesValue;
import org.eclipse.jdt.internal.debug.core.model.JDIClassType;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIInterfaceType;
import org.eclipse.jdt.internal.debug.core.model.JDINullValue;
import org.eclipse.jdt.internal.debug.core.model.JDIReferenceType;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.display.JavaInspectExpression;
import org.eclipse.jdt.internal.ui.JavaUIMessages;
import org.eclipse.jdt.internal.ui.dialogs.OpenTypeSelectionDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
	private static boolean showInstancesOfSubclasses = true;
	private static boolean showInnerClasses = false;
	private static boolean showAnonymousInnerClasses = false;
	private static boolean showZeroInstances = true;
	
	private static Pattern dollarNumber = Pattern.compile(Pattern.quote("$") + "\\d");

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
					.getProgressService(), null, IJavaSearchConstants.TYPE) {
				protected Control createDialogArea(Composite parent) {
					Composite dialogArea = (Composite) super.createDialogArea(parent);
					final Button showInstancesOfSubclassesButton = new Button(dialogArea, SWT.CHECK);
					showInstancesOfSubclassesButton.setText("Show instances of subclasses");
					GridData gd = new GridData(GridData.FILL_HORIZONTAL);
					showInstancesOfSubclassesButton.setLayoutData(gd);
					showInstancesOfSubclassesButton.setSelection(showInstancesOfSubclasses);
					
					final Button showInnerClassesButton = new Button(dialogArea, SWT.CHECK);
					showInnerClassesButton.setText("Include inner classes");
					gd = new GridData(GridData.FILL_HORIZONTAL);
					gd.horizontalIndent = 20;
					showInnerClassesButton.setLayoutData(gd);
					showInnerClassesButton.setSelection(showInnerClasses);
					showInnerClassesButton.setEnabled(showInstancesOfSubclasses);

					final Button showAnonymousInnerClassesButton = new Button(dialogArea, SWT.CHECK);
					showAnonymousInnerClassesButton.setText("Include anonymous inner classes");
					gd = new GridData(GridData.FILL_HORIZONTAL);
					gd.horizontalIndent = 40;
					showAnonymousInnerClassesButton.setLayoutData(gd);
					showAnonymousInnerClassesButton.setSelection(showAnonymousInnerClasses);
					showAnonymousInnerClassesButton.setEnabled(showInnerClasses);
					showAnonymousInnerClassesButton.addSelectionListener(new SelectionListener() {
						public void widgetSelected(SelectionEvent e) {
							showAnonymousInnerClasses = showAnonymousInnerClassesButton.getSelection();
						}

						public void widgetDefaultSelected(SelectionEvent e) {
						}
					});
					
					final Button showZeroInstancesButton = new Button(dialogArea, SWT.CHECK);
					showZeroInstancesButton.setText("Show even if there are zero instances");
					gd = new GridData(GridData.FILL_HORIZONTAL);
					showZeroInstancesButton.setLayoutData(gd);
					showZeroInstancesButton.setSelection(showZeroInstances);
					showZeroInstancesButton.addSelectionListener(new SelectionListener() {
						public void widgetSelected(SelectionEvent e) {
							showZeroInstances = showZeroInstancesButton.getSelection();
						}

						public void widgetDefaultSelected(SelectionEvent e) {
						}
					});
					
					showInnerClassesButton.addSelectionListener(new SelectionListener() {
						public void widgetSelected(SelectionEvent e) {
							showInnerClasses = showInnerClassesButton.getSelection();
							showAnonymousInnerClassesButton.setEnabled(showInnerClasses);
						}

						public void widgetDefaultSelected(SelectionEvent e) {
						}
					});
					
					showInstancesOfSubclassesButton.addSelectionListener(new SelectionListener() {
						public void widgetSelected(SelectionEvent e) {
							showInstancesOfSubclasses = showInstancesOfSubclassesButton.getSelection();
							showInnerClassesButton.setEnabled(showInstancesOfSubclasses);
						}

						public void widgetDefaultSelected(SelectionEvent e) {
						}
					});
					return dialogArea;
				}
			};
			dialog.setTitle("Select Class, Interface or Annotation");
			dialog.setMessage(
					"Select the class to show all instances of or\n" +
					"select the interface to show all implementors of or\n" +
					"select the annotation to show all instances annotated with");
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
					Job job = new Job("Getting all instances for " + iType.getFullyQualifiedName()) {

						@Override
						protected IStatus run(IProgressMonitor monitor) {
							IJavaType[] types;
							try {
								types = target.getJavaTypes(iType.getFullyQualifiedName());
								if (types == null || types.length == 0) {
									// If the type is not known the VM, open
									// a pop-up dialog with 0 instances
									PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
										public void run() {
											MessageDialog.openError(shell, "No Class", "No Class with name: " + iType.getFullyQualifiedName());
										}
									});

									return Status.CANCEL_STATUS;
								}

								List<IJavaClassObject> allClassObjects = new LinkedList<IJavaClassObject>();
								if (showInstancesOfSubclasses) {
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
								}

								IThread suspendedThread = null;
								// find any thread that is suspended
								IThread[] threads = target.getThreads();
								for (IThread thread : threads) {
									if (thread.isSuspended()) {
										suspendedThread = thread;
										break;
									}
								}

								for (IJavaType type : types) {
									if (monitor.isCanceled()) {
										return Status.CANCEL_STATUS;
									}
									if (type instanceof JDIClassType || type instanceof JDIInterfaceType) {
										boolean isAnnotation = false;
										JDIReferenceType referenceType = (JDIReferenceType) type;
										if (referenceType instanceof JDIInterfaceType) {
											JDIInterfaceType interfaceType = (JDIInterfaceType) referenceType;
											IJavaInterfaceType[] superInterfaces = interfaceType.getSuperInterfaces();
											for (int i = 0; i < superInterfaces.length; i++) {
												IJavaInterfaceType superInterfaceType = superInterfaces[i];
												if (Annotation.class.getName().equals(superInterfaceType.getName())) {
													isAnnotation = true;
												}
											}
										}
										Set<JDIReferenceType> typeAndSubTypes = new LinkedHashSet<JDIReferenceType>();
										typeAndSubTypes.add(referenceType);
										if (showInstancesOfSubclasses) {
											// Skip over java.lang.Object to prevent loading all instances
											// in the VM
											if (type instanceof JDIInterfaceType || (!Object.class.getName().equals(((JDIClassType) type).getName()))) {
												for (IJavaClassObject allClassObject : allClassObjects) {
													IJavaType instanceType = allClassObject.getInstanceType();
													if (instanceType instanceof JDIClassType) {
														JDIClassType jdiClassTypeSaved = (JDIClassType) instanceType;
														JDIClassType jdiClassType = jdiClassTypeSaved;
														boolean isSubClassOrImplementor = false;
														loop: do {
															if (typeAndSubTypes.contains(jdiClassType)) {
																isSubClassOrImplementor = true;
																break;
															}
															if (type instanceof JDIInterfaceType) {
																if (isAnnotation) {
																	if (suspendedThread != null) {
																		// Get annotations of the class and see if has the selected annotation
																		IJavaValue javaValue = allClassObject.sendMessage("getAnnotations", "()[Ljava/lang/annotation/Annotation;", null, (IJavaThread) suspendedThread, null);
																		if (javaValue instanceof IJavaArray) {
																			IJavaArray javaArray = (IJavaArray) javaValue;
																			if (javaArray.getLength() > 0) {
																				IJavaValue[] values = javaArray.getValues();
																				for (IJavaValue value : values) {
																					if (value instanceof IJavaObject) {
																						IJavaObject javaObject = (IJavaObject) value;
																						IJavaType javaType = javaObject.getJavaType();
																						if (javaType instanceof IJavaClassType) {
																							IJavaClassType javaClassType = (IJavaClassType) javaType;
																							IJavaInterfaceType[] interfaces = javaClassType.getInterfaces();
																							for (IJavaInterfaceType anInterface : interfaces) {
																								// Found a matching Annotation
																								if (anInterface.getName().equals(type.getName())) {
																									isSubClassOrImplementor = true;
																									break;
																								}
																							}
																						}
																					}
																				}
																			}
																		}
																	}
																} else {
																	try {
																	IJavaInterfaceType[] jdiInterfaceTypes = jdiClassType.getAllInterfaces();
																	for (IJavaInterfaceType jdiIterfaceType : jdiInterfaceTypes) {
																		if (typeAndSubTypes.contains(jdiIterfaceType)) {
																			isSubClassOrImplementor = true;
																			break loop;
																		}
																		IJavaInterfaceType[] jdiSuperInterfacesTypes = jdiIterfaceType.getSuperInterfaces();
																		for (IJavaInterfaceType jdiSuperInterfaceType : jdiSuperInterfacesTypes) {
																			if (typeAndSubTypes.contains(jdiSuperInterfaceType)) {
																				isSubClassOrImplementor = true;
																				break loop;
																			}
																		}
																	}
																	} catch (Exception e) {
																		// Ignore
																	}
																}
															}
															jdiClassType = (JDIClassType)jdiClassType.getSuperclass();
														} while (jdiClassType != null);
														if (isSubClassOrImplementor) {
															typeAndSubTypes.add(jdiClassTypeSaved);
														}
													}
												}
											}
										}
										
										// TODO Topological sort - superclasses before subclasses
										for (JDIReferenceType typeOrSubType : typeAndSubTypes) {
											if (monitor.isCanceled()) {
												return Status.CANCEL_STATUS;
											}
											if (typeOrSubType instanceof JDIInterfaceType) {
												continue;
											}
											if (!showZeroInstances && typeOrSubType.getInstanceCount() <= 0) {
												continue;
											}
											try {
												if (!showInnerClasses) { 
													if (typeOrSubType.getName().indexOf('$') != -1) {
														continue;
													}
												} else if (!showAnonymousInnerClasses) {
													Matcher matcher = dollarNumber.matcher(typeOrSubType.getName());
													if (matcher.find()) {
														continue;
													}
												}
												
												IJavaClassObject classObject = typeOrSubType.getClassObject();
												JDIAllInstancesValue aiv = new JDIAllInstancesValue(
														(JDIDebugTarget) typeOrSubType.getDebugTarget(), typeOrSubType);
												DebugPlugin
													.getDefault()
													.getExpressionManager()
													.addExpression(
															new JavaInspectExpression(typeOrSubType.getName() + " Instances (" + typeOrSubType.getInstanceCount() + ")", aiv));
												try {
													if (suspendedThread != null) {
														IJavaValue javaValue = classObject.sendMessage("getProtectionDomain", "()Ljava/security/ProtectionDomain;", null, (IJavaThread) suspendedThread, null);
														if (javaValue instanceof IJavaObject) {
															javaValue = ((IJavaObject) javaValue).sendMessage("getCodeSource", "()Ljava/security/CodeSource;", null, (IJavaThread) suspendedThread, null);
															if (javaValue instanceof IJavaObject && !(javaValue instanceof JDINullValue)) {
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

											} catch (final DebugException e) {
												PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
													public void run() {
														MessageDialog.openError(shell, "Exception",
																"Exception while showing all instances of :" + iType.getFullyQualifiedName()
																+ ". " + e.getMessage());
													}
												});
											}
										}
									}
								}
							} catch (final DebugException e) {
								PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
									public void run() {
										MessageDialog.openError(shell, "Exception",
												"Exception while trying to get all instances of :" + iType.getFullyQualifiedName()
												+ ". " + e.getMessage());
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
