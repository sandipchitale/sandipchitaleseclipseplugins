package commandkeybinding.xref;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.IParameterValues;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.commands.contexts.Context;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.bindings.Binding;
import org.eclipse.jface.bindings.Scheme;
import org.eclipse.jface.bindings.Trigger;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.bindings.keys.KeySequence;
import org.eclipse.jface.bindings.keys.KeySequenceText;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.progress.UIJob;

public class CommandKeybindingXREFDialog extends PopupDialog {
	public static enum MODE {COMMAND, KEYSEQUENCE};
	
	@SuppressWarnings("unchecked")
	public static class MODEValues implements IParameterValues {
		private static Map modeValueMap = new HashMap();
		static {
			modeValueMap.put(MODE.COMMAND.name(), MODE.COMMAND.name());
			modeValueMap.put(MODE.KEYSEQUENCE.name(), MODE.KEYSEQUENCE.name());
		}
		public Map getParameterValues() {
			return modeValueMap;
		}
	}

	private MODE mode = MODE.COMMAND;
	
	private static final Point INITIAL_SIZE = new Point(760, 400);
	
	private static final String SWT_PLATFORM = SWT.getPlatform();

	private static class CommandKeybinding {

		private String commandName;
		private String keySequence;
		private String naturalKeySequence;
		private String context;
		private String schemeId;
		private String platform;
		private final String type;
		private Command command;
		private final Binding binding;
		private boolean executable;
		
		private enum REASON {PLATFORM, CONTEXT};
		private REASON reason;
		
		private CommandKeybinding(String commandName, Command command) {
			this(commandName, null, null, null, "", "", null, true, null);
			this.command = command;			
		}
		
		private CommandKeybinding(String commandName, TriggerSequence keySequence, String context, String schemeId, String platform, String type, Binding binding, boolean executable, REASON reason) {
			this.commandName = commandName;
			this.binding = binding;
			this.keySequence = "";
			this.naturalKeySequence = "";
			if (keySequence != null) {
				Trigger[] triggers = keySequence.getTriggers();
				for (Trigger trigger : triggers) {
					if (this.keySequence.length() > 0) {
						this.keySequence += " ";
					}
					if (this.naturalKeySequence.length() > 0) {
						this.naturalKeySequence += " ";
					}
					KeyStroke keyStroke = (KeyStroke) trigger;
					this.keySequence += keyStroke.format();
					this.naturalKeySequence += KeyStroke.getInstance(keyStroke.getNaturalKey()).format();
				}
			}
			this.context = (context == null ? "" : context);
			this.schemeId = (schemeId == null ? "" : schemeId);
			this.platform = (platform == null ? "all" : platform);
			this.type = type;
			this.executable = executable;
			if (!executable) {
				this.reason = reason;
			}
		}
		
		private String getCommandName() {
			return commandName;
		}

		private String getKeySequence() {
			return keySequence;
		}

		private String getNaturalKeySequence() {
			return naturalKeySequence;
		}

		private String getContext() {
			return context;
		}
		
		public String getSchemeId() {
			return schemeId;
		}
		
		public String getPlatform() {
			return platform;
		}
		
		public String getType() {
			return type;
		}
		
		public Command getCommand() {
			return command;
		}
		
		public Binding getBinding() {
			return binding;
		}
		
		public boolean isExecutable() {
			return executable;
		}
		
