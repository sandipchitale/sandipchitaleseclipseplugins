package themeclipse;

import org.eclipse.core.runtime.IProduct;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Sash;
import org.eclipse.ui.internal.presentations.classic.WorkbenchPresentationFactoryClassic;
import org.eclipse.ui.presentations.IStackPresentationSite;
import org.eclipse.ui.presentations.StackPresentation;

@SuppressWarnings("restriction")
public class AbstractPresentationFactory extends WorkbenchPresentationFactoryClassic {

	public AbstractPresentationFactory() {		
		IProduct product = Platform.getProduct();
	}
	
	@Override
	public Sash createSash(Composite parent, int style) {
		Sash sash = super.createSash(parent, style);
		sash.setBackground(sash.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		return sash;
	}
	
	
	@Override
	public StackPresentation createEditorPresentation(Composite parent,
			IStackPresentationSite site) {
		StackPresentation stackPresentation = super.createEditorPresentation(parent, site);
		Control control = stackPresentation.getControl();
		control.setBackground(control.getDisplay().getSystemColor(SWT.COLOR_GRAY));
		control.setForeground(control.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		return stackPresentation;
	}
	
	@Override
	public StackPresentation createViewPresentation(Composite parent,
			IStackPresentationSite site) {
		StackPresentation stackPresentation = super.createViewPresentation(parent, site);
		Control control = stackPresentation.getControl();
		control.setBackground(control.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		control.setForeground(control.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		return stackPresentation;
	}
	
	@Override
	public Control createStatusLineControl(IStatusLineManager statusLine,
			Composite parent) {
		Control control = super.createStatusLineControl(statusLine, parent);
		if (control instanceof Composite) {
			Composite composite = (Composite) control;
			
		}
		control.setBackground(control.getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		control.setForeground(control.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		return control;
	}
	
	@Override
	public IStatusLineManager createStatusLineManager() {
		IStatusLineManager statusLineManager = super.createStatusLineManager();
		return statusLineManager;
	}
}
