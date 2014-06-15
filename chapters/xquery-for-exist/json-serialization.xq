xquery version "1.0" encoding "UTF-8";

declare option exist:serialize "method=json media-type=text/plain";

<Root xmlns:json="something-completely-ignored">
    <Items>
        <Item id="1">Bananas</Item>
        <Item>CPU motherboards</Item>
    </Items>
    <Items>
        <Item json:array="yes">Bricks</Item>
    </Items>
    <Mixed>This is <i>mixed</i> content</Mixed>
    <Empty/>
    <Literal json:literal="true">1</Literal>
    <json:value>xx</json:value>
</Root>
