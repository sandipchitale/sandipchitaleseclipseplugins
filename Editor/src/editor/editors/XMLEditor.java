package editor.editors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;

import editorfindbar.api.FindBarDecoratorFactory;
import editorfindbar.api.IFindBarDecorated;
import editorfindbar.api.IFindBarDecorator;

public class XMLEditor extends TextEditor implements IFindBarDecorated {

	private ColorManager colorManager;

	public XMLEditor() {
		super();
		colorManager = new ColorManager();
		setSourceViewerConfiguration(new XMLConfiguration(colorManager));
		setDocumentProvider(new XMLDocumentProvider());
	}
	public void dispose() {
		colorManager.dispose();
		super.dispose();
	}
	
	@Override
	public void createPartControl(Composite parent) {
		Composite findBarComposite = getFindBarDecorator().createFindBarComposite(parent);
		super.createPartControl(findBarComposite);
		getFindBarDecorator().createFindBar(getSourceViewer());
	}

	private IFindBarDecorator findBarDecorator;
	
	public IFindBarDecorator getFindBarDecorator() {
		if (findBarDecorator == null) {
			findBarDecorator = FindBarDecoratorFactory.createFindBarDecorator(this, getStatusLineManager());
		}
		return findBarDecorator;
	}
}
