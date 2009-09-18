package eclipsemate.bundle.model;

import org.eclipse.jface.bindings.keys.KeySequence;

public class EMBindable extends EMScoped {

	protected BINDING_TYPE bindingType;
	private KeySequence keySequence = null;
	private String prefix = null;

	public EMBindable(String name, String scope, KeySequence keySequence) {
		super(name, scope);
		this.bindingType = BINDING_TYPE.KEY_SEQUENCE;
		this.keySequence = keySequence;
	}

	public EMBindable(String name, String scope, String prefix) {
		super(name, scope);
		this.prefix = prefix;
		this.bindingType = BINDING_TYPE.TAB;
	}

	public BINDING_TYPE getBindingType() {
		return bindingType;
	}

	public void setBindingType(BINDING_TYPE bindingType) {
		this.bindingType = bindingType;
	}


	public KeySequence getKeySequence() {
		return keySequence;
	}

	public void setKeySequence(KeySequence keySequence) {
		this.keySequence = keySequence;
	}

	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}	
	
}
