(:~
: XQuery Trigger which listens for after create
: events and writes a message to eXist's log file
:
: The Trigger makes use of a single external variable
: to provide configuration:
:
:   @param et:log-level  Indicates the Log4j level that the 
:     the message should be written to eXist's log file at
:
: An example Trigger configuration to be placed into a Collection Configuration
: document (collection.xconf) might look like:
:
: <collection xmlns="http://exist-db.org/collection-config/1.0">
:   <triggers> 
:     <trigger class="org.exist.collections.triggers.XQueryTrigger">
:       <parameter name="url" value="xmldb:exist:///db/simple-example-trigger.xquery"/>
:       <parameter name="bindingPrefix" value="et"/>
:
:       <parameter name="log-level" value="INFO"/>
:     </trigger>
:    </triggers>
: </collection>
:
: @author Adam Retter
:)
xquery version "1.0";

module namespace et = "http://example/trigger";

declare namespace trigger = "http://exist-db.org/xquery/trigger";
import module namespace util = "http://exist-db.org/xquery/util";

declare variable $et:log-level external;

declare function trigger:after-create-document($uri as xs:anyURI) {
    et:log(("XQuery Trigger called after document '", $uri, "' created."))
};



(: *** Business and Service Logic Below here! *** :)

declare function et:log($msgs as xs:string+) {
    util:log($et:log-level, $msgs) 
};