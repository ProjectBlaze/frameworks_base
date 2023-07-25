/*
 * Copyright (c) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.android.systemui.statusbar.notification.row

import android.app.ActivityManager
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.provider.Settings.Secure
import android.testing.AndroidTestingRunner
import android.testing.TestableLooper
import androidx.test.filters.SmallTest
import com.android.systemui.SysuiTestCase
import com.android.systemui.dump.DumpManager
import com.android.systemui.settings.UserTracker
import com.android.systemui.statusbar.notification.row.NotificationSettingsController.Listener
import com.android.systemui.util.mockito.any
import com.android.systemui.util.mockito.capture
import com.android.systemui.util.mockito.eq
import com.android.systemui.util.mockito.mock
import com.android.systemui.util.mockito.whenever
import com.android.systemui.util.settings.SecureSettings
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations

@SmallTest
@RunWith(AndroidTestingRunner::class)
@TestableLooper.RunWithLooper
class NotificationSettingsControllerTest : SysuiTestCase() {

    val setting1: String = Secure.NOTIFICATION_BUBBLES
    val setting2: String = Secure.ACCESSIBILITY_ENABLED
    val settingUri1: Uri = Secure.getUriFor(setting1)
    val settingUri2: Uri = Secure.getUriFor(setting2)

    @Mock
    private lateinit var userTracker: UserTracker
    private lateinit var handler: Handler
    private lateinit var testableLooper: TestableLooper
    @Mock
    private lateinit var secureSettings: SecureSettings
    @Mock
    private lateinit var dumpManager: DumpManager

    @Captor
    private lateinit var userTrackerCallbackCaptor: ArgumentCaptor<UserTracker.Callback>
    @Captor
    private lateinit var settingsObserverCaptor: ArgumentCaptor<ContentObserver>

    private lateinit var controller: NotificationSettingsController

    @Before
    fun setUp() {
        MockitoAnnotations.initMocks(this)
        testableLooper = TestableLooper.get(this)
        handler = Handler(testableLooper.looper)
        allowTestableLooperAsMainThread()
        controller =
                NotificationSettingsController(
                        userTracker,
                        handler,
                        secureSettings,
                        dumpManager
                )
    }

    @After
    fun tearDown() {
        disallowTestableLooperAsMainThread()
    }

    @Test
    fun creationRegistersCallbacks() {
        verify(userTracker).addCallback(any(), any())
        verify(dumpManager).registerNormalDumpable(anyString(), eq(controller))
    }
    @Test
    fun updateContentObserverRegistration_onUserChange_noSettingsListeners() {
        verify(userTracker).addCallback(capture(userTrackerCallbackCaptor), any())
        val userCallback = userTrackerCallbackCaptor.value
        val userId = 9

        // When: User is changed
        userCallback.onUserChanged(userId, context)

        // Validate: Nothing to do, since we aren't monitoring settings
        verify(secureSettings, never()).unregisterContentObserver(any())
        verify(secureSettings, never()).registerContentObserverForUser(
                any(Uri::class.java), anyBoolean(), any(), anyInt())
    }
    @Test
    fun updateContentObserverRegistration_onUserChange_withSettingsListeners() {
        // When: someone is listening to a setting
        controller.addCallback(settingUri1,
                Mockito.mock(Listener::class.java))

        verify(userTracker).addCallback(capture(userTrackerCallbackCaptor), any())
        val userCallback = userTrackerCallbackCaptor.value
        val userId = 9

        // Then: User is changed
        userCallback.onUserChanged(userId, context)

        // Validate: The tracker is unregistered and re-registered with the new user
        verify(secureSettings).unregisterContentObserver(any())
        verify(secureSettings).registerContentObserverForUser(
                eq(settingUri1), eq(false), any(), eq(userId))
    }

    @Test
    fun addCallback_onlyFirstForUriRegistersObserver() {
        controller.addCallback(settingUri1,
                Mockito.mock(Listener::class.java))
        verify(secureSettings).registerContentObserverForUser(
                eq(settingUri1), eq(false), any(), eq(ActivityManager.getCurrentUser()))

        controller.addCallback(settingUri1,
                Mockito.mock(Listener::class.java))
        verify(secureSettings).registerContentObserverForUser(
                any(Uri::class.java), anyBoolean(), any(), anyInt())
    }

    @Test
    fun addCallback_secondUriRegistersObserver() {
        controller.addCallback(settingUri1,
                Mockito.mock(Listener::class.java))
        verify(secureSettings).registerContentObserverForUser(
                eq(settingUri1), eq(false), any(), eq(ActivityManager.getCurrentUser()))

        controller.addCallback(settingUri2,
                Mockito.mock(Listener::class.java))
        verify(secureSettings).registerContentObserverForUser(
                eq(settingUri2), eq(false), any(), eq(ActivityManager.getCurrentUser()))
        verify(secureSettings).registerContentObserverForUser(
                eq(settingUri1), anyBoolean(), any(), anyInt())
    }

    @Test
    fun removeCallback_lastUnregistersObserver() {
        val listenerSetting1 : Listener = mock()
        val listenerSetting2 : Listener = mock()
        controller.addCallback(settingUri1, listenerSetting1)
        verify(secureSettings).registerContentObserverForUser(
                eq(settingUri1), eq(false), any(), eq(ActivityManager.getCurrentUser()))

        controller.addCallback(settingUri2, listenerSetting2)
        verify(secureSettings).registerContentObserverForUser(
                eq(settingUri2), anyBoolean(), any(), anyInt())

        controller.removeCallback(settingUri2, listenerSetting2)
        verify(secureSettings, never()).unregisterContentObserver(any())

        controller.removeCallback(settingUri1, listenerSetting1)
        verify(secureSettings).unregisterContentObserver(any())
    }

    @Test
    fun addCallback_updatesCurrentValue() {
        whenever(secureSettings.getStringForUser(
                setting1, ActivityManager.getCurrentUser())).thenReturn("9")
        whenever(secureSettings.getStringForUser(
                setting2, ActivityManager.getCurrentUser())).thenReturn("5")

        val listenerSetting1a : Listener = mock()
        val listenerSetting1b : Listener = mock()
        val listenerSetting2 : Listener = mock()

        controller.addCallback(settingUri1, listenerSetting1a)
        controller.addCallback(settingUri1, listenerSetting1b)
        controller.addCallback(settingUri2, listenerSetting2)

        testableLooper.processAllMessages()

        verify(listenerSetting1a).onSettingChanged(
                settingUri1, ActivityManager.getCurrentUser(), "9")
        verify(listenerSetting1b).onSettingChanged(
                settingUri1, ActivityManager.getCurrentUser(), "9")
        verify(listenerSetting2).onSettingChanged(
                settingUri2, ActivityManager.getCurrentUser(), "5")
    }

    @Test
    fun removeCallback_noMoreUpdates() {
        whenever(secureSettings.getStringForUser(
                setting1, ActivityManager.getCurrentUser())).thenReturn("9")

        val listenerSetting1a : Listener = mock()
        val listenerSetting1b : Listener = mock()

        // First, register
        controller.addCallback(settingUri1, listenerSetting1a)
        controller.addCallback(settingUri1, listenerSetting1b)
        testableLooper.processAllMessages()

        verify(secureSettings).registerContentObserverForUser(
                any(Uri::class.java), anyBoolean(), capture(settingsObserverCaptor), anyInt())
        verify(listenerSetting1a).onSettingChanged(
                settingUri1, ActivityManager.getCurrentUser(), "9")
        verify(listenerSetting1b).onSettingChanged(
                settingUri1, ActivityManager.getCurrentUser(), "9")
        Mockito.clearInvocations(listenerSetting1b)
        Mockito.clearInvocations(listenerSetting1a)

        // Remove one of them
        controller.removeCallback(settingUri1, listenerSetting1a)

        // On update, only remaining listener should get the callback
        settingsObserverCaptor.value.onChange(false, settingUri1)
        testableLooper.processAllMessages()

        verify(listenerSetting1a, never()).onSettingChanged(
                settingUri1, ActivityManager.getCurrentUser(), "9")
        verify(listenerSetting1b).onSettingChanged(
                settingUri1, ActivityManager.getCurrentUser(), "9")
    }

}