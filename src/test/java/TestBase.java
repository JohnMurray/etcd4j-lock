import org.junit.AfterClass;
import org.junit.Rule;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.text.DecimalFormat;
import java.util.Properties;

/**
 * Simple base class to setup some basic stuff for running tests. Including:
 *  - Configure simple test-logger to be picked up by SLF4J
 *  - Add test-rule to print out test name and timing info (for "mvn test")
 *  - AfterClass handler to print spacing (for formatting in "mvn test")
 */
public class TestBase {

    public TestBase() {
        Properties props = System.getProperties();
        props.setProperty("org.slf4j.simpleLogger.logFile",         "target/test.log");
        props.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "warn");
    }

    @Rule
    public TestCasePrinterRule pr = new TestCasePrinterRule();

    /**
     * Simple printer rule for outputting the test-name that is being run
     */
    public class TestCasePrinterRule implements TestRule {

        private final TestCasePrinter printer = new TestCasePrinter();

        private String beforeContent = null;
        private String afterContent = null;
        private long timeStart;
        private long timeEnd;

        private class TestCasePrinter extends ExternalResource {
            @Override
            protected void before() throws Throwable {
                timeStart = System.currentTimeMillis();
                System.out.print(beforeContent);
            };


            @Override
            protected void after() {
                timeEnd = System.currentTimeMillis();
                double seconds = (timeEnd-timeStart)/1000.0;
                System.out.print(afterContent+" ("+new DecimalFormat("0.000").format(seconds)+"s)");
            };
        }

        public final Statement apply(Statement statement, Description description) {
            beforeContent = "\n\t+ "+description.getMethodName()+"... ";
            afterContent =  "done ";
            return printer.apply(statement, description);
        }
    }


    @AfterClass public static void printLineBreak() {
        System.out.println("");
        System.out.println("");
    }
}
