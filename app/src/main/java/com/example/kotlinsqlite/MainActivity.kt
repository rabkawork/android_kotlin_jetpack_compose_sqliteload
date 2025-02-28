/***
 * @author: Ahadian Akbar
 * @date : 28 Feb 2024
 */

package com.example.kotlinsqlite


import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Environment
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.kotlinsqlite.dao.Item
//import com.example.kotlinsqlite.dao.exportDatabase
import com.example.kotlinsqlite.model.ItemViewModel
import com.example.kotlinsqlite.model.ItemViewModelFactory
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

//    private fun copyDatabaseToAppStorage(context: Context, uri: Uri): File? {
//        val file = File(context.filesDir, "uploaded_database.db")
//        return try {
//            context.contentResolver.openInputStream(uri)?.use { input ->
//                file.outputStream().use { output ->
//                    input.copyTo(output)
//                }
//            }
//            file
//        } catch (e: IOException) {
//            e.printStackTrace()
//            null
//        }
//    }

    // Salin file ke lokasi yang dapat diakses oleh Room
    private fun copyDatabaseToInternalStorage(context: Context, uri: Uri): String? {
        val destFile = context.getDatabasePath("uploaded_database.db")
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
                    ItemListScreen(context = applicationContext, databasePath)
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
//                                    onClick = { filePickerLauncher.launch("application/x-sqlite3") },
                                    onClick = {
                                        filePickerLauncher.launch(arrayOf("application/octet-stream")) // Format SQLite sering terdeteksi sebagai ini
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
                    value = newName,
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
    val sourcePath = databasePath ?: "item_database.db"
    val sourceFile = File(context.getDatabasePath(sourcePath).absolutePath)

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
fun ItemListScreen(context: Context, databasePath: String?) {
//    val viewModel: ItemViewModel = viewModel(factory = ItemViewModelFactory(context))

    val viewModel: ItemViewModel = viewModel(factory = ItemViewModelFactory(context, databasePath))

    val items by viewModel.items.collectAsState()

    var itemName by remember { mutableStateOf("") }
    var itemQuantity by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp) // Beri jarak antar elemen
    ) {


        Spacer(modifier = Modifier.height(59.dp)) // Beri jarak sebelum daftar item

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Membantu form tetap terlihat
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
                        viewModel.addItem(itemName, quantity)
                        itemName = ""
                        itemQuantity = ""
                        errorMessage = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Simpan")
            }

            Button(
                onClick = {
                    exportDatabaseReal(context, databasePath)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Export database")
            }



        }
        var selectedItem by remember { mutableStateOf<Item?>(null) }

        selectedItem?.let { item ->
            EditItemDialog(
                item = item,
                onDismiss = { selectedItem = null },
                onUpdate = { updatedItem -> viewModel.updateItem(updatedItem) }
            )
        }


        Spacer(modifier = Modifier.height(8.dp)) // Kurangi jarak sebelum daftar item
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
                            Button(onClick = { selectedItem = item },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.LightGray, // Warna latar belakang merah
                                    contentColor = Color.White  // Warna teks putih agar kontras
                                )
                                ) {
                                Text("Edit")
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Button(onClick = { viewModel.deleteItem(item) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red, // Warna latar belakang merah
                                    contentColor = Color.White  // Warna teks putih agar kontras
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



