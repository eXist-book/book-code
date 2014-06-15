package exist.book.example.module.internal;

import org.exist.dom.QName;
import org.exist.memtree.MemTreeBuilder;
import org.exist.xquery.BasicFunction;
import org.exist.xquery.Cardinality;
import org.exist.xquery.FunctionSignature;
import org.exist.xquery.XPathException;
import org.exist.xquery.XQueryContext;
import org.exist.xquery.value.*;

import java.util.ArrayList;
import java.util.List;


/**
 * Example functions for the HelloModule
 *
 *  h:hello-world() as xs:string
 *  h:say-hello($greeter as xs:string, $greeting as xs:string?, $visitors as xs:string+) as xs:string+
 *
 * @author Adam Retter <adam@exist-db.org>
 */
public class HelloFunctions extends BasicFunction {

    //signature of our XQuery h:hello-world() function
    private final static QName qnHelloWorld = new QName("hello-world", HelloModule.NS, HelloModule.NS_PREFIX);
    public final static FunctionSignature FNS_HELLO_WORLD = new FunctionSignature(
        qnHelloWorld,
        "Say \"hello world\"!",
        null,
        new FunctionReturnSequenceType(Type.DOCUMENT, Cardinality.ONE, "The hello!")
    );

    //signature of our XQuery h:say-hello($greeter as xs:string, $greeting as xs:string?, $visitors as xs:string+) as xs:string+ function
    private final static QName qnSayHello = new QName("say-hello", HelloModule.NS, HelloModule.NS_PREFIX);
    public final static FunctionSignature FNS_SAY_HELLO = new FunctionSignature(
        qnSayHello,
        "Say \"hello world\"!",
        new SequenceType[] {
            new FunctionParameterSequenceType("greeter", Type.STRING, Cardinality.EXACTLY_ONE, "The greeter, i.e. the name of the person that is saying 'hello'."),
            new FunctionParameterSequenceType("greeting", Type.STRING, Cardinality.ZERO_OR_ONE, "An optional greeting, if omitted then 'hello' is used."),
            new FunctionParameterSequenceType("visitors", Type.STRING, Cardinality.ONE_OR_MORE, "The visitors, i.e. the names of the people that the greeter is saying 'hello' to."),
        },
        new FunctionReturnSequenceType(Type.DOCUMENT, Cardinality.ONE, "The hello!")
    );



    //standard constructor, which allows multiple functions to be implemented in one class
    public HelloFunctions(final XQueryContext context, final FunctionSignature signature) {
        super(context, signature);
    }

    //called when the XQuery function is executed
    @Override
    public Sequence eval(final Sequence[] args, final Sequence contextSequence) throws XPathException {

        final Sequence result;

        //act on the invoked function name
        if(isCalledAs(qnHelloWorld.getLocalName())) {
            result = sayHelloWorld();

        } else if(isCalledAs(qnSayHello.getLocalName())) {

            final String greeter = args[0].itemAt(0).getStringValue();

            final String greeting;
            if(args[1].hasOne()) {
                greeting = args[1].itemAt(0).getStringValue();
            } else {
                greeting = "hello";
            }

            final List<String> visitors = new ArrayList<String>(args[2].getItemCount());
            final SequenceIterator itVisitors = args[2].iterate();
            while(itVisitors.hasNext()) {
                final String visitor = itVisitors.nextItem().getStringValue();
                visitors.add(visitor);
            }

            result = sayHello(greeter, greeting, visitors);

        } else {
            throw new XPathException("Unknown function call: " + this.getName().toString());
        }

        return result;
    }

    /**
     * Constructs the in-memory XML document:
     * <hello>world</hello>
     *
     * @return The in-memory XML document
     */
    private Sequence sayHelloWorld() {
        final MemTreeBuilder builder = new MemTreeBuilder();
        builder.startDocument();
        builder.startElement(new QName("hello", HelloModule.NS, HelloModule.NS_PREFIX), null);
        builder.characters("world");
        builder.endElement();
        builder.endDocument();

        return builder.getDocument();
    }

    /**
     * Says a greeting to many people
     *
     * @param greeter The name of the person saying the greeting
     * @param greeting The greeting to use
     * @param visitors The visitors to say the greeting to
     *
     * @return A sequence of greetings, one for each visitor
     */
    private Sequence sayHello(final String greeter, final String greeting, final List<String> visitors) throws XPathException {
        final Sequence results = new ValueSequence();

        for(final String visitor : visitors) {
            final StringValue result = new StringValue(greeter + " says " + greeting + " to " + visitor);
            results.add(result);
        }

        return results;
    }
}
