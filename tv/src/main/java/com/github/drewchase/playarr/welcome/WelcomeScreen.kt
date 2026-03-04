package com.github.drewchase.playarr.welcome

import android.Manifest
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.github.drewchase.playarr.SetupServer
import com.github.drewchase.playarr.ui.components.PlayarrText
import com.github.drewchase.playarr.ui.theme.PlayarrTheme
import com.github.drewchase.playarr.ui.theme.TvPreviews
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set

class WelcomeScreen {

    @RequiresApi(Build.VERSION_CODES.M)
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    @OptIn(ExperimentalTvMaterial3Api::class)
    @TvPreviews
    @Composable
    fun View() {
        val context = LocalContext.current
        val setupServer = remember { SetupServer(context) }

        DisposableEffect(Unit) {
            setupServer.start()
            onDispose {
                setupServer.stop()
            }
        }

        val connectionUrl = setupServer.getConnectionUrl()
        val fullUrl = "http://$connectionUrl"

        val qrBitmap = remember(fullUrl) { generateQrCode(fullUrl, 280) }

        PlayarrTheme {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                PlayarrTheme.colors.background,
                                PlayarrTheme.colors.content1
                            )
                        )
                    )
                    .border(width = 4.dp, color = PlayarrTheme.colors.primary),
                contentAlignment = Alignment.Center,
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(32.dp),
                ) {
                    // Title
                    PlayarrText(
                        "Connect to your Playarr server",
                        style = PlayarrTheme.typography.headline.copy(fontWeight = FontWeight.Bold),
                        color = PlayarrTheme.colors.foreground,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Two-column content
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        // Left column: URL
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.width(400.dp),
                        ) {
                            PlayarrText(
                                "Visit this link in a browser",
                                style = PlayarrTheme.typography.lg,
                                color = PlayarrTheme.colors.foreground,
                            )
                            PlayarrText(
                                fullUrl,
                                style = PlayarrTheme.typography.title.copy(fontWeight = FontWeight.Bold),
                                color = PlayarrTheme.colors.primary,
                            )
                        }

                        // Divider with OR
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(horizontal = 48.dp)
                                .height(280.dp),
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.fillMaxHeight(),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .weight(1f)
                                        .background(PlayarrTheme.colors.content3)
                                )
                                PlayarrText(
                                    "OR",
                                    style = PlayarrTheme.typography.base,
                                    color = PlayarrTheme.colors.foreground,
                                    modifier = Modifier.padding(vertical = 12.dp),
                                )
                                Box(
                                    modifier = Modifier
                                        .width(1.dp)
                                        .weight(1f)
                                        .background(PlayarrTheme.colors.content3)
                                )
                            }
                        }

                        // Right column: QR code
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.width(400.dp),
                        ) {
                            PlayarrText(
                                "Use the camera on your mobile\ndevice to scan the QR code",
                                style = PlayarrTheme.typography.lg,
                                color = PlayarrTheme.colors.foreground,
                                textAlign = TextAlign.Center,
                            )
                            if (qrBitmap != null) {
                                Image(
                                    bitmap = qrBitmap.asImageBitmap(),
                                    contentDescription = "QR code to connect to Playarr server",
                                    modifier = Modifier.size(200.dp),
                                )
                            }
                        }
                    }

                    // Footer disclaimer
                    PlayarrText(
                        "Make sure your phone is connected to the same network as this device.",
                        style = PlayarrTheme.typography.sm,
                        color = PlayarrTheme.colors.foreground.copy(alpha = 0.5f),
                    )
                }
            }
        }
    }

    private fun generateQrCode(content: String, size: Int): Bitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size)
            val bitmap = createBitmap(size, size)
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap[x, y] = if (bitMatrix[x, y]) 0xFF1CE783.toInt() else 0xFF0B0B0D.toInt()
                }
            }
            bitmap
        } catch (_: Exception) {
            null
        }
    }
}
