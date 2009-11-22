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
		IFindBarDecorator findBarDecorator = FindBarDecoratorFactory.createFindBarDecorator(this, getStatusLineManager());
		Composite findBarComposite = findBarDecorator.createFindBarComposite(parent);
		super.createPartControl(findBarComposite);
		findBarDecorator.createFindBar(getSourceViewer());
	}

}