		public REASON getReason() {
			return reason;
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
		private boolean naturalKeySequence;
		private boolean completeKeySequence;
		private String[] keySequenceTextParts;
		
		private static String splitRegexp;
		
		static {
			if (Platform.OS_WIN32.equals(Platform.getOS())) {
				splitRegexp = ", ";
			} else {
				splitRegexp = " ";
			}
			splitRegexp = Pattern.quote(splitRegexp);
		}
		
		CommandKeybindingXREFKeySequenceFilter() {
		}

		public void setKeySequenceText(String keySequenceText, boolean naturalKeySequence, boolean completeKeySequence) {
			this.keySequenceText = keySequenceText;
			this.naturalKeySequence = naturalKeySequence;
			this.completeKeySequence = completeKeySequence;
			if (completeKeySequence) {
				this.keySequenceTextParts = null;
			} else {
				this.keySequenceTextParts = keySequenceText.split(splitRegexp);
			}
		}

		@Override
		public boolean select(Viewer viewer, Object parentElement,
				Object element) {
			CommandKeybinding commandKeybinding = (CommandKeybinding) element;
			
			if (keySequenceText.equals("")) {
				return true;
			}
			if (naturalKeySequence) {
				if (commandKeybinding.getNaturalKeySequence().startsWith(keySequenceText)) {
					return true;
				}
			} else {
				String keySequence = commandKeybinding.getKeySequence();
				if (completeKeySequence) {
					if (keySequence.startsWith(keySequenceText)) {
						return true;
					}
				} else {
					String[] keySequenceParts = keySequence.split(splitRegexp);
					if (keySequenceParts.length < keySequenceTextParts.length) {
						return false;
					}
					int i = 0;
					for (; i < keySequenceTextParts.length -1; i++) {
						if (!keySequenceParts[i].equals(keySequenceTextParts[i])) {
							return false;
						}
					}
					return keySequenceParts[i].indexOf(keySequenceTextParts[i]) != -1;
				}
			}
			return false;
		}
	}

	private class CommandKeybindingXREFContentProvider implements IStructuredContentProvider {
		private CommandKeybinding[] commandKeybindings;
		private List<String> currentContextStrings;
		
		public CommandKeybindingXREFContentProvider() {
		}

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

			List<CommandKeybinding> commandKeybindings = new LinkedList<CommandKeybinding>();
			
			Comparator<CommandKeybinding> comprator = new Comparator<CommandKeybinding>() {
				public int compare(CommandKeybinding o1, CommandKeybinding o2) {
					return o1.getCommandName().compareTo(o2.getCommandName());
				}
			};
			
			List<CommandKeybinding> commandKeybindingsForAllPlatforms = new LinkedList<CommandKeybinding>();
			List<CommandKeybinding> commandKeybindingsForPlatform = new LinkedList<CommandKeybinding>();
			List<CommandKeybinding> commandKeybindingsForOtherPlatforms = new LinkedList<CommandKeybinding>();

			Binding[] bindings = bindingService.getBindings();
			for (Binding binding : bindings) {
				ParameterizedCommand parameterizedCommand = binding.getParameterizedCommand();
				if (parameterizedCommand != null) {
					String commandId = parameterizedCommand.getId();
					commands.remove(commandId);
					String platform = binding.getPlatform();
					String contextId = binding.getContextId();
					try {
						String schemeId = binding.getSchemeId();
						String schemeName = idToName.get(schemeId);
						if (schemeName == null) {
							schemeName = schemeId;
						}
						String type = (binding.getType() == Binding.USER ? "U" : "");
						String contextString = contextService.getContext(contextId).getName();
						if (platform == null || SWT_PLATFORM.equals(platform)) {
							commandKeybindingsForPlatform.add(
									new CommandKeybinding(parameterizedCommand.getName(),
											binding.getTriggerSequence(),
											contextString,
											schemeName,
											platform,
											type,
											binding,
											(currentContextStrings == null || currentContextStrings.contains(contextString)),
											CommandKeybinding.REASON.CONTEXT));
						} else {
							commandKeybindingsForOtherPlatforms.add(
									new CommandKeybinding(parameterizedCommand.getName(),
											binding.getTriggerSequence(),
											contextString,
											schemeName,
											platform,
											type,
											binding,
											false,
											CommandKeybinding.REASON.PLATFORM));
						}
					} catch (NotDefinedException e) {
					}
				}
			}
			
