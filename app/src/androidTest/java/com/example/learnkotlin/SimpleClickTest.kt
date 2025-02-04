import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.testing.FragmentScenario
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import com.example.learnkotlin.MainActivity
import com.example.learnkotlin.R
import org.junit.Test

class SimpleClickTest {

    @Test
    fun testFragment2ButtonClick() {
        // 启动自定义的 Activity 并加载 Fragment2
        val scenario = ActivityScenario.launch(MainActivity::class.java)
        scenario.onActivity { activity ->
            val fragment = Fragment2()
            activity.supportFragmentManager.beginTransaction()
                .replace(android.R.id.content, fragment)
                .commitNow()
        }


        // 检查按钮点击是否更新 TextView
        onView(withId(R.id.button)).perform(click())
        onView(withId(R.id.textView)).check(matches(withText("Button Clicked")))
    }
}
