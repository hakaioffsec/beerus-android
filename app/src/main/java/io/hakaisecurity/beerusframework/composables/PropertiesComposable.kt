package io.hakaisecurity.beerusframework.composables

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.hakaisecurity.beerusframework.core.functions.Properties.Properties.removeProperty
import io.hakaisecurity.beerusframework.core.functions.Properties.Properties.addProperty
import io.hakaisecurity.beerusframework.core.functions.Properties.Properties.editProperty
import io.hakaisecurity.beerusframework.core.functions.Properties.Properties.listProperties
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.animationStart
import io.hakaisecurity.beerusframework.core.models.NavigationState.Companion.updateanimationStartState
import io.hakaisecurity.beerusframework.core.utils.CommandUtils.Companion.runSuCommand
import io.hakaisecurity.beerusframework.ui.theme.Edit
import io.hakaisecurity.beerusframework.ui.theme.Trash
import io.hakaisecurity.beerusframework.ui.theme.ibmFont

@Composable
fun PropertiesScreen(modifier: Modifier, context: Context) {
    var newProperyName by remember { mutableStateOf("") }
    var newProperyValue by remember { mutableStateOf("") }

    val propertiesState = remember { mutableStateOf(listProperties()) }

    val properties = propertiesState.value
    var showDeleteDialog by remember { mutableStateOf(false) }

    var showEditDialog by remember { mutableStateOf(false) }
    var propertyToEdit by remember { mutableStateOf("") }
    var propertyEditValue by remember { mutableStateOf("") }

    fun refreshProperties() {
        propertiesState.value = listProperties()
    }

    LaunchedEffect(Unit) {
        refreshProperties()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .padding(35.dp, 0.dp)
            .fillMaxSize()
    ) {
        Row () {
            Button(
                onClick = {
                    if(!animationStart) {
                        showDeleteDialog = true
                    } else {
                        updateanimationStartState(false)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = modifier
                    .padding(end = 5.dp)
                    .width(140.dp)
            ) {
                Text(
                    text = "Add Property",
                    fontSize = 11.sp,
                    color = Color.Red,
                    modifier = modifier.padding(0.dp, 3.dp),
                    fontFamily = ibmFont
                )
            }
            Button(
                onClick = {
                    runSuCommand("reboot") {}
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = modifier
                    .padding(end = 5.dp)
                    .width(140.dp)
            ) {
                Text(
                    text = "Reboot",
                    fontSize = 11.sp,
                    color = Color.Red,
                    modifier = modifier.padding(0.dp, 3.dp),
                    fontFamily = ibmFont
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            properties.forEach { property ->

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(0.dp, 5.dp)
                        .background(Color(0xFF151515), shape = RoundedCornerShape(4.dp))
                        .border(width = 2.dp, color = Color.White)
                ) {
                    Column {
                        Column(Modifier.padding(16.dp, 8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = property.name,
                                    textDecoration = TextDecoration.None,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = ibmFont,
                                    fontSize = 16.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = property.value,
                                    textDecoration = TextDecoration.None,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold,
                                    fontFamily = ibmFont,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(
                            modifier = Modifier
                                .height(2.dp)
                                .fillMaxWidth()
                                .background(Color.White)
                        )

                        Row(
                            modifier = modifier
                                .fillMaxWidth()
                                .padding(16.dp, 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    if (!animationStart) {
                                        showEditDialog = true
                                        propertyToEdit = property.name
                                        propertyEditValue = property.value
                                    } else {
                                        updateanimationStartState(false)
                                    }
                                },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Edit,
                                    contentDescription = "Edit",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(20.dp)
                                        .padding(end = 5.dp)
                                )
                                Text(
                                    text = "Edit",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = ibmFont,
                                    fontSize = 14.sp
                                )
                            }

                            Row(
                                modifier = Modifier.clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    if (!animationStart) {
                                        removeProperty(property.name)
                                        refreshProperties()
                                    } else {
                                        updateanimationStartState(false)
                                    }
                                },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Remove",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = ibmFont,
                                    fontSize = 14.sp
                                )
                                Icon(
                                    imageVector = Trash,
                                    contentDescription = "Trash",
                                    tint = Color.White,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(start = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Enter Property details") },
            text = {
                Column {
                    TextField(
                        value = newProperyName,
                        onValueChange = { newProperyName = it },
                        placeholder = { Text("Property name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextField(
                        value = newProperyValue,
                        onValueChange = {
                            newProperyValue = it
                        },
                        placeholder = { Text("Property value") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    addProperty(name = newProperyName, value = newProperyValue)
                    newProperyName = ""
                    newProperyValue = ""
                    refreshProperties()
                }) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Property") },
            text = {
                Column {
                    Text(
                        text = "Editing: $propertyToEdit",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    TextField(
                        value = propertyEditValue,
                        onValueChange = { propertyEditValue = it },
                        placeholder = { Text("New value") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    showEditDialog = false
                    editProperty(propertyToEdit, propertyEditValue)
                    refreshProperties()
                }) {
                    Text("Edit")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

}

