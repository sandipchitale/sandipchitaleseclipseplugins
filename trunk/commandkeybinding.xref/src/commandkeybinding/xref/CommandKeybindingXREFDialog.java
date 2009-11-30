package commandkeybinding.xref;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.Trigger;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeySequenceText;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.keys.IBindingService;

public class CommandKeybindingXREFDialog extends PopupDialog {
	public enum MODE {COMMAND, KEYSEQUENCE};
	
	private MODE mode = MODE.COMMAND;
	
	private static final Point INITIAL_SIZE = new Point(750, 400);

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
		private String activeScheme;

		CommandKeybindingXREFSchemeIdFilter() {
		}
		
		public void setActiveScheme(String activeScheme) {
			this.activeScheme = activeScheme;
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			CommandKeybinding commandKeybinding = (CommandKeybinding) element;
			
			String schemeId = commandKeybinding.getSchemeId();
			if (schemeId.equals("") || schemeId.equals(activeScheme)) {
				return true;
			}
			return false;
		}
	}
	
	private static class CommandKeybindingXREFCommandFilter extends ViewerFilter {
		private Pattern commandFilterPattern;

		CommandKeybindingXREFCommandFilter() {
		}
		
		public void setCommandFilterText(String commandFilterText) {
			if ("".equals(commandFilterText)) {
				commandFilterPattern = Pattern.compile(".*");
				return;
			}
			int flags = 0;
			if (commandFilterText.toLowerCase().equals(commandFilterText)) {
				flags |= Pattern.CASE_INSENSITIVE;
			}
			boolean appendDotStar = true;
			boolean prependAnchor = false;
			if (commandFilterText.indexOf("?") != -1) {
				appendDotStar = false;
				commandFilterText = commandFilterText.replaceAll(Pattern.quote("?"), Matcher.quoteReplacement("."));
			}
			if (commandFilterText.indexOf("*") != -1) {
				appendDotStar = false;
				commandFilterText = commandFilterText.replaceAll(Pattern.quote("*"), Matcher.quoteReplacement(".*"));
			}
			if (commandFilterText.startsWith("^")) {
				prependAnchor = true;
				commandFilterText = commandFilterText.substring(1);
			}
			if (appendDotStar) {
				commandFilterText = Pattern.quote(commandFilterText) + ".*";
			}
			if (prependAnchor) {
				commandFilterText = "^" + commandFilterText;
			} else {
				commandFilterText = ".*" + commandFilterText;
			}
			commandFilterPattern = Pattern.compile(commandFilterText, flags);
		}
		
		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			CommandKeybinding commandKeybinding = (CommandKeybinding) element;
			return commandFilterPattern.matcher(commandKeybinding.getCommandName()).matches();
		}
	}
	
	private static class CommandKeybindingXREFKeySequenceFilter extends ViewerFilter {
		private String keySequenceText;

		CommandKeybindingXREFKeySequenceFilter() {
		}
		
		public void setKeySequenceText(String keySequenceText) {
			this.keySequenceText = keySequenceText;
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			CommandKeybinding commandKeybinding = (CommandKeybinding) element;
			
			if (keySequenceText.equals("") || commandKeybinding.getKeySequence().startsWith(keySequenceText)) {
				return true;
			}
			return false;
		}
	}

	private static class CommandKeybindingXREFNonModifierKeySequenceFilter extends ViewerFilter {
		private String nonModifierKeySequenceText;

		CommandKeybindingXREFNonModifierKeySequenceFilter() {
		}
		
		public void setNonModifierKeySequenceText(
				String nonModifierKeySequenceText) {
			this.nonModifierKeySequenceText = nonModifierKeySequenceText;
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			CommandKeybinding commandKeybinding = (CommandKeybinding) element;
			
			if (nonModifierKeySequenceText.equals("") || commandKeybinding.getNonModifierKeySequence().startsWith(nonModifierKeySequenceText)) {
				return true;
			}
			return false;
		}
	}

	private class CommandKeybindingXREFContentProvider implements IStructuredContentProvider {
		private CommandKeybinding[] commandKeybindings;
		
		public Object[] getElements(Object inputElement) {
			return commandKeybindings;
		}

		public void dispose() {
			commandKeybindings = null;
		}

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
			
			Map<String, String> idToName = new HashMap<String, String>();
			Scheme[] definedSchemes = bindingService.getDefinedSchemes();
			for (Scheme scheme : definedSchemes) {
				try {
					idToName.put(scheme.getId(), scheme.getName());
				} catch (NotDefinedException e) {
				}
			}
			Scheme activeScheme = bindingService.getActiveScheme();
			String activeSchemeName = activeScheme.getId();
			try {
				activeSchemeName = activeScheme.getName();
			} catch (NotDefinedException e1) {
			}
			commandKeybindingXREFSchemeIdFilter.setActiveScheme(activeSchemeName);
			Binding[] bindings = bindingService.getBindings();
			for (Binding binding : bindings) {
				ParameterizedCommand parameterizedCommand = binding.getParameterizedCommand();
				if (parameterizedCommand != null) {
					String commandId = parameterizedCommand.getId();
					commands.remove(commandId);
					String contextId = binding.getContextId();
					try {
						String schemeId = binding.getSchemeId();
						String schemeName = idToName.get(schemeId);
						if (schemeName == null) {
							schemeName = schemeId;
						}
						commandKeybindings.add(
								new CommandKeybinding(parameterizedCommand.getName(),
										binding.getTriggerSequence(),
										contextService.getContext(contextId).getName(),
										schemeName));
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
				public int compare(CommandKeybinding o1, CommandKeybinding o2) {
					if ("".equals(o1.getKeySequence())) {
						if ("".equals(o2.getKeySequence())) {
							return o1.getCommandName().compareTo(o2.getCommandName());
						} else {
							return 1;
						}
					}
					if ("".equals(o2.getKeySequence())) {
							return -1;
					}
					return o1.getCommandName().compareTo(o2.getCommandName());
				}
			});
			this.commandKeybindings = new CommandKeybinding[commandKeybindings.size()];
			this.commandKeybindings = commandKeybindings.toArray(this.commandKeybindings);
		}
	}
	
	private static class CommandKeybindingXREFLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

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

		public void addListener(ILabelProviderListener listener) {}

		public void dispose() {}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
		}
	}
	
	private Table table;
	private TableViewer tableViewer;
	
	private Combo schemeFilterCombo;
	private CommandKeybindingXREFSchemeIdFilter commandKeybindingXREFSchemeIdFilter;

	private Text commandSearchText;
	private CommandKeybindingXREFCommandFilter commandKeybindingXREFCommandFilter;
	
	private Text keySequenceSearchText;
	private KeySequenceText keySequenceSearchKeySequenceText;
	private CommandKeybindingXREFKeySequenceFilter commandKeybindingXREFKeySequenceFilter;

	private Text nonModifierKeySequenceText;
	private KeySequenceText nonModifierKeySequenceKeySequenceText;
	private CommandKeybindingXREFNonModifierKeySequenceFilter commandKeybindingXREFNonModifierKeySequenceFilter;
	
	public CommandKeybindingXREFDialog() {
		this(MODE.COMMAND);
	}
	
	public CommandKeybindingXREFDialog(MODE mode) {
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), PopupDialog.INFOPOPUP_SHELLSTYLE, true, true, true, true, true, "", "");
		this.mode = mode;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		commandKeybindingXREFSchemeIdFilter = new CommandKeybindingXREFSchemeIdFilter();
		
		commandKeybindingXREFCommandFilter = new CommandKeybindingXREFCommandFilter();
		commandKeybindingXREFKeySequenceFilter = new CommandKeybindingXREFKeySequenceFilter();
		commandKeybindingXREFNonModifierKeySequenceFilter = new CommandKeybindingXREFNonModifierKeySequenceFilter();

		Composite dialogArea = (Composite) super.createDialogArea(parent);
		GridLayout layout = (GridLayout) dialogArea.getLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		
		Label keySequenceSearchLabel = new Label(dialogArea, SWT.RIGHT);
		keySequenceSearchLabel.setText("Keysequence Search :");
		GridData keySequenceSearchLabelGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		keySequenceSearchLabel.setLayoutData(keySequenceSearchLabelGridData);
		
		keySequenceSearchText = new Text(dialogArea, SWT.SINGLE|SWT.SEARCH|SWT.ICON_SEARCH|SWT.ICON_CANCEL);
		GridData keySequenceSearchTextGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		keySequenceSearchText.setLayoutData(keySequenceSearchTextGridData);
		keySequenceSearchKeySequenceText = new KeySequenceText(keySequenceSearchText);
		
		keySequenceSearchText.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				keySequenceSearchText.setText("");
			}
			
			public void focusGained(FocusEvent e) {
				commandKeybindingXREFKeySequenceFilter.setKeySequenceText(keySequenceSearchText.getText());
				setFilters(commandKeybindingXREFKeySequenceFilter);
				tableViewer.refresh();
			}
		});
		keySequenceSearchText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				KeySequence keySequence = keySequenceSearchKeySequenceText.getKeySequence();
				if (keySequence.isComplete()) {
					commandKeybindingXREFKeySequenceFilter.setKeySequenceText(keySequence.format());
					tableViewer.refresh();
				}
			}
		});
		
		Label nonModifierKeySequenceLabel = new Label(dialogArea, SWT.RIGHT);
		nonModifierKeySequenceLabel.setText("Natural Keysequence Search :");
		GridData nonModifierKeySequenceLabelGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		nonModifierKeySequenceLabel.setLayoutData(nonModifierKeySequenceLabelGridData);
		
		nonModifierKeySequenceText = new Text(dialogArea, SWT.SINGLE|SWT.SEARCH|SWT.ICON_SEARCH|SWT.ICON_CANCEL);
		GridData nonModifierKeySequenceGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		nonModifierKeySequenceText.setLayoutData(nonModifierKeySequenceGridData);
		nonModifierKeySequenceKeySequenceText = new KeySequenceText(nonModifierKeySequenceText);
		
		nonModifierKeySequenceText.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				nonModifierKeySequenceText.setText("");
			}
			
			public void focusGained(FocusEvent e) {
				commandKeybindingXREFNonModifierKeySequenceFilter.setNonModifierKeySequenceText(nonModifierKeySequenceText.getText());
				setFilters(commandKeybindingXREFNonModifierKeySequenceFilter);
				tableViewer.refresh();
			}
		});
		
		final ModifyListener modifyListener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				KeySequence keySequence = nonModifierKeySequenceKeySequenceText.getKeySequence();
				if (keySequence.isComplete()) {
					try {
						nonModifierKeySequenceText.removeModifyListener(this);
						KeyStroke[] keyStrokes = keySequence.getKeyStrokes();
						for (int i = 0; i < keyStrokes.length; i++) {
							keyStrokes[i] = KeyStroke.getInstance(keyStrokes[i].getNaturalKey());
						}
						keySequence = KeySequence.getInstance(keyStrokes);
						nonModifierKeySequenceKeySequenceText.setKeySequence(keySequence);
						
						if (keySequence.isComplete()) {
							commandKeybindingXREFNonModifierKeySequenceFilter.setNonModifierKeySequenceText(keySequence.format());
							tableViewer.refresh();
						}
					} finally {
						nonModifierKeySequenceText.addModifyListener(this);
					}
				}
			}
		};
		nonModifierKeySequenceText.addModifyListener(modifyListener);
		
		Label schemeFilterLabel = new Label(dialogArea, SWT.RIGHT);
		schemeFilterLabel.setText("Scheme Filter: ");
		GridData schemeFilterLabelGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		schemeFilterLabel.setLayoutData(schemeFilterLabelGridData);
		
		schemeFilterCombo = new Combo(dialogArea, SWT.DROP_DOWN|SWT.READ_ONLY);
		GridData schemeFilterComboGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		schemeFilterCombo.setLayoutData(schemeFilterComboGridData);
		schemeFilterCombo.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				commandKeybindingXREFSchemeIdFilter.setActiveScheme(schemeFilterCombo.getText());
				tableViewer.refresh();
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		table = new Table(dialogArea, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData tableLayoutData = new GridData(GridData.FILL_BOTH);
		tableLayoutData.horizontalSpan = 2;
		table.setLayoutData(tableLayoutData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		tableViewer = new TableViewer(table);
		tableViewer.setContentProvider(new CommandKeybindingXREFContentProvider());
		tableViewer.setLabelProvider(new CommandKeybindingXREFLabelProvider());
		
		TableColumn tc;
		
		tc = new TableColumn(table, SWT.LEFT, 0);
		tc.setText("Command");
		tc.setWidth(275);
		
		tc = new TableColumn(table, SWT.LEFT, 1);
		tc.setText("Keysequence");
		tc.setWidth(200);
		
		tc = new TableColumn(table, SWT.LEFT, 2);
		tc.setText("Natural Keys");
		tc.setWidth(100);
		
		tc = new TableColumn(table, SWT.LEFT, 3);
		tc.setText("Context");
		tc.setWidth(125);
	    
		IWorkbench workbench = PlatformUI.getWorkbench();
		tableViewer.setInput(workbench);
		
		IBindingService bindingService = (IBindingService) workbench.getService(IBindingService.class);
		
		Scheme[] definedSchemes = bindingService.getDefinedSchemes();
		for (Scheme scheme : definedSchemes) {
			try {
				schemeFilterCombo.add(scheme.getName());
			} catch (NotDefinedException e1) {
			}
		}
		try {
			schemeFilterCombo.setText(bindingService.getActiveScheme().getName());
		} catch (NotDefinedException e1) {
		}
		
		setInfoText("Search using Command Name (^, *, ? allowed), Key Sequence or Natural Key Sequence");
		return dialogArea;
	}
	
	@Override
	protected Control createTitleControl(Composite parent) {
		commandSearchText = new Text(parent, SWT.SINGLE|SWT.SEARCH|SWT.ICON_SEARCH|SWT.ICON_CANCEL);
		GridData commandSearchTextGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		commandSearchText.setLayoutData(commandSearchTextGridData);
		commandSearchText.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				commandSearchText.setText("");
			}
			
			public void focusGained(FocusEvent e) {
				commandKeybindingXREFCommandFilter.setCommandFilterText(commandSearchText.getText());
				setFilters(commandKeybindingXREFCommandFilter);
				tableViewer.refresh();
			}
		});
		commandSearchText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				commandKeybindingXREFCommandFilter.setCommandFilterText(commandSearchText.getText());
				tableViewer.refresh();
			}
		});
		return commandSearchText;
	}
	
	@Override
	protected Control getFocusControl() {
		switch (mode) {
			case KEYSEQUENCE:			
				return keySequenceSearchText;
		}
		return commandSearchText;
	}
	
	private void setFilters(ViewerFilter viewFilter) {
		tableViewer.setFilters(new ViewerFilter[] {commandKeybindingXREFSchemeIdFilter, viewFilter});
		tableViewer.refresh();
	}
	
	@Override
	public int open() {
		return super.open();
	}
	
	@Override
	protected Point getDefaultSize() {
		return INITIAL_SIZE;
	}
}
