package reorder;

import reorder.SwapOperations.Bias;

public class SwapBackwardAction extends AbstractSwapAction {

	@Override
	protected Bias getBias() {
		return Bias.BACKWARD;
	}
	
}
