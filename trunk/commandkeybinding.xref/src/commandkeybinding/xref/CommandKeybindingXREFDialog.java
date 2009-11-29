package commandkeybinding.xref;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.Trigger;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.keys.IBindingService;

public class CommandKeybindingXREFDialog extends PopupDialog {
	
	private static class CommandKeybinding {
		private String commandName;
		private String keySequence;
		private String nonModifierKeySequence;
		private String context;
		private String schemeId;

		private CommandKeybinding(String commandName) {
			this(commandName, null, null, null);			
		}
		
		private CommandKeybinding(String commandName, TriggerSequence keySequence, String context, String schemeId) {
			this.commandName = commandName;
			this.keySequence = "";
			this.nonModifierKeySequence = "";
			if (keySequence != null) {
				this.keySequence = keySequence.format();
				Trigger[] triggers = keySequence.getTriggers();
				for (Trigger trigger : triggers) {
					if (this.nonModifierKeySequence.length() > 0) {
						this.nonModifierKeySequence += " ";
					}
					KeyStroke keyStroke = (KeyStroke) trigger;
					this.nonModifierKeySequence += KeyStroke.getInstance(keyStroke.getNaturalKey()).format();
				}
			}
			this.context = (context == null ? "" : context);
			this.schemeId = (schemeId == null ? "" : schemeId);
		}

		private String getCommandName() {
			return commandName;
		}

		private String getKeySequence() {
			return keySequence;
		}

		private String getNonModifierKeySequence() {
			return nonModifierKeySequence;
		}

		private String getContext() {
			return context;
		}
		
		public String getSchemeId() {
			return schemeId;
		}
	}
	
	private static class CommandKeybindingXREFSchemeIdFilter extends ViewerFilter {
		private final String activeSchemeId;

		CommandKeybindingXREFSchemeIdFilter(String activeSchemeId) {
			this.activeSchemeId = activeSchemeId;
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			CommandKeybinding commandKeybinding = (CommandKeybinding) element;
			
			String schemeId = commandKeybinding.getSchemeId();
			if (schemeId.equals("") || schemeId.equals(activeSchemeId)) {
				return true;
			}
			return false;
		}
	}
	
	private static class CommandKeybindingXREFContentProvider implements IStructuredContentProvider {
		private CommandKeybinding[] commandKeybindings;
		
		@Override
		public Object[] getElements(Object inputElement) {
			return commandKeybindings;
		}

		@Override
		public void dispose() {
			commandKeybindings = null;
		}

		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			if (newInput == null) {
				commandKeybindings = new CommandKeybinding[0];
				return;
			}
			
			TableViewer tableViewer = (TableViewer) viewer;
			tableViewer.setFilters(new ViewerFilter[0]);
			
			IWorkbench workbench = (IWorkbench)newInput;
			List<CommandKeybinding> commandKeybindings = new LinkedList<CommandKeybinding>();
			ICommandService commandService = (ICommandService) workbench.getService(ICommandService.class);
			Map<String, Command> commands = new HashMap<String, Command>();
			Command[] definedCommands = commandService.getDefinedCommands();
			for (Command command : definedCommands) {
				commands.put(command.getId(), command);
			}
			IContextService contextService = (IContextService) workbench.getService(IContextService.class);
			IBindingService bindingService = (IBindingService) workbench.getService(IBindingService.class);
			String activeSchemeId  = bindingService.getActiveScheme().getId();
			
			tableViewer.addFilter(new CommandKeybindingXREFSchemeIdFilter(activeSchemeId));
			
			Binding[] bindings = bindingService.getBindings();
			for (Binding binding : bindings) {
					ParameterizedCommand parameterizedCommand = binding.getParameterizedCommand();
					if (parameterizedCommand != null) {
						String commandId = parameterizedCommand.getId();
						commands.remove(commandId);
						String contextId = binding.getContextId();
						try {
							commandKeybindings.add(
									new CommandKeybinding(parameterizedCommand.getName(),
											binding.getTriggerSequence(),
											contextService.getContext(contextId).getName(),
											binding.getSchemeId()));
						} catch (NotDefinedException e) {
						}
					}
			}
			Set<Entry<String,Command>> entrySet = commands.entrySet();
			for (Entry<String, Command> entry : entrySet) {
				Command command = entry.getValue();
				try {
					commandKeybindings.add(new CommandKeybinding(command.getName()));
				} catch (NotDefinedException e) {
				}
			}
			Collections.sort(commandKeybindings, new Comparator<CommandKeybinding>() {

				@Override
				public int compare(CommandKeybinding o1, CommandKeybinding o2) {
					return o1.getCommandName().compareTo(o2.getCommandName());
				}
			});
			this.commandKeybindings = new CommandKeybinding[commandKeybindings.size()];
			this.commandKeybindings = commandKeybindings.toArray(this.commandKeybindings);
		}
	}
	
	private static class CommandKeybindingXREFLabelProvider implements ITableLabelProvider {

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public String getColumnText(Object element, int columnIndex) {
			CommandKeybinding commandKeybinding = (CommandKeybinding) element;
			switch (columnIndex) {
			case 0:
				return commandKeybinding.getCommandName();
			case 1:
				return commandKeybinding.getKeySequence();
			case 2:
				return commandKeybinding.getNonModifierKeySequence();
			case 3:
				return commandKeybinding.getContext();
			}
			return null;
		}

		@Override
		public void addListener(ILabelProviderListener listener) {
		}

		@Override
		public void dispose() {
			
		}

		@Override
		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		@Override
		public void removeListener(ILabelProviderListener listener) {
		}
		
	}
	
	public CommandKeybindingXREFDialog() {
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), PopupDialog.INFOPOPUP_SHELLSTYLE, true, true, true, true, true, "", "");		
	}
	
	private Table table;
	private TableViewer tableViewer;
	
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite dialogArea = (Composite) super.createDialogArea(parent);
		
		// Place a table inside the tab.
		table = new Table(dialogArea, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(new CommandKeybindingXREFContentProvider());
		tableViewer.setLabelProvider(new CommandKeybindingXREFLabelProvider());
		
		TableColumn tc;
		
		tc = new TableColumn(table, SWT.LEFT, 0);
		tc.setText("Command Name");
		tc.setWidth(200);
		
		tc = new TableColumn(table, SWT.LEFT, 1);
		tc.setText("KS");
		tc.setWidth(250);
		
		tc = new TableColumn(table, SWT.LEFT, 2);
		tc.setText("NKS");
		tc.setWidth(100);
		
		tc = new TableColumn(table, SWT.LEFT, 3);
		tc.setText("Context");
		tc.setWidth(100);
	    
		tableViewer.setInput(PlatformUI.getWorkbench());

		return dialogArea;
	}
	
	@Override
	public int open() {
		return super.open();
	}
	
	@Override
	protected Point getDefaultSize() {
		return new Point(700, 400);
	}
}
