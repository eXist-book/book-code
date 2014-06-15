(:~
: Simple example of an Image API built
: as a Stored Query using the REST Server API in eXist
:
: You will need to make sure that the collection /db/images exists
: and is writeable or adjust the value of $local:image-collection
: which is defined below.
:
: This module uses the Image Module XQuery extension in eXist
: you will need to make sure it is enabled in eXist's
: $EXIST_HOME/conf.xml configuartion file.
:
: You will need to make sure the XQuery file has execute
: rights granted in the database for the user calling it.
:
: If this XQuery Main Module is stored into /db/image-api.xquery
: then examples of the API calls are:
:
: 1) Storing a JPEG image
:   
:    HTTP POST http://localhost:8080/exist/rest/db/image-api.xquery
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
:   HTTP GET http://localhost:8080/exist/rest/db/image-api.xquery/image-identifier.jpg
:   
:       The API will respond with a 200 OK and the image data if it exists, otherwise a
:       404 Not Found will be returned in the response.
:
: 3) Retrieving a thumbnail of a JPEG image
:
:   HTTP GET http://localhost:8080/exist/rest/db/image-api.xquery/thumbnail/image-identifier.jpg
:   
:       The API will respond with a 200 OK and the thumbnail image data if the thumbnail exists
:       (or can be created on the fly). Otherwise a 404 Not Found will be returned in the response
:       if the original image of which you are requesting a thumbnail does not exist.
:
: If you attempt a request which is not GET or POST you will receive a 406 Method Not Allowed response.
:
:
: @author Adam Retter
:)
xquery version "1.0";

import module namespace image = "http://exist-db.org/xquery/image";
import module namespace request = "http://exist-db.org/xquery/request";
import module namespace response = "http://exist-db.org/xquery/response";
import module namespace util = "http://exist-db.org/xquery/util";
import module namespace xmldb = "http://exist-db.org/xquery/xmldb";

(: HTTP Response codes :)
declare variable $response:CREATED := 201;
declare variable $response:BAD-REQUEST := 400;
declare variable $response:NOT-FOUND := 404;
declare variable $response:METHOD-NOT-ALLOWED := 406;

(: Configuration of this API :)
declare variable $local:image-collection := "/db/images";   (: Collection in the database where images should be stored :)
declare variable $local:uuidv4-pattern := "[a-f0-9]{8}-[a-f0-9]{4}-4[a-f0-9]{3}-[89ab][a-f0-9]{3}-[a-f0-9]{12}";

(:~
: Store a JPEG image into the database
:
: @param $image-data The base64binary data comprising the JPEG image
:
: @return The path to the newly stored image in the database
:)
declare function local:store-image($image-data) {
    let $image-name := concat(util:uuid(), ".jpg")
    return
        xmldb:store($local:image-collection, $image-name, $image-data, "image/jpeg")
};

(:~
: Retrieves a thumbnail of a JPEG image from the database
: collection $local:image-collection. If the thumnail does not exist
: but the original image does, then the thumbnail is created on-the-fly
:
: @param $image-name The filename of the JPEG image (not the thumnail image!)
:
: @return The base64binary data comprising the thubnail JPEG image,
:   or the empty sequence if the thumbnail and original image do not exist
:)
declare function local:get-or-create-thumbnail($image-name as xs:string) as xs:base64Binary? {
    let $thumnail-image-name := concat("thumbnail-", $image-name),
    $thumbnail-db-path := concat($local:image-collection, "/", $thumnail-image-name)
    return
        
        (: does the thumbnail already exist in the database? :)
        if(util:binary-doc-available($thumbnail-db-path))then
            (: yes, return the thumbail :)
            local:get-image($thumnail-image-name)
        
        else
            (: no, does the original image of which we want a thubmnail exist in the database? :)
            let $image := local:get-image($image-name)
            return
                if(not(empty($image)))then
                    (: yes, create the thumbnail :)
                    let $thumbnail := image:scale($image, (400, 200), "image/jpeg"),
                    $thumnail-db-path := xmldb:store($local:image-collection, $thumnail-image-name, $thumbnail, "image/jpeg")
                    return
                        $thumbnail
                else()
};

(:~
: Retrieve a JPEG image from the database collection $local:image-collection
:
: @param $image-name The filename of the JPEG image
:
: @return The base64binary data comprising the JPEG image,
:   or the empty sequence if the image does not exist
:)
declare function local:get-image($image-name as xs:string) as xs:base64Binary? {
    let $db-path := concat($local:image-collection, "/", $image-name)
    return
        util:binary-doc($db-path)
};


(:
    Main part  of the XQuery which
    handles the incoming HTTP request
    calling our functions (above) and
    creating the subsequent HTTP response
:)
if(request:get-method() eq "POST")then
    if(request:get-header("Content-Type") eq "image/jpeg")then
        let $db-path := local:store-image(request:get-data()),
        $uri-to-resource := concat(request:get-uri(), substring-after($db-path, $local:image-collection))
        return
        (
            response:set-status-code($response:CREATED),
            response:set-header("Location", $uri-to-resource),
            <identifier>{substring-after($db-path, concat($local:image-collection, "/"))}</identifier>
        )
    else
        response:set-status-code($response:BAD-REQUEST)
        
        
else if(request:get-method() eq "GET")then
    if(matches(request:get-uri(), concat(".*/thumbnail/", $local:uuidv4-pattern, "\.jpg$")))then
        let $image-name := tokenize(request:get-uri(), "/")[last()],
        $image := local:get-or-create-thumbnail($image-name)
        return
            if(not(empty($image)))then
                response:stream-binary($image, "image/jpeg", concat("thumbnail-", $image-name))
            else
            (
                response:set-status-code($response:NOT-FOUND),
                <image-not-found>{$image-name}</image-not-found>
            )
    
    
    else if(matches(request:get-uri(), concat(".*/", $local:uuidv4-pattern, "\.jpg$")))then
        let $image-name := tokenize(request:get-uri(), "/")[last()],
        $image := local:get-image($image-name)
        return
            if(not(empty($image)))then
                response:stream-binary($image, "image/jpeg", $image-name)
            else
            (
                response:set-status-code($response:NOT-FOUND),
                <image-not-found>{$image-name}</image-not-found>
            )
    
    else
    ( 
        response:set-status-code($response:BAD-REQUEST),
        <error>You must request an image or thumbnail</error>
    )
else
( 
    response:set-status-code($response:METHOD-NOT-ALLOWED),
    response:set-header("Allow", "POST, GET")
)