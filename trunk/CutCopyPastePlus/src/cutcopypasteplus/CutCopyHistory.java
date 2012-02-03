package cutcopypasteplus;

import java.util.ArrayList;

public class CutCopyHistory {

	private static CutCopyHistory INSTANCE;

	synchronized static CutCopyHistory getInstance() {

		if (INSTANCE == null) {
			INSTANCE = new CutCopyHistory();
		}
		return INSTANCE;
	}

	private ArrayList<String> history;
	private int index = -1;

	private CutCopyHistory() {
		this.history = new ArrayList<String>();
	}

	void add(String cutCopy) {
		try {
			if (cutCopy != null && cutCopy.length() > 0) {
				if (history.size() > 0) {
					if (cutCopy.equals(history.get(0))) {
						return;
					}
				}
				history.add(0, cutCopy);
			}
		} finally {
			while (history.size() > Activator.getDefault().getMaxHistoryCount()) {
				history.remove(history.size() - 1);
			}
		}
	}

	void clear() {
		history.clear();
	}

	public void reset() {
		index = -1;
	}
	
	public String getFirstTextToPaste() {
		reset();
		return getNextTextToPaste();
	}

	public String getNextTextToPaste() {
		if (history.size() > 0) {
			index++;
			index = index % history.size();
			if (history.size() > index) {
				return history.get(index);
			}
		}
		return null;
	}

}
