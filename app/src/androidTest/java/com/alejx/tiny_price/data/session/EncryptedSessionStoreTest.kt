package com.alejx.tiny_price.data.session

import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.*
import org.junit.Test
import java.io.File

class EncryptedSessionStoreTest {
    @Test fun tokenIsEncryptedOutsideBackupsAndCanBeDeleted() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val store = EncryptedSessionStore(context)
        val previous = store.read()
        try {
            val sample = SavedSession("test-sensitive-access", "test-sensitive-refresh", 123456)
            store.write(sample)
            assertEquals(sample, store.read())
            val file = File(context.noBackupFilesDir, "search-session.bin")
            assertTrue(file.exists())
            assertFalse(String(file.readBytes(), Charsets.UTF_8).contains("test-sensitive"))
            store.write(null)
            assertEquals(null, store.read())
            assertFalse(file.exists())
        } finally { store.write(previous) }
    }
}
