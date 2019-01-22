package com.nochino.support.androidui.testing

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.matcher.BoundedMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher

// TODO :: WIP :: https://medium.com/2359media/custom-recyclerview-matcher-and-viewassertion-with-espresso-kotlin-45845c64ab44
// TODO :: DOC
// TODO :: Add more custom ViewMatcher tests for the RecyclerView
class RecyclerViewMatchers {
    companion object {
        fun withItemCount(count: Int): Matcher<View> {
            return object : BoundedMatcher<View, RecyclerView> (RecyclerView::class.java) {
                override fun describeTo(description: Description?) {
                    description?.appendText("RecyclerView with item count: $count")
                }

                override fun matchesSafely(item: RecyclerView?): Boolean {
                    return item?.adapter?.itemCount == count
                }

                override fun describeMismatch(item: Any?, description: Description?) {
                    description?.appendText("Whoah!") // TODO :: Change and test description
                }
            }
        }
    }
}