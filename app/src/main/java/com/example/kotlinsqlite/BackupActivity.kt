package com.example.kotlinsqlite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.compose.material3.ExperimentalMaterial3Api

import android.content.Context
import android.net.Uri
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
            try {
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

                // Hapus instance agar Room membaca ulang DB
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
//fun ProdukScreen(db: ProdukDatabase, refreshDb: () -> Unit) {

fun ProdukScreen(db: ProdukDatabase, refreshDb: (Uri?) -> Unit) {

    val produkDao = db.produkDao()
    val produkList by produkDao.getAllProduk().observeAsState(initial = emptyList())
    val context = LocalContext.current

    val filePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            ProdukDatabase.replaceDatabase(context, it) // Gunakan context dari LocalContext
            refreshDb(it) // Paksa reload database
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Daftar Produk") },
                actions = {
                    Button(onClick = { filePickerLauncher.launch("*/*") }) {
                        Text("Upload DB")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(contentPadding = padding) {
            items(produkList) { produk ->
                Card(
                    modifier = Modifier.padding(8.dp),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Text(text = "${produk.nama} - Qty: ${produk.quantity}", modifier = Modifier.padding(16.dp))
                }
            }
        }
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




