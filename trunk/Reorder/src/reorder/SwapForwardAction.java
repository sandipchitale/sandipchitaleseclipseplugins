package reorder;

import reorder.SwapOperations.Bias;

public class SwapForwardAction extends AbstractSwapAction {

	@Override
	protected Bias getBias() {
		return Bias.FORWARD;
	}
	
}
