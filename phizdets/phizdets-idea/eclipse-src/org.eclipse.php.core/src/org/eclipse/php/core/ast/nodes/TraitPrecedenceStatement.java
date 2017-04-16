/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Zend Technologies
 *******************************************************************************/
package org.eclipse.php.core.ast.nodes;

import org.eclipse.php.core.ast.match.ASTMatcher;
import org.eclipse.php.core.ast.visitor.Visitor;

public class TraitPrecedenceStatement extends TraitStatement {
	private TraitPrecedence precedence;

	public TraitPrecedenceStatement(int start, int end, AST ast, TraitPrecedence precedence) {
		super(start, end, ast, precedence);
		this.precedence = precedence;
	}

	public TraitPrecedenceStatement(AST ast) {
		super(ast);
	}

	public TraitPrecedence getPrecedence() {
		return precedence;
	}

	public void setPrecedence(TraitPrecedence precedence) {
		setExp(precedence);
		this.precedence = precedence;
	}

	@Override
	public boolean subtreeMatch(ASTMatcher matcher, Object other) {
		if (!(other instanceof TraitPrecedenceStatement)) {
			return false;
		}
		return super.subtreeMatch(matcher, other);
	}

	public void toString(StringBuffer buffer, String tab) {
		buffer.append(tab).append("<TraitPrecedenceStatement"); //$NON-NLS-1$
		appendInterval(buffer);
		buffer.append(">\n"); //$NON-NLS-1$
		precedence.toString(buffer, TAB + tab);
		buffer.append("\n"); //$NON-NLS-1$
		buffer.append(tab).append("</TraitPrecedenceStatement>"); //$NON-NLS-1$
	}

	@Override
	ASTNode clone0(AST target) {
		TraitPrecedence precedence = ASTNode.copySubtree(target, getPrecedence());
		final TraitPrecedenceStatement result = new TraitPrecedenceStatement(this.getStart(), this.getEnd(), target,
				precedence);
		return result;
	}

	public void accept0(Visitor visitor) {
		final boolean visit = visitor.visit(this);
		if (visit) {
			childrenAccept(visitor);
		}
		visitor.endVisit(this);
	}
}
