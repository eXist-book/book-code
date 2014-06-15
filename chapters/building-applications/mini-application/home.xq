xquery version "1.0" encoding "UTF-8";

declare option exist:serialize "method=html media-type=text/html indent=no";

declare variable $page-title as xs:string := "Mini Application Home Page";

<html>
    <head>
	   <meta HTTP-EQUIV="Content-Type" content="text/html; charset=UTF-8"/>
        <title>{$page-title}</title>
    </head>
    <body>
        <h1>{$page-title}</h1>
        <form method="post" action="hello">
            What's your name? 
            <input type="text" name="personname"/>
   			<input type="submit"/>
        </form>
        <br/>
        <p>Powered by:</p>
        <img src="images/existdb.png" width="10%"/>
    </body>
</html>