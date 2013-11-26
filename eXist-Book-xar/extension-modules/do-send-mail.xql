xquery version "3.0" encoding "UTF-8";

declare option exist:serialize "method=html media-type=text/html";

let $page-title := 'Sending mail...'
let $receiver-email := request:get-parameter('receiver', '?')
let $smtp-server := request:get-parameter('smtp-server', '?')
let $message :=
  <mail>
      <from>E. Mailtester &lt;emailtester@dummy.org&gt;</from>
      <to>{$receiver-email}</to>
      <subject>Testing send-email()</subject>
      <message>
          <text>Test message, Testing 3, 2, 1 at {current-dateTime()}</text>
          <xhtml>
              <html>
               <head>
                  <title>Testing</title>
                 </head>
                  <body>
                      <h1>Testing</h1>
                      <p>Test message, Testing 3, 2, 1 at {current-dateTime()}</p>
                  </body>
              </html>
          </xhtml>
      </message>
  </mail>
let $props := 
  <properties>
      <property name="mail.smtp.auth" value="false"/>
      <property name="mail.smtp.port" value="25"/>
      <property name="mail.smtp.host" value="{$smtp-server}"/>
  </properties>

let $session := mail:get-mail-session( $props )
let $result := 
  try {
    mail:send-email($session, $message)
  }
  catch * {
    $err:description
  }

return
<html>
  	 <head>
  	   <meta HTTP-EQUIV="Content-Type" content="text/html; charset=UTF-8"/>
  			 <title>{$page-title}</title>
  	 </head>
  	 <body>
  	   <h1>{$page-title}</h1>
  	   <p>Sending mail to <code>{$receiver-email}</code> via SMTP server <code>{$smtp-server}</code></p>
       {
         if (empty($result)) 
           then <p>Success!</p>
           else <p>Error: {$result}</p>
       }
  	 </body>
  </html>



