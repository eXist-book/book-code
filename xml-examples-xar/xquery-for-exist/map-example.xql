xquery version "3.0" encoding "UTF-8";

let $map1 := map {
  "a" := 1,
  "b" := <XML>this is <i>cool</i></XML>
}

return 
  <MapContents>
  {
    ( $map1("a"), $map1("b") )
  }
  </MapContents>