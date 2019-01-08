package de.mss.backup.exception;

import de.mss.utils.exception.Error;
import de.mss.utils.exception.MssException;

public class ErrorCodes {

   private static final int  ERROR_CODE_BASE       = 2000;
   public static final Error ERROR_ARCHIVE_FAILED  = new Error(ERROR_CODE_BASE + 0, "Failed to read/write Archive");
   public static final Error ERROR_UNKNOWN_OS_TYPE = new Error(ERROR_CODE_BASE + 1, "Unknown OS type");
   public static final Error ERROR_FAILED_TO_READ_CONFIG = new Error(ERROR_CODE_BASE + 2, "Failed to read config");


   public ErrorCodes() throws MssException {
      throw new MssException(
            new Error(
                  de.mss.utils.exception.ErrorCodes.ERROR_NOT_INSTANCABLE.getErrorCode(),
                  de.mss.utils.exception.ErrorCodes.ERROR_NOT_INSTANCABLE.getErrorText() + " (" + getClass().getName() + ")"));
   }
}
