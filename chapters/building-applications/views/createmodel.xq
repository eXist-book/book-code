xquery version "1.0" encoding "UTF-8";

<Model timestamp="{current-dateTime()}">
{
    for $item in 1 to 10
    return
        <Item id="{$item}">Item number {$item}</Item>
}
</Model>