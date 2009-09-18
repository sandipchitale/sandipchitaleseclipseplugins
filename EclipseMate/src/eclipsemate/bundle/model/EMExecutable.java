package eclipsemate.bundle.model;

import org.eclipse.jface.bindings.keys.KeySequence;

import eclipsemate.bundle.model.dynamic.EMContext;

public abstract class EMExecutable extends EMBindable {

	public EMExecutable(String name, String scope, KeySequence keySequence) {
		super(name, name, keySequence);
	}
	
	public EMExecutable(String name, String scope, String prefix) {
		super(name, name, prefix);
	}
	
	public abstract Object execute(EMContext Context);
}
