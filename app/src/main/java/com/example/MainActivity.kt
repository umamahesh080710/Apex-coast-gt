package com.example

import android.annotation.SuppressLint
import android.graphics.Color as AndroidColor
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge(
      statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
      navigationBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT)
    )
    setContent {
      MyApplicationTheme {
        ApexCoastGameScreen()
      }
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun ApexCoastGameScreen() {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  var isPageLoaded by remember { mutableStateOf(false) }

  val webView = remember {
    WebView(context).apply {
      setBackgroundColor(AndroidColor.parseColor("#0B0A14"))
      overScrollMode = View.OVER_SCROLL_NEVER
      isHorizontalScrollBarEnabled = false
      isVerticalScrollBarEnabled = false
      settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        mediaPlaybackRequiresUserGesture = false
        useWideViewPort = true
        loadWithOverviewMode = true
        setSupportZoom(false)
        builtInZoomControls = false
        displayZoomControls = false
        cacheMode = WebSettings.LOAD_DEFAULT
      }
      webChromeClient = WebChromeClient()
      webViewClient = object : WebViewClient() {
        override fun shouldInterceptRequest(
          view: WebView,
          request: WebResourceRequest
        ): WebResourceResponse? {
          val url = request.url
          if (url.host == "appassets.androidplatform.net" && url.path == "/assets/apex_coast_gt.html") {
            return try {
              val stream = context.assets.open("apex_coast_gt.html")
              WebResourceResponse("text/html", "UTF-8", stream)
            } catch (_: Exception) {
              null
            }
          }
          return super.shouldInterceptRequest(view, request)
        }

        override fun onPageFinished(view: WebView?, url: String?) {
          super.onPageFinished(view, url)
          isPageLoaded = true
        }
      }
      loadUrl("https://appassets.androidplatform.net/assets/apex_coast_gt.html")
    }
  }

  DisposableEffect(lifecycleOwner, webView) {
    val observer = LifecycleEventObserver { _, event ->
      when (event) {
        Lifecycle.Event.ON_RESUME -> webView.onResume()
        Lifecycle.Event.ON_PAUSE -> webView.onPause()
        else -> Unit
      }
    }
    lifecycleOwner.lifecycle.addObserver(observer)
    onDispose {
      lifecycleOwner.lifecycle.removeObserver(observer)
      webView.destroy()
    }
  }

  BackHandler(enabled = webView.canGoBack()) {
    webView.goBack()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF0B0A14))
      .windowInsetsPadding(WindowInsets.safeDrawing)
  ) {
    AndroidView(
      factory = { webView },
      modifier = Modifier
        .fillMaxSize()
        .testTag("game_webview")
    )

    AnimatedVisibility(
      visible = !isPageLoaded,
      exit = fadeOut(),
      modifier = Modifier.fillMaxSize()
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(
            Brush.radialGradient(
              colors = listOf(Color(0xFF2C1828), Color(0xFF0D0C16))
            )
          )
          .testTag("loading_overlay"),
        contentAlignment = Alignment.Center
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(16.dp),
          modifier = Modifier.padding(24.dp)
        ) {
          Text(
            text = stringResource(R.string.app_name).uppercase(),
            color = Color(0xFFFF9642),
            fontSize = 26.sp,
            fontWeight = FontWeight.Black,
            fontStyle = FontStyle.Italic,
            letterSpacing = 2.sp
          )
          CircularProgressIndicator(color = Color(0xFFFF5E2B))
          Text(
            text = stringResource(R.string.loading_circuit),
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xB3F8FAFC)
          )
        }
      }
    }
  }
}
