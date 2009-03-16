package googleclips;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.PlainTextConstruct;
import com.google.gdata.data.spreadsheet.Cell;
import com.google.gdata.data.spreadsheet.CellEntry;
import com.google.gdata.data.spreadsheet.CellFeed;
import com.google.gdata.data.spreadsheet.ListEntry;
import com.google.gdata.data.spreadsheet.ListFeed;
import com.google.gdata.data.spreadsheet.SpreadsheetEntry;
import com.google.gdata.data.spreadsheet.SpreadsheetFeed;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.data.spreadsheet.WorksheetFeed;
import com.google.gdata.util.ServiceException;

/**
 * The activator class controls the plug-in life cycle static main
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "GoogleClips";

	public static final String GOOGLE_ID = "googleId";
	public static final String SPREADSHEET_NAME = "spreadsheetName";
	public static final String WORKSHEET_NAME = "worksheetName";
	public static final String COLUMN_NAME = "columnName";

	public static final String AUTO_CLIP_CUT_COPY = "autoClipCutAndCopy";
	public static final String MAX_CLIPS_COUNT = "maxClips";

	private static final String defaultGoogleId = System.getProperty("user.name") + "@gmail.com";
	private static final String defaultSpreadsheetName = "CLIPBOARD";
	private static final String defaultWorksheetName = "CLIPBOARD";
	private static final String defaultColumnName = "clipboard";

	private static final boolean defaultAUTO_CLIP_CUT_COPY = false;
	private static final int defaultMAX_CLIPS_COUNT = 24;

	// The shared instance
	private static Activator plugin;

	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (event.getProperty().equals(GOOGLE_ID) || 
						event.getProperty().equals(SPREADSHEET_NAME) ||
						event.getProperty().equals(WORKSHEET_NAME) ||
						event.getProperty().equals(COLUMN_NAME)) {
					spreadsheetService = null;
				}
			}
			
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

	@Override
	protected void initializeDefaultPreferences(IPreferenceStore store) {
		store.setDefault(AUTO_CLIP_CUT_COPY, defaultAUTO_CLIP_CUT_COPY);
		store.setDefault(MAX_CLIPS_COUNT, defaultMAX_CLIPS_COUNT);
		store.setDefault(GOOGLE_ID, defaultGoogleId);
		store.setDefault(SPREADSHEET_NAME, defaultSpreadsheetName);
		store.setDefault(WORKSHEET_NAME, defaultWorksheetName);
		store.setDefault(COLUMN_NAME, defaultColumnName);
	}

	public boolean isAutoClipCutCopy() {
		return getPreferenceStore().getBoolean(AUTO_CLIP_CUT_COPY);
	}
	
	public void setAutoClipCutCopy(boolean autoClipCutCopy) {
		getPreferenceStore().setValue(AUTO_CLIP_CUT_COPY, autoClipCutCopy);
	}

	public int getMaxClipsCount() {
		return getPreferenceStore().getInt(MAX_CLIPS_COUNT);
	}

	public String getGoogleId() {
		return getPreferenceStore().getString(GOOGLE_ID);
	}

	public String getSpreadsheetName() {
		return getPreferenceStore().getString(SPREADSHEET_NAME);
	}

	public String getWorksheetName() {
		return getPreferenceStore().getString(WORKSHEET_NAME);
	}

	public String getColumnName() {
		return getPreferenceStore().getString(COLUMN_NAME);
	}

	public void copyGoogleClip(String clip) {
		SpreadsheetService spreadsheetService = getSpreadsheetService();
		if (spreadsheetService != null) {
			try {
				ListEntry listEntry = new ListEntry();
				listEntry.getCustomElements().setValueLocal(getColumnName(), clip);
				spreadsheetService.insert(listFeedUrl, listEntry);
			} catch (IOException e) {
			} catch (ServiceException e) {
			}
		}
	}

	public String getGoogleClip() {
		SpreadsheetService spreadsheetService = getSpreadsheetService();
		if (spreadsheetService != null) {
			try {
				ListFeed listFeed = spreadsheetService.getFeed(listFeedUrl, ListFeed.class);
				List<ListEntry> listEntries = listFeed.getEntries();
				if (listEntries.size() > 0) {
					ListEntry listEntry = listEntries.get(listEntries.size() - 1);
					return listEntry.getCustomElements().getValue(getColumnName());
				}
			} catch (IOException e) {
			} catch (ServiceException e) {
			}
		}
		return null;
	}

	public List<String> getGoogleClips() {
		SpreadsheetService spreadsheetService = getSpreadsheetService();
		if (spreadsheetService != null) {
			try {
				ListFeed listFeed = spreadsheetService.getFeed(listFeedUrl, ListFeed.class);
				List<ListEntry> listEntries = listFeed.getEntries();
				int size = listEntries.size();
				if (size > 0) {
					List<String> clips = new LinkedList<String>();
					for (ListEntry listEntry : listEntries) {
						String clip = listEntry.getCustomElements().getValue(getColumnName());
						if (clip != null) {
							clips.add(clip);
						}
					}
					return clips;
				}
			} catch (IOException e) {
			} catch (ServiceException e) {
			}
		}
		return Collections.emptyList();
	}

	private SpreadsheetService spreadsheetService;
	private URL listFeedUrl;

	SpreadsheetService getSpreadsheetService() {
		if (spreadsheetService == null) {
			try {
				InputDialog inputDialog = new InputDialog(
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						"Enter Password",
						"Password required to access Google Spreadsheet '" + getSpreadsheetName() + "'.\n" +
						"You can edit Google Clips settings by visiting\n" +
						"Preferences > General > Google Clips page.\n" +
						"Enter password for '" + getGoogleId() + "' :",
						"",
						null) {
					protected int getInputTextStyle() {
						return super.getInputTextStyle() | SWT.PASSWORD;
					}
				};
				if (inputDialog.open() != Window.OK) {
					throw new SecurityException("Must specify password!");
				}
				spreadsheetService = new SpreadsheetService(PLUGIN_ID);
				spreadsheetService.setUserCredentials(getGoogleId(), inputDialog.getValue());
				SpreadsheetFeed spreadsheetFeed = spreadsheetService.getFeed(new URL("http://spreadsheets.google.com/feeds/spreadsheets/private/full"), SpreadsheetFeed.class);

				String spreadsheetName = getSpreadsheetName();

				SpreadsheetEntry clipsSpreadsheetEntry = null;
				for (SpreadsheetEntry spreadsheetEntry : spreadsheetFeed.getEntries()) {
					if (spreadsheetEntry.getTitle().getPlainText().equals(spreadsheetName)) {
						clipsSpreadsheetEntry = spreadsheetEntry;
						break;
					}
				}

				if (clipsSpreadsheetEntry == null) {
					throw new IllegalStateException("Spreadsheet document '" + spreadsheetName + "' not found. Please create one first.");
				}

				String worksheetName = getWorksheetName();

				URL worksheetFeedUrl = clipsSpreadsheetEntry.getWorksheetFeedUrl();
				WorksheetFeed worksheetFeed = spreadsheetService.getFeed(worksheetFeedUrl, WorksheetFeed.class);
				WorksheetEntry clipsWorksheetEntry = null;
				for (WorksheetEntry worksheetEntry : worksheetFeed.getEntries()) {
					if (worksheetEntry.getTitle().getPlainText().equals(worksheetName)) {
						clipsWorksheetEntry = worksheetEntry;
						break;
					}
				}

				String columnName = getColumnName();

				CellEntry headerEntry = null;
				if (clipsWorksheetEntry == null) {
					clipsWorksheetEntry = new WorksheetEntry();
					clipsWorksheetEntry.setTitle(new PlainTextConstruct(worksheetName));
					clipsWorksheetEntry.setRowCount(1);
					clipsWorksheetEntry.setColCount(1);

					clipsWorksheetEntry = spreadsheetService.insert(worksheetFeedUrl, clipsWorksheetEntry);

					headerEntry = new CellEntry(1, 1, columnName);
					spreadsheetService.insert(clipsWorksheetEntry.getCellFeedUrl(), headerEntry);
				}

				listFeedUrl = clipsWorksheetEntry.getListFeedUrl();

				if (headerEntry == null) {
					CellFeed cellFeed = spreadsheetService.getFeed(clipsWorksheetEntry.getCellFeedUrl(), CellFeed.class);

					for (CellEntry cellEntry : cellFeed.getEntries()) {
						Cell cell = cellEntry.getCell();
						if (cell.getRow() == 2) {
							break;
						}
						if (cell.getValue().equals(columnName)) {
							headerEntry = cellEntry;
							break;
						}
					}
				}

				if (headerEntry == null) {
					clipsWorksheetEntry.setColCount(clipsWorksheetEntry.getColCount() + 1);
					clipsWorksheetEntry = clipsWorksheetEntry.update();
					headerEntry = new CellEntry(1, 1, columnName);
					spreadsheetService.insert(clipsWorksheetEntry.getCellFeedUrl(), headerEntry);
				}
			} catch (Exception exception) {
				throw new RuntimeException(exception);
			}
		}
		return spreadsheetService;
	}

	public static String abbreviate(String clip) {
		int length = clip.length();
		return clip.replaceAll("\n", "\u00B6").replaceAll("\t", "\u21E5").substring(0, Math.min(length, 40)) + (length < 40 ? "" : "...");
	}
}
