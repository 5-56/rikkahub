package me.rerere.rikkahub.ui.components.chat

import android.speech.tts.TextToSpeech
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withLink
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.util.fastForEachIndexed
import coil3.compose.AsyncImage
import com.composables.icons.lucide.BookDashed
import com.composables.icons.lucide.BookHeart
import com.composables.icons.lucide.ChevronDown
import com.composables.icons.lucide.ChevronUp
import com.composables.icons.lucide.CircleStop
import com.composables.icons.lucide.Copy
import com.composables.icons.lucide.Lightbulb
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.Pencil
import com.composables.icons.lucide.RefreshCw
import com.composables.icons.lucide.Volume2
import com.composables.icons.lucide.Wrench
import kotlinx.datetime.toJavaLocalDateTime
import me.rerere.ai.core.MessageRole
import me.rerere.ai.provider.Model
import me.rerere.ai.ui.UIMessage
import me.rerere.ai.ui.UIMessageAnnotation
import me.rerere.ai.ui.UIMessagePart
import me.rerere.ai.ui.isEmptyInputMessage
import me.rerere.ai.ui.isEmptyUIMessage
import me.rerere.highlight.HighlightText
import me.rerere.rikkahub.R
import me.rerere.rikkahub.ui.components.richtext.MarkdownBlock
import me.rerere.rikkahub.ui.components.ui.AutoAIIcon
import me.rerere.rikkahub.ui.components.ui.Favicon
import me.rerere.rikkahub.ui.components.ui.FormItem
import me.rerere.rikkahub.ui.components.ui.ImagePreviewDialog
import me.rerere.rikkahub.ui.hooks.tts.rememberTtsState
import me.rerere.rikkahub.ui.modifier.shimmer
import me.rerere.rikkahub.ui.theme.extendColors
import me.rerere.rikkahub.utils.JsonInstantPretty
import me.rerere.rikkahub.utils.copyMessageToClipboard
import me.rerere.rikkahub.utils.toLocalString
import me.rerere.rikkahub.utils.urlDecode

@Composable
fun ChatMessage(
    message: UIMessage,
    modifier: Modifier = Modifier,
    showIcon: Boolean = true,
    model: Model? = null,
    onRegenerate: () -> Unit,
    onEdit: () -> Unit,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
    ) {
        ModelIcon(showIcon, message, model)

        Column(
            modifier = modifier.weight(1f),
            horizontalAlignment = if (message.role == MessageRole.USER) Alignment.End else Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            MessagePartsBlock(
                message.role,
                message.parts,
                message.annotations,
            )
            if (message.isValidToShowActions()) {
                Actions(
                    message = message,
                    onRegenerate = onRegenerate,
                    onEdit = onEdit
                )
            }
        }
    }
}

@Composable
private fun ModelIcon(
    showIcon: Boolean,
    message: UIMessage,
    model: Model?
) {
    if (showIcon && message.role == MessageRole.ASSISTANT && !message.parts.isEmptyUIMessage() && model != null) {
        AutoAIIcon(
            model.modelId,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
private fun ColumnScope.Actions(
    message: UIMessage,
    onRegenerate: () -> Unit,
    onEdit: () -> Unit,
) {
    val context = LocalContext.current
    var showInformation by remember { mutableStateOf(false) }
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            Lucide.Copy, stringResource(R.string.copy), modifier = Modifier
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = LocalIndication.current,
                    onClick = {
                        context.copyMessageToClipboard(message)
                    }
                )
                .padding(8.dp)
                .size(16.dp)
        )

        Icon(
            Lucide.RefreshCw, stringResource(R.string.regenerate), modifier = Modifier
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = LocalIndication.current,
                    onClick = {
                        onRegenerate()
                    }
                )
                .padding(8.dp)
                .size(16.dp)
        )

        if (message.role == MessageRole.USER || message.role == MessageRole.ASSISTANT) {
            Icon(
                Lucide.Pencil, stringResource(R.string.edit), modifier = Modifier
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                        onClick = {
                            onEdit()
                        }
                    )
                    .padding(8.dp)
                    .size(16.dp)
            )
        }
        if (message.role == MessageRole.ASSISTANT) {
            val tts = rememberTtsState()
            val isSpeaking by tts.isSpeaking.collectAsState()
            Icon(
                imageVector = if (isSpeaking) Lucide.CircleStop else Lucide.Volume2,
                contentDescription = stringResource(R.string.tts),
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                        onClick = {
                            if (!isSpeaking) {
                                tts.speak(message.toText(), TextToSpeech.QUEUE_FLUSH)
                            } else {
                                tts.stop()
                            }
                        }
                    )
                    .padding(8.dp)
                    .size(16.dp)
            )
        }
        if (message.role == MessageRole.USER) {
            Icon(
                imageVector = if (showInformation) Lucide.ChevronUp else Lucide.ChevronDown,
                contentDescription = "Info",
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = LocalIndication.current,
                        onClick = {
                            showInformation = !showInformation
                        }
                    )
                    .padding(8.dp)
                    .size(16.dp)
            )
        }
    }

    // Information
    AnimatedVisibility(showInformation) {
        ProvideTextStyle(MaterialTheme.typography.labelSmall) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            ) {
                Text(message.createdAt.toJavaLocalDateTime().toLocalString())
            }
        }
    }
}

