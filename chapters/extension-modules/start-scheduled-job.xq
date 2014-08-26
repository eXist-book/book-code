xquery version "3.0" encoding "UTF-8";

import module namespace scheduler = "http://exist-db.org/xquery/scheduler";

declare option exist:serialize "method=html media-type=text/html";

let $page-title := "Start a scheduled job"
let $xquery-script := "xmldb:exist:///db/apps/exist-book/extension-modules/do-scheduled-job.xql"
let $job-name := "SCHEDULER-TEST-JOB"
let $interval := 10000
let $repeats := 5
let $result := scheduler:schedule-xquery-periodic-job($xquery-script, $interval, $job-name, (), 0, $repeats)
return
    <html>
        <head>
            <meta HTTP-EQUIV="Content-Type" content="text/html; charset=UTF-8"/>
            <title>{$page-title}</title>
        </head>
        <body>
            <h1>{$page-title}</h1>
            <p>Starting job {$job-name} that calls <code>{$xquery-script}</code> - {$repeats} times with {$interval} msecs in between: <b>{$result}</b></p>
            <h3>Running jobs:</h3>
            <table border="1" cellspacing="0">
                <tr>
                    <th>Group</th>
                    <th>Name</th>
                    <th>Started</th>
                </tr>
                {
                    for $job in scheduler:get-scheduled-jobs()/*/scheduler:group/scheduler:job
                    return
                    <tr>
                        <td>{ string($job/../@name) }</td>
                        <td>{ string($job/@name) }</td>
                        <td>{ string($job/scheduler:trigger/start) }</td>              
                    </tr>
                }
            </table>
        </body>
    </html>
