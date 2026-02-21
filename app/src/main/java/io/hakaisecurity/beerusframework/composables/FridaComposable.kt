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
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import io.github.rosemoe.sora.langs.textmate.TextMateColorScheme
import io.github.rosemoe.sora.langs.textmate.TextMateLanguage
import io.github.rosemoe.sora.langs.textmate.registry.FileProviderRegistry
import io.github.rosemoe.sora.langs.textmate.registry.GrammarRegistry
import io.github.rosemoe.sora.langs.textmate.registry.ThemeRegistry
import io.github.rosemoe.sora.langs.textmate.registry.model.ThemeModel
import io.github.rosemoe.sora.langs.textmate.registry.provider.AssetsFileResolver
import io.github.rosemoe.sora.widget.schemes.EditorColorScheme
import io.hakaisecurity.beerusframework.R
import io.hakaisecurity.beerusframework.core.functions.frida.AutoInject.Companion.deleteScript
import io.hakaisecurity.beerusframework.core.functions.frida.AutoInject.Companion.getFileNameFromUri
import io.hakaisecurity.beerusframework.core.functions.frida.AutoInject.Companion.getScriptsContent
import io.hakaisecurity.beerusframework.core.functions.frida.AutoInject.Companion.injectFridaCore
import io.hakaisecurity.beerusframework.core.functions.frida.AutoInject.Companion.saveScript
import io.hakaisecurity.beerusframework.core.functions.frida.FridaSetup.Companion.startFridaModule
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.currentFridaVersionDownloaded
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.currentFridaVersionFromList
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.fridaRunningState
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.fridaVersions
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.inEditorMode
import io.hakaisecurity.beerusframework.core.models.FridaState.Companion.packageName
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.animationStart
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.updateanimationStartState
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.confirmRootModuleInstallerDialog
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.hasModule
import io.hakaisecurity.beerusframework.core.models.StartModel.Companion.hasRoot
import io.hakaisecurity.beerusframework.ui.theme.Add
import io.hakaisecurity.beerusframework.ui.theme.Arrow_back
import io.hakaisecurity.beerusframework.ui.theme.Trash
import io.hakaisecurity.beerusframework.ui.theme.Upload
import io.hakaisecurity.beerusframework.ui.theme.ibmFont
import org.eclipse.tm4e.core.registry.IThemeSource
import java.io.File

