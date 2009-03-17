package googleclips.ui;

import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class LoginDialog extends TrayDialog {
	private Text googleIdField;
	private Text passwordField;
	private String googleId;
	private String password;

	public LoginDialog(Shell parentShell, String googleId) {
		super(parentShell);
		this.googleId = googleId;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout layout = (GridLayout) composite.getLayout();
		layout.numColumns = 2;

		Label usernameLabel = new Label(composite, SWT.RIGHT);
		usernameLabel.setText("Google ID: ");

		googleIdField = new Text(composite, SWT.SINGLE | SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		googleIdField.setLayoutData(data);
		googleIdField.setText(googleId);
		googleId = null;

		Label passwordLabel = new Label(composite, SWT.RIGHT);
		passwordLabel.setText("Password: ");

		passwordField = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		data = new GridData(GridData.FILL_HORIZONTAL);
		passwordField.setLayoutData(data);
		return composite;
	}
	
	@Override
	protected void okPressed() {
		googleId = googleIdField.getText();
		password = passwordField.getText();
		super.okPressed();
	}
	
	public String getGoogleId() {
		return googleId;
	}
	
	public String getPassword() {
		return password;
	}
}
