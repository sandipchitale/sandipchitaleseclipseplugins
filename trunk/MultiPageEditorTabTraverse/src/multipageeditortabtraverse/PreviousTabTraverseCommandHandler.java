package multipageeditortabtraverse;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.MultiPageEditorPart;

public class PreviousTabTraverseCommandHandler extends AbstractHandler {
	public Object execute(ExecutionEvent event) throws ExecutionException {
			goToNextTab();
		return null;
	}

	private void goToNextTab() {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			IWorkbenchPart activePart = activeWorkbenchWindow.getActivePage().getActivePart();
			if (activePart instanceof MultiPageEditorPart) {
				MultiPageEditorPart multiPageEditorPart = (MultiPageEditorPart) activePart;
				try {
					Method getContainerMethod = MultiPageEditorPart.class.getDeclaredMethod("getContainer");
					getContainerMethod.setAccessible(true);
					Object container = getContainerMethod.invoke(multiPageEditorPart);
					if (container instanceof CTabFolder) {
						CTabFolder tabFolder = (CTabFolder) container;
						int itemCount = tabFolder.getItemCount();
						if (itemCount > 0) {
							int selectionIndex = tabFolder.getSelectionIndex();
							selectionIndex--;
							if (selectionIndex < 0) {
								selectionIndex = itemCount - 1;
							}
							Method setActivePageMethod = MultiPageEditorPart.class.getDeclaredMethod("setActivePage", int.class);
							setActivePageMethod.setAccessible(true);
							setActivePageMethod.invoke(multiPageEditorPart, selectionIndex);
						}
					}
				} catch (SecurityException e) {
				} catch (NoSuchMethodException e) {
				} catch (IllegalArgumentException e) {
				} catch (IllegalAccessException e) {
				} catch (InvocationTargetException e) {
				}
			}
		}
	}

	@Override
	public boolean isEnabled() {
		IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (activeWorkbenchWindow != null) {
			IWorkbenchPart activePart = activeWorkbenchWindow.getActivePage().getActivePart();
			if (activePart instanceof MultiPageEditorPart) {
				return true;
			}
		}
		return false;
	}
}
