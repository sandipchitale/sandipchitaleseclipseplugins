package eclipsemate.bundle.model;

public abstract class EMScoped extends EMBase {
	private String scope;

	public EMScoped(String name, String scope) {
		super(name);
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getScope() {
		return scope;
	}

}
