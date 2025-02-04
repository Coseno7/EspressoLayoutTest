package com.example.learnkotlin

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.FragmentNavigator
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileWriter
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class CombinedTest {

    private val logMessages = mutableListOf<String>()
    private val REQUEST_CODE = 123 // 请求码，用于权限请求的回调

    private fun customTestInfo(context: Context): Pair<String, String> {
        // 用户指定的目标 Fragment 的完整类名
        val targetFragmentClassName = "com.example.learnkotlin.fragment.Fragment1" // 默认值，用户可以修改

        return Pair(targetFragmentClassName, "")
    }

    @Test
    fun testFragmentLayoutAndVisibility() {
        // 启动 MainActivity
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            // 调用 customTestInfo() 获取用户自定义的测试参数，传入 activity 作为 context
            val (targetFragmentClassName, _) = customTestInfo(activity)

            if (!checkAndRequestWritePermission(activity)) {
                logAndSave("没有写入权限，测试终止", isPass = false)
                return@onActivity
            }

            logMessages.clear() // 清空日志

            val navHostFragment =
                activity.supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
            val navController = activity.findNavController(R.id.nav_host_fragment)

            // 获取导航图中的所有目的地
            val destinations = navController.graph.iterator().asSequence().toList()

            // 遍历所有目的地，查找目标 Fragment
            val targetDestination = destinations.firstOrNull { destination ->
                val fragmentClassName = destination.className() // 获取目标类名
                fragmentClassName == targetFragmentClassName // 匹配用户输入的类名
            }

            if (targetDestination != null) {
                // 导航到目标目的地
                navController.navigate(targetDestination.id)

                // 等待目标 Fragment 加载
                activity.runOnUiThread {
                    val currentFragment =
                        navHostFragment.childFragmentManager.fragments.firstOrNull { it.isVisible }

                    if (currentFragment != null) {
                        val fragmentName = currentFragment::class.java.simpleName
                        val view = currentFragment.requireView()

                        // 1. 检查未完全显示的控件
                        try {
                            val partiallyVisibleViews =
                                findPartiallyVisibleViews(
                                    view,
                                    activity,
                                    fragmentName,
                                    parentInfo = null
                                )

                            if (partiallyVisibleViews.isEmpty()) {
                                logAndSave("$fragmentName 中所有控件均完全显示", isPass = true)
                            }
                        } catch (e: AssertionError) {
                            // 捕获并记录 AssertionError，表示测试失败
                            logAndSave(e.message ?: "未知错误", isPass = false)
                            throw e // 继续抛出错误以确保 Espresso 标记测试为失败
                        }
                    } else {
                        val message = "目标 Fragment 未正确加载或不可见"
                        logAndSave(message, isPass = false)
                        throw AssertionError(message) // 标记为测试失败
                    }
                }
            } else {
                val message = "未找到类名为 $targetFragmentClassName 的 Fragment"
                logAndSave(message, isPass = false)
                throw AssertionError(message) // 标记为测试失败
            }
        }
    }

    /**
     * 检查和请求写入权限
     */
    private fun checkAndRequestWritePermission(activity: Activity): Boolean {
        return if (ContextCompat.checkSelfPermission(
                activity,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CODE
            )
            false // 权限未授予，需要请求
        } else {
            true // 已经有权限
        }
    }

    private fun findPartiallyVisibleViews(
        view: View,
        activity: Activity,
        fragmentName: String,
        parentInfo: String? = null
    ): List<String> {
        val partiallyVisibleInfo = mutableListOf<String>()

        // 检查当前 View 是否部分可见或完全不可见
        if (!isViewFullyVisible(view)) {
            val currentInfo = formatViewInfo(view, activity, fragmentName, parentInfo)
            partiallyVisibleInfo.add(currentInfo)

            // 标记测试失败
            throw AssertionError("测试失败: $currentInfo")
        }

        // 如果是 ViewGroup，递归检查子 View
        if (view is ViewGroup) {
            val currentViewInfo = getViewDescription(view, activity) // 当前视图的信息作为子控件的父级描述
            for (i in 0 until view.childCount) {
                partiallyVisibleInfo.addAll(
                    findPartiallyVisibleViews(
                        view.getChildAt(i),
                        activity,
                        fragmentName,
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
    private fun formatViewInfo(
        view: View,
        activity: Activity,
        fragmentName: String,
        parentInfo: String?
    ): String {
        val resourceId = if (view.id != View.NO_ID) {
            try {
                // 获取控件 ID 的名称部分
                activity.resources.getResourceEntryName(view.id)
            } catch (e: Exception) {
                "未知ID"
            }
        } else {
            "无ID"
        }

        val viewType = view.javaClass.simpleName // 获取控件类型
        return if (parentInfo != null) {
            "$fragmentName 中 id 为 $resourceId 的 $viewType 未显示完全。这个控件嵌套于 $parentInfo 中"
        } else {
            "$fragmentName 中 id 为 $resourceId 的 $viewType 未显示完全"
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

    // 获取目的地的类名
    private fun NavDestination.className(): String? {
        // 检查是否为 FragmentNavigator.Destination 类型
        if (this is FragmentNavigator.Destination) {
            return this.className // 直接返回 Fragment 类名
        }
        return null
    }

    /**
     * 日志记录并保存到文件
     */
    private fun logAndSave(message: String, isPass: Boolean = true) {
        logMessages.add(message)
        Log.d("CombinedTest", message)

        // 打印到标准输出，Espresso 报告会捕获这些内容
        if (!isPass) {
            println("TEST FAILURE: $message")
        } else {
            println("TEST INFO: $message")
        }
    }

    /**
     * 将日志写入文件
     */
    private fun writeLogToFile(message: String) {
        val logFile =
            File("/storage/emulated/0/Android/data/com.example.learnkotlin/files/test_log.txt")
        try {
            if (!logFile.exists()) {
                logFile.parentFile?.mkdirs()
                logFile.createNewFile()
            }
            FileWriter(logFile, true).use { writer ->
                writer.appendLine(message)
            }
        } catch (e: IOException) {
            Log.e("CombinedTest", "日志写入文件失败: ${e.message}")
        }
    }
}
