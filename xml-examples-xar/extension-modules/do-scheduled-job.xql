xquery version "3.0" encoding "UTF-8";

let $element as element() := doc('/db/apps/eXist-book/data/schedule-log.xml')/*
return
  update insert <JobEntry timestamp="{current-dateTime()}"/> into $element  