			List<CommandKeybinding> unboundCommands = new LinkedList<CommandKeybinding>();
			Set<Entry<String,Command>> entrySet = commands.entrySet();
			for (Entry<String, Command> entry : entrySet) {
				Command command = entry.getValue();
				try {
					unboundCommands.add(new CommandKeybinding(command.getName(), command));
				} catch (NotDefinedException e) {
				}
			}
			Collections.sort(commandKeybindingsForPlatform, comprator);
			commandKeybindings.addAll(commandKeybindingsForPlatform);
			Collections.sort(commandKeybindingsForAllPlatforms, comprator);
			commandKeybindings.addAll(commandKeybindingsForAllPlatforms);
			Collections.sort(commandKeybindingsForOtherPlatforms, comprator);
			commandKeybindings.addAll(commandKeybindingsForOtherPlatforms);
			Collections.sort(unboundCommands, comprator);
			commandKeybindings.addAll(unboundCommands);
			this.commandKeybindings = new CommandKeybinding[commandKeybindings.size()];
			this.commandKeybindings = commandKeybindings.toArray(this.commandKeybindings);
		}
		
		public void setCurrentContextStrings(List<String> currentContextStrings) {
			this.currentContextStrings = currentContextStrings;
		}
	}
	
	private static class CommandKeybindingXREFLabelProvider implements ITableLabelProvider, ITableColorProvider, ITableFontProvider {

		private final Color disabledForeground;
		private final Font diabledFont;

		public CommandKeybindingXREFLabelProvider(Color disabledForeground, Font diabledFont) {
			this.disabledForeground = disabledForeground;
			this.diabledFont = diabledFont;
		}

		public Image getColumnImage(Object element, int columnIndex) {
			CommandKeybinding commandKeybinding = (CommandKeybinding) element;
			switch (columnIndex) {
			case 3:
				return Activator.getDefault().getImage(commandKeybinding.getPlatform());
			case 4:
				return Activator.getDefault().getImage(commandKeybinding.getType());
			}
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
				return commandKeybinding.getContext();
			case 3:
				return "";
			case 4:
				return commandKeybinding.getType();
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

		public Color getBackground(Object element, int columnIndex) {
			return null;
		}

		public Color getForeground(Object element, int columnIndex) {
			CommandKeybinding commandKeybinding = (CommandKeybinding) element;
			if (commandKeybinding.isExecutable()) {
				return null;
			}
			return disabledForeground;
		}

		public Font getFont(Object element, int columnIndex) {
			if (columnIndex == 2 || columnIndex == 3) {
				CommandKeybinding commandKeybinding = (CommandKeybinding) element;
				if (!commandKeybinding.isExecutable()) {
					CommandKeybinding.REASON reason = commandKeybinding.getReason();
					switch (reason) {
					case PLATFORM:
						if (columnIndex == 3) {
							return diabledFont;
						}
						break;
					case CONTEXT:
						if (columnIndex == 2) {
							return diabledFont;
						}
						break;
					}
				}
			}
			return null;
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

	private List<Context> currentContext;
	
	public CommandKeybindingXREFDialog() {
		this(MODE.COMMAND);
	}
	
	public CommandKeybindingXREFDialog(MODE mode) {
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), PopupDialog.INFOPOPUP_SHELLSTYLE, true, true, true, true, true, "", "");
		this.mode = mode;
	}

