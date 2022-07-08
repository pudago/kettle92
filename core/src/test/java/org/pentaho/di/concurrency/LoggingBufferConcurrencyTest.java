/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2018 by Hitachi Vantara : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.di.concurrency;

import org.junit.Test;
import org.pentaho.di.core.logging.KettleLoggingEvent;
import org.pentaho.di.core.logging.LogLevel;
import org.pentaho.di.core.logging.LogMessage;
import org.pentaho.di.core.logging.LoggingBuffer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class LoggingBufferConcurrencyTest {

  private LoggingBuffer buffer;

  
  private class Appender extends StopOnErrorCallable<Void> {
    Appender( AtomicBoolean condition ) {
      super( condition );
    }

    @Override
    Void doCall() {
      for ( int i = 0; i < 5000; i++ ) {
        buffer.addLogggingEvent( new KettleLoggingEvent(
          new LogMessage( "subject", LogLevel.DEBUG ), System.currentTimeMillis(), LogLevel.DEBUG ) );
      }
      return null;
    }
  }

  private class Reader extends StopOnErrorCallable<StringBuffer> {
    Reader( AtomicBoolean condition ) {
      super( condition );
    }

    @Override
    StringBuffer doCall() {
      return buffer.getBuffer();
    }
  }
}
