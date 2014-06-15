xquery version "1.0" encoding "UTF-8";

declare namespace javafile = "java:java.io.File";

let $fobject as object := javafile:new(system:get-exist-home())
return
    <Files>
    {
        for $file in javafile:list($fobject)
        return
            <File>{$file}</File>
    }
    </Files>