@ExperimentalLayoutApi
@Composable
fun FridaScreen(modifier: Modifier, activity: Activity) {
    var expanded by remember { mutableStateOf(false) }
    var showAddVersionDialog by remember { mutableStateOf(false) }
    var newVersionText by remember { mutableStateOf("") }
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dec() * .30f
    val keyboardController = LocalSoftwareKeyboardController.current

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

    LaunchedEffect(selectedScript, inEditorMode) {
        if (selectedScript.isNotEmpty() && inEditorMode) {
            kotlinx.coroutines.delay(300)
            isEditorReady = true
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.fillMaxSize().padding(60.dp, 0.dp)
        ) {
            Spacer(modifier = modifier.height(screenHeight.dp))

            Image(
                painter = painterResource(id = R.drawable.fridalogo),
                contentDescription = "Frida Logo"
            )

            Spacer(modifier = modifier.height(10.dp))

            Row {
                Text(
                    text = "Version: ",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = ibmFont
                )

                Text(
                    text = currentFridaVersionDownloaded,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = ibmFont
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Status: ",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    fontFamily = ibmFont
                )

                Text(
                    text = when (fridaRunningState) {
                        "start" -> "Stopped"
                        "stop" -> "Running"
                        else -> "Downloading"
                    },
                    color = Color.White,
                    fontSize = 18.sp,
                    fontFamily = ibmFont
                )

                androidx.compose.foundation.Canvas(
                    modifier = modifier
                        .size(25.dp)
                        .padding(start = 5.dp)
                ) {
                    drawCircle(
                        color = when (fridaRunningState) {
                            "start" -> Color.Red
                            "stop" -> Color.Green
                            else -> Color.Yellow
                        }
                    )
                }
            }

            Spacer(modifier = modifier.height(20.dp))

            Row(modifier = modifier.padding(bottom = 5.dp)) {
                Button(
                    onClick = {
                        if (!inEditorMode) {
                            if (!animationStart) {
                                expanded = true
                            } else {
                                updateanimationStartState(false)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    modifier = modifier
                        .padding(end = 5.dp)
                        .width(140.dp)
                ) {
                    Text(
                        text = "Versions",
                        fontSize = 11.sp,
                        color = Color.Red,
                        modifier = modifier.padding(0.dp, 3.dp),
                        fontFamily = ibmFont
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Add version manually", fontFamily = ibmFont) },
                        onClick = {
                            expanded = false
                            showAddVersionDialog = true
                        }
                    )
                    if (fridaVersions.isEmpty()) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "No versions available",
                                    fontFamily = ibmFont
                                )
                            },
                            onClick = { expanded = false }
                        )
                    } else {
                        fridaVersions.forEach { item ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = item,
                                        fontFamily = ibmFont,
                                        color = if (item == currentFridaVersionFromList) Color.White else Color.Black
                                    )
                                },
                                onClick = {
                                    currentFridaVersionFromList = item
                                    expanded = false
                                },
                                modifier = Modifier.background(color = if (item == currentFridaVersionFromList) Color.Red else Color.White)
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        if (!inEditorMode) {
                            when (fridaRunningState) {
                                "processing" -> {}
                                else -> {
                                    currentFridaVersionFromList?.let {
                                        if (it == "None") {
                                            startFridaModule(
                                                activity,
                                                fridaVersions[0],
                                                fridaRunningState
                                            )
                                        } else {
                                            startFridaModule(
                                                activity,
                                                it,
                                                fridaRunningState
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    },
                    colors = when (fridaRunningState) {
                        "processing" -> ButtonDefaults.buttonColors(containerColor = Color(0xFFBDBDBD))
                        else -> ButtonDefaults.buttonColors(containerColor = Color.White)
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                    modifier = modifier
                        .padding(start = 5.dp)
                        .width(140.dp)
                ) {
                    Text(
                        text = when (fridaRunningState) {
                            "stop" -> "Stop Frida"
                            else -> "Start Frida"
                        },
                        fontSize = 11.sp,
                        color = when (fridaRunningState) {
                            "processing" -> Color(0xFFA10000)
                            else -> Color.Red
                        },
                        modifier = modifier.padding(1.dp, 3.dp),
                        fontFamily = ibmFont
                    )
                }
            }

            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(Color.White)
            )

            Row(modifier = Modifier.padding(top = 5.dp)) {
                AddScriptButton(
                    activity,
                    inEditorMode,
                    newScriptName = newScriptName,
                    onScriptNameChange = { newScriptName = it },
                    refreshScript = { refreshScripts() },
                )

                Spacer(modifier = Modifier.width(10.dp))
                UploadScriptButton(activity, inEditorMode, refreshScript = { refreshScripts() })
                Spacer(modifier = Modifier.width(10.dp))
            }

            val listState = rememberLazyListState()

            LaunchedEffect(scripts.size) {
                if (scripts.isNotEmpty()) {
                    listState.animateScrollToItem(scripts.size - 1)
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(scripts.entries.toList()) { (fileName, content) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
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
                                }
                                .padding(0.dp, 5.dp, 0.dp, 10.dp)
                        ) {
                            Text(
                                text = fileName,
                                fontSize = 16.sp,
                                color = if (!hasModule) Color(0xFF858585) else Color.White,
                                fontFamily = ibmFont
                            )
                        }

                        Icon(
                            imageVector = Trash,
                            contentDescription = "Delete",
                            tint = if (!hasModule) Color(0xFF858585) else Color.White,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(22.dp)
                                .clickable {
                                    if (!inEditorMode && hasModule) {
                                        deleteScript(activity, fileName)
                                        refreshScripts()
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
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Icon(
                            imageVector = Arrow_back,
                            contentDescription = "Icon",
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
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .wrapContentWidth(Alignment.CenterHorizontally)
                                .padding(end = 10.dp)
                        )
                    }
                }

                if (isEditorReady) {
                    SoraCodeEditor(
                        initialText = selectedScriptContent.text,
                        onEditorCreated = { editor -> editorRef = editor },
                        modifier = modifier.weight(1f),
                        context = activity
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Loading...", fontSize = 16.sp, color = Color.White, fontFamily = ibmFont)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(25.dp, 0.dp)) {
                    Button(
                        onClick = {
                            if (packageName != "") {
                                injectFridaCore(
                                    activity,
                                    packageName,
                                    selectedScript
                                )
                            } else {
                                Toast.makeText(activity, "select an app package", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .padding(end = 5.dp)
                            .weight(1f)
                    ) {
                        Text("Run", fontSize = 11.sp, color = Color.Red, fontFamily = ibmFont)
                    }

                    Button(
                        onClick = {
                            // normalize only when persisting
                            val toPersist = (editorRef?.text?.toString() ?: selectedScriptContent.text).replace("\r\n", "\n")
                            saveScript(activity, selectedScript, toPersist)
                            refreshScripts()
                            Toast.makeText(activity, "Script saved successfully", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .weight(1f)
                    ) {
                        Text("Save", fontSize = 11.sp, color = Color.Red, fontFamily = ibmFont)
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
                AppListDialog(
                    context = activity,
                    onDismiss = { showPackageDialog = false }
                )
            }
        }
    }

    if (showAddVersionDialog) {
        AlertDialog(
            onDismissRequest = { showAddVersionDialog = false },
            title = { Text("Add Version") },
            text = {
                TextField(
                    value = newVersionText,
                    onValueChange = { newVersionText = it },
                    placeholder = { Text("e.g., 16.1.2") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newVersionText.isNotBlank()) {
                        fridaVersions.add(newVersionText.trim())
                        currentFridaVersionFromList = newVersionText.trim()
                        showAddVersionDialog = false
                        newVersionText = ""
                    }
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddVersionDialog = false
                    newVersionText = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }
}


/**
 * Singleton to manage TextMate initialization.
 */
private object TextMateInitializer {
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

/**
 * High-performance code editor using Sora Editor with TextMate syntax highlighting.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SoraCodeEditor(
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
                TextMateInitializer.init(context)
                
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
fun AddScriptButton(
    context: Context,
    inEditorMode: Boolean,
    newScriptName: String,
    onScriptNameChange: (String) -> Unit,
    refreshScript: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var noModule by remember { mutableStateOf(false) }
    var noRoot by remember { mutableStateOf(false) }

    Icon(
        imageVector = Add,
        contentDescription = "Add",
        tint = if (!hasModule) Color(0xFF858585) else Color.White,
        modifier = Modifier
            .padding(end = 8.dp)
            .size(32.dp)
            .clickable {
                if (!inEditorMode && hasModule) {
                    showDialog = true
                }

                if (!hasModule) {
                    noModule = true
                }

                if (!hasRoot) {
                    noRoot = true
                }
            }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Enter script name") },
            text = {
                TextField(
                    value = newScriptName,
                    onValueChange = onScriptNameChange,
                    placeholder = { Text("example") },
                    singleLine = true
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    saveScript(context, "${newScriptName.replace(".js", "")}.js", "// Write your code here")
                    refreshScript()
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Cancel") }
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
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, "https://topjohnwu.github.io/Magisk/".toUri())
                    )
                }) { Text("Magisk") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        noRoot = false
                        context.startActivity(
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
                    confirmRootModuleInstallerDialog(context)
                }) { Text("Install") }
            },
            dismissButton = { TextButton(onClick = { noModule = false }) { Text("Do After", fontFamily = ibmFont) } }
        )
    }
}

@Composable
fun UploadScriptButton(context: Context, inEditorMode: Boolean, refreshScript: () -> Unit) {
    var noModule by remember { mutableStateOf(false) }
    var noRoot by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            val fileName = getFileNameFromUri(context, uri)
            val scriptsDir = File(context.filesDir, "scripts")

            val outputFile = File(scriptsDir, fileName)

            val inputStream = context.contentResolver.openInputStream(uri)
            inputStream.use { input ->
                outputFile.outputStream().use { output ->
                    input?.copyTo(output)
                }
            }

            refreshScript()
        }
    }

    Icon(
        imageVector = Upload,
        contentDescription = "Upload",
        tint = if (!hasModule) Color(0xFF858585) else Color.White,
        modifier = Modifier
            .padding(end = 8.dp)
            .size(32.dp)
            .clickable {
                if (!inEditorMode && hasModule) {
                    launcher.launch(arrayOf("application/javascript"))
                }

                if (!hasModule) {
                    noModule = true
                }

                if (!hasRoot) {
                    noRoot = true
                }
            }
    )

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
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, "https://topjohnwu.github.io/Magisk/".toUri())
                    )
                }) { Text("Magisk") }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        noRoot = false
                        context.startActivity(
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
                    confirmRootModuleInstallerDialog(context)
                }) { Text("Install") }
            },
            dismissButton = { TextButton(onClick = { noModule = false }) { Text("Do After", fontFamily = ibmFont) } }
        )
    }
}

@SuppressLint("QueryPermissionsNeeded")
@Composable
fun AppListDialog(context: Context, onDismiss: () -> Unit) {
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