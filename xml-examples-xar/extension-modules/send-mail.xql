xquery version "3.0" encoding "UTF-8";

declare option exist:serialize "method=html media-type=text/html";

let $page-title := 'Send mail example'
return
   <html>
  	 <head>
  	   <meta HTTP-EQUIV="Content-Type" content="text/html; charset=UTF-8"/>
  			 <title>{$page-title}</title>
  	 </head>
  	 <body>
  	   <h1>{$page-title}</h1>
       <form method="POST" action="do-send-mail.xql">
         <p>SMTP Server (only straight SMTP, using port 25): <input type="text" name="smtp-server" size="80"/></p>
         <p>Receiver mail address: <input type="text" name="receiver" size="80"/></p>
         <p><input type="submit" value="Send test e-mail"/></p>
       </form>
  	 </body>
  </html>
