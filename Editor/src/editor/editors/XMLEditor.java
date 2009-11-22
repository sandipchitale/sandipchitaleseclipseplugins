package editor.editors;

import java.util.concurrent.atomic.AtomicReference;

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

	private AtomicReference<IFindBarDecorator> findBarDecorator = new AtomicReference<IFindBarDecorator>();
	private IFindBarDecorator getfindBarDecorator() {
		findBarDecorator.compareAndSet(null, FindBarDecoratorFactory.createFindBarDecorator(this, getStatusLineManager()));
		return findBarDecorator.get();
	}
}
