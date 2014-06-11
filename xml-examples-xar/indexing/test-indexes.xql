xquery version "1.0" encoding "UTF-8";

declare namespace tei="http://www.tei-c.org/ns/1.0";
declare option exist:serialize "method=html media-type=text/html indent=no";

declare variable $page-title as xs:string := 'Index performance test';

declare variable $doc-with-indexes as xs:string := "/db/apps/eXist-book/indexing/data/Encyclopedia.xml";
declare variable $doc-without-indexes as xs:string := "/db/apps/eXist-book/indexing/data/Encyclopedia-ALT.xml";

declare variable $repeats-range as xs:integer := 200;
declare variable $phrase-range as xs:string := 'Alexander';

declare variable $repeats-ngram as xs:integer := 500;
declare variable $phrase-ngram as xs:string := 'north of Africa';

declare variable $repeats-fulltext as xs:integer := 200;
declare variable $phrase-fulltext as xs:string := 'distinguish';


let $test-results := 
  <IndexTests>
  
    <RangeIndex repeat="{$repeats-range}" phrase="{$phrase-range}">
      <Description>Range index search for name elements with specific contents</Description>
       {
         let $starttime as xs:time := util:system-time()
         let $result  := for $i in 1 to $repeats-range return 
           for $n in doc($doc-with-indexes)//tei:name[. eq $phrase-range] return $n
         let $endtime as xs:time := util:system-time()
         return
           <Result type="indexed" time="{seconds-from-duration($endtime - $starttime)}s"/>
       }  
       {
          let $starttime as xs:time := util:system-time()
          let $result  := for $i in 1 to $repeats-range return 
            for $n in doc($doc-without-indexes)//name[. eq $phrase-range] return $n
          let $endtime as xs:time := util:system-time()
          return
            <Result type="non-indexed" time="{seconds-from-duration($endtime - $starttime)}s"/>
       }
     </RangeIndex>
     
     <NGramIndex repeat="{$repeats-ngram}" phrase="{$phrase-ngram}">
      <Description>NGram index search for a phrase in a p element</Description>
      {
         let $starttime as xs:time := util:system-time()
         let $result  := for $i in 1 to $repeats-ngram return 
           for $n in doc($doc-with-indexes)//tei:p[ngram:contains(., $phrase-ngram)] return $n
         let $endtime as xs:time := util:system-time()
         return
           <Result type="indexed" time="{seconds-from-duration($endtime - $starttime)}s"/>
      }
      {
         let $starttime as xs:time := util:system-time()
         let $result  := for $i in 1 to $repeats-ngram return 
           for $n in doc($doc-without-indexes)//p[contains(., $phrase-ngram)] return $n
         let $endtime as xs:time := util:system-time()
         return
           <Result type="non-indexed" time="{seconds-from-duration($endtime - $starttime)}s"/>
      }   
     </NGramIndex>
     
     <FullTextIndex repeat="{$repeats-fulltext}" phrase="{$phrase-fulltext}">
      <Description>Full-text search for a specific word</Description> 
      {
        let $starttime as xs:time := util:system-time()
        let $results := for $i in 1 to $repeats-fulltext return
          for $hit in doc($doc-with-indexes)//tei:p[ft:query(., $phrase-fulltext)]
            return $hit
        let $endtime as xs:time := util:system-time()
        return 
          <Result type="indexed" time="{seconds-from-duration($endtime - $starttime)}s"/>
      }
      {
        let $starttime as xs:time := util:system-time()
        let $results := for $i in 1 to $repeats-fulltext return
          for $hit in doc($doc-without-indexes)//p[contains(., concat($phrase-fulltext, ' '))]
            return $hit
        let $endtime as xs:time := util:system-time()
        return 
          <Result type="non-indexed" time="{seconds-from-duration($endtime - $starttime)}s"/>
      }  
    </FullTextIndex>

</IndexTests>

return
  <html>
		<head>
			<meta HTTP-EQUIV="Content-Type" content="text/html; charset=UTF-8"/>
			<title>{$page-title}</title>
		</head>
		<body>
			<h1>{$page-title}</h1>
			<table border="1" cellspacing="0">
        <tr>
          <th>Index</th>
          <th>Description</th>
          <th>Repeats</th>
          <th>Search phrase</th>
          <th>Time with index (s)</th>
          <th>Time without index (s)</th>
        </tr>
        {
          for $result in $test-results/*
            return
              <tr>
                <td>{local-name($result)}</td>
                <td>{string($result/Description)}</td>  
                <td>{string($result/@repeat)}</td>
                <td>{string($result/@phrase)}</td>     
                <td>{string($result/Result[@type eq 'indexed']/@time)}</td>
                <td>{string($result/Result[@type eq 'non-indexed']/@time)}</td>
              </tr>
        }
	    </table>
		</body>
</html>