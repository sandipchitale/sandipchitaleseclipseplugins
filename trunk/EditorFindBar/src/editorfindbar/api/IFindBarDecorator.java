package editorfindbar.api;

import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.swt.widgets.Composite;

public interface IFindBarDecorator {
	Composite createFindBarComposite(Composite parent);
	void createFindBar(ISourceViewer sourceViewer);
}
