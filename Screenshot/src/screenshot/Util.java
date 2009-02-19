package screenshot;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

public class Util {
	
	public static void processImage(Shell shell, Image image) {
		new ImageDialog(shell, image).open();
		image.dispose();
	}
	
	private static class ImageDialog extends TrayDialog {

		private final Image image;

		protected ImageDialog(Shell shell, Image image) {
			super(shell);
			this.image = image;
			setDialogHelpAvailable(false);
		}
		
		protected Control createDialogArea(Composite parent) {
			Composite composite = (Composite) super.createDialogArea(parent);
			GridLayout gridLayout = (GridLayout) composite.getLayout();
			gridLayout.numColumns = 2;
			
			Display display = getShell().getDisplay();
			final Canvas canvas = new Canvas(composite, SWT.BORDER);
			GridData canvasGridData = new GridData();
			canvasGridData.verticalSpan = 3;
			canvasGridData.widthHint = display.getBounds().width/4;
			canvasGridData.heightHint = display.getBounds().height/4;
			canvas.setLayoutData(canvasGridData);
			canvas.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					Point size = canvas.getSize();
					Rectangle bounds = image.getBounds();
					e.gc.drawImage(image, 0, 0, bounds.width, bounds.height, 0, 0, size.x, size.y);
				}
			});
			
			Button saveToFileButton = new Button(composite, SWT.PUSH);
			saveToFileButton.setText("Save to File...");
			GridData saveToFileButtonGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
			saveToFileButton.setLayoutData(saveToFileButtonGridData);
			
			saveToFileButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}

				public void widgetSelected(SelectionEvent e) {
					saveToFile(getShell(), image);
				}
			}); 
			Button saveInWorkspaceButton = new Button(composite, SWT.PUSH);
			saveInWorkspaceButton.setText("Save in Workspace...");
			GridData saveInWorkspaceButtonGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
			saveInWorkspaceButton.setLayoutData(saveInWorkspaceButtonGridData);
			
			Button copyToClipboardButton = new Button(composite, SWT.PUSH);
			copyToClipboardButton.setText("Copy to Clipboard...");
			GridData copyToClipboardButtonGridData = new GridData(SWT.FILL, SWT.TOP, true, false);
			copyToClipboardButton.setLayoutData(copyToClipboardButtonGridData);
			copyToClipboardButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}

				public void widgetSelected(SelectionEvent e) {
					copyToClipboard(getShell(), image);
				}
			});

			return composite;
		}
		
		protected void createButtonsForButtonBar(Composite parent) {
			createButton(parent, IDialogConstants.CLOSE_ID, IDialogConstants.CLOSE_LABEL,
					true);
		}
		
		protected void buttonPressed(int buttonId) {
			if (buttonId == IDialogConstants.CLOSE_ID) {
				setReturnCode(OK);
				close();
			}
		}
		
		private static void saveToFile(Shell shell, Image image) {
			FileDialog fileDialog = new FileDialog(shell, SWT.SAVE);
			fileDialog.setOverwrite(true);
			fileDialog.setFilterExtensions(new String[] {"*.png", "*.gif", "*.jpg", "*.bmp", "*.ico"});
			fileDialog.setFilterIndex(0);
			fileDialog.setFilterPath(System.getProperty("java.io.tmpdir", System.getProperty("usre.home")));
			fileDialog.setFileName("screenshot.png");
			final String fileName = fileDialog.open();
			if (fileName != null) {
				final ImageLoader imageLoader = new ImageLoader();
				imageLoader.data = new ImageData[] {image.getImageData()};
				Job job = new Job("Save screenshot") {
					protected IStatus run(IProgressMonitor monitor) {
						int format = SWT.IMAGE_PNG;
						String lowerCaseFileName = fileName.toLowerCase();
						if (lowerCaseFileName.endsWith(".gif")) {
							format = SWT.IMAGE_GIF;
						} else if (lowerCaseFileName.endsWith(".jpg")) {
							format = SWT.IMAGE_JPEG;
						} else if (lowerCaseFileName.endsWith(".bmp")) {
							format = SWT.IMAGE_BMP;
						} else if (lowerCaseFileName.endsWith(".ico")) {
							format = SWT.IMAGE_ICO;
						}			
						imageLoader.save(fileName, format);		
						return Status.OK_STATUS;
					}

				};
				job.schedule();
			}
		}
		
		private static void copyToClipboard(Shell shell, Image image) {
			ImageTransfer imageTransfer = ImageTransfer.getInstance();
			TextTransfer textTransfer = TextTransfer.getInstance();
			new Clipboard(shell.getDisplay()).setContents(new Object[]{image.getImageData(), "screenshot"}, 
					new Transfer[]{imageTransfer, textTransfer});
		}
	}
}