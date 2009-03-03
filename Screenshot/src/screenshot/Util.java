package screenshot;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.ImageTransfer;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.GC;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tracker;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.wizards.newresource.BasicNewFileResourceWizard;

public class Util {
	public static Image getImage(Shell parentShell) {
		return new DesktopAreaImage(parentShell).getImage();
	}
	
	public static Image getDesktopImage(Display display) {
        return getDesktopImage(display, display.getBounds());
	}
	
	public static Image getDesktopImage(Display display, Rectangle bounds) {
		GC gc = new GC(display);
		Image image = new Image(display, new Rectangle(0, 0, bounds.width, bounds.height));
        gc.copyArea(image, bounds.x, bounds.y);
        gc.dispose();
        return image;
	}
	
	public static void processImage(Shell shell, Image image) {
        if (shell.getMinimized()) {
        	shell.setMinimized(false);
        }
        shell.setActive();
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
			ScrolledComposite scroller = new ScrolledComposite(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
			GridData scrollerGridData = new GridData();
			scrollerGridData.verticalSpan = 3;
			scrollerGridData.widthHint = display.getBounds().width/4;
			scrollerGridData.heightHint = display.getBounds().height/4;
			scroller.setLayoutData(scrollerGridData);
			
			final Canvas canvas = new Canvas(scroller, SWT.NONE);
			scroller.setContent(canvas);
			Rectangle bounds = image.getBounds();
			canvas.setSize(bounds.width, bounds.height);
			canvas.addPaintListener(new PaintListener() {
				public void paintControl(PaintEvent e) {
					Rectangle bounds = image.getBounds();
					e.gc.drawImage(image, 0, 0, bounds.width, bounds.height, 0, 0, bounds.width, bounds.height);
					e.gc.drawRectangle(0, 0, bounds.width-1, bounds.height-1);				
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
			saveInWorkspaceButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					widgetSelected(e);
				}

				public void widgetSelected(SelectionEvent e) {
					saveInWorkspace(getShell(), image);
				}

			});
			
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
				Job job = new Job("Save to File") {
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

		private void saveInWorkspace(Shell shell, final Image image) {
			IWorkbenchWizard wizard = new BasicNewFileResourceWizard() {
				public String getWindowTitle() {
					return "Save screenshot in Workspace";
				}
				
				public void addPage(IWizardPage page) {
					super.addPage(page);
					
					if (page instanceof WizardNewFileCreationPage) {
						page.setTitle("Screenshot Image File");
						page.setDescription("Specify the name of Image File" +
								"\n(allowed extentions .png, .gif, .jpg, .bmp, .ico)");
					}
				}
				public boolean performFinish() {
					final IFile file = ((WizardNewFileCreationPage) getPages()[0]).createNewFile();
					if (file == null) {
						return false;
					}
					final String fileName = file.getLocation().toOSString();
					if (fileName != null) {
						final ImageLoader imageLoader = new ImageLoader();
						imageLoader.data = new ImageData[] {image.getImageData()};
						Job job = new Job("Save in Workspace") {
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
								try {
									// Refresh after save
									file.refreshLocal(IResource.DEPTH_ZERO, null);
								} catch (CoreException e) {
								}
								UIJob uiJob = new UIJob("") {
									public IStatus runInUIThread(IProgressMonitor monitor) {
										selectAndReveal(file);
										return Status.OK_STATUS;
									}
								};
								uiJob.setSystem(true);
								uiJob.schedule();
								return Status.OK_STATUS;
							}

						};
						job.schedule();
					}
					return true;
				}
			};
			ISelection selection = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getSelectionService().getSelection();
			wizard.init(PlatformUI.getWorkbench(), (IStructuredSelection)(selection instanceof IStructuredSelection ? selection : null));
			WizardDialog dialog = new WizardDialog(shell, wizard);
			dialog.open();
		}
		
		private static void copyToClipboard(Shell shell, Image image) {
			ImageTransfer imageTransfer = ImageTransfer.getInstance();
			TextTransfer textTransfer = TextTransfer.getInstance();
			new Clipboard(shell.getDisplay()).setContents(new Object[]{image.getImageData(), "screenshot"}, 
					new Transfer[]{imageTransfer, textTransfer});
		}
	}
	
	private static class DesktopAreaImage implements Listener, PaintListener {
		private Shell shell;
		private Image image;
		private Point start;
		
		DesktopAreaImage(Shell parentShell) {
			Display display = parentShell.getDisplay();
			image = getDesktopImage(display);
			Rectangle bounds = display.getBounds();
			shell = new Shell(parentShell, SWT.NO_TRIM | SWT.ON_TOP);
			shell.setBounds(bounds);
		}

		public void handleEvent(Event event) {
			switch (event.type) {
        	case SWT.MouseDown:
        		shell.removePaintListener(this);
    			shell.removeListener(SWT.MouseDown, this);
    			shell.removeListener(SWT.MouseMove, this);
        		shell.setBounds(0,0,0,0);
        		image = null;
        		final Display display = shell.getDisplay();
        		start = new Point(event.x, event.y);
        		UIJob uiJob = new UIJob("") {
					public IStatus runInUIThread(IProgressMonitor monitor) {
		        		Tracker tracker = new Tracker (display, SWT.RESIZE | SWT.LEFT | SWT.RIGHT | SWT.UP | SWT.DOWN);
		        		tracker.setCursor(display.getSystemCursor(SWT.CURSOR_SIZESE));
		        		tracker.setStippled(true);
		    			tracker.setRectangles (new Rectangle [] {new Rectangle(start.x, start.y, 0, 0)});
		    			if (tracker.open()) {
		    				Rectangle rectangle = tracker.getRectangles()[0];
		        			GC gc = new GC(display);
		        			image = new Image(display, new Rectangle(0, 0, rectangle.width, rectangle.height));
		        			gc.copyArea(image, rectangle.x, rectangle.y);
		        	        gc.dispose();
		    			}
		    			shell.close();
						return Status.OK_STATUS;
					}
        		};
        		uiJob.setSystem(true);
        		uiJob.setPriority(UIJob.INTERACTIVE);
        		uiJob.schedule(10l);
        		break;
        	case SWT.MouseMove:
        		if (start == null) {
        			shell.redraw();
        		}
        		break;
			}
		}

		public void paintControl(PaintEvent e) {
			if (image != null) {
				e.gc.drawImage(image, 0, 0);
			}
			if (start == null) {
				Point cursorLocation = e.display.getCursorLocation();
				e.gc.drawString(
						"Click and drag to select screenshot area. Type ESC to cancel.",
						cursorLocation.x + 2,
						cursorLocation.y + 10);
			}
		}
		
		Image getImage() {
			final Display display = shell.getDisplay();
			shell.addListener(SWT.MouseDown, this);
			shell.addListener(SWT.MouseMove, this);
			shell.addPaintListener(this);
			shell.setCursor(display.getSystemCursor(SWT.CURSOR_CROSS));
			shell.forceActive();
			shell.forceFocus();
			shell.open();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch())
					display.sleep();
			}
			return image;
		}
	}
}