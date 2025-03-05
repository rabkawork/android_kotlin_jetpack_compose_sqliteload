package com.example.kotlinsqlite

import android.app.DownloadManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData

import androidx.room.*
import java.io.FileOutputStream
import java.io.IOException
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext



import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Share
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import com.example.kotlinsqlite.restapi.ApiService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileInputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream


@Entity(tableName = "master_produk")
data class Produk(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val nama: String,
    val quantity: Int,
)

@Dao
interface ProdukDao {
    @Query("SELECT * FROM master_produk")
    fun getAllProduk(): LiveData<List<Produk>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduk(produk: Produk)

    @Update
    suspend fun updateProduk(produk: Produk)

    @Delete
    suspend fun deleteProduk(produk: Produk)
}



@Database(entities = [Produk::class], version = 1, exportSchema = false)
abstract class ProdukDatabase : RoomDatabase() {

    abstract fun produkDao(): ProdukDao

    companion object {
        @Volatile
        private var INSTANCE: ProdukDatabase? = null

        fun getDatabase(context: Context): ProdukDatabase {
            return INSTANCE ?: synchronized(this) {
                val dbPath = context.getDatabasePath("produk.db").absolutePath
                Log.d("ProdukDatabase", "Mengakses database di path: $dbPath") // ✅ Log Path Database
                Room.databaseBuilder(
                    context.applicationContext,
                    ProdukDatabase::class.java,
                    dbPath
                ).allowMainThreadQueries()
                    .fallbackToDestructiveMigration() // Menjaga agar database tetap bisa digunakan tanpa konflik skema

                    .build().also {
                    INSTANCE = it
                    Log.d("ProdukDatabase", "Database berhasil di-load") // ✅ Log setelah load DB
                }
            }
        }

        fun replaceDatabase(context: Context, uri: Uri) {
            val dbFile = context.getDatabasePath("produk.db")

            val shmFile = File(dbFile.absolutePath + "-shm")
            val walFile = File(dbFile.absolutePath + "-wal")



            try {

                // Hapus database lama jika ada
                if (dbFile.exists()) {
                    dbFile.delete()
                    Log.d("ProdukDatabase", "Database lama dihapus")
                }
                if (shmFile.exists()) {
                    shmFile.delete()
                    Log.d("ProdukDatabase", "File shm dihapus")
                }
                if (walFile.exists()) {
                    walFile.delete()
                    Log.d("ProdukDatabase", "File wal dihapus")
                }


                context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    FileOutputStream(dbFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                Log.d("ProdukDatabase", "Database berhasil diganti dengan file baru") // ✅ Log sukses copy DB

                // Tambahkan log path database
                Log.d("ProdukDatabase", "Database diganti, path baru: ${dbFile.absolutePath}")



                // Pastikan file ada setelah diganti
                if (dbFile.exists()) {
                    Log.d("ProdukDatabase", "File database baru ditemukan: ${dbFile.absolutePath}") // ✅ Log file ada
                } else {
                    Log.e("ProdukDatabase", "File database tidak ditemukan setelah replace!") // ❌ Error jika file tidak ada
                }

                // Reverse INSTANCE menjadi null -> karena menggunakan database baru

                Log.d("moveDatabaseFile", "Instance value berhasil direverse, instance siap melakukan operasi SQL")

                INSTANCE = null

                // Tampilkan pesan sukses
                Toast.makeText(context, "Database berhasil diperbarui", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Log.e("ProdukDatabase", "Gagal mengganti database: ${e.message}") // ❌ Log error
                e.printStackTrace()
            }
        }

        fun closeDatabase() {
            INSTANCE?.close()
            INSTANCE = null
            Log.d("ProdukDatabase", "Database ditutup dan INSTANCE di-reset")
        }

        fun exportDatabase(context: Context) {
            val timestamp = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault()).format(Date())
            val downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val backupDir = File(downloadDir, timestamp)

            if (!backupDir.exists()) {
                backupDir.mkdirs()
            }

            val dbPath = context.getDatabasePath("produk.db").absolutePath
            val dbFiles = listOf(
                "produk.db",
                "produk.db-shm",
                "produk.db-wal"
            )
            val copiedFiles = mutableListOf<File>()

            dbFiles.forEach { fileName ->
                val sourceFile = File(context.getDatabasePath("produk.db").parent, fileName)
                val destFile = File(backupDir, fileName)

                if (sourceFile.exists()) {
                    try {
                        FileInputStream(sourceFile).use { input ->
                            FileOutputStream(destFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        copiedFiles.add(destFile)


                        Log.d("ExportDatabase", "Berhasil menyalin $fileName ke ${destFile.absolutePath}")
                        Toast.makeText(context, "Berhasil menyalin $fileName ke ${destFile.absolutePath}", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {


                        Log.e("ExportDatabase", "Gagal menyalin $fileName: ${e.message}")

                        Toast.makeText(context, "Gagal menyalin $fileName: ${e.message}", Toast.LENGTH_SHORT).show()

                    }
                } else {
                    Log.w("ExportDatabase", "File $fileName tidak ditemukan, dilewati")
                    Toast.makeText(context, "File $fileName tidak ditemukan, dilewati", Toast.LENGTH_SHORT).show()

                }
            }

            if (copiedFiles.isNotEmpty()) {
                val zipFile = File(downloadDir, "produk_backup_$timestamp.zip")
                if (zipFiles(zipFile, copiedFiles)) {
                    uploadZipToServer(context, zipFile)
                    Log.d("ExportDatabase", "File ZIP berhasil dibuat: ${zipFile.absolutePath}")
                    Toast.makeText(context, "Backup berhasil! ZIP: ${zipFile.absolutePath}", Toast.LENGTH_SHORT).show()

                    // 🔄 Kirim ZIP ke server
                } else {
                    Log.e("ExportDatabase", "Gagal membuat file ZIP")
                    Toast.makeText(context, "Gagal membuat file ZIP", Toast.LENGTH_SHORT).show()
                }
            }
        }

        fun moveDownloadedDatabaseFile(context: Context, downloadedUri: Uri, fileName: String) {
            val dbFile = context.getDatabasePath("produk.db")
            val shmFile = File(dbFile.absolutePath + "-shm")
            val walFile = File(dbFile.absolutePath + "-wal")

            try {
                // Hapus database lama jika ada
                if (dbFile.exists()) dbFile.delete()
                if (shmFile.exists()) shmFile.delete()
                if (walFile.exists()) walFile.delete()

                // Salin database baru ke lokasi database aplikasi
                context.contentResolver.openInputStream(downloadedUri)?.use { inputStream ->
                    FileOutputStream(dbFile).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }

                Log.d("moveDatabaseFile", "Database berhasil dipindahkan ke: ${dbFile.absolutePath}")

                // Reset instance agar Room membaca ulang database yang baru
                ProdukDatabase.closeDatabase()

                Toast.makeText(context, "Database berhasil diperbarui", Toast.LENGTH_SHORT).show()
            } catch (e: IOException) {
                Log.e("moveDatabaseFile", "Gagal mengganti database: ${e.message}")
                Toast.makeText(context, "Gagal mengganti database", Toast.LENGTH_SHORT).show()
            }
        }



        fun refreshDatabase(context: Context) {

            CoroutineScope(Dispatchers.IO).launch {

                // Pastikan instance Room ditutup terlebih dahulu
                ProdukDatabase.closeDatabase()

                // Buat ulang instance Room agar membaca database baru
                ProdukDatabase.getDatabase(context)
                val newDb = ProdukDatabase.getDatabase(context)

                withContext(Dispatchers.Main) {
                    if (newDb != null) {
                        Log.d("refreshDatabase", "Database baru berhasil di-load.")

                        // Restart Activity hanya setelah database selesai dibuat
                        val intent = (context as? ComponentActivity)?.intent
                        intent?.let {
                            context.finish()
                            context.startActivity(it)
                        }

                        Log.d("refreshDatabase", "Database telah direfresh dan siap digunakan kembali.")
                        Toast.makeText(context, "Database berhasil diperbarui dan direfresh", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("refreshDatabase", "Gagal memuat ulang database!")
                        Toast.makeText(context, "Terjadi kesalahan saat memuat database", Toast.LENGTH_SHORT).show()
                    }
                }
            }


        }

    }
}

fun downloadAndReplaceDatabase(context: Context, fileUrl: String, fileName: String) {
    val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
    val request = DownloadManager.Request(Uri.parse(fileUrl))
        .setTitle("produk")
        .setDescription("Downloading $fileName")
        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        .setAllowedOverMetered(true)
        .setAllowedOverRoaming(true)
        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)

    try {
        val downloadId = downloadManager.enqueue(request)
        CoroutineScope(Dispatchers.IO).launch {
            var downloading = true
            val query = DownloadManager.Query().setFilterById(downloadId)

            while (downloading) {
                downloadManager.query(query).use { cursor ->
                    if (cursor.moveToFirst()) {
                        when (cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))) {
                            DownloadManager.STATUS_SUCCESSFUL -> {
                                downloading = false
                                val uriString = cursor.getString(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_LOCAL_URI))
                                uriString?.let {
                                    withContext(Dispatchers.Main) {
                                        ProdukDatabase.moveDownloadedDatabaseFile(context, Uri.parse(it), fileName)
                                        ProdukDatabase.refreshDatabase(context) // 🔄 Paksa reload database setelah mengganti
                                    }
                                }
                            }
                            DownloadManager.STATUS_FAILED -> {
                                downloading = false
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Download gagal", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    }
                }
                delay(1000) // Delay agar tidak blocking
            }
        }
    } catch (e: Exception) {
        Log.e("Download", "Error: ${e.message}", e)
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    }
}


fun uploadZipToServer(context: Context, zipFile: File) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val requestBody = zipFile.asRequestBody("application/zip".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", zipFile.name, requestBody)

            val retrofit = Retrofit.Builder()
                .baseUrl("http://192.168.2.116:3000/") // Ganti dengan URL server kamu
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(ApiService::class.java)
            val response = apiService.uploadFile(filePart)

            Log.d("response",response.toString())

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Upload berhasil!", Toast.LENGTH_SHORT).show()
                    Log.d("UploadZIP", "Upload sukses: ${response.body()?.string()}")
                } else {
                    Toast.makeText(context, "Upload gagal!", Toast.LENGTH_SHORT).show()
                    Log.e("UploadZIP", "Upload gagal: ${response.errorBody()?.string()}")
                }
            }
        } catch (e: Exception) {
            Log.e("UploadZIP", "Error saat upload: ${e.message}")
        }
    }
}


fun zipFiles(zipFile: File, files: List<File>): Boolean {
    return try {
        ZipOutputStream(FileOutputStream(zipFile)).use { zipOut ->
            files.forEach { file ->
                FileInputStream(file).use { input ->
                    val entry = ZipEntry(file.name)
                    zipOut.putNextEntry(entry)
                    input.copyTo(zipOut)
                    zipOut.closeEntry()
                }
            }
        }
        true
    } catch (e: Exception) {
        Log.e("ZipFiles", "Gagal membuat ZIP: ${e.message}")
        false
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
//fun ProdukScreen(db: ProdukDatabase, refreshDb: () -> Unit) {

fun ProdukScreen(db: ProdukDatabase, refreshDb: (Uri?) -> Unit) {

    val produkDao = db.produkDao()
    val produkList by produkDao.getAllProduk().observeAsState(initial = emptyList())
    val context = LocalContext.current

    val scope = rememberCoroutineScope()

    var showDialog by remember { mutableStateOf(false) }
    var namaProduk by remember { mutableStateOf("") }
    var quantityProduk by remember { mutableStateOf("") }
    var editingProduk: Produk? by remember { mutableStateOf(null) }


    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            Log.d("checkimport",it.toString())
            ProdukDatabase.replaceDatabase(context, it) // Gunakan context dari LocalContext
            refreshDb(it) // Paksa reload database
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Produk") },
                actions = {


                    Row(
                        modifier = Modifier.padding(end = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.LightGray)
                    ) {
                        IconButton(onClick = { filePickerLauncher.launch("*/*") }) {
                            Icon(
                                imageVector = Icons.Default.AddCircle,
                                contentDescription = "Import SQLITE",
                                tint = Color.White
                            )
                        }
                    }

                        Spacer(modifier = Modifier.width(8.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Green)
                        ) {
                            IconButton(onClick = {
                                val fileUrl = "http://192.168.2.116:3000/download/produk.db"
//                                val fileUrl = "http://10.0.2.2:3000/download/produk.db"
                                val fileName = "produk.db"
                                downloadAndReplaceDatabase(context, fileUrl, fileName)
                            }) {
                                Icon(
                                    imageVector = Icons.Default.MailOutline,
                                    contentDescription = "Download",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            )
        }, floatingActionButton = {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomEnd // Menempatkan FAB di pojok kanan bawah
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp), // Jarak antar FAB
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier.padding(end = 16.dp, bottom = 16.dp)
                ) {
                    FloatingActionButton(
                        onClick = { showDialog = true },
                        containerColor = Color(0xFF00796B), // Warna hijau elegan
                        contentColor = Color.White,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Tambah Produk",
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    FloatingActionButton(
                        onClick = { /* Tambahkan logika ekspor di sini */
                            ProdukDatabase.exportDatabase(context)
                        },
                        containerColor = Color(0xFF0288D1), // Warna biru untuk eksport
                        contentColor = Color.White,
                        shape = CircleShape,
                        elevation = FloatingActionButtonDefaults.elevation(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share, // Ikon ekspor
                            contentDescription = "Export Data",
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }

        }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize() // Pastikan mengisi layar
                .padding(padding) // Gunakan padding dari Scaffold agar tidak tertutup TopBar
                .padding(bottom = 80.dp) // Tambah margin bawah untuk FAB
        ) {
            items(produkList) { produk ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            namaProduk = produk.nama
                            quantityProduk = produk.quantity.toString()
                            editingProduk = produk
                            showDialog = true
                        }
                        .padding(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(6.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = produk.nama,
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.Black
                            )
                            Text(
                                text = "Qty: ${produk.quantity}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }

                        IconButton(
                            onClick = {
                                scope.launch {
                                    produkDao.deleteProduk(produk)
                                    Toast.makeText(context, "Produk dihapus", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Hapus",
                                tint = Color.Red,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    // Dialog untuk Tambah/Edit Produk
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = if (editingProduk == null) "Tambah Produk" else "Edit Produk",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = namaProduk,
                        onValueChange = { namaProduk = it },
                        label = { Text("Nama Produk") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = quantityProduk,
                        onValueChange = { quantityProduk = it },
                        label = { Text("Quantity") },
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            val produk = Produk(
                                id = editingProduk?.id ?: 0,
                                nama = namaProduk,
                                quantity = quantityProduk.toIntOrNull() ?: 0
                            )
                            if (editingProduk == null) {
                                produkDao.insertProduk(produk)
                                Toast.makeText(context, "Produk ditambahkan", Toast.LENGTH_SHORT).show()
                            } else {
                                produkDao.updateProduk(produk)
                                Toast.makeText(context, "Produk diperbarui", Toast.LENGTH_SHORT).show()
                            }
                            showDialog = false
                            namaProduk = ""
                            quantityProduk = ""
                            editingProduk = null
                        }
                    },
                    modifier = Modifier.padding(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00796B))
                ) {
                    Text(
                        text = if (editingProduk == null) "Tambah" else "Simpan",
                        color = Color.White
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Batal", color = Color.Red)
                }
            }
        )
    }
}

class BackupActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var db by remember { mutableStateOf(ProdukDatabase.getDatabase(this)) }
            val context = this@BackupActivity // ✅ Gunakan context yang benar



            ProdukScreen(db, refreshDb = { uri ->
                uri?.let {
                    Log.d("BackupActivity", "Memulai proses penggantian database...")

                    // 1️⃣ Tutup database lama
                    ProdukDatabase.closeDatabase()

                    // 2️⃣ Ganti database dengan yang baru
                    ProdukDatabase.replaceDatabase(context, it)
                    Log.d("BackupActivity", "Database telah diganti, memuat ulang...")

                    // 3️⃣ Paksa Room membaca ulang database yang baru
                    db = ProdukDatabase.getDatabase(context)
                    Log.d("BackupActivity", "Database di-refresh setelah upload, data siap digunakan!")

                    // 4️⃣ Tampilkan pesan sukses
                    Toast.makeText(context, "Database berhasil diperbarui!", Toast.LENGTH_SHORT).show()
                } ?: Log.e("BackupActivity", "URI database yang diunggah null!") // Handle jika URI null
            })
        }
    }
}




