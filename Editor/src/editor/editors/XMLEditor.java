package editor.editors;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.editors.text.TextEditor;

import editorfindbar.api.FindBarDecoratorFactory;
import editorfindbar.api.IFindBarDecorator;

public class XMLEditor extends TextEditor {

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
		Composite findBarComposite = getfindBarDecorator().createFindBarComposite(parent);
		super.createPartControl(findBarComposite);
		getfindBarDecorator().createFindBar(getSourceViewer());
	}
	
	@Override
	protected void createActions() {
		super.createActions();
		getfindBarDecorator().createActions();
	}

	private IFindBarDecorator findBarDecorator;
	private IFindBarDecorator getfindBarDecorator() {
		if (findBarDecorator == null) {
			findBarDecorator = FindBarDecoratorFactory.createFindBarDecorator(this, getStatusLineManager());
		}
		return findBarDecorator;
	}
}
