package com.example.edit88.ui.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.edit88.ai.GeminiService
import com.example.edit88.data.SettingsRepository
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import kotlinx.coroutines.launch

@Composable
fun SettingsSheet(
    settingsRepository: SettingsRepository,
    geminiService: GeminiService,
    onClose: () -> Unit
) {
    var apiKeyText by remember { mutableStateOf(settingsRepository.getApiKey()) }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var testMessage by remember { mutableStateOf(settingsRepository.getLastStatus()) }
    var testSuccess by remember { mutableStateOf<Boolean?>(null) }
    var saveFeedback by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(DarkSurface, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .padding(20.dp)
    ) {
        // Drag handle
        Box(
            modifier = Modifier
                .size(40.dp, 4.dp)
                .background(Color(0xFF334155), RoundedCornerShape(2.dp))
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Key,
                    contentDescription = "API Settings",
                    tint = AccentCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Gemini AI Settings",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Connection Status Pill
            Box(
                modifier = Modifier
                    .background(
                        if (testSuccess == true) Color(0xFF065F46) else Color(0xFF1E293B),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (testSuccess == true) "Active" else if (apiKeyText.isNotBlank()) "Configured" else "Key Required",
                    color = if (testSuccess == true) Color(0xFF34D399) else Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Enter your Google Gemini API Key to enable AI Auto-Subtitles (Urdu & English), Script-to-Video generation, and AI Color Grading.",
            color = Color(0xFF94A3B8),
            fontSize = 12.sp,
            lineHeight = 16.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        // API Key Input Field
        OutlinedTextField(
            value = apiKeyText,
            onValueChange = {
                apiKeyText = it
                saveFeedback = ""
            },
            label = { Text("GEMINI_API_KEY", color = Color(0xFF94A3B8)) },
            placeholder = { Text("AIzaSy...", color = Color(0xFF64748B)) },
            singleLine = true,
            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                    Icon(
                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Toggle Visibility",
                        tint = Color(0xFF94A3B8)
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentPurple,
                unfocusedBorderColor = Color(0xFF334155),
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = DarkSurfaceVariant,
                unfocusedContainerColor = DarkSurfaceVariant
            ),
            modifier = Modifier.fillMaxWidth()
        )

        if (saveFeedback.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = saveFeedback,
                color = Color(0xFF34D399),
                fontSize = 12.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action Buttons: Save & Test Connection
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = {
                    settingsRepository.saveApiKey(apiKeyText)
                    saveFeedback = "✓ API Key securely saved locally!"
                },
                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Key", color = Color.White, fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = {
                    isTesting = true
                    testSuccess = null
                    scope.launch {
                        val result = geminiService.testConnection(apiKeyText)
                        isTesting = false
                        testSuccess = result.success
                        testMessage = if (result.success) {
                            "Connected in ${result.latencyMs}ms! (Model: gemini-3.5-flash)"
                        } else {
                            result.message
                        }
                        settingsRepository.saveTestStatus(testMessage, result.latencyMs)
                    }
                },
                enabled = !isTesting,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan),
                modifier = Modifier.weight(1f)
            ) {
                if (isTesting) {
                    CircularProgressIndicator(
                        color = AccentCyan,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Testing...", fontSize = 13.sp)
                } else {
                    Text("Test Connection", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                }
            }
        }

        // Test Output Banner
        if (testMessage.isNotBlank() && testMessage != "Not verified") {
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        if (testSuccess == true) Color(0xFF064E3B) else Color(0xFF3B1E1E),
                        RoundedCornerShape(8.dp)
                    )
                    .border(
                        1.dp,
                        if (testSuccess == true) Color(0xFF059669) else Color(0xFFDC2626),
                        RoundedCornerShape(8.dp)
                    )
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (testSuccess == true) Icons.Default.CheckCircle else Icons.Default.ErrorOutline,
                    contentDescription = "Status",
                    tint = if (testSuccess == true) Color(0xFF34D399) else Color(0xFFF87171),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = testMessage,
                    color = if (testSuccess == true) Color(0xFFD1FAE5) else Color(0xFFFECACA),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Done button
        Button(
            onClick = onClose,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF242938)),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Done", color = Color(0xFFE2E8F0))
        }
    }
}
