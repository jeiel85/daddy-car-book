package com.jeiel85.daddycarbook

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Smoke test guarding the app's public identity (display name and application id).
 * Runs on the host JVM via Robolectric, so it executes in `./gradlew test` without a device.
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AppIdentityTest {

  private val context: Context
    get() = ApplicationProvider.getApplicationContext()

  @Test
  fun appName_isLocalizedBrandName() {
    assertEquals("아빠 차 차계부", context.getString(R.string.app_name))
  }

  @Test
  fun applicationId_isReleasePackage() {
    assertEquals("com.jeiel85.daddycarbook", context.packageName)
  }
}
