package com.elg.myges.adapters.primary.ui

import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class I18nResourceParityTest {
    @Test
    fun englishResourcesExposeSameTranslatableKeysAsDefaultResources() {
        val defaultKeys = resourceKeys(File("src/main/res/values/strings.xml"))
        val englishKeys = resourceKeys(File("src/main/res/values-en/strings.xml"))

        assertEquals(defaultKeys, englishKeys)
    }

    @Test
    fun primaryUiDoesNotHardcodeVisibleStrings() {
        val sourceRoot = File("src/main/java/com/elg/myges/adapters/primary")
        val hardcodedVisibleStringPatterns = listOf(
            Regex("""Text\(\s*""""),
            Regex("""contentDescription\s*=\s*""""),
            Regex("""setTitle\(\s*""""),
            Regex("""setSubtitle\(\s*""""),
            Regex("""setNegativeButtonText\(\s*"""")
        )
        val violations = sourceRoot.walkTopDown()
            .filter { it.isFile && it.extension == "kt" }
            .flatMap { file ->
                file.readLines().mapIndexedNotNull { index, line ->
                    if (hardcodedVisibleStringPatterns.any { it.containsMatchIn(line) }) {
                        "${file.relativeTo(sourceRoot)}:${index + 1}"
                    } else {
                        null
                    }
                }
            }
            .toList()

        assertEquals(emptyList<String>(), violations)
    }

    private fun resourceKeys(file: File): Set<String> {
        val document = DocumentBuilderFactory.newInstance()
            .newDocumentBuilder()
            .parse(file)
        val resources = mutableSetOf<String>()
        listOf("string", "plurals").forEach { tagName ->
            val nodes = document.getElementsByTagName(tagName)
            for (index in 0 until nodes.length) {
                val node = nodes.item(index)
                val attributes = node.attributes
                val translatable = attributes.getNamedItem("translatable")?.nodeValue
                if (translatable != "false") {
                    resources += attributes.getNamedItem("name").nodeValue
                }
            }
        }
        return resources
    }
}
