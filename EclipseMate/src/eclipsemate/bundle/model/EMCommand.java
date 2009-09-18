package eclipsemate.bundle.model;

import org.eclipse.jface.bindings.keys.KeySequence;

import eclipsemate.bundle.model.dynamic.EMContext;

public class EMCommand extends EMExecutable {
	
	private SAVE_BEFORE saveBefore;
	protected String command;
	protected INPUT_TYPE inputType;
	protected OUTPUT_TYPE outputType;
	
	public EMCommand(String name, String scope, KeySequence keySequence, SAVE_BEFORE saveBefore, String command, INPUT_TYPE inputType, OUTPUT_TYPE outputType) {
		super(name, scope, keySequence);
		this.saveBefore = saveBefore;
		this.command = command;
		this.inputType = inputType;
		this.inputType = inputType;
	}
	
	public EMCommand(String name, String scope, String prefix, SAVE_BEFORE saveBefore, String command, INPUT_TYPE inputType, OUTPUT_TYPE outputType) {
		super(name, scope, prefix);
		this.saveBefore = saveBefore;
		this.command = command;
		this.inputType = inputType;
		this.inputType = inputType;
	}
	
	@Override
	public Object execute(EMContext context) {
		return null;
	}

	public SAVE_BEFORE getSaveBefore() {
		return saveBefore;
	}

	public void setSaveBefore(SAVE_BEFORE saveBefore) {
		this.saveBefore = saveBefore;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public INPUT_TYPE getInputType() {
		return inputType;
	}

	public void setInputType(INPUT_TYPE inputType) {
		this.inputType = inputType;
	}

	public OUTPUT_TYPE getOutputType() {
		return outputType;
	}

	public void setOutputType(OUTPUT_TYPE outputType) {
		this.outputType = outputType;
	}
}
