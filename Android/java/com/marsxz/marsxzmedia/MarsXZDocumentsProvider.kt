package com.marsxz.marsxzmedia

import android.database.Cursor
import android.database.MatrixCursor
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.provider.DocumentsContract
import android.provider.DocumentsProvider
import android.webkit.MimeTypeMap
import java.io.File
import java.io.FileNotFoundException

class MarsXZDocumentsProvider : DocumentsProvider() {

    // Идентификаторы колонок для таблиц
    private val DEFAULT_ROOT_PROJECTION = arrayOf(
        DocumentsContract.Root.COLUMN_ROOT_ID,
        DocumentsContract.Root.COLUMN_ICON,
        DocumentsContract.Root.COLUMN_TITLE,
        DocumentsContract.Root.COLUMN_FLAGS,
        DocumentsContract.Root.COLUMN_DOCUMENT_ID
    )

    private val DEFAULT_DOCUMENT_PROJECTION = arrayOf(
        DocumentsContract.Document.COLUMN_DOCUMENT_ID,
        DocumentsContract.Document.COLUMN_MIME_TYPE,
        DocumentsContract.Document.COLUMN_DISPLAY_NAME,
        DocumentsContract.Document.COLUMN_LAST_MODIFIED,
        DocumentsContract.Document.COLUMN_FLAGS,
        DocumentsContract.Document.COLUMN_SIZE
    )

    override fun onCreate(): Boolean = true

    // 1. Показываем "Корень" (название приложения в боковом меню)
    override fun queryRoots(projection: Array<out String>?): Cursor {
        val cursor = MatrixCursor(projection ?: DEFAULT_ROOT_PROJECTION)
        cursor.newRow().apply {
            add(DocumentsContract.Root.COLUMN_ROOT_ID, "main_root")
            add(DocumentsContract.Root.COLUMN_DOCUMENT_ID, getBaseDir().absolutePath)
            add(DocumentsContract.Root.COLUMN_TITLE, "MarsXZ Media")
            add(DocumentsContract.Root.COLUMN_ICON, R.mipmap.ic_launcher) // твоя иконка
            add(DocumentsContract.Root.COLUMN_FLAGS, DocumentsContract.Root.FLAG_LOCAL_ONLY)
        }
        return cursor
    }

    // 2. Показываем содержимое папок
    override fun queryChildDocuments(
        parentDocumentId: String?,
        projection: Array<out String>?,
        sortOrder: String?
    ): Cursor {
        val cursor = MatrixCursor(projection ?: DEFAULT_DOCUMENT_PROJECTION)
        val parent = File(parentDocumentId ?: getBaseDir().absolutePath)

        parent.listFiles()?.forEach { file ->
            includeFile(cursor, file)
        }
        return cursor
    }

    // 3. Получаем информацию о конкретном файле/папке
    override fun queryDocument(documentId: String?, projection: Array<out String>?): Cursor {
        val cursor = MatrixCursor(projection ?: DEFAULT_DOCUMENT_PROJECTION)
        includeFile(cursor, File(documentId ?: getBaseDir().absolutePath))
        return cursor
    }

    // 4. Открываем файл для чтения/просмотра
    override fun openDocument(
        documentId: String?,
        mode: String?,
        signal: CancellationSignal?
    ): ParcelFileDescriptor {
        val file = File(documentId ?: throw FileNotFoundException())
        return ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    private fun getBaseDir(): File {
        // Теперь провайдер смотрит в ту же папку, куда AppPaths сохраняет видео и музыку
        // Теперь эта строка найдет метод в AppPaths.kt
        return AppPaths.publicRoot(context!!)
    }

    private fun includeFile(cursor: MatrixCursor, file: File) {
        cursor.newRow().apply {
            add(DocumentsContract.Document.COLUMN_DOCUMENT_ID, file.absolutePath)
            add(DocumentsContract.Document.COLUMN_DISPLAY_NAME, file.name)
            add(DocumentsContract.Document.COLUMN_LAST_MODIFIED, file.lastModified())
            add(DocumentsContract.Document.COLUMN_SIZE, file.length())

            if (file.isDirectory) {
                add(DocumentsContract.Document.COLUMN_MIME_TYPE, DocumentsContract.Document.MIME_TYPE_DIR)
            } else {
                add(DocumentsContract.Document.COLUMN_MIME_TYPE, getMimeType(file))
            }
            add(DocumentsContract.Document.COLUMN_FLAGS, 0)
        }
    }

    private fun getMimeType(file: File): String {
        val extension = file.extension
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension) ?: "application/octet-stream"
    }
}