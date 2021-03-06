/*
 * #%L
 * P6Spy
 * %%
 * Copyright (C) 2002 - 2013 P6Spy
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.p6spy.engine.spy.appender;

import static org.junit.Assert.assertEquals;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.LogManager;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.xml.DOMConfigurator;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.p6spy.engine.spy.P6TestUtil;
import com.p6spy.engine.test.BaseTestCase;
import com.p6spy.engine.test.P6TestFramework;

public class Log4jLoggerTest extends BaseTestCase {

  P6TestFramework framework;

  @Before
  public void setup() throws Exception {
    // reset log4j
    LogManager.resetConfiguration();
  }

  @After
  public void cleanup() throws Exception {
    // reset log4j
    LogManager.resetConfiguration();

    // load default configuration
    configureLog4J();
  }

  private void configureLog4J() {
    DOMConfigurator.configure("target/test-classes/log4j.xml");
  }
  
  private void configureLog4JInTest() {
    DOMConfigurator.configure("target/test-classes/log4j-in-test.xml");
  }

  @Test
  public void testExternallyConfiguredLog4J() throws Exception {
    // configure log4j externally
    configureLog4JInTest();

    // initialize framework
    framework = new P6TestFramework("log4j") {
    };
    framework.setUpFramework();

    Connection con = DriverManager.getConnection("jdbc:p6spy:h2:mem:p6spy", "sa", null);

    Log4JTestApppender.clearCapturedMessages();
    P6TestUtil.queryForInt(con, "select count(*) from customers");

    con.close();

    assertEquals(1, Log4JTestApppender.getCapturedMessages().size());

    framework.closeConnection();
  }

  public static class Log4JTestApppender extends ConsoleAppender {
    static List<String> messages = new ArrayList<String>();

    public static void clearCapturedMessages() {
      messages.clear();
    }

    public static List<String> getCapturedMessages() {
      return messages;
    }

    @Override
    protected void subAppend(LoggingEvent event) {
      messages.add(event.getMessage().toString());
      super.subAppend(event);
    }
  }
}
