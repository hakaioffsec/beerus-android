package io.hakaisecurity.beerusframework.composables

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import io.hakaisecurity.beerusframework.R
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import io.hakaisecurity.beerusframework.core.functions.frida.AutoInject.Companion.consoleLogs
import io.hakaisecurity.beerusframework.core.functions.frida.AutoInject.Companion.deleteScript
import io.hakaisecurity.beerusframework.core.functions.frida.AutoInject.Companion.getFileNameFromUri
import io.hakaisecurity.beerusframework.core.functions.frida.AutoInject.Companion.getScriptsContent
import io.hakaisecurity.beerusframework.core.functions.frida.AutoInject.Companion.injectFridaCore
import io.hakaisecurity.beerusframework.core.functions.frida.AutoInject.Companion.saveScript
import io.hakaisecurity.beerusframework.core.functions.frida.AutoInject.Companion.stopFridaCore
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.inEditorMode
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.packageName
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.animationStart
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.confirmRootModuleInstallerDialog
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.hasModule
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.hasRoot
import io.hakaisecurity.beerusframework.ui.theme.Arrow_back
import io.hakaisecurity.beerusframework.ui.theme.Terminal
import io.hakaisecurity.beerusframework.ui.theme.Trash
import io.hakaisecurity.beerusframework.ui.theme.ibmFont
import org.eclipse.tm4e.core.registry.IThemeSource
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@ExperimentalLayoutApi
@Composable
fun FridaScriptsScreen(modifier: Modifier, activity: Activity) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val configuration = LocalConfiguration.current
    val startPadding = (configuration.screenWidthDp * 0.09f).dp
    val endPadding = (configuration.screenWidthDp * 0.15f).dp

    val borderRadius by animateFloatAsState(
        targetValue = if (animationStart) 16f else 0f,
        animationSpec = tween(durationMillis = 300),
        label = "borderRadiusAnimation"
    )

    val scriptsState = remember { mutableStateOf(getScriptsContent(activity)) }
    val scripts = scriptsState.value

    fun refreshScripts() {
        scriptsState.value = getScriptsContent(activity)
    }

    var selectedScriptContent by remember { mutableStateOf(TextFieldValue("")) }
    var selectedScript by remember { mutableStateOf("") }
    var isEditorReady by remember { mutableStateOf(false) }
    var editorRef by remember { mutableStateOf<io.github.rosemoe.sora.widget.CodeEditor?>(null) }

    var newScriptName by remember { mutableStateOf("") }
    var noModule by remember { mutableStateOf(false) }
    var noRoot by remember { mutableStateOf(false) }

    var searchQuery by remember { mutableStateOf("") }
    var addMenuExpanded by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var scriptToDelete by remember { mutableStateOf<String?>(null) }

    val uploadLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            val fileName = getFileNameFromUri(activity, uri)
            val scriptsDir = File(activity.filesDir, "scripts")
            val outputFile = File(scriptsDir, fileName)
            activity.contentResolver.openInputStream(uri).use { input ->
                outputFile.outputStream().use { output -> input?.copyTo(output) }
            }
            refreshScripts()
        }
    }

    LaunchedEffect(selectedScript, inEditorMode) {
        if (selectedScript.isNotEmpty() && inEditorMode) {
            kotlinx.coroutines.delay(300)
            isEditorReady = true
        }
    }

    val filteredScripts = scripts.entries.filter {
        it.key.contains(searchQuery, ignoreCase = true)
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(start = startPadding, end = endPadding)
        ) {
            Spacer(modifier = Modifier.height(50.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Absolute.Right
            ) {

                Box {
                    Button(
                        onClick = {
                            if (!inEditorMode && hasModule) {
                                addMenuExpanded = true
                            }
                            if (!hasModule) {
                                noModule = true
                            }
                            if (!hasRoot) {
                                noRoot = true
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Add Script",
                            fontSize = 12.sp,
                            color = Color.Red,
                            fontFamily = ibmFont
                        )
                    }

                    DropdownMenu(
                        expanded = addMenuExpanded,
                        onDismissRequest = { addMenuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("New script", fontFamily = ibmFont) },
                            onClick = {
                                addMenuExpanded = false
                                showCreateDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Upload script", fontFamily = ibmFont) },
                            onClick = {
                                addMenuExpanded = false
                                uploadLauncher.launch(arrayOf("application/javascript"))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1A1A))
                    .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(12.dp))
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.search_icon),
                    contentDescription = "Search",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search scripts...",
                            color = Color(0xFF858585),
                            fontSize = 15.sp,
                            fontFamily = ibmFont
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 15.sp,
                            fontFamily = ibmFont
                        ),
                        cursorBrush = SolidColor(Color.Red),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val listState = rememberLazyListState()

            LaunchedEffect(scripts.size) {
                if (scripts.isNotEmpty()) {
                    listState.animateScrollToItem(scripts.size - 1)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredScripts) { (fileName, content) ->
                    ScriptListItem(
                        fileName = fileName,
                        content = content,
                        timestamp = scriptTimestamp(activity, fileName),
                        enabled = hasModule,
                        onClick = {
                            if (!inEditorMode && hasModule) {
                                selectedScript = fileName
                                selectedScriptContent = TextFieldValue(content)
                                isEditorReady = false
                                inEditorMode = true
                            }
                            if (!hasModule) {
                                noModule = true
                            }
                            if (!hasRoot) {
                                noRoot = true
                            }
                        },
                        onDelete = {
                            if (!inEditorMode && hasModule) {
                                scriptToDelete = fileName
                            }
                            if (!hasModule) {
                                noModule = true
                            }
                            if (!hasRoot) {
                                noRoot = true
                            }
                        }
                    )
                }
            }
        }

        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { showCreateDialog = false },
                title = { Text("Enter script name") },
                text = {
                    TextField(
                        value = newScriptName,
                        onValueChange = { newScriptName = it },
                        placeholder = { Text("example") },
                        singleLine = true
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        showCreateDialog = false
                        if (newScriptName.isNotBlank()) {
                            saveScript(activity, "${newScriptName.replace(".js", "")}.js", "// Write your code here")
                            refreshScripts()
                        }
                        newScriptName = ""
                    }) {
                        Text("Create")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showCreateDialog = false
                        newScriptName = ""
                    }) { Text("Cancel") }
                }
            )
        }

        scriptToDelete?.let { fileName ->
            AlertDialog(
                onDismissRequest = { scriptToDelete = null },
                title = { Text("Delete script") },
                text = {
                    Text(
                        text = "Are you sure you want to delete \"$fileName\"? This action cannot be undone.",
                        fontSize = 16.sp
                    )
                },
                confirmButton = {
                    TextButton(onClick = {
                        if (!inEditorMode && hasModule) {
                            deleteScript(activity, fileName)
                            refreshScripts()
                        }
                        scriptToDelete = null
                    }) {
                        Text("Delete", color = Color.Red, fontFamily = ibmFont)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { scriptToDelete = null }) {
                        Text("Cancel", fontFamily = ibmFont)
                    }
                }
            )
        }

        if (noRoot) {
            AlertDialog(
                onDismissRequest = { noRoot = false },
                title = { Text("Note") },
                text = {
                    Text(
                        text = "Hey, if you want to use this feature you may install Magisk or KernelSU!",
                        fontSize = 18.sp
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        noRoot = false
                        activity.startActivity(
                            Intent(Intent.ACTION_VIEW, "https://topjohnwu.github.io/Magisk/".toUri())
                        )
                    }) { Text("Magisk") }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = {
                            noRoot = false
                            activity.startActivity(
                                Intent(Intent.ACTION_VIEW, "https://kernelsu.org/".toUri())
                            )
                        }) { Text("KernelSU") }

                        TextButton(onClick = { noRoot = false }) {
                            Text("Dismiss", fontFamily = ibmFont)
                        }
                    }
                }
            )
        }

        if (noModule) {
            AlertDialog(
                onDismissRequest = { noModule = false },
                title = { Text("Note") },
                text = { Text(text = "Hey, if you want to use this feature you may install our module!", fontSize = 18.sp) },
                confirmButton = {
                    Button(onClick = {
                        noModule = false
                        confirmRootModuleInstallerDialog(activity)
                    }) {
                        Text("Install")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { noModule = false }) {
                        Text("Do After", fontFamily = ibmFont)
                    }
                }
            )
        }

        AnimatedVisibility(
            visible = inEditorMode,
            enter = slideInVertically(
                initialOffsetY = { it },
                animationSpec = tween(durationMillis = 300)
            ),
            exit = slideOutVertically(
                targetOffsetY = { it },
                animationSpec = tween(durationMillis = 300)
            )
        ) {
            var showPackageDialog by remember { mutableStateOf(false) }
            var showConsole by remember { mutableStateOf(false) }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .drawWithContent {
                        val radiusPx = borderRadius.dp.toPx()
                        drawRoundRect(
                            color = Color(0xFF2D2D2D),
                            size = size,
                            cornerRadius = CornerRadius(radiusPx, radiusPx)
                        )
                        drawContent()
                        drawRoundRect(
                            color = Color(0xFF2D2D2D),
                            cornerRadius = CornerRadius(radiusPx, radiusPx),
                            size = size,
                            style = Stroke(width = 4.dp.toPx())
                        )
                    }
            ) {
                Box(modifier = Modifier.fillMaxWidth().padding(5.dp, 15.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Arrow_back,
                            contentDescription = "Back",
                            tint = Color.White,
                            modifier = modifier
                                .size(36.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    keyboardController?.hide()
                                    inEditorMode = false
                                    isEditorReady = false
                                }
                        )

                        Text(
                            text = selectedScript,
                            fontSize = 20.sp,
                            color = Color.White,
                            fontFamily = ibmFont,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )

                        Icon(
                            imageVector = Terminal,
                            contentDescription = "Console",
                            tint = if (showConsole) Color.Red else Color.White,
                            modifier = Modifier
                                .size(28.dp)
                                .padding(end = 8.dp)
                        )
                    }
                }

                Crossfade(
                    targetState = showConsole,
                    animationSpec = tween(durationMillis = 300),
                    label = "consoleEditorCrossfade",
                    modifier = Modifier.weight(1f)
                ) { inConsole ->
                    if (inConsole) {
                        FridaConsoleView(modifier = modifier.fillMaxSize())
                    } else if (isEditorReady) {
                        ScriptsSoraCodeEditor(
                            initialText = selectedScriptContent.text,
                            onEditorCreated = { editor -> editorRef = editor },
                            modifier = modifier.fillMaxSize(),
                            context = activity
                        )
                    } else {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Loading...", fontSize = 16.sp, color = Color.White, fontFamily = ibmFont)
                        }
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(25.dp, 0.dp)) {
                    if (showConsole) {
                        Button(
                            onClick = {
                                stopFridaCore()
                                showConsole = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .padding(end = 5.dp)
                                .weight(1f)
                        ) {
                            Text("Stop", fontSize = 11.sp, color = Color.White, fontFamily = ibmFont)
                        }

                        Button(
                            onClick = { consoleLogs.clear() },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .weight(1f)
                        ) {
                            Text("Clear", fontSize = 11.sp, color = Color.Red, fontFamily = ibmFont)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (packageName != "") {
                                    injectFridaCore(
                                        activity,
                                        packageName,
                                        selectedScript
                                    )
                                    showConsole = true
                                } else {
                                    Toast.makeText(activity, "select an app package", Toast.LENGTH_SHORT).show()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .padding(end = 5.dp)
                                .weight(1f)
                        ) {
                            Text("Run", fontSize = 11.sp, color = Color.Red, fontFamily = ibmFont)
                        }

                        Button(
                            onClick = {
                                val toPersist = (editorRef?.text?.toString() ?: selectedScriptContent.text).replace("\r\n", "\n")
                                saveScript(activity, selectedScript, toPersist)
                                refreshScripts()
                                Toast.makeText(activity, "Script saved successfully", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .weight(1f)
                        ) {
                            Text("Save", fontSize = 11.sp, color = Color.Red, fontFamily = ibmFont)
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(25.dp, 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(35.dp)
                            .weight(1f)
                            .background(Color.White, shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp))
                            .clickable {
                                if (hasModule) {
                                    showPackageDialog = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (packageName.isEmpty()) "Click to select a package" else packageName,
                            fontSize = 11.sp,
                            color = Color.Red,
                            fontFamily = ibmFont,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (showPackageDialog) {
                ScriptsAppListDialog(
                    context = activity,
                    onDismiss = { showPackageDialog = false }
                )
            }
        }
    }
}

private object ScriptsTextMateInitializer {
    private var initialized = false
    private const val TAG = "TextMateInit"
    
    fun init(context: Context) {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            try {
                android.util.Log.d(TAG, "Starting TextMate initialization...")
                
                FileProviderRegistry.getInstance().addFileProvider(
                    AssetsFileResolver(context.assets)
                )
                android.util.Log.d(TAG, "FileProvider registered")
                
                val themeInputStream = context.assets.open("textmate/darcula.json")
                val themeSource = IThemeSource.fromInputStream(themeInputStream, "darcula.json", null)
                val themeModel = ThemeModel(themeSource, "darcula")
                ThemeRegistry.getInstance().loadTheme(themeModel)
                ThemeRegistry.getInstance().setTheme("darcula")
                android.util.Log.d(TAG, "Theme loaded and set")
                
                GrammarRegistry.getInstance().loadGrammars("textmate/languages.json")
                android.util.Log.d(TAG, "Grammars loaded")
                
                initialized = true
                android.util.Log.d(TAG, "TextMate initialization complete!")
            } catch (e: Exception) {
                android.util.Log.e(TAG, "TextMate init failed: ${e.message}", e)
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScriptsSoraCodeEditor(
    initialText: String,
    onEditorCreated: (io.github.rosemoe.sora.widget.CodeEditor) -> Unit,
    modifier: Modifier = Modifier,
    context: Context
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
    ) {
        AndroidView(
            factory = { ctx ->
                ScriptsTextMateInitializer.init(context)
                
                io.github.rosemoe.sora.widget.CodeEditor(ctx).apply {
                    try {
                        android.util.Log.d("SoraEditor", "Creating TextMate color scheme...")
                        val textMateScheme = TextMateColorScheme.create(ThemeRegistry.getInstance())
                        colorScheme = textMateScheme
                        android.util.Log.d("SoraEditor", "Color scheme applied")
                        
                        android.util.Log.d("SoraEditor", "Creating JavaScript language...")
                        val jsLanguage = TextMateLanguage.create("source.js", true)
                        setEditorLanguage(jsLanguage)
                        android.util.Log.d("SoraEditor", "JavaScript language applied successfully!")
                    } catch (e: Exception) {
                        android.util.Log.e("SoraEditor", "Error setting up TextMate: ${e.message}", e)
                        colorScheme = EditorColorScheme().apply {
                            setColor(EditorColorScheme.WHOLE_BACKGROUND, 0xFF1E1E1E.toInt())
                            setColor(EditorColorScheme.TEXT_NORMAL, 0xFFD4D4D4.toInt())
                            setColor(EditorColorScheme.LINE_NUMBER_BACKGROUND, 0xFF1E1E1E.toInt())
                            setColor(EditorColorScheme.LINE_NUMBER, 0xFF858585.toInt())
                            setColor(EditorColorScheme.CURRENT_LINE, 0xFF2D2D2D.toInt())
                        }
                    }
                    
                    setTextSize(14f)
                    isLineNumberEnabled = true
                    isWordwrap = false
                    setInterceptParentHorizontalScrollIfNeeded(true)
                    
                    setText(initialText)
                    onEditorCreated(this)
                }
            },
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun ScriptListItem(
    fileName: String,
    content: String,
    timestamp: String,
    enabled: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val previewLine = content.lineSequence().firstOrNull { it.isNotBlank() }?.trim().orEmpty()
    val primaryColor = if (enabled) Color.White else Color(0xFF858585)
    val secondaryColor = Color(0xFF858585)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF161616))
            .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0x33FF0000)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_code),
                contentDescription = null,
                tint = Color.Red,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fileName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    fontFamily = ibmFont,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                if (timestamp.isNotEmpty()) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = timestamp,
                        fontSize = 10.sp,
                        color = secondaryColor,
                        fontFamily = ibmFont,
                        maxLines = 1
                    )
                }
            }

            if (previewLine.isNotEmpty()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = previewLine,
                    fontSize = 12.sp,
                    color = secondaryColor,
                    fontFamily = ibmFont,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Icon(
            imageVector = Trash,
            contentDescription = "Delete",
            tint = secondaryColor,
            modifier = Modifier
                .size(20.dp)
                .clickable { onDelete() }
        )
    }
}

private fun scriptTimestamp(context: Context, fileName: String): String {
    val file = File(File(context.filesDir, "scripts"), fileName)
    val lastModified = file.lastModified()
    if (lastModified <= 0L) return ""
    return SimpleDateFormat("M/d/yy, h:mm a", Locale.getDefault()).format(Date(lastModified))
}


@SuppressLint("QueryPermissionsNeeded")
@Composable
fun ScriptsAppListDialog(context: Context, onDismiss: () -> Unit) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dec() * .65f

    val packageManager = context.packageManager
    val appsList = remember {
        val bannedPatterns = listOf(Regex("com\\.android\\..*"), Regex("com\\.google\\..*"))
        val bannedTerms = listOf(".auto_generated_")

        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { packageManager.getLaunchIntentForPackage(it.packageName) != null }
            .filterNot { app ->
                bannedPatterns.any { it.matches(app.packageName) } || bannedTerms.any { app.packageName.contains(it) }
            }
            .sortedBy { it.loadLabel(packageManager).toString() }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.height(screenHeight.dp),
        title = { Text("Select App") },
        text = {
            LazyColumn {
                items(appsList) { app ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                packageName = app.packageName
                                onDismiss()
                            }
                            .padding(8.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(app.loadIcon(packageManager)),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(text = app.loadLabel(packageManager).toString(), fontWeight = FontWeight.Bold)
                            Text(text = app.packageName, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun FridaConsoleView(modifier: Modifier = Modifier) {
    val listState = rememberLazyListState()

    LaunchedEffect(consoleLogs.size) {
        if (consoleLogs.isNotEmpty()) {
            listState.animateScrollToItem(consoleLogs.size - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color(0xFF0D0D0D))
            .border(1.dp, Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
    ) {
        if (consoleLogs.isEmpty()) {
            Text(
                text = "No logs yet. Run a script to see output.",
                color = Color(0xFF858585),
                fontSize = 13.sp,
                fontFamily = ibmFont,
                modifier = Modifier.align(Alignment.Center)
            )
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp)
            ) {
                items(consoleLogs.size) { index ->
                    val line = consoleLogs[index]
                    val color = when {
                        line.startsWith("[err]") -> Color(0xFFFF6B6B)
                        line.startsWith("[*]") -> Color(0xFF69DB7C)
                        else -> Color(0xFFD4D4D4)
                    }
                    Text(
                        text = line,
                        color = color,
                        fontSize = 12.sp,
                        fontFamily = ibmFont,
                        modifier = Modifier.padding(vertical = 1.dp)
                    )
                }
            }
        }
    }
}
