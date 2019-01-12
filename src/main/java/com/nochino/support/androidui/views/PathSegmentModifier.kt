package com.nochino.support.androidui.views

import timber.log.Timber

interface PathSegmentable {

    /**
     * Defines the URIs that have a modifiable path segment
     */
    //<Key = URI, Value = Modifier>
    val pathSegmentsMap: Map<String, PathSegmentModifier>

    /**
     * Defines the contract to retrieve the [PathSegmentModifier]
     */
    fun getPathSegmentModifier(pathString: String): PathSegmentModifier?
}

interface PathSegmentProvider {
    // <Key = URI, ReplacementValue>
    val moddedSegmentsMap: Map<String, String>
}

class PathSegmentModifier(private val url: String?, private val pathSegments: Map<String, String>) {

    // Defines the path segments in a URL that can be replaced
    companion object {
        const val widthPathSegment = "width"
        const val heightPathSegment = "height"
    }

    private var moddedSegmentsUrl: String? = null

    fun getModdedSegmentsUrl(replacementSegmentValues: Map<String, String>): String? {

        moddedSegmentsUrl = url

        replacementSegmentValues.forEach {
            k, v ->
            pathSegments[k]?.let {
                moddedSegmentsUrl = moddedSegmentsUrl?.replace(it, v)
                Timber.d("Replaced {$it} with {$v}")
                return@forEach
            }

            Timber.d("Could not find {$k} in url. Value {$v} will not be used!")
        }

        Timber.d("Segment modifications complete\nOld URL [{$url}]\nNew URL [{$moddedSegmentsUrl}]")

        return moddedSegmentsUrl
    }
}