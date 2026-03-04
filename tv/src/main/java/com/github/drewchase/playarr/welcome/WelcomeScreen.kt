package com.github.drewchase.playarr.welcome

import android.Manifest
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.tv.material3.ExperimentalTvMaterial3Api
import com.github.drewchase.playarr.AppConfiguration
import com.github.drewchase.playarr.SetupServer
import com.github.drewchase.playarr.commonlib.PlexAuthClient
import com.github.drewchase.playarr.ui.components.PlayarrText
import com.github.drewchase.playarr.ui.theme.PlayarrTheme
import com.github.drewchase.playarr.ui.theme.TvPreviews
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withContext

class WelcomeScreen {

    @RequiresApi(Build.VERSION_CODES.M)
    @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE)
    @OptIn(ExperimentalTvMaterial3Api::class)
    @TvPreviews
    @Composable
    fun View() {
        val context = LocalContext.current
        val config = remember { AppConfiguration(context) }
        val setupServer = remember { SetupServer(context, config.plexClientId) }
        val plexAuth = remember { PlexAuthClient(config.plexClientId) }

        val pinCode = remember { mutableStateOf<String?>(null) }
        val pinId = remember { mutableStateOf<Long?>(null) }
        val pinError = remember { mutableStateOf<String?>(null) }

        // Start web server
        DisposableEffect(Unit) {
            setupServer.start()
            onDispose {
                setupServer.stop()
            }
        }

        // Create Plex PIN and poll for authorization
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                try {
                    val pin = plexAuth.createPin()
                    pinCode.value = pin.code
                    pinId.value = pin.id

                    // Share PIN with the web frontend via the setup server
                    setupServer.pinCode = pin.code
                    setupServer.pinId = pin.id

                    // Poll every 2 seconds until the user authorizes
                    while (isActive && pinId.value != null) {
                        delay(2000)
                        val status = plexAuth.checkPin(pinId.value!!)
                        if (!status.authToken.isNullOrEmpty()) {
                            config.authToken = status.authToken
                            break
                        }
                    }
                } catch (e: Exception) {
                    pinError.value = "Failed to connect to Plex: ${e.message}"
                }
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
                    ),
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
                        // Left column: Plex link + PIN code
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

                            Spacer(modifier = Modifier.height(8.dp))

                            PlayarrText(
                                "enter this code",
                                style = PlayarrTheme.typography.lg,
                                color = PlayarrTheme.colors.foreground,
                            )

                            // PIN code display - large spaced characters
                            if (pinCode.value != null) {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                ) {
                                    pinCode.value!!.forEach { char ->
                                        PlayarrText(
                                            text = char.uppercase(),
                                            style = PlayarrTheme.typography.hero.copy(
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 4.sp,
                                            ),
                                            color = PlayarrTheme.colors.primary,
                                        )
                                    }
                                }
                            } else if (pinError.value != null) {
                                PlayarrText(
                                    pinError.value!!,
                                    style = PlayarrTheme.typography.sm,
                                    color = PlayarrTheme.colors.statusRed,
                                )
                            } else {
                                PlayarrText(
                                    "Loading...",
                                    style = PlayarrTheme.typography.title,
                                    color = PlayarrTheme.colors.foreground.copy(alpha = 0.5f),
                                )
                            }
                        }

                        // Divider with OR
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .padding(horizontal = 48.dp)
                                .height(320.dp),
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
                                    contentDescription = "QR code to connect to Playarr setup",
                                    modifier = Modifier.size(200.dp),
                                )
                            }
                        }
                    }

                    // Footer
                    PlayarrText(
                        "The QR code links to the setup page on this device. The code above can be entered at plex.tv/link to sign in.\nMake sure your phone is connected to the same network as this device.",
                        style = PlayarrTheme.typography.sm,
                        color = PlayarrTheme.colors.foreground.copy(alpha = 0.5f),
                        textAlign = TextAlign.Center,
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
                    bitmap[x, y] =
                        if (bitMatrix[x, y]) 0xFF1CE783.toInt() else 0xFF0B0B0D.toInt()
                }
            }
            bitmap
        } catch (_: Exception) {
            null
        }
    }
}
