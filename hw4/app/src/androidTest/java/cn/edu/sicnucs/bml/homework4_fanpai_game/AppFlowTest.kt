package cn.edu.sicnucs.bml.homework4_fanpai_game
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import org.junit.Test
import org.junit.runner.RunWith
@RunWith(AndroidJUnit4::class)
class AppFlowTest {
 @Test fun saveAndCancelProfile() {
  ActivityScenario.launch(MainActivity::class.java).use {
   onView(withId(R.id.btnEditProfile)).perform(scrollTo(),click())
   onView(withId(R.id.et_name)).perform(scrollTo(),replaceText("Alice"),closeSoftKeyboard())
   onView(withId(R.id.btnSave)).perform(scrollTo(),click())
   onView(withId(R.id.tv_name)).check(matches(withText("姓名：Alice")))
   onView(withId(R.id.btnEditProfile)).perform(scrollTo(),click())
   onView(withId(R.id.et_name)).perform(scrollTo(),replaceText("Bob"),closeSoftKeyboard())
   onView(withId(R.id.btnCancel)).perform(scrollTo(),click())
   onView(withId(R.id.tv_name)).check(matches(withText("姓名：Alice")))
   it.recreate()
   onView(withId(R.id.tv_name)).check(matches(withText("姓名：Alice")))
  }
 }
 @Test fun flipResetAndExit() {
  ActivityScenario.launch(MainActivity::class.java).use {
   onView(withId(R.id.btnStartGame)).perform(scrollTo(),click())
   onView(withContentDescription("第1张牌，未翻开")).perform(scrollTo(),click())
   onView(withId(R.id.tvScore)).check(matches(withText("得分：-1")))
   onView(withId(R.id.btnReset)).perform(scrollTo(),click())
   onView(withId(R.id.tvScore)).check(matches(withText("得分：0")))
   onView(withContentDescription("第1张牌，未翻开")).perform(scrollTo()).check(matches(isDisplayed()))
   onView(withId(R.id.btnExit)).perform(scrollTo(),click())
   onView(withId(R.id.btnEditProfile)).check(matches(isDisplayed()))
  }
 }
}
