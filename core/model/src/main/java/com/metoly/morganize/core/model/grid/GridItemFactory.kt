package com.metoly.morganize.core.model.grid

import java.util.UUID

/**
 * Factory for creating grid items and pages, abstracting the ID generation
 * away from the ViewModel and UI layers.
 */
object GridItemFactory {

    fun createNotePage(): NotePage =
        NotePage(id = generateId())

    fun createTextItem(
        x: Int = 0, y: Int = 0,
        width: Int, height: Int,
        textContent: String = ""
    ): GridItem.Text =
        GridItem.Text(
            id = generateId(),
            x = x, y = y,
            width = width, height = height,
            textContent = textContent
        )

    fun createImageItem(
        x: Int = 0, y: Int = 0,
        width: Int, height: Int,
        imageUri: String
    ): GridItem.Image =
        GridItem.Image(
            id = generateId(),
            x = x, y = y,
            width = width, height = height,
            imageUri = imageUri
        )

    fun createChecklistItem(
        x: Int = 0, y: Int = 0,
        width: Int, height: Int,
        title: String = ""
    ): GridItem.Checklist =
        GridItem.Checklist(
            id = generateId(),
            x = x, y = y,
            width = width, height = height,
            title = title,
            entries = listOf(createCheckboxEntry())
        )

    fun createCheckboxEntry(text: String = "", isChecked: Boolean = false): CheckboxEntry =
        CheckboxEntry(id = generateId(), text = text, isChecked = isChecked)

    fun createSecretItem(
        id: String = generateId(),
        x: Int = 0, y: Int = 0,
        width: Int, height: Int,
        encryptedPayload: String,
        salt: String,
        iv: String,
        hasBiometric: Boolean = false,
        biometricWrappedPassword: String? = null,
        biometricWrappedPasswordIv: String? = null
    ): GridItem.SecretItem =
        GridItem.SecretItem(
            id = id,
            x = x, y = y,
            width = width, height = height,
            encryptedPayload = encryptedPayload,
            salt = salt,
            iv = iv,
            hasBiometric = hasBiometric,
            biometricWrappedPassword = biometricWrappedPassword,
            biometricWrappedPasswordIv = biometricWrappedPasswordIv
        )

    private fun generateId(): String = UUID.randomUUID().toString()
}
