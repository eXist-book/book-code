package exist.book.example.module.internal;

import org.apache.log4j.Logger;
import org.exist.xquery.AbstractInternalModule;
import org.exist.xquery.FunctionDef;

import java.util.Map;
import java.util.List;


/**
 * Example InternalModule which provides
 * a few very simple modules.
 *
 * Once the compiled code is placed onto eXist's
 * classpath it can be enabled by adding the line:
 *
 * <module uri="http://hello.world"
 *  class="exist.book.example.module.internal.HelloModule" />
 *
 * to the xquery/builtin-modules element of eXist's
 * configuration file: $EXIST_HOME/conf.xml and then
 * restarting eXist.
 *
 * @author Adam Retter <adam@exist-db.org>
 */
public class HelloModule extends AbstractInternalModule {

    private final static Logger LOG = Logger.getLogger(HelloModule.class);

    protected final static String NS = "http://hello";
    protected final static String NS_PREFIX = "h";

    //The signatures of all of the functions available in this module
    private final static FunctionDef functions[] = {
      new FunctionDef(HelloFunctions.FNS_HELLO_WORLD, HelloFunctions.class),
      new FunctionDef(HelloFunctions.FNS_SAY_HELLO, HelloFunctions.class)
    };

    public HelloModule(Map<String, List<? extends Object>> parameters) {
        super(functions, parameters);
    }

    @Override
    public String getNamespaceURI() {
        return NS;
    }

    @Override
    public String getDefaultPrefix() {
        return NS_PREFIX;
    }

    @Override
    public String getDescription() {
        return "Simple Hello World module";
    }

    @Override
    public String getReleaseVersion() {
        return "2.1";
    }
}
