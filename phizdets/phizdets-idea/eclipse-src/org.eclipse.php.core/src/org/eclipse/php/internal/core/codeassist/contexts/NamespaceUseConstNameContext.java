/*******************************************************************************
 * Copyright (c) 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Zend Technologies
 *******************************************************************************/
package org.eclipse.php.internal.core.codeassist.contexts;

import org.eclipse.dltk.core.CompletionRequestor;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.php.internal.core.PHPCorePlugin;

/**
 * This context represents the state when staying in a use const statement in a
 * namespace name part. <br/>
 * Examples:
 * 
 * <pre>
 *  use const A\B| 
 *  etc...
 * </pre>
 * 
 */
public class NamespaceUseConstNameContext extends AbstractNamespaceUseContext {

	public boolean isValid(ISourceModule sourceModule, int offset, CompletionRequestor requestor) {
		if (!super.isValid(sourceModule, offset, requestor)) {
			return false;
		}

		try {
			String previousWord = getPreviousWord();
			if (!"const".equalsIgnoreCase(previousWord)) { //$NON-NLS-1$
				return false;
			}
		} catch (BadLocationException e) {
			PHPCorePlugin.log(e);
		}
		return validateNamespace(sourceModule, offset, requestor);
	}

}
