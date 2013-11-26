xquery version "3.0" encoding "UTF-8";

declare function local:sort-callback($node as node()) as xs:string {
  upper-case(normalize-space($node))
};  

let $node-set as element()+ := doc('/db/apps/eXist-book/extension-modules/sort-node-set.xml')/*/Node
let $index-id as xs:string := 'SORTINDEX'
let $sort-index := sort:create-index-callback($index-id, $node-set, local:sort-callback#1, ())

return
  <Results>
    <StraightSort>
    {
      for $node in $node-set
        let $node-value := normalize-space($node)
        order by $node-value
        return $node-value
    }
    </StraightSort>
    <SortWithIndex>
    {
      for $node in $node-set
        let $node-value := normalize-space($node)
        order by sort:index($index-id, $node)
        return $node-value
    }
    </SortWithIndex>
  </Results>
