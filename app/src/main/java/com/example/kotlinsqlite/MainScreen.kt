package com.example.kotlinsqlite


import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.kotlinsqlite.dao.Mahasiswa
import com.example.kotlinsqlite.dao.MahasiswaDatabase
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

@Composable
fun MainScreen() {
    val context = LocalContext.current
    val db = remember { MahasiswaDatabase.getDatabase(context).mahasiswaDao() }
    val scope = rememberCoroutineScope()
    var mahasiswaList by remember { mutableStateOf<List<Mahasiswa>>(emptyList()) }
    var nama by remember { mutableStateOf("") }
    var nim by remember { mutableStateOf("") }

    LaunchedEffect(true) {
        mahasiswaList = db.getAll()
    }

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(value = nama, onValueChange = { nama = it }, label = { Text("Nama") })
        TextField(value = nim, onValueChange = { nim = it }, label = { Text("NIM") })

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            scope.launch {
                db.insert(Mahasiswa(nama = nama, nim = nim))
                mahasiswaList = db.getAll() // Refresh data
            }
        }) {
            Text("Tambah Mahasiswa")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(onClick = {
            //exportDatabase(context)
        }) {
            Text("Export Database")
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn {
            items(mahasiswaList) { mahasiswa ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "${mahasiswa.nama} - ${mahasiswa.nim}")
                    Button(onClick = {
                        scope.launch {
                            db.delete(mahasiswa)
                            mahasiswaList = db.getAll()
                        }
                    }) {
                        Text("Hapus")
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
fun exportDatabase(context: Context) {
    val dbFile = context.getDatabasePath("mydatabase.db")
    if (!dbFile.exists()) {
        println("Database file not found: ${dbFile.absolutePath}")
        return
    }

    val resolver = context.contentResolver
    val contentValues = android.content.ContentValues().apply {
        put(android.provider.MediaStore.Downloads.DISPLAY_NAME, "mahasiswa_export.db")
        put(android.provider.MediaStore.Downloads.MIME_TYPE, "application/x-sqlite3")
        put(android.provider.MediaStore.Downloads.RELATIVE_PATH, "Download/")
    }

    val uri = resolver.insert(android.provider.MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

    if (uri == null) {
        println("Failed to create URI for export")
        return
    }

    println("Export URI created: $uri")

    try {
        resolver.openOutputStream(uri)?.use { outputStream ->
            FileInputStream(dbFile).use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        println("Database exported successfully")
    } catch (e: IOException) {
        e.printStackTrace()
        println("Failed to export database: ${e.message}")
    }
}


