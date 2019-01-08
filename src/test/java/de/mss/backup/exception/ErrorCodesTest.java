package de.mss.backup.exception;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

import de.mss.utils.exception.MssException;

public class ErrorCodesTest {

   @Test
   public void testErrorCodes() {
      try {
         new ErrorCodes();
         fail();
      }
      catch (MssException e) {
         assertEquals("ErrorCode", de.mss.utils.exception.ErrorCodes.ERROR_NOT_INSTANCABLE.getErrorCode(), e.getError().getErrorCode());
      }
   }
}