	protected Color getBackground() {
		 return getShell().getDisplay()
			.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected Control createDialogArea(Composite parent) {
		commandKeybindingXREFSchemeIdFilter = new CommandKeybindingXREFSchemeIdFilter();
		commandKeybindingXREFCommandFilter = new CommandKeybindingXREFCommandFilter();
		commandKeybindingXREFKeySequenceFilter = new CommandKeybindingXREFKeySequenceFilter();

		Composite dialogArea = (Composite) super.createDialogArea(parent);
		GridLayout layout = (GridLayout) dialogArea.getLayout();
		layout.numColumns = 2;
		layout.makeColumnsEqualWidth = false;
		layout.marginWidth = 2;
		
		table = new Table(dialogArea, SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL | SWT.V_SCROLL);
		GridData tableLayoutData = new GridData(GridData.FILL_BOTH);
		tableLayoutData.horizontalSpan = 2;
		table.setLayoutData(tableLayoutData);
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		
		tableViewer = new TableViewer(table);
		CommandKeybindingXREFContentProvider commandKeybindingXREFContentProvider = new CommandKeybindingXREFContentProvider();
		tableViewer.setContentProvider(commandKeybindingXREFContentProvider);
		
		FontRegistry fontRegistry = JFaceResources.getFontRegistry();
		CommandKeybindingXREFLabelProvider commandKeybindingXREFLabelProvider =
			new CommandKeybindingXREFLabelProvider(table.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND),
					fontRegistry.getItalic(table.getFont().getFontData()[0].getName()));
		tableViewer.setLabelProvider(
				commandKeybindingXREFLabelProvider);
		
		TableColumn tc;
		
		tc = new TableColumn(table, SWT.LEFT, 0);
		tc.setText("Command");
		tc.setWidth(280);
		
		tc = new TableColumn(table, SWT.LEFT, 1);
		tc.setText("Keysequence");
		tc.setWidth(200);
		
		tc = new TableColumn(table, SWT.LEFT, 2);
		tc.setText("Context");
		tc.setWidth(200);
		
		tc = new TableColumn(table, SWT.LEFT, 3);
		tc.setText("P");
		tc.setWidth(20);
		
		tc = new TableColumn(table, SWT.LEFT, 4);
		tc.setText("U");
		tc.setWidth(20);
		
		table.addListener(SWT.DefaultSelection, new Listener() {
			public final void handleEvent(final Event event) {
				executeCommand();
			}
		});
		
		IWorkbench workbench = PlatformUI.getWorkbench();
		IBindingService bindingService = (IBindingService) workbench.getService(IBindingService.class);		
		IContextService contextService = (IContextService) workbench.getService(IContextService.class);
		
		Collection activeContextIds = contextService.getActiveContextIds();
		Map<Context, List<Context>> contextToContextParents = new HashMap<Context, List<Context>>();
		for (Iterator iterator = activeContextIds.iterator(); iterator
				.hasNext();) {
			String activeContextId = (String) iterator.next();
			Context context = contextService.getContext(activeContextId);
			if (!context.isDefined()) {
				continue;
			}
			Context theContext = context;
			List<Context> parentContexts = new LinkedList<Context>();
			boolean actionSetContext = false;
			while (context != null) {
				parentContexts.add(context);
				try {
					activeContextId = context.getParentId();
					// No more parents
					if (activeContextId == null) {
						if ("org.eclipse.ui.contexts.actionSet".equals(context.getId())) {
							actionSetContext = true;
						}
						break;
					}
					context = contextService.getContext(activeContextId);
				} catch (NotDefinedException e1) {
					break;
				}
			}
			if (!actionSetContext) {
				contextToContextParents.put(theContext, parentContexts);
			}
		}
		int lastSize = -1;
		Set<Context> keySet = contextToContextParents.keySet();
		currentContext = null;
		for (Context context : keySet) {
			List<Context> contexts = contextToContextParents.get(context);
			if (lastSize < contexts.size()) {
				lastSize = contexts.size();
				currentContext = contexts;
			}
		}
		
		StringBuilder sb = new StringBuilder();
		if (currentContext != null) {
			List<String> currentContextStrings = new LinkedList<String>();
			for (Context context : currentContext) {
				try {
					currentContextStrings.add(context.getName());
				} catch (NotDefinedException e1) {
					currentContextStrings.add(context.getId());
				}
			}
			commandKeybindingXREFContentProvider.setCurrentContextStrings(currentContextStrings);
			sb.append("Current context: ");
			int i = 0;
			for (Iterator iterator = currentContext.iterator(); iterator
					.hasNext();) {
				Context context = (Context) iterator.next();
				if (i > 0) {
					sb.append(" < ");
				}
				try {
					sb.append(context.getName());
				} catch (NotDefinedException e1) {
					sb.append(context.getId());
				}
				i++;
			}
			sb.append(" \n");
		}
		
		String activeSchemName = bindingService.getActiveScheme().getId();
		try {
			activeSchemName = bindingService.getActiveScheme().getName();
			schemeFilterCombo.setText(activeSchemName);
		} catch (NotDefinedException e1) {
		}
		
		setInfoText(
				sb +
				"Current platform: " + SWT_PLATFORM +
				" | " +
				"Active Scheme: " + activeSchemName + " " +
				"\n" +
				"Search using Command Name (^, *, ? allowed) or Key Sequence. U: User override "
			    );

		tableViewer.setInput(workbench);

		selectNext();
		return dialogArea;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	protected Control createTitleControl(Composite parent) {
		Composite titleArea = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(7, false);
		titleArea.setLayout(layout);

		titleArea.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		Label commandSearchLabel = new Label(titleArea, SWT.RIGHT);
		commandSearchLabel.setText("Command:");
		GridData commandSearchLabelGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		commandSearchLabel.setLayoutData(commandSearchLabelGridData);
		
		commandSearchText = new Text(titleArea, SWT.SINGLE|SWT.SEARCH|SWT.ICON_SEARCH|SWT.ICON_CANCEL);
		GridData commandSearchTextGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		commandSearchText.setLayoutData(commandSearchTextGridData);
		commandSearchText.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				commandSearchText.setForeground(commandSearchText.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
			}
			
			public void focusGained(FocusEvent e) {
				commandSearchText.setForeground(null);
				commandKeybindingXREFCommandFilter.setCommandFilterText(commandSearchText.getText());
				setFilters(commandKeybindingXREFCommandFilter);
				tableViewer.refresh();
			}
		});
		commandSearchText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				commandKeybindingXREFCommandFilter.setCommandFilterText(commandSearchText.getText());
				tableViewer.refresh();
				if (tableViewer.getTable().getItemCount() > 0) {
					tableViewer.getTable().select(0);
				}
			}
		});
		
		commandSearchText.addKeyListener(new KeyListener() {
			public void keyPressed(KeyEvent e) {
				if (e.keyCode == 0x0D) {
					// Return key was pressed
					executeCommand();
				} else if (e.keyCode == SWT.ARROW_DOWN) {
					// Down key was pressed
					selectNext();
				} else if (e.keyCode == SWT.ARROW_UP) {
					// Up key was pressed
					selectPrevious();
				}
			}

			public void keyReleased(KeyEvent e) {
				// NO-OP
			}
		});
		
		Label keySequenceSearchLabel = new Label(titleArea, SWT.RIGHT);
		keySequenceSearchLabel.setText("Keysequence:");
		GridData keySequenceSearchLabelGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		keySequenceSearchLabel.setLayoutData(keySequenceSearchLabelGridData);
		
		keySequenceSearchText = new Text(titleArea, SWT.SINGLE|SWT.SEARCH|SWT.ICON_SEARCH|SWT.ICON_CANCEL);
		GridData keySequenceSearchTextGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		keySequenceSearchText.setLayoutData(keySequenceSearchTextGridData);
		keySequenceSearchKeySequenceText = new KeySequenceText(keySequenceSearchText);
		keySequenceSearchKeySequenceText.setKeyStrokeLimit(4);
		
		keySequenceSearchText.addFocusListener(new FocusListener() {
			public void focusLost(FocusEvent e) {
				keySequenceSearchText.setForeground(keySequenceSearchText.getDisplay().getSystemColor(SWT.COLOR_TITLE_INACTIVE_FOREGROUND));
			}
			
			public void focusGained(FocusEvent e) {
				keySequenceSearchText.setForeground(null);
				KeySequence keySequence = keySequenceSearchKeySequenceText.getKeySequence();
				commandKeybindingXREFKeySequenceFilter.setKeySequenceText(keySequence.format(), isNaturalKeySequence(keySequence), isCompleteKeySequence(keySequence));
				setFilters(commandKeybindingXREFKeySequenceFilter);
				tableViewer.refresh();
			}
		});
		keySequenceSearchText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				KeySequence keySequence = keySequenceSearchKeySequenceText.getKeySequence();
				commandKeybindingXREFKeySequenceFilter.setKeySequenceText(keySequence.format(), isNaturalKeySequence(keySequence), isCompleteKeySequence(keySequence));
				tableViewer.refresh();
			}
		});
		
		keySequenceSearchText.addKeyListener(new KeyListener() {
			public void keyReleased(KeyEvent e) {}
			
			public void keyPressed(KeyEvent e) {
				// Trap escape key and close
				if (e.keyCode == SWT.ESC && e.stateMask == SWT.NONE) {
					e.doit = false;
					close();
				}
			}
		});
		
		final ToolBar toolBar = new ToolBar(titleArea, SWT.FLAT);
		GridData tolBarGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		toolBar.setLayoutData(tolBarGridData);
		
		// Button for adding trapped key strokes
		ToolItem keySequenceSearchAddTrappedKeysButton = new ToolItem(toolBar, SWT.PUSH);
		keySequenceSearchAddTrappedKeysButton.setImage(
				Activator.getDefault().getImage(Activator.ADD_KEYSTROKE));
		keySequenceSearchAddTrappedKeysButton.setToolTipText("Insert special keystrokes");
		
		new ToolItem(toolBar, SWT.SEPARATOR);

		// Construct the menu to attach to the above button.
		final Menu menuButtonAddKey = new Menu(toolBar);
		List trappedKeys = new LinkedList(KeySequenceText.TRAPPED_KEYS);
		trappedKeys.add(KeyStroke.getInstance(SWT.ESC));
		final Iterator trappedKeyItr = trappedKeys.iterator();
		while (trappedKeyItr.hasNext()) {
			final KeyStroke trappedKey = (KeyStroke) trappedKeyItr.next();
			final MenuItem menuItem = new MenuItem(menuButtonAddKey, SWT.PUSH);
			menuItem.setText(trappedKey.format());
			menuItem.addSelectionListener(new SelectionAdapter() {

				public void widgetSelected(SelectionEvent e) {
					keySequenceSearchKeySequenceText.insert(trappedKey);
					keySequenceSearchText.setFocus();
					keySequenceSearchText.setSelection(keySequenceSearchText.getTextLimit());
				}
			});
		}
		
		keySequenceSearchAddTrappedKeysButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent selectionEvent) {
				Point buttonLocation = toolBar.getLocation();
				buttonLocation = toolBar.getParent().toDisplay(buttonLocation.x, buttonLocation.y);
				Point buttonSize = toolBar.getSize();
				menuButtonAddKey.setLocation(buttonLocation.x, buttonLocation.y
						+ buttonSize.y);
				menuButtonAddKey.setVisible(true);
			}
		});
		
		IWorkbench workbench = PlatformUI.getWorkbench();
		IBindingService bindingService = (IBindingService) workbench.getService(IBindingService.class);		

		Label schemeFilterLabel = new Label(titleArea, SWT.RIGHT);
		schemeFilterLabel.setText("Scheme:");
		GridData schemeFilterLabelGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		schemeFilterLabel.setLayoutData(schemeFilterLabelGridData);
		
		schemeFilterCombo = new Combo(titleArea, SWT.DROP_DOWN|SWT.READ_ONLY);
		GridData schemeFilterComboGridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
		schemeFilterCombo.setLayoutData(schemeFilterComboGridData);
		schemeFilterCombo.addSelectionListener(new SelectionListener() {

			public void widgetSelected(SelectionEvent e) {
				commandKeybindingXREFSchemeIdFilter.setActiveScheme(schemeFilterCombo.getText());
				tableViewer.refresh();
			}

			public void widgetDefaultSelected(SelectionEvent e) {}
		});
		
		Scheme[] definedSchemes = bindingService.getDefinedSchemes();
		for (Scheme scheme : definedSchemes) {
			try {
				schemeFilterCombo.add(scheme.getName());
			} catch (NotDefinedException e1) {
			}
		}
		String activeSchemName = bindingService.getActiveScheme().getId();
		try {
			activeSchemName = bindingService.getActiveScheme().getName();
			schemeFilterCombo.setText(activeSchemName);
		} catch (NotDefinedException e1) {
		}
		return titleArea;
	}
	
	protected IDialogSettings getDialogSettings() {
		final IDialogSettings workbenchDialogSettings = Activator.getDefault().getDialogSettings();
		IDialogSettings result = workbenchDialogSettings.getSection(getId());
		if (result == null) {
			result = workbenchDialogSettings.addNewSection(getId());
		}
		return result;
	}

	protected String getId() {
		return getClass().getName(); //$NON-NLS-1$
	}
	
	private void selectPrevious() {
		int itemCount = table.getItemCount();
		if (itemCount > 1) {
			int selectionIndex = table.getSelectionIndex();
			if (selectionIndex == -1) {
				selectionIndex = 0;
			} else {
				selectionIndex--;
				if (selectionIndex < 0) {
					selectionIndex = itemCount -1;
				}
			}
			table.select(selectionIndex);
		}
	}

	private void selectNext() {
		int itemCount = table.getItemCount();
		if (itemCount > 1) {
			int selectionIndex = table.getSelectionIndex();
			if (selectionIndex == -1) {
				selectionIndex = 0;
			} else {
				selectionIndex++;
				if (selectionIndex >= itemCount) {
					selectionIndex = 0;
				}
			}
			table.select(selectionIndex);
		}
	}
	
	/**
	 * Handles the default selection event on the table of possible completions.
	 * This attempts to execute the given command.
	 */
	private final void executeCommand() {
		final IWorkbench workbench = PlatformUI.getWorkbench();
		
		// Try to execute the corresponding command.
		final int selectionIndex = table.getSelectionIndex();
		if (selectionIndex >= 0) {
			CommandKeybinding commandKeybinding = (CommandKeybinding) tableViewer.getElementAt(selectionIndex);
			ParameterizedCommand parameterizedCommand = null;
			Binding binding = commandKeybinding.getBinding();
			if (binding == null) {
				Command command = commandKeybinding.getCommand();
				if (command == null) {
					workbench.getDisplay().beep();
					return;
				}
				if (command.isDefined()) {
					parameterizedCommand = new ParameterizedCommand(command, null);
				}
			} else {
				String contextId = binding.getContextId();
				if (contextId != null) {
					IContextService contextService = (IContextService) workbench.getService(IContextService.class);
					Context context = contextService.getContext(contextId);
					if (context == null) {
						workbench.getDisplay().beep();
						return;
					} else if (currentContext.contains(context)) {
						parameterizedCommand = binding.getParameterizedCommand();
					} else {
						workbench.getDisplay().beep();
						return;
					}
				} else {					
					parameterizedCommand = binding.getParameterizedCommand();
				}
			}
			if (parameterizedCommand != null) {
				close();
				final ParameterizedCommand finalParameterizedCommand = parameterizedCommand;
				UIJob job = new UIJob("") {
					@Override
					public IStatus runInUIThread(IProgressMonitor monitor) {
						final IStatusLineManager statusLineManager = (IStatusLineManager) workbench.getActiveWorkbenchWindow().getService(IStatusLineManager.class);
						IHandlerService handlerService = (IHandlerService) workbench.getService(IHandlerService.class);
						try {
							handlerService.executeCommand(finalParameterizedCommand, null);
						} catch (CommandException e) {
							if (statusLineManager != null) {
								statusLineManager.setErrorMessage(e.getMessage());
							} else {
								workbench.getDisplay().beep();
							}
						} 
						return Status.OK_STATUS;
					}
				};
				job.setSystem(true);
				job.schedule();
			}
		}
	}
	
	private static boolean isNaturalKeySequence(KeySequence keySequence) {
		KeyStroke[] keyStrokes = keySequence.getKeyStrokes();
		for (KeyStroke keyStroke : keyStrokes) {
			if (keyStroke.getModifierKeys() != KeyStroke.NO_KEY) {
				return false;
			}
		}
		return true;
	}
	
	private static boolean isCompleteKeySequence(KeySequence keySequence) {
		KeyStroke[] keyStrokes = keySequence.getKeyStrokes();
		for (KeyStroke keyStroke : keyStrokes) {
			if (!keyStroke.isComplete()) {
				return false;
			}
		}
		return true;
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
