package cloneview;

import org.eclipse.rse.internal.ui.view.SystemViewPart;

@SuppressWarnings("restriction")
public class SystemViewPartClone extends SystemViewPart {
	void setCloneId(int i) {
		setPartName(getPartName() + " " + i);
	}
	
	protected void setPartName(String name) {
		super.setPartName(name);
	}
}
