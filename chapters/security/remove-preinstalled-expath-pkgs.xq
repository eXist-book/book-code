xquery version "1.0";

import module namespace repo = "http://exist-db.org/xquery/repo";

(:~
: This XQuery removes the pre-installed EXPath packages
: which ship with eXist 2.1
:)

declare variable $local:preinstalled-pkgs := (
	"http://exist-db.org/apps/shared", 
	"http://exist-db.org/apps/dashboard",
	"http://exist-db.org/apps/eXide"
);


for $pkg in $local:preinstalled-pkgs return (
	repo:undeploy($pkg),
	repo:remove($pkg)
)