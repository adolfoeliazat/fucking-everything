package org.eclipse.php.internal.debug.core.xdebug.dbgp.session;

import org.eclipse.debug.core.DebugEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface FuckingDebugTarget {
    void sessionEnded();

    /**
     * @param detail {@link DebugEvent}.*
     */
    void suspended(int detail);

    void breakpointHit(@NotNull String fileName, int lineNo, @NotNull String exception);
}


