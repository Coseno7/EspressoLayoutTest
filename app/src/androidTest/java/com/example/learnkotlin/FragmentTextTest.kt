package com.example.learnkotlin

import Fragment3
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FragmentTextTest {

    @Test
    fun testTextFullyDisplayedInFragment() {
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            val fragment = Fragment3()
            activity.supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .commitNow()

            val rootView = fragment.view
            rootView?.post {
                val notFullyDisplayedTextViews = mutableListOf<String>()

                traverseViews(rootView) { view ->
                    if (view is TextView) {
                        val layout = view.layout
                        if (layout != null) {
                            val isTextFullyDisplayed = layout.lineCount == 1 &&
                                    layout.getEllipsisCount(0) == 0
                            if (!isTextFullyDisplayed) {
                                val idName = try {
                                    view.resources.getResourceEntryName(view.id)
                                } catch (e: Exception) {
                                    "Unknown ID"
                                }
                                notFullyDisplayedTextViews.add("id: $idName")
                            }
                        }
                    }
                }

                if (notFullyDisplayedTextViews.isEmpty()) {
                    Log.d("FragmentTextTest", "Fragment中文字都显示完全了")
                } else {
                    notFullyDisplayedTextViews.forEach {
                        Log.e("FragmentTextTest", "Fragment中以下文字未显示完全: $it")
                    }
                }

                // 延迟几秒以观察界面
                try {
                    Thread.sleep(2000) // 停留 2 秒
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun traverseViews(view: View, action: (View) -> Unit) {
        action(view)
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                traverseViews(view.getChildAt(i), action)
            }
        }
    }
}

