package com.lafaspot.logfast.logging;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.avro.Schema;
import org.apache.avro.tool.BinaryFragmentToJsonTool;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.lafaspot.logfast.logging.Logger.Level;
import com.lafaspot.logfast.logging.internal.LogPage;

/**
 * Logger testcases
 *
 * @author lafa
 *
 */
public class LoggerTest {

    private LogContext context2;
    private ArrayList<Integer> numbers2;
    private Exception e2;
    private LogDataUtil data2;
    private LogManager manager2;
    private Logger logger2;

    /**
     *
     */
    @BeforeClass
    public void init() {
        context2 = new LogContext("email=123@lafaspot.com") {
            @Override
            public String getSerial() {
                return "{sledid=" + getSled() + "/" + getName() + "}";
            }

            public String getSled() {
                return "1291298";
            }

        };

        numbers2 = new ArrayList<Integer>();
        numbers2.add(10);
        numbers2.add(20);

        // some data to log
        data2 = new LogDataUtil();
        data2.set(LoggerTest.class, numbers2, new Date());

        e2 = new Exception();
        // some exception to log with stack
        e2.fillInStackTrace();

        manager2 = new LogManager();
        manager2.setLevel(Level.INFO);
        logger2 = manager2.getLogger(context2);
        logger2.flush();
    }

    /**
     *
     */
    @Test
    public void testLoggerNullException() {
        final Logger logger3 = manager2.getLogger(context2);
        logger3.info(data2, null);
    }

    /**
     *
     */
    @Test(threadPoolSize = 1, invocationCount = 40, enabled = true)
    public void testLoggerSpeed() {
        final Logger logger3 = manager2.getLogger(context2);
        for (int i = 0; i < 2000; i++) {
            logger3.info(data2, e2);
        }
    }

    /**
     *
     */
    @Test(threadPoolSize = 1, invocationCount = 40, enabled = true)
    public void testLoggerSpeedInactive() {
        final Logger logger3 = manager2.getLogger(context2);
        for (int i = 0; i < 2000; i++) {
            logger3.debug(data2, e2);
        }
    }

    /**
     *
     */
    @Test(threadPoolSize = 1, invocationCount = 40, enabled = false)
    public void testLoggerSpeedLegacy() {
        final org.slf4j.Logger loggerLegacy = LoggerFactory.getLogger(LoggerTest.class);
        for (int i = 0; i < 2000; i++) {
            loggerLegacy.info(data2.toString(), e2);
        }
    }

    /**
     *
     */
    @Test(threadPoolSize = 1, invocationCount = 40, enabled = true)
    public void testLoggerSpeedInactiveLegacy() {
        final org.slf4j.Logger loggerLegacy = LoggerFactory.getLogger(LoggerTest.class);
        for (int i = 0; i < 2000; i++) {
            loggerLegacy.debug(data2.toString(), e2);
        }
    }

    /**
     * @throws UnsupportedEncodingException
     *             failure
     * @throws Exception
     *             failure
     */
    @Test
    public void testMemoryLogger() throws UnsupportedEncodingException, Exception {
        final LogContext context = new LogContext("email=123@lafaspot.com") {
            @Override
            public String getSerial() {
                return "{sledid=" + getSled() + "/" + getName() + "}";
            }

            public String getSled() {
                return "1291298";
            }

        };

        final ArrayList<Integer> numbers = new ArrayList<Integer>();
        numbers.add(10);
        numbers.add(20);

        // some data to log
        final LogDataUtil data = new LogDataUtil();
        final Exception e = new Exception();
        // some exception to log with stack
        e.fillInStackTrace();

        final LogManager manager = new LogManager();
        manager.setLegacy(true);
        final Logger logger = manager.getLogger(context);

        // Example of log calls
        logger.fatal(data.set(LoggerTest.class, numbers, new Long(5)), e);
        logger.warn(data.set(LoggerTest.class, new BigInteger("912398"), new Double(0.5)), e);
        logger.error(data, e);
        logger.info(data, e);

        if (logger.isDebug()) {
            logger.debug(data, e);
        }

        logger.trace(data, e);

        final byte[] bytes = manager.getBytes();
        Assert.assertTrue(bytes.length > 0, "size should bigger than zero");

        final Schema schema = new Schema.Parser().parse(LogPage.SCHEMA_STR);
        final String json = binaryToJson(bytes, "--no-pretty", schema.toString());
        @SuppressWarnings("checkstyle:linelength")
        String s = "{\"name\":\"{sledid=1291298/email=123@lafaspot.com}\",\"level\":1,\"data\":\"class com.lafaspot.logfast.logging.LoggerTest [10, 20] 5\",\"eMessages\":{\"string\":\"[java.lang.Exception, null],\"},\"eStackTrace\":{\"string\":\"stack trace here\"}}\n{\"name\":\"{sledid=1291298/email=123@lafaspot.com}\",\"level\":3,\"data\":\"class com.lafaspot.logfast.logging.LoggerTest 912398 0.5\",\"eMessages\":{\"string\":\"[java.lang.Exception, null],\"},\"eStackTrace\":{\"string\":\"stack trace here\"}}\n{\"name\":\"{sledid=1291298/email=123@lafaspot.com}\",\"level\":2,\"data\":\"class com.lafaspot.logfast.logging.LoggerTest 912398 0.5\",\"eMessages\":{\"string\":\"[java.lang.Exception, null],\"},\"eStackTrace\":{\"string\":\"stack trace here\"}}\n{\"name\":\"{sledid=1291298/email=123@lafaspot.com}\",\"level\":4,\"data\":\"class com.lafaspot.logfast.logging.LoggerTest 912398 0.5\",\"eMessages\":{\"string\":\"[java.lang.Exception, null],\"},\"eStackTrace\":{\"string\":\"stack trace here\"}}\n";
        Assert.assertEquals(json, s, "expect: " + json + "\n But got: " + s);
    }

