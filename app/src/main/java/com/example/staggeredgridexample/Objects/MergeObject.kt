package com.example.staggeredgridexample.Objects

import org.simpleframework.xml.Element
import org.simpleframework.xml.Root

@Root(name = "root", strict = false)
data class MergeObject(
    @field:Element(name = "uri", required = false)
    var mergeVideoUri : String = ""
)
