xquery version "1.0" encoding "UTF-8";

let $data as element()* := (
  <item>Apples</item>,
  <item>Bananas</item>,
  <item>Apricots</item>,
  <item>Pears</item>,
  <item>Brambles</item>
)
return
    <GroupedItems>
    {
        for $item in $data
        group by $key := upper-case(substring($item, 1, 1))
        order by $key
        return
            <Group key="{$key}">{$item}</Group>
    }
    </GroupedItems>