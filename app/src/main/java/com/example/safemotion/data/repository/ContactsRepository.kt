package com.example.safemotion.data.repository

import android.content.Context
import android.provider.ContactsContract
import com.example.safemotion.data.model.Contact
import com.example.safemotion.data.model.phoneKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Lee los contactos del teléfono que tienen número. La pantalla pide READ_CONTACTS antes. */
class ContactsRepository(context: Context) {
    private val resolver = context.applicationContext.contentResolver

    suspend fun loadContacts(): List<Contact> = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )
        val contacts = mutableListOf<Contact>()
        try {
            resolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                projection,
                null,
                null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )?.use { cursor ->
                val idColumn = cursor.getColumnIndexOrThrow(projection[0])
                val nameColumn = cursor.getColumnIndexOrThrow(projection[1])
                val phoneColumn = cursor.getColumnIndexOrThrow(projection[2])
                while (cursor.moveToNext()) {
                    val phone = cursor.getString(phoneColumn).orEmpty()
                    if (phoneKey(phone).isEmpty()) continue
                    contacts += Contact(
                        id = "${cursor.getLong(idColumn)}-${phoneKey(phone)}",
                        name = cursor.getString(nameColumn).orEmpty().ifBlank { phone },
                        phone = phone
                    )
                }
            }
        } catch (e: SecurityException) {
            return@withContext emptyList()
        }
        // Un mismo número puede estar guardado varias veces; se deja una sola entrada.
        contacts.distinctBy { phoneKey(it.phone) }
    }
}
