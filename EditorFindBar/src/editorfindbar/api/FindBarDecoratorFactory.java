package editorfindbar.api;

import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.ui.texteditor.ITextEditor;

import editorfindbar.impl.FindBarDecorator;

/**
 * This is a factory for FindBarDecorator.
 * <p>
 * The typical usage is as follows:
 * <code>
 * 
 * 	public void createPartControl(Composite parent) {
 *      IFindBarDecorator findBarDecorator = 
 *          FindBarDecoratorFactory.createBarDecorator(this, getStatusLineManager());
 *      Composite findBarComposite = findBarDecorator.createFindBarComposite(parent);
 *      super.createPartControl(parent);
 *      findBarComposite.createFindBar(getSourceViewer());
 *  }
 * </code>
 * @author schitale
 *
 */
public class FindBarDecoratorFactory {
	public static IFindBarDecorator createFindBarDecorator(ITextEditor textEditor,
			IStatusLineManager statusLineManager) {
		return new FindBarDecorator(textEditor, statusLineManager);
	}
}
