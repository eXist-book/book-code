(:~
: Simple example of an Image API built
: as a Stored Query using the RESTXQ API in eXist
:
: You will need to make sure that the collection /db/images exists
: and is writeable or adjust the value of $ii:image-collection
: which is defined below.
:
: This module uses the Image Module XQuery extension in eXist
: you will need to make sure it is enabled in eXist's
: $EXIST_HOME/conf.xml configuartion file.
:
: You may store this query anywhere in the database where the
: RESTXQ Trigger is configured for a collection. By default
: the RESTXQ Trigger is enabled for all database collections.
:
: You will need to make sure the XQuery file has execute
: rights granted in the database for the user calling it.
:
: Examples of the API calls are:
:
: 1) Storing a JPEG image
:   
:    HTTP POST http://localhost:8080/exist/restxq/image
:  
:       You must set the Content-Type header in the request to "image/jpeg"
:       and the binary data of the JPEG image to store should be in the body
:       of the request
:
:       The API will respond with a 201 Created if all goes well and the
:       Location header in the response will contain the URI to retrieve the newly
:       stored image in the database via this API. The body will also contain an
:       identifier for the image.
:       
:       If the correct Content-Type header is missing from the request you will
:       get a 400 Bad Request in the response.
:
: 2) Retrieving a JPEG image
:
:   HTTP GET http://localhost:8080/exist/restxq/image/image-identifier.jpg
:   
:       The API will respond with a 200 OK and the image data if it exists, otherwise a
:       404 Not Found will be returned in the response.
:
: 3) Retrieving a thumbnail of a JPEG image
:
:   HTTP GET http://localhost:8080/exist/restxq/image/thumbnail/image-identifier.jpg
:   
:       The API will respond with a 200 OK and the thumbnail image data if the thumbnail exists
:       (or can be created on the fly). Otherwise a 404 Not Found will be returned in the response
:       if the original image of which you are requesting a thumbnail does not exist.
:
: @author Adam Retter
:)
xquery version "3.0";

module namespace ii = "http://image-api";

import module namespace rest = "http://exquery.org/ns/restxq";
declare namespace http = "http://expath.org/ns/http-client";
declare namespace output = "http://www.w3.org/2010/xslt-xquery-serialization";

import module namespace image = "http://exist-db.org/xquery/image";
import module namespace util = "http://exist-db.org/xquery/util";
import module namespace xmldb = "http://exist-db.org/xquery/xmldb";

(: HTTP Response codes :)
declare variable $ii:HTTP-CREATED := 201;
declare variable $ii:HTTP-NOT-FOUND := 404;

(: Configuration of this API :)
declare variable $ii:image-collection := "/db/images";   (: Collection in the database where images should be stored :)
declare variable $ii:uuidv4-pattern := "[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89ab][a-f0-9]{3}-[a-f0-9]{12}";

(:~
: Store a JPEG image into the database
:
: @param $image-data The base64binary data comprising the JPEG image
:
: @return The path to the newly stored image in the database
:)
declare
    %rest:POST("{$image-data}")
    %rest:path("/image")
    %rest:consumes("image/jpeg")
function ii:store-image($image-data) {
    let $image-name := util:uuid() || ".jpg"
    let $db-path := xmldb:store($ii:image-collection, $image-name, $image-data, "image/jpeg")
    let $uri-to-resource := rest:uri() || "/" || $image-name
    return
    (
        <rest:response>
            <http:response status="{$ii:HTTP-CREATED}">
                <http:header name="Location" value="{$uri-to-resource}"/>
            </http:response>
        </rest:response>
        ,
        <identifier>{$image-name}</identifier>
    )
};

(:~
: Retrieves a thumbnail of a JPEG image from the database
: collection $ii:image-collection. If the thumnail does not exist
: but the original image does, then the thumbnail is created on-the-fly
:
: @param $image-name The filename of the JPEG image (not the thumnail image!)
:
: @return The base64binary data comprising the thubnail JPEG image,
:   or the empty sequence if the thumbnail and original image do not exist
:)
declare
    %rest:GET
    %rest:path("/image/thumbnail/{$image-name}")
    %rest:produces("image/jpeg")
    %output:method("binary")
function ii:get-or-create-thumbnail($image-name) {
    let $thumnail-image-name := "thumbnail-" || $image-name,
    $thumbnail-db-path := $ii:image-collection || "/" || $thumnail-image-name
    return
        
        (: does the thumbnail already exist in the database? :)
        if(util:binary-doc-available($thumbnail-db-path))then
            (: yes, return the thumbail :)
            ii:get-image($thumnail-image-name)
        
        else
            (: no, does the original image of which we want a thubmnail exist in the database? :)
            let $image := ii:get-image($image-name)
            return
                if(not(empty($image)))then
                    (: yes, create the thumbnail :)
                    let $thumbnail := image:scale($image, (400, 200), "image/jpeg"),
                    $thumnail-db-path := xmldb:store($ii:image-collection, $thumnail-image-name, $thumbnail, "image/jpeg")
                    return
                        $thumbnail
                else
                    <rest:response>
                        <http:response status="{$ii:HTTP-NOT-FOUND}">
                            <http:header name="Content-Type" value="application/xml"/>
                        </http:response>
                    </rest:response>
};

(:~
: Retrieve a JPEG image from the database collection $ii:image-collection
:
: @param $image-name The filename of the JPEG image
:
: @return The base64binary data comprising the JPEG image,
:   or the empty sequence if the image does not exist
:)
declare
    %rest:GET
    %rest:path("/image/{$image-name}")
    %rest:produces("image/jpeg")
    %output:method("binary")
function ii:get-image-rest($image-name) {
    let $image := ii:get-image($image-name)
    return
        if(not(empty($image)))then
            $image
        else
            <rest:response>
                <http:response status="{$ii:HTTP-NOT-FOUND}">
                    <http:header name="Content-Type" value="application/xml"/>
                </http:response>
            </rest:response>
};

declare
    %private
function ii:get-image($image-name as xs:string) as xs:base64Binary? {
    let $db-path := $ii:image-collection || "/" || $image-name
    return
        util:binary-doc($db-path)
};
