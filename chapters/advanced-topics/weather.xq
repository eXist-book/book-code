(:~
: Simple example XQuery which retrieves 
: the current Weather from a public Web Service
: located at http://www.webservicex.net/globalweather.asmx/GetWeather
: parse the result and stores it into the database.
:
: You will need to make sure that the collection indicated
: by $local:weather-collection exists and is writable.
:
: This module uses the EXPath HTTP Client extension
: in eXist to call the Web Service. You will need to make sure
: it is enabled in eXist's $EXIST_HOME/conf.xml configuration file.
:
: This query was written with the idea that it would be scheduled
: for recurring execution, which would effectively build up a
: collection of historic weather data. The query takes three
: parameters:
:
:   $local:city 
:   Which determine for which city the weather is retrieved.
:
:   $local:country
:   Which identifies the country of the city.
:
:   $local:weather-collection
:   Which is the database collection that the weather data
:   should be stored into.
:
: You will need to make sure the XQuery file has execute
: rights granted in the database by the guest user before
: scheduling it.
:
: The following example Scheduler Configuration for $EXIST_HOME/conf.xml
: would execute /db/weather.xquery once every hour:
:
:   <job type="user" name="hourly-weather" xquery="/db/weather.xquery" cron-trigger="0 0 0/1 * * ?">
:       <parameter name="city" value="Exeter"/>
:       <parameter name="country" value="United Kingdom"/>
:       <parameter name="weather-collection" value="/db/weather"/>
:   </job>
:
:
: @author Adam Retter
:)
xquery version "3.0";

import module namespace http = "http://expath.org/ns/http-client";
import module namespace util = "http://exist-db.org/xquery/util";
import module namespace xmldb = "http://exist-db.org/xquery/xmldb";

declare namespace wsx = "http://www.webserviceX.NET";

(: Configuration :)
declare variable $local:city external;
declare variable $local:country external;
declare variable $local:weather-collection external;


let $webservice := "http://www.webservicex.net/globalweather.asmx/GetWeather",
$url := $webservice || "?CityName=" || encode-for-uri($local:city) || "&amp;CountryName=" || encode-for-uri($local:country),
$result := http:send-request(<http:request href="{$url}" method="get"/>)
return

	let $doc := if($result[1]/@status eq "200" and $result[2]/wsx:string) then
		(: reconstruct XML, the webservice provides it as a string for some reason! :)
		util:parse($result[2]/wsx:string/text())
	else
		(: record failure :)
		<failed at="{current-dateTime()}">{$result}</failed>
	return

		let $stored := xmldb:store($local:weather-collection, (), $doc)
		return
            (: log that we ran! :)
			util:log("debug", "Stored hourly weather to: " || $stored)