@Composable
fun MessagePartsBlock(
    role: MessageRole,
    parts: List<UIMessagePart>,
    annotations: List<UIMessageAnnotation>,
) {
    val contentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)

    // Search
    parts.filterIsInstance<UIMessagePart.Search>().fastForEach { search ->
        SearchResultList(search.search)
    }

    // Reasoning
    parts.filterIsInstance<UIMessagePart.Reasoning>().fastForEach { reasoning ->
        ReasoningCard(
            reasoning = reasoning,
            loading = parts.isEmptyInputMessage()
        )
    }

    // Text
    parts.filterIsInstance<UIMessagePart.Text>().fastForEach { part ->
        SelectionContainer {
            if (role == MessageRole.USER) {
                Card(
                    modifier = Modifier
                        .animateContentSize(),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Column(Modifier.padding(8.dp)) {
                        MarkdownBlock(part.text)
                    }
                }
            } else {
                MarkdownBlock(part.text)
            }
        }
    }

    // Tool Calls
    parts.filterIsInstance<UIMessagePart.ToolResult>().fastForEachIndexed { index, toolCall ->
        key(index) {
            var showResult by remember { mutableStateOf(false) }
            Surface(
                shape = RoundedCornerShape(25),
                tonalElevation = 4.dp,
                onClick = {
                    showResult = true
                }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .padding(vertical = 4.dp, horizontal = 8.dp)
                        .height(IntrinsicSize.Min)
                ) {
                    Icon(
                        imageVector = when (toolCall.toolName) {
                            "create_memory", "edit_memory" -> Lucide.BookHeart
                            "delete_memory" -> Lucide.BookDashed
                            else -> Lucide.Wrench
                        },
                        contentDescription = null,
                        modifier = Modifier.fillMaxHeight()
                    )
                    Column {
                        Text(
                            text = when (toolCall.toolName) {
                                "create_memory" -> "创建了记忆"
                                "edit_memory" -> "更新了记忆"
                                "delete_memory" -> "删除了记忆"
                                else -> "调用工具 ${toolCall.toolName}"
                            },
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
            if (showResult) {
                AlertDialog(
                    onDismissRequest = {
                        showResult = false
                    },
                    title = {
                        Text("工具调用")
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .heightIn(max = 400.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            FormItem(
                                label = {
                                    Text("调用工具 ${toolCall.toolName}")
                                }
                            ) {
                                HighlightText(
                                    code = JsonInstantPretty.encodeToString(toolCall.arguments),
                                    language = "json",
                                    fontSize = 12.sp
                                )
                            }
                            FormItem(
                                label = {
                                    Text("调用结果")
                                }
                            ) {
                                HighlightText(
                                    code = JsonInstantPretty.encodeToString(toolCall.content),
                                    language = "json",
                                    fontSize = 12.sp
                                )
                            }
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                showResult = false
                            }
                        ) {
                            Text("确定")
                        }
                    }
                )
            }
        }
    }

    // Annotations
    if (annotations.isNotEmpty()) {
        Column(
            modifier = Modifier.animateContentSize(),
        ) {
            var expand by remember { mutableStateOf(false) }
            if (expand) {
                ProvideTextStyle(
                    MaterialTheme.typography.labelMedium.copy(
                        color = MaterialTheme.extendColors.gray8.copy(alpha = 0.65f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .drawWithContent {
                                drawContent()
                                drawRoundRect(
                                    color = contentColor.copy(alpha = 0.2f),
                                    size = Size(width = 10f, height = size.height),
                                )
                            }
                            .padding(start = 16.dp)
                            .padding(4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        annotations.fastForEachIndexed { index, annotation ->
                            when (annotation) {
                                is UIMessageAnnotation.UrlCitation -> {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Favicon(annotation.url, modifier = Modifier.size(20.dp))
                                        Text(
                                            text = buildAnnotatedString {
                                                append("${index + 1}. ")
                                                withLink(LinkAnnotation.Url(annotation.url)) {
                                                    append(annotation.title.urlDecode())
                                                }
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            TextButton(
                onClick = {
                    expand = !expand
                }
            ) {
                Text(stringResource(R.string.citations_count, annotations.size))
            }
        }
    }

    // Images
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        var showImageViewer by remember { mutableStateOf(false) }
        val images = parts.filterIsInstance<UIMessagePart.Image>()
        images.fastForEach {
            AsyncImage(
                model = it.url,
                contentDescription = null,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .width(72.dp)
                    .clickable {
                        showImageViewer = true
                    }
            )
        }
        if (showImageViewer) {
            ImagePreviewDialog(images.map { it.url }) {
                showImageViewer = false
            }
        }
    }
}

@Composable
fun ReasoningCard(
    reasoning: UIMessagePart.Reasoning,
    loading: Boolean,
    modifier: Modifier = Modifier,
    fadeHeight: Float = 64f,
) {
    var expanded by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    LaunchedEffect(reasoning, loading) {
        if (loading) {
            if (!expanded) expanded = true
            scrollState.animateScrollTo(scrollState.maxValue.toInt())
        } else {
            if (expanded) expanded = false
        }
    }

    OutlinedCard(
        modifier = modifier,
        onClick = {
            expanded = !expanded
        }
    ) {
        Column(
            modifier = Modifier
                .padding(8.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.let { if (expanded) it.fillMaxWidth() else it.width(150.dp) },
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Lucide.Lightbulb,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = stringResource(R.string.deep_thinking),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.shimmer(
                        isLoading = loading
                    )
                )
                Spacer(Modifier.weight(1f))
                Icon(
                    imageVector = if (expanded) Lucide.ChevronUp else Lucide.ChevronDown,
                    contentDescription = null,
                    modifier = Modifier
                        .clickable {
                            expanded = !expanded
                        }
                        .size(14.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
            if (expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .let {
                            if (loading) {
                                it
                                    .graphicsLayer { alpha = 0.99f } // 触发离屏渲染，保证蒙版生效
                                    .drawWithCache {
                                        // 创建顶部和底部的渐变蒙版
                                        val brush = Brush.verticalGradient(
                                            startY = 0f,
                                            endY = size.height,
                                            colorStops = arrayOf(
                                                0.0f to Color.Transparent,
                                                (fadeHeight / size.height) to Color.Black,
                                                (1 - fadeHeight / size.height) to Color.Black,
                                                1.0f to Color.Transparent
                                            )
                                        )
                                        onDrawWithContent {
                                            drawContent()
                                            drawRect(
                                                brush = brush,
                                                size = Size(size.width, size.height),
                                                blendMode = androidx.compose.ui.graphics.BlendMode.DstIn // 用蒙版做透明渐变
                                            )
                                        }
                                    }
                                    .heightIn(max = 100.dp)
                                    .verticalScroll(scrollState)
                            } else {
                                it
                            }
                        }

                ) {
                    SelectionContainer {
                        Text(
                            text = reasoning.reasoning,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ReasoningCardPreview() {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        ReasoningCard(
            reasoning = UIMessagePart.Reasoning(
                """
            Ok, I'll use the following information to answer your question:

            - The current weather in New York City is sunny with a temperature of 75 degrees Fahrenheit.
            - The current weather in Los Angeles is partly cloudy with a temperature of 68 degrees Fahrenheit.
            - The current weather in Tokyo is rainy with a temperature of 60 degrees Fahrenheit.
            - The current weather in Sydney is sunny with a temperature of 82 degrees Fahrenheit.
            - The current weather in Mumbai is partly cloudy with a temperature of 70 degrees Fahrenheit.
        """.trimIndent()
            ),
            modifier = Modifier.padding(8.dp),
            loading = false
        )

        ReasoningCard(
            reasoning = UIMessagePart.Reasoning(
                """
            Ok, I'll use the following information to answer your question:

            - The current weather in New York City is sunny with a temperature of 75 degrees Fahrenheit.
            - The current weather in Los Angeles is partly cloudy with a temperature of 68 degrees Fahrenheit.
            - The current weather in Tokyo is rainy with a temperature of 60 degrees Fahrenheit.
            - The current weather in Sydney is sunny with a temperature of 82 degrees Fahrenheit.
            - The current weather in Mumbai is partly cloudy with a temperature of 70 degrees Fahrenheit.
        """.trimIndent()
            ),
            modifier = Modifier.padding(8.dp),
            loading = true
        )
    }
}