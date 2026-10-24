package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.DriveFile
import com.example.data.model.FileCategory
import com.example.data.security.SecureAccountStorage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MultiDrive", appName)
    }

    @Test
    fun `test secure account storage initial accounts`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val storage = SecureAccountStorage(context)
        val accounts = storage.getAllAccounts()
        assertTrue(accounts.isNotEmpty())
        assertEquals("alex.dev@googlemail.com", accounts[0].email)
    }

    @Test
    fun `test drive file category detection`() {
        val folder = DriveFile(
            id = "f1",
            name = "Documents",
            mimeType = "application/vnd.google-apps.folder"
        )
        assertTrue(folder.isFolder)
        assertEquals(FileCategory.FOLDER, folder.category)

        val pdf = DriveFile(
            id = "f2",
            name = "Spec.pdf",
            mimeType = "application/pdf",
            size = 2_048_000L
        )
        assertEquals(FileCategory.DOCUMENT, pdf.category)
        assertEquals("2.0 MB", pdf.formattedSize)
    }
}
