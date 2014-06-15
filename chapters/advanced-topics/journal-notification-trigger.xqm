(:~
: XQuery Trigger which listens for after create and update
: events and sends emails notifications of new content
:
: Requires the XQuery Mail Extension Module
: to be enabled and configured in $EXIST_HOME/conf.xml
:
: The Trigger makes use of several external variables to
: provide configuration:
:
:   @param xmpl:path-filter  A regular expression to use to filter paths,
:     if you want to include all paths, you need not set this variable
:
:   @param xmpl:doc-title-xpath  An XPath expression which will extract
:     the title from a document
:        
:   @param xmpl:sender  An email address that the email should be sent from
:
:   @param xmpl:recipient  The email address of whom should receive the
:     notificataions
:
:   @param xmpl:server  The IP address or hostname of the SMTP server to use
:
:
: An example Trigger configuration to be placed into a Collection Configuration
: document (collection.xconf) might look like:
:
: <trigger class="org.exist.collections.triggers.XQueryTrigger">
:   <parameter name="url" value="xmldb:exist:///db/journal-notification-trigger.xquery"/>
:   <parameter name="bindingPrefix" value="xmpl"/>
:       
:   <parameter name="path-filter" value="/db/.+\.journal.xml"/>
:   <parameter name="doc-title-xpath" value="//title"/>
:   <parameter name="sender" value="exist-journal-monitor@somewhere.org"/>
:   <parameter name="recipient" value="editor@somewhere.org"/>
:   <parameter name="server" value="smtp.example.com"/> 
: </trigger>
:
: @author Adam Retter
:)
xquery version "3.0";

module namespace xmpl = "http://exist.book/xquery/trigger/example";

declare namespace trigger = "http://exist-db.org/xquery/trigger";
import module namespace mail = "http://exist-db.org/xquery/mail";
import module namespace util= "http://exist-db.org/xquery/util";

declare variable $xmpl:path-filter external;
declare variable $xmpl:doc-title-xpath external;
declare variable $xmpl:sender external;
declare variable $xmpl:recipient external;
declare variable $xmpl:server external;


(:~
: Trigger function called by eXist
: after a new document is stored into a
: Collection.
:
: @param uri The URI of the documemnt that was stored
:)
declare function trigger:after-create-document($uri as xs:anyURI) {
    let $notified := xmpl:notify($uri, true())
    return
        if($notified)then
            util:log("INFO", ("Notified ", $xmpl:recipient, " about creation of ", $uri))
        else() 
};


(:~
: Trigger function called by eXist
: after a document is updated
:
: @param uri The URI of the documemnt that was stored
:)
declare function trigger:after-update-document($uri as xs:anyURI) {
    let $notified := xmpl:notify($uri, false())
    return
        if($notified)then
            util:log("INFO", ("Notified ", $xmpl:recipient, " about update of ", $uri))
        else() 
};




(: *** Business and Service Logic Below here! *** :)

(:~
: Notifies a recipient by email
: about a document creation of update
: if it is of interest
:
: @param uri The URI of the document to notify someone about
: @param created boolean indicating whether the document was created
:  or updated
:
: @return true or false indicating whether an email
:   notification was sent or not
:)
declare function xmpl:notify($uri, $created) {
    if(xmpl:is-interested($uri))then
        let $mail := xmpl:prepare-mail($uri, $created)
        return
            if(xmpl:safe-send-email($mail, $xmpl:server, "UTF-8"))then
                true()
             else
                let $log-error := util:log("ERROR", ("Unable to notify ", $xmpl:recipient, " by email. See exist.log for details!"))
                return
                    false()
             
    else
        false()
};

(:~
: Prepares an XML document which is a suitable description
: or an email. The resultant document can be used with the
: mail:send-email extension function probvided with eXist.
:
: @param uri The URI of the document to create an email about
: @param created boolean indicating whether the document was created
:  or updated
:)
declare function xmpl:prepare-mail($uri, $created) {
    let $action-text := if($created)then "created" else "updated",
    $subject := string-join(("Document", $uri, $action-text), " "),
    $html-body :=
        <body>
            <p>
                The document titled <blockquote>{xmpl:get-title($uri)}</blockquote>
                has been <i>{$action-text}</i> at <a href="{xmpl:to-web-uri($uri)}">{$uri}</a>
            </p>
        </body>
    return
        <mail>
            <from>{$xmpl:sender}</from>
            <to>{$xmpl:recipient}</to>
            <subject>{$subject}</subject>
            <message>
                <text>{$html-body//text()}</text>
                <xhtml>
                    <html>
                        <head><title>{$subject}</title></head>
                        {$html-body}
                    </html>
                </xhtml>
            </message>
        </mail>
};


(:~
: A simple wrapper around mail:send-email
: that handles and contains any exceptions.
:
: For example if the email server is unavailable
: we do not want to fail, as notifying
: recipients is a courtesy rather than a critical
: process.
:)
declare function xmpl:safe-send-email($mail, $server, $charset) {
    try {
        mail:send-email($mail, $server, $charset)
    } catch * {
        util:log("ERROR", ("An error occured whilst sending email: ", $err:code, $err:description, $err:value)),
        false()
    }
};


(:~
: Given a database URI, we return a 
: Web Server URI for a document accessible
: from the REST Server
:
: @param uri database uri
: @return REST Server URI
:
:)
declare function xmpl:to-web-uri($uri) {
    "http://localhost:8080/exist/rest" || $uri
};


(:~
: Retrieves the title from a document
:
: @param uri The URI of the document
: @return The title of the document of "*** UNKNOWN ***"
:   if the title cannot be located
:)
declare function xmpl:get-title($uri) {
    let $title := data(util:eval-inline(doc($uri), $xmpl:doc-title-xpath))
    return
        if($title)then
            $title
        else
            "*** UNKNOWN ***"
};


(:~
: Determines if we are interested in the URI
:
: Determination is calculated based on the optional
: global variable $xmpl:path-filter
:
: @param uri The URI to examine
: @return true if we are interested in the URI, false otherwise
:)
declare function xmpl:is-interested($uri) {
    if($xmpl:path-filter) then
        matches($uri, $xmpl:path-filter)
    else
        (: no filter, so we must be interested! :)
        true()
};
