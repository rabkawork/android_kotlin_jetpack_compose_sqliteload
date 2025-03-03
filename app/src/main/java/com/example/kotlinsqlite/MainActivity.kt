package com.example.kotlinsqlite

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kotlinsqlite.dao.Item
import com.example.kotlinsqlite.model.ItemViewModel
import com.example.kotlinsqlite.utils.FileUtils
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    // Salin file ke lokasi yang dapat diakses oleh Room
    private fun copyDatabaseToInternalStorage(context: Context, uri: Uri): String? {
        val destFile = context.getDatabasePath("item_database.db")
        return try {
            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            destFile.absolutePath
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var databasePath by remember { mutableStateOf("") }
            var isDatabaseImported by remember { mutableStateOf(false) }

            val filePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri: Uri? ->
                uri?.let { selectedUri ->
                    val file = copyDatabaseToInternalStorage(this@MainActivity, selectedUri)
                    if (file != null) {
                        databasePath = file
                        isDatabaseImported = true
                    }
                }
            }
            val context = LocalContext.current

            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Research Inodigi - Library SQLITE") },
                        navigationIcon = {
                            IconButton(onClick = { /* TODO: Navigasi ke Home */ }) {
                                Icon(imageVector = Icons.Default.Home, contentDescription = "Home")
                            }
                        }
                    )
                }
            ) { paddingValues ->
                if (isDatabaseImported) {
                    val viewModel: ItemViewModel = viewModel()
                    ItemListScreen(viewModel, paddingValues)
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Upload Database",
                                    style = MaterialTheme.typography.headlineSmall
                                )

                                Button(
                                    onClick = {
                                        filePickerLauncher.launch(arrayOf("application/octet-stream"))
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.KeyboardArrowUp, contentDescription = "Upload")
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Import")
                                }

                                if (databasePath.isNotEmpty()) {
                                    OutlinedTextField(
                                        value = databasePath,
                                        onValueChange = {},
                                        label = { Text("Database Path") },
                                        readOnly = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditItemDialog(
    item: Item,
    onDismiss: () -> Unit,
    onUpdate: (Item) -> Unit
) {
    var newName by remember { mutableStateOf(item.name) }
    var newQuantity by remember { mutableStateOf(item.quantity.toString()) }

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Edit Item") },
        text = {
            Column {
                OutlinedTextField(
                    value = newName.toString(),
                    onValueChange = { newName = it },
                    label = { Text("Item Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = newQuantity,
                    onValueChange = { newQuantity = it },
                    label = { Text("Quantity") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val updatedQuantity = newQuantity.toIntOrNull()
                if (newName.isNotBlank() && updatedQuantity != null && updatedQuantity > 0) {
                    onUpdate(item.copy(name = newName, quantity = updatedQuantity))
                    onDismiss()
                }
            }) {
                Text("Update")
            }
        },
        dismissButton = {
            Button(onClick = { onDismiss() }) {
                Text("Cancel")
            }
        }
    )
}

fun exportDatabaseReal(context: Context, databasePath: String?) {
    Log.d("test = ",databasePath.toString())
    val sourcePath = databasePath ?: "item_database.db"

    Log.d("check",sourcePath.toString())

    val sourceFile = context.getDatabasePath("item_database.db")

    val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    val timestamp = dateFormat.format(Date())
    val destFile = File(
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
        "exported_$timestamp.db"
    )

    try {
        if (sourceFile.exists()) {
            sourceFile.copyTo(destFile, overwrite = true)
            Toast.makeText(context, "Database berhasil diekspor ke folder Downloads", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, "Database tidak ditemukan!", Toast.LENGTH_LONG).show()
        }
    } catch (e: IOException) {
        e.printStackTrace()
        Toast.makeText(context, "Gagal mengekspor database", Toast.LENGTH_LONG).show()
    }
}

@Composable
fun ItemListScreen(viewModel: ItemViewModel, paddingValues: PaddingValues = PaddingValues()) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Menggunakan collectAsState untuk mengamati perubahan data dari Flow
    val items by viewModel.allItems.collectAsState(initial = emptyList())

    var itemName by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    var selectedItem by remember { mutableStateOf<Item?>(null) }

    // Inisialisasi data saat layar pertama kali dibuka
    LaunchedEffect(key1 = Unit) {
        viewModel.loadItems()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(text = "Form Rokok", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = itemName,
                onValueChange = { itemName = it },
                label = { Text("Item Name") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = itemQuantity,
                onValueChange = { itemQuantity = it },
                label = { Text("Quantity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage.isNotEmpty()) {
                Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    val quantity = itemQuantity.toIntOrNull()
                    if (itemName.isBlank() || quantity == null || quantity <= 0) {
                        errorMessage = "Nama item tidak boleh kosong dan jumlah harus angka positif"
                    } else {
                        coroutineScope.launch {
                            viewModel.addItem(itemName, quantity)
                            itemName = ""
                            itemQuantity = ""
                            errorMessage = ""
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simpan")
            }

            Button(
                onClick = {
                    coroutineScope.launch {
                        FileUtils.exportDatabase(context, "item_database.db")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export Database")
            }
        }

        selectedItem?.let { item ->
            EditItemDialog(
                item = item,
                onDismiss = { selectedItem = null },
                onUpdate = { updatedItem ->
                    coroutineScope.launch {
                        viewModel.updateItem(updatedItem)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Daftar Item", style = MaterialTheme.typography.titleMedium)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(items) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = item.name, style = MaterialTheme.typography.bodyLarge)
                            Text(text = "Jumlah: ${item.quantity}", style = MaterialTheme.typography.bodyMedium)
                        }

                        Row {
                            Button(
                                onClick = { selectedItem = item },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.LightGray,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Edit")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.deleteItem(item)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
