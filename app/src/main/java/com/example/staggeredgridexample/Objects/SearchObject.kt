package com.example.staggeredgridexample.Objects

import org.simpleframework.xml.Element
import org.simpleframework.xml.ElementList
import org.simpleframework.xml.Root

data class SearchObject(
    @field:ElementList(entry = "item", inline = true, required = false)
    var searchItems : ArrayList<SearchItem> = ArrayList()
) {
    @Root(name = "root", strict = false)
    data class SearchItem(
        @field:Element(name = "text", required = false)
        var text : String = "",
        @field:Element(name = "thumbnail_uri", required = false)
        var thumbnail_uri : String = "",
        @field:Element(name = "title", required = false)
        var videoTitle : String = "",
        @field:Element(name = "youtubeURL", required = false)
        var youtubeURL : String = "",
        @field:Element(name = "id", required = false)
        var dicId : String = "",
        @field:Element(name = "picture_uri", required = false)
        var userImage : String = "",
        @field:Element(name = "last_name", required = false)
        var userName : String = "",
        var isSelected : Boolean = false
    )
}
