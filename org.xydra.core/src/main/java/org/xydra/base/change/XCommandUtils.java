package org.xydra.base.change;

import org.xydra.annotations.RunsInGWT;


/**
 * Small utility to help deal with XCommand executing return codes.
 *
 * @author xamde
 */
@RunsInGWT(true)
public class XCommandUtils {

    public static boolean changedSomething(final long commandResult) {
        return commandResult >= 0;
    }

    /**
     * @param commandResult
     * @return true if there was no error. NoChange is also a success.
     */
    public static boolean success(final long commandResult) {
        return commandResult >= 0 || commandResult == XCommand.NOCHANGE;
    }

    /**
     * @param result
     * @return true if the result indicates a failed Xydra command. Nothing
     *         else.
     */
    public static boolean failed(final long result) {
        return result == XCommand.FAILED;
    }

    public static boolean noChange(final long result) {
        return result == XCommand.NOCHANGE;
    }

}
