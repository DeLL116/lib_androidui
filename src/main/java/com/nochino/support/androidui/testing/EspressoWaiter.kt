package com.nochino.support.androidui.testing

/**
 * Used in Espresso tests to wait for a specified [Condition]. Can be used in place of
 * an [androidx.test.espresso.idling.CountingIdlingResource] and is useful in making an
 * Espresso test wait for trivial things like a Fragment Transition Animation to end (
 * instead of using a [androidx.test.espresso.idling.CountingIdlingResource] object.
 *
 * Contrary to what the class name would entice you to believe, this class will not
 * bring you a cup of Espresso :-)
 *
 * Usage Ex.
 *
 * EspressoWaiter(object : Wait.Condition {
 *      override fun check(): Boolean {
 *      return mainActivity
 *      .supportFragmentManager
 *      .findFragmentById(R.id.nav_host_fragment)
 *      ?.childFragmentManager
 *      ?.primaryNavigationFragment is MainFragment
 *  }
 *  }).waitForIt()
 *
 */
class EspressoWaiter(private val mCondition: Condition) {

    companion object {
        private const val CHECK_INTERVAL = 100
        private const val TIMEOUT = 10000
    }

    interface Condition {
        fun check(): Boolean
    }

    fun waitForIt() {
        var state = mCondition.check()
        val startTime = System.currentTimeMillis()
        while (!state) {
            try {
                Thread.sleep(CHECK_INTERVAL.toLong())
            } catch (e: InterruptedException) {
                throw RuntimeException(e)
            }

            if (System.currentTimeMillis() - startTime > TIMEOUT) {
                throw AssertionError("Wait timeout.")
            }
            state = mCondition.check()
        }
    }
}