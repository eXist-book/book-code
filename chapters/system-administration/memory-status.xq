xquery version "3.0";

declare function local:human-units($bytes) {
	let $unit := if($bytes > math:pow(1024, 3)) then
		(math:pow(1024, 3), "GB")
	else if($bytes > math:pow(1024, 2)) then
		(math:pow(1024, 2), "MB")
	else
		(1024, "KB")
	return
		format-number($bytes div $unit[1], ".00") || " " || $unit[2]
};


<memory>
	<max>{local:human-units(system:get-memory-max())}</max>
	<allocated>
		<in-use>{local:human-units(system:get-memory-total() - system:get-memory-free())}</in-use>
		<free>{local:human-units(system:get-memory-free())}</free>
		<total>{local:human-units(system:get-memory-total())}</total>
	</allocated>
	<available>{local:human-units(system:get-memory-max() - system:get-memory-total() - system:get-memory-free())}</available>
</memory>