package com.example.kotlinsqlite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp


class ExampleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent{
            ConfigurationScreen()
        }

    }
}




    @Composable
fun ConfigurationScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF66CC77)) // Background hijau muda
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.9f)
                .align(Alignment.Center),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header dengan ikon konfigurasi
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = Color(0xFF66CC77)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Configuration",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Black
                    )
                }

                // Kolom pencarian yang sangat rounded
                OutlinedTextField(
                    value = "",
                    onValueChange = {},
                    placeholder = { Text("Search", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
//                    colors = OutlinedTextFieldDefaults.colors(
//                        unfocusedBorderColor = Color.LightGray,
//                        focusedBorderColor = Color(0xFF66CC77),
//                        containerColor = Color(0xFFF8F8F8)
//                    ),
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true
                )

                // Menu items
                MenuItemRow(
                    icon = Icons.Default.Person,
                    text = "Personal Information"
                )

                MenuItemRow(
                    icon = Icons.Default.KeyboardArrowUp,
                    text = "Pair Device"
                )

                MenuItemRow(
                    icon = Icons.Default.KeyboardArrowUp,
                    text = "Show IMEI"
                )

                MenuItemRow(
                    icon = Icons.Default.KeyboardArrowUp,
                    text = "Administrator Password"
                )

                MenuItemRow(
                    icon = Icons.Default.Delete,
                    text = "Clear Data"
                )

                MenuItemRow(
                    icon = Icons.Default.ExitToApp,
                    text = "Exit Application"
                )
            }
        }
    }
}

@Composable
fun MenuItemRow(
    icon: ImageVector,
    text: String
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = text,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp),
                color = Color.DarkGray
            )
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
        }
        Divider(color = Color.LightGray.copy(alpha = 0.5f))
    }
}

@Preview(showBackground = true)
@Composable
fun ConfigurationScreenPreview() {
    ConfigurationScreen()
}
