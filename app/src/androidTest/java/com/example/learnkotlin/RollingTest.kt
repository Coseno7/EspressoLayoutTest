package com.example.learnkotlin

import Fragment4
import android.app.Activity
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RollingTest {

    @Test
    fun testVisibleViewsInScrollableLayout() {
        // 启动 MainActivity
        val scenario = ActivityScenario.launch(MainActivity::class.java)

        scenario.onActivity { activity ->
            val fragment = Fragment4()
            activity.supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .commitNow()

            val rootView = fragment.view

            if (rootView != null) {
                val partiallyVisibleViews = findPartiallyVisibleViews(rootView, activity)

                if (partiallyVisibleViews.isNotEmpty()) {
                    partiallyVisibleViews.forEach { Log.e("RollingTest", it) }
                } else {
                    Log.d("RollingTest", "滚动布局中所有控件均显示正常")
                }
            } else {
                Log.e("RollingTest", "无法获取根布局")
            }
        }
    }

    /**
     * 遍历布局，查找未完全显示的控件
     */
    private fun findPartiallyVisibleViews(
        view: View,
        activity: Activity,
        parentInfo: String? = null
    ): List<String> {
        val partiallyVisibleInfo = mutableListOf<String>()

        // 如果是滚动布局（如 NestedScrollView 或 RecyclerView），跳过完全可见检查
        if (view is NestedScrollView || view is RecyclerView || view is ScrollView) {
            Log.d("RollingTest", "跳过滚动布局的完全可见检查: ${getViewDescription(view, activity)}")
            return partiallyVisibleInfo // 滚动布局中的控件不做检查
        }

        // 检查当前 View 是否部分可见或完全不可见
        if (!isViewFullyVisible(view)) {
            val currentInfo = formatViewInfo(view, activity, parentInfo)
            partiallyVisibleInfo.add(currentInfo)
        }

        // 如果是 ViewGroup，递归检查子 View
        if (view is ViewGroup) {
            val currentViewInfo = getViewDescription(view, activity) // 当前视图的信息作为子控件的父级描述
            for (i in 0 until view.childCount) {
                partiallyVisibleInfo.addAll(
                    findPartiallyVisibleViews(
                        view.getChildAt(i),
                        activity,
                        currentViewInfo
                    )
                )
            }
        }

        return partiallyVisibleInfo
    }

    /**
     * 判断 View 是否完全可见
     */
    private fun isViewFullyVisible(view: View): Boolean {
        val visibleRect = Rect()
        val isVisible = view.getGlobalVisibleRect(visibleRect) // 获取 View 的可见矩形

        // 完全显示的条件：可见 && 矩形区域等于 View 本身的宽高
        return isVisible && visibleRect.width() == view.width && visibleRect.height() == view.height
    }

    /**
     * 格式化未完全显示控件的输出信息
     */
    private fun formatViewInfo(view: View, activity: Activity, parentInfo: String?): String {
        val resourceId = if (view.id != View.NO_ID) {
            try {
                activity.resources.getResourceEntryName(view.id)
            } catch (e: Exception) {
                "未知ID"
            }
        } else {
            "无ID"
        }

        val viewType = view.javaClass.simpleName // 获取控件类型
        return if (parentInfo != null) {
            "id 为 $resourceId 的 $viewType 未显示完全。这个控件嵌套于 $parentInfo 中"
        } else {
            "id 为 $resourceId 的 $viewType 未显示完全"
        }
    }

    /**
     * 获取控件的描述信息（用于嵌套信息）
     */
    private fun getViewDescription(view: View, activity: Activity): String {
        val resourceId = if (view.id != View.NO_ID) {
            try {
                activity.resources.getResourceEntryName(view.id)
            } catch (e: Exception) {
                "未知ID"
            }
        } else {
            "无ID"
        }

        val viewType = view.javaClass.simpleName // 获取控件类型
        return "$resourceId 的 $viewType"
    }
}
