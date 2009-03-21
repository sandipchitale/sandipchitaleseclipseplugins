package googleclips.ui;

import googleclips.Activator;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * 
 * @author Sandip V. Chitale
 *
 */
public class LoginDialog extends TitleAreaDialog {
	private Text usernameField;
	private Text passwordField;
	private String username;
	private String password;

	public LoginDialog(Shell parentShell, String username) {
		super(parentShell);
		this.username = username;
	}
	
	@Override
	protected Control createContents(Composite parent) {
		Control contents = super.createContents(parent);
		setTitle("Google credentials");
		setMessage("Enter Google credentials to access spreadsheet: " + Activator.getDefault().getSpreadsheetName() 
				+ "\nYou can edit settings at Preferences > General > Google Clips\n");
		return contents;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite compositeParent = (Composite) super.createDialogArea(parent);
		
		Composite composite = new Composite(compositeParent, SWT.NONE);
		GridData compositeData = new GridData(GridData.FILL_BOTH);
		composite.setLayoutData(compositeData);
		
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		layout.numColumns = 2;
		
		Label usernameLabel = new Label(composite, SWT.RIGHT);
		usernameLabel.setText("Google ID: ");

		usernameField = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		usernameField.setLayoutData(data);
		usernameField.setText(username);
		username = null;

		Label passwordLabel = new Label(composite, SWT.RIGHT);
		passwordLabel.setText("Password: ");

		passwordField = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		data = new GridData(GridData.FILL_HORIZONTAL);
		passwordField.setLayoutData(data);
		
		return composite;
	}
	
	public int open() {
		boolean autoClipCutCopy = Activator.getDefault().isAutoClipCutCopy();
		try {
			Activator.getDefault().setAutoClipCutCopy(false);
			return super.open();
		} finally {
			Activator.getDefault().setAutoClipCutCopy(autoClipCutCopy);
		}
	}
	
	@Override
	protected void okPressed() {
		username = usernameField.getText();
		password = passwordField.getText();
		super.okPressed();
	}
	
	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
}