    /**
     * @throws UnsupportedEncodingException
     *             failure
     * @throws Exception
     *             failure
     */
    @Test
    public void testBasicExample() throws UnsupportedEncodingException, Exception {
        final LogManager manager = new LogManager();
        // utility to serialize data
        final LogDataUtil data = new LogDataUtil();
        final LogContext context = new LogContext("email=123@lafaspot.com") {
        };
        final Logger logger = manager.getLogger(context);

        // some exception to log with stack
        final Exception e = new Exception();
        e.fillInStackTrace();

        // Example of a log call
        if (logger.isWarn()) {
            logger.warn(data.set(LoggerTest.class, new Time(912398), new Date(0)), e);
        }

        // This is not part of the example
        final byte[] bytes = manager.getBytes();
        Assert.assertTrue(bytes.length > 0, "size should bigger than zero");
    }

    /**
     * @throws UnsupportedEncodingException
     *             failure
     * @throws Exception
     *             failure
     */
    @Test
    public void testMultipleLoggerExample() throws UnsupportedEncodingException, Exception {
        final LogManager manager = new LogManager(Level.DEBUG, 10);
        // utility to serialize data
        final LogDataUtil data = new LogDataUtil();
        final LogContext context = new LogContext("email=123@lafaspot.com") {
        };

        // some exception to log with stack
        final Exception e = new Exception();
        e.fillInStackTrace();

        final Logger logger1 = manager.getLogger(context);
        final Logger logger2 = manager.getLogger(context);
        final Logger logger3 = manager.getLogger(context);
        final Logger logger4 = manager.getLogger(context);

        for (int i = 0; i < 100000; i++) {
            // Example of a log call
            if (logger1.isWarn()) {
                logger1.warn(data.set(LoggerTest.class, new Time(912398), new Date(0)), e);
            }
            if (logger2.isWarn()) {
                logger2.warn(data.set(LoggerTest.class, new Time(912398), new Date(0)), e);
            }
            if (logger3.isWarn()) {
                logger3.warn(data.set(LoggerTest.class, new Time(912398), new Date(0)), e);
            }
            if (logger4.isWarn()) {
                logger4.warn(data.set(LoggerTest.class, new Time(912398), new Date(0)), e);
            }
        }
        logger1.flush();
        logger2.flush();
        logger3.flush();
        logger4.flush();

        // Something wrong here should less or equal to 10
        Assert.assertEquals(manager.stats().activePages(), 12, "active");
        Assert.assertEquals(manager.stats().deletedPages(), 52, "deleted");
        Assert.assertEquals(manager.stats().createdPages(), 64, "created");

        for (int i = 0; i < 100; i++) {
            final Logger logger = manager.getLogger(context);
            // Example of a log call
            if (logger.isWarn()) {
                logger.warn(data.set(LoggerTest.class, new Time(912398), new Date(0)), e);
            }
            logger.flush();
        }

        Assert.assertEquals(manager.stats().activePages(), 10, "active");
        Assert.assertEquals(manager.stats().deletedPages(), 154, "deleted");
        Assert.assertEquals(manager.stats().createdPages(), 164, "created");

        for (int i = 0; i < 100; i++) {
            final Logger logger = manager.getLogger(context);
            // Example of a log call
            if (logger.isWarn()) {
                logger.warn(data.set(LoggerTest.class, new Time(912398), new Date(0)), e);
            }
        }

        final Logger logger5 = manager.getLogger(context);
        // Example of a log call
        if (logger5.isWarn()) {
            logger5.warn(data.set(LoggerTest.class, new Time(912398), new Date(0)), e);
        }

        Assert.assertEquals(manager.stats().activePages(), 6, "active");
        Assert.assertEquals(manager.stats().deletedPages(), 259, "deleted");
        Assert.assertEquals(manager.stats().createdPages(), 265, "created");

        // This is not part of the example
        final byte[] bytes = manager.getBytes();
        Assert.assertTrue(bytes.length > 0, "size should bigger than zero");
    }

    /**
     * Tests the level.
     */
    @Test
    public void testLevel() {
        Assert.assertEquals(Level.fromNumeric(1), Level.FATAL, "Expected fatal level");
        Assert.assertEquals(Level.fromNumeric(2), Level.ERROR, "Expected error level");
        Assert.assertEquals(Level.fromNumeric(3), Level.WARN, "Expected warn level");
        Assert.assertEquals(Level.fromNumeric(4), Level.INFO, "Expected info level");
        Assert.assertEquals(Level.fromNumeric(5), Level.DEBUG, "Expected debug level");
        Assert.assertEquals(Level.fromNumeric(6), Level.TRACE, "Expected trace level");
        Assert.assertEquals(Level.fromNumeric(8), Level.INFO, "Expected default level as info");
    }

    private String binaryToJson(final byte[] avro, final String... options) throws UnsupportedEncodingException, Exception {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final PrintStream p = new PrintStream(new BufferedOutputStream(baos));

        final List<String> args = new ArrayList<String>();
        args.addAll(Arrays.asList(options));
        args.add("-");
        new BinaryFragmentToJsonTool().run(new ByteArrayInputStream(avro), // stdin
                        p, // stdout
                        null, // stderr
                        args);
        return baos.toString("utf-8").replace("\r", "");

    }

}
