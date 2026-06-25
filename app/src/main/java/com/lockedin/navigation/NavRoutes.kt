package com.lockedin.navigation

object Routes {
    const val HOME = "home"
    const val FILES = "files"
    const val PDF_VIEWER = "pdf_viewer/{fileId}"
    const val IMAGE_VIEWER = "image_viewer/{fileId}"
    const val AI_CHAT = "ai_chat"
    const val CALCULATOR = "calculator"
    const val DICTIONARY = "dictionary"
    const val TIMER = "timer"
    const val NOTES = "notes"
    const val CONVERTER = "converter"
    const val FORMULAS = "formulas"
    const val SETTINGS = "settings"

    fun pdfViewer(fileId: Long) = "pdf_viewer/$fileId"
    fun imageViewer(fileId: Long) = "image_viewer/$fileId"
}
