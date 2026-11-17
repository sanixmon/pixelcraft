package com.app.pixelcraft.presentation.components.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.pixelcraft.ui.theme.PixelCraftColors

/**
 * Renders a GitHub release body (Markdown) as a changelog: `###` headings become coloured
 * section labels (Added / Changed / Fixed…), the bullets under each heading are grouped into
 * one rounded card, and bold, italic, `code` and links are styled inline. Anything it doesn't
 * recognise is shown as plain text rather than dropped.
 */
@Composable
fun ChangelogView(markdown: String, modifier: Modifier = Modifier) {
    val sections = remember(markdown) { parseChangelog(markdown) }
    if (sections.isEmpty()) {
        Text("No changelog for this release.", color = PixelCraftColors.TextSecondary, fontSize = 12.sp, modifier = modifier)
        return
    }
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        for (section in sections) {
            if (section.title != null) SectionLabel(section.title)
            if (section.paragraphs.isNotEmpty()) {
                for (p in section.paragraphs) {
                    Text(
                        text = remember(p) { inlineMarkdown(p) },
                        color = PixelCraftColors.TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            }
            if (section.items.isNotEmpty()) ItemsCard(section.items)
        }
    }
}

@Composable
private fun SectionLabel(title: String) {
    val color = sectionColor(title)
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(color.copy(alpha = 0.16f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(title.uppercase(), color = color, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.8.sp)
        }
    }
}

@Composable
private fun ItemsCard(items: List<Item>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PixelCraftColors.Border.copy(alpha = 0.35f))
    ) {
        items.forEachIndexed { index, item ->
            if (index > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 30.dp)
                        .height(1.dp)
                        .background(PixelCraftColors.Border.copy(alpha = 0.6f))
                )
            }
            Row(
                modifier = Modifier.padding(start = (14 + item.indent * 14).dp, end = 14.dp, top = 10.dp, bottom = 10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(PixelCraftColors.Accent)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = remember(item.text) { inlineMarkdown(item.text) },
                    color = PixelCraftColors.TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }
    }
}

private fun sectionColor(title: String): Color = when (title.trim().lowercase()) {
    "added", "new", "features" -> PixelCraftColors.Success
    "fixed", "fixes", "bug fixes" -> PixelCraftColors.Danger
    "removed", "deprecated", "security" -> PixelCraftColors.Warning
    else -> PixelCraftColors.Accent
}

// --- Parsing ---------------------------------------------------------------------------

private data class Item(val text: String, val indent: Int)

private data class Section(
    val title: String?,
    val paragraphs: MutableList<String> = mutableListOf(),
    val items: MutableList<Item> = mutableListOf()
)

private val Heading = Regex("""^#{1,6}\s+(.*?)\s*#*\s*$""")
private val Bullet = Regex("""^(\s*)(?:[-*+]|\d+[.)])\s+(?:\[[ xX]]\s+)?(.*)$""")
private val Rule = Regex("""^\s*([-*_])(\s*\1){2,}\s*$""")
private val HtmlTag = Regex("""</?[a-zA-Z][^>]*>""")

private fun parseChangelog(markdown: String): List<Section> {
    val sections = mutableListOf<Section>()
    var current = Section(title = null)
    val paragraph = StringBuilder()
    var lastWasItem = false

    fun flushParagraph() {
        val text = paragraph.toString().trim()
        if (text.isNotEmpty()) current.paragraphs += text
        paragraph.clear()
    }

    fun startSection(title: String?) {
        flushParagraph()
        if (current.title != null || current.paragraphs.isNotEmpty() || current.items.isNotEmpty()) sections += current
        current = Section(title)
    }

    val lines = markdown.replace("\r\n", "\n").lines()
    for (raw in lines) {
        val line = raw.replace(Regex("<!--.*?-->"), "").replace(HtmlTag, "")
            .replace("&amp;", "&").replace("&lt;", "<").replace("&gt;", ">").replace("&nbsp;", " ")
        when {
            line.isBlank() -> { flushParagraph(); lastWasItem = false }
            Rule.matches(line) -> { flushParagraph(); lastWasItem = false }
            Heading.matches(line) -> {
                val title = Heading.find(line)!!.groupValues[1]
                // The release title ("## PixelCraft v0.4.0") repeats what the dialog already shows.
                if (title.startsWith("PixelCraft", ignoreCase = true)) continue
                startSection(title)
                lastWasItem = false
            }
            Bullet.matches(line) -> {
                flushParagraph()
                val m = Bullet.find(line)!!
                current.items += Item(m.groupValues[2].trim(), (m.groupValues[1].length / 2).coerceAtMost(2))
                lastWasItem = true
            }
            lastWasItem && raw.startsWith("  ") -> {
                // An indented line under a bullet continues that bullet.
                val last = current.items.removeAt(current.items.lastIndex)
                current.items += last.copy(text = last.text + " " + line.trim())
            }
            else -> {
                if (paragraph.isNotEmpty()) paragraph.append(' ')
                paragraph.append(line.trim())
                lastWasItem = false
            }
        }
    }
    startSection(null)
    return sections
}

private val Inline = Regex(
    """(\*\*|__)(.+?)\1""" +                                  // 2: bold
        """|`([^`]+)`""" +                                    // 3: code
        """|\[([^\]]+)]\(([^)\s]+)\)""" +                     // 4-5: link
        """|(?<![\w*])\*(?![\s*])(.+?)(?<![\s*])\*(?![\w*])""" + // 6: italic
        """|(https?://[^\s<>()]+[^\s<>().,;:!?'"])"""         // 7: bare url
)

private fun inlineMarkdown(text: String): AnnotatedString = buildAnnotatedString { appendInline(text) }

private fun AnnotatedString.Builder.appendInline(text: String) {
    val strong = SpanStyle(fontWeight = FontWeight.SemiBold, color = PixelCraftColors.TextPrimary)
    val link = TextLinkStyles(SpanStyle(color = PixelCraftColors.Accent, textDecoration = TextDecoration.Underline))
    var cursor = 0
    while (cursor < text.length) {
        val m = Inline.find(text, cursor)
        if (m == null) {
            append(text.substring(cursor))
            return
        }
        append(text.substring(cursor, m.range.first))
        val g = m.groupValues
        when {
            g[2].isNotEmpty() -> withStyle(strong) { appendInline(g[2]) }
            g[3].isNotEmpty() -> withStyle(
                SpanStyle(fontFamily = FontFamily.Monospace, color = PixelCraftColors.TextPrimary, background = Color.White.copy(alpha = 0.08f))
            ) { append(g[3]) }
            g[4].isNotEmpty() -> withLink(LinkAnnotation.Url(g[5], link)) { appendInline(g[4]) }
            g[6].isNotEmpty() -> withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { appendInline(g[6]) }
            g[7].isNotEmpty() -> withLink(LinkAnnotation.Url(g[7], link)) { append(g[7]) }
        }
        cursor = m.range.last + 1
    }
}
