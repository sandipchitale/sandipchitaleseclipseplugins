package editorfindbar.api;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.texteditor.ITextEditor;

import editorfindbar.impl.FindBarDecorator;

/**
 * This is a factory for FindBarDecorator.
 * <p>
 * The typical usage is as follows:
 * <pre>
 * public void createPartControl(Composite parent) {
 *     Composite findBarComposite = getfindBarDecorator().createFindBarComposite(parent);
 *     super.createPartControl(findBarComposite);
 *     getfindBarDecorator().createFindBar(getSourceViewer());
 * }
 * 
 * protected void createActions() {
 *     super.createActions();
 *     getfindBarDecorator().createActions();
 * }
 * 
 * private AtomicReference<IFindBarDecorator> findBarDecorator = new AtomicReference<IFindBarDecorator>();
 * private IFindBarDecorator getfindBarDecorator() {
 *     findBarDecorator.compareAndSet(null, FindBarDecoratorFactory.createFindBarDecorator(this, getStatusLineManager()));
 *     return findBarDecorator.get();
 * }
 * </pre>
 * @author schitale
 *
 */
public class FindBarDecoratorFactory {
	public static IFindBarDecorator createFindBarDecorator(ITextEditor textEditor,
			IStatusLineManager statusLineManager) {
		return new FindBarDecorator(textEditor, statusLineManager);
	}
}
