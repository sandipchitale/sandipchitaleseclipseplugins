package eclipsemate.bundle.model;

import org.eclipse.jface.bindings.keys.KeySequence;

import eclipsemate.bundle.model.dynamic.EMContext;

public class EMSnippet extends EMExecutable {

	private String snippet;

	public EMSnippet(String name, String scope, KeySequence keySequence,
			String snippet) {
		super(name, scope, keySequence);
		this.snippet = snippet;

	}

	public EMSnippet(String name, String scope, String prefix, String snippet) {
		super(name, scope, prefix);
		this.snippet = snippet;

	}

	@Override
	public Object execute(EMContext context) {
		// TODO
		return null;
	}

	public String getSnippet() {
		return snippet;
	}

	public void setSnippet(String snippet) {
		this.snippet = snippet;
	}

}
