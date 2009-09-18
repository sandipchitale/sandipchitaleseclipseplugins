package eclipsemate.bundle.model;

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

public class EMBundle extends EMBase {
	private Set<EMExecutable> executables;

	public EMBundle(String name) {
		super(name);
		executables = new TreeSet<EMExecutable>();
	}
	
	public void addExecutable(EMExecutable executable) {
		executables.add(executable);
	}
	
	public void removeExecutable(EMExecutable executable) {
		executables.remove(executable);
	}
	
	public Set<EMExecutable> getExecutables() {
		return Collections.unmodifiableSet(executables);
	}
}
