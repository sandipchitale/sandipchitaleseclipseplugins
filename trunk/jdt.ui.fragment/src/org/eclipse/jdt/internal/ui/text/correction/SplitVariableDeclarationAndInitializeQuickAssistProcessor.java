package org.eclipse.jdt.internal.ui.text.correction;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.ChildListPropertyDescriptor;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ListRewrite;
import org.eclipse.jdt.internal.ui.JavaPluginImages;
import org.eclipse.jdt.internal.ui.text.correction.proposals.ASTRewriteCorrectionProposal;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;
import org.eclipse.swt.graphics.Image;

public class SplitVariableDeclarationAndInitializeQuickAssistProcessor implements IQuickAssistProcessor {
	public static final String SPLIT_VARIABLE_DECLARATION_AND_INITIALIZE_ID = "splitVariableDeclarationAndInitialize.assist"; //$NON-NLS-1$

	public boolean hasAssists(IInvocationContext context) throws CoreException {
		ASTNode coveringNode = context.getCoveringNode();
		if (coveringNode != null) {
			return getSplitVariableAndInitializeProposals(context, coveringNode, null);
		}
		return false;
	}

	public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations)
			throws CoreException {
		ASTNode coveringNode = context.getCoveringNode();
		boolean noErrorsAtLocation = noErrorsAtLocation(locations);

		if (coveringNode != null) {
			ArrayList<ASTRewriteCorrectionProposal> resultingCollections = new ArrayList<ASTRewriteCorrectionProposal>();
			if (noErrorsAtLocation) {
				getSplitVariableAndInitializeProposals(context, coveringNode, resultingCollections);
				return resultingCollections.toArray(new IJavaCompletionProposal[resultingCollections.size()]);
			}
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes" })
	private static boolean getSplitVariableAndInitializeProposals(IInvocationContext context, ASTNode node,
			Collection<ASTRewriteCorrectionProposal> resultingCollections) {
		VariableDeclarationFragment fragment;
		if (node instanceof VariableDeclarationFragment) {
			fragment = (VariableDeclarationFragment) node;
		} else if (node.getLocationInParent() == VariableDeclarationFragment.NAME_PROPERTY) {
			fragment = (VariableDeclarationFragment) node.getParent();
		} else {
			return false;
		}

		if (fragment.getInitializer() == null) {
			return false;
		}

		VariableDeclarationStatement variableDeclarationStatement = null;
		ASTNode fragParent = fragment.getParent();
		if (fragParent instanceof VariableDeclarationStatement) {
			variableDeclarationStatement = (VariableDeclarationStatement) fragParent;
		} else {
			return false;
		}
		// variableDeclarationStatement is VariableDeclarationStatement

		// This applies to only non-primitive
		if (variableDeclarationStatement.getType().isPrimitiveType()) {
			return false;
		}

		ASTNode statementParent = variableDeclarationStatement.getParent();
		StructuralPropertyDescriptor property = variableDeclarationStatement.getLocationInParent();
		if (!property.isChildListProperty()) {
			return false;
		}

		List list = (List) statementParent.getStructuralProperty(property);

		if (resultingCollections == null) {
			return true;
		}

		AST ast = variableDeclarationStatement.getAST();
		ASTRewrite rewrite = ASTRewrite.create(ast);

		String label = CorrectionMessages.QuickAssistProcessor_splitdeclaration_description
				+ " and Initialize to default"; //$NON-NLS-1$
		Image image = JavaPluginImages.get(JavaPluginImages.IMG_CORRECTION_LOCAL);
		ASTRewriteCorrectionProposal proposal = new ASTRewriteCorrectionProposal(label, context.getCompilationUnit(),
				rewrite, 1, image);
		proposal.setCommandId(SPLIT_VARIABLE_DECLARATION_AND_INITIALIZE_ID);

		Statement newStatement;
		int insertIndex = list.indexOf(variableDeclarationStatement);

		Expression placeholder = (Expression) rewrite.createMoveTarget(fragment.getInitializer());
		ITypeBinding binding = fragment.getInitializer().resolveTypeBinding();
		if (placeholder instanceof ArrayInitializer && binding != null && binding.isArray()) {
			ArrayCreation creation = ast.newArrayCreation();
			creation.setInitializer((ArrayInitializer) placeholder);
			final ITypeBinding componentType = binding.getElementType();
			Type type = null;
			if (componentType.isPrimitive())
				type = ast.newPrimitiveType(PrimitiveType.toCode(componentType.getName()));
			else
				type = ast.newSimpleType(ast.newSimpleName(componentType.getName()));
			creation.setType(ast.newArrayType(type, binding.getDimensions()));
			placeholder = creation;
		}
		Assignment assignment = ast.newAssignment();
		assignment.setRightHandSide(placeholder);
		assignment.setLeftHandSide(ast.newSimpleName(fragment.getName().getIdentifier()));

		newStatement = ast.newExpressionStatement(assignment);
		insertIndex += 1; // add after declaration

		ListRewrite listRewriter = rewrite.getListRewrite(statementParent, (ChildListPropertyDescriptor) property);
		listRewriter.insertAt(newStatement, insertIndex, null);

		VariableDeclarationFragment replacementFragment = ast.newVariableDeclarationFragment();
		replacementFragment.setName(ast.newSimpleName(fragment.getName().getIdentifier()));
		replacementFragment.setInitializer(ast.newNullLiteral());
		VariableDeclarationStatement replacementStatement = ast.newVariableDeclarationStatement(replacementFragment);
		Type type = (Type) rewrite.createCopyTarget(variableDeclarationStatement.getType());
		replacementStatement.setType(type);
		listRewriter.replace(variableDeclarationStatement, replacementStatement, null);

		resultingCollections.add(proposal);
		return true;
	}

	static boolean noErrorsAtLocation(IProblemLocation[] locations) {
		if (locations != null) {
			for (int i = 0; i < locations.length; i++) {
				IProblemLocation location = locations[i];
				if (location.isError()) {
					if (IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER.equals(location.getMarkerType())
							&& JavaCore.getOptionForConfigurableSeverity(location.getProblemId()) != null) {
						// continue (only drop out for severe (non-optional)
						// errors)
					} else {
						return false;
					}
				}
			}
		}
		return true;
	}
}
