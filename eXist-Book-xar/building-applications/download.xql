xquery version "1.0" encoding "UTF-8";

response:stream-binary(
  util:string-to-binary(
    util:serialize(<Hello/>, 'method=xml'),
    'UTF-8'
  ),
  'application/octet-stream',
  'download.xml'
)

  