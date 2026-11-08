package com.example

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.viewinterop.AndroidView

class MainActivity : ComponentActivity() {

  private var filePathCallback: ValueCallback<Array<Uri>>? = null

  private val fileChooserLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) { result ->
    if (filePathCallback != null) {
      val data: Intent? = result.data
      val results: Array<Uri>? = when {
        result.resultCode != Activity.RESULT_OK -> null
        data?.data != null -> arrayOf(data.data!!)
        data?.clipData != null -> {
          val clipData = data.clipData!!
          Array(clipData.itemCount) { i -> clipData.getItemAt(i).uri }
        }
        else -> null
      }
      filePathCallback?.onReceiveValue(results)
      filePathCallback = null
    }
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      CyberVaultApp(
        onOpenFileChooser = { callback, fileParams ->
          filePathCallback?.onReceiveValue(null)
          filePathCallback = callback

          val intent = fileParams?.createIntent() ?: Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
          }

          try {
            fileChooserLauncher.launch(intent)
            true
          } catch (e: Exception) {
            filePathCallback?.onReceiveValue(null)
            filePathCallback = null
            false
          }
        },
        onExternalUrl = { url ->
          try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
          } catch (e: Exception) {
            // ignore if no handler
          }
        }
      )
    }
  }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun CyberVaultApp(
  onOpenFileChooser: (ValueCallback<Array<Uri>>, WebChromeClient.FileChooserParams?) -> Boolean,
  onExternalUrl: (String) -> Unit
) {
  var webViewRef: WebView? = remember { null }

  BackHandler {
    if (webViewRef?.canGoBack() == true) {
      webViewRef?.goBack()
    } else {
      // Exit activity or stay
      webViewRef?.evaluateJavascript(
        "if (typeof closeMediaViewer === 'function') { closeMediaViewer(); }",
        null
      )
    }
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF060D0A))
      .safeDrawingPadding()
  ) {
    AndroidView(
      modifier = Modifier.fillMaxSize(),
      factory = { context ->
        WebView(context).apply {
          webViewRef = this
          setBackgroundColor(android.graphics.Color.parseColor("#060D0A"))

          settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            allowFileAccess = true
            allowContentAccess = true
            loadWithOverviewMode = true
            useWideViewPort = true
            cacheMode = WebSettings.LOAD_DEFAULT
            mediaPlaybackRequiresUserGesture = false
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
          }

          webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
              webView: WebView?,
              filePathCallback: ValueCallback<Array<Uri>>?,
              fileChooserParams: FileChooserParams?
            ): Boolean {
              return if (filePathCallback != null) {
                onOpenFileChooser(filePathCallback, fileChooserParams)
              } else {
                false
              }
            }
          }

          webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
              if (url != null) {
                if (url.startsWith("file:///android_asset/")) {
                  return false
                }
                onExternalUrl(url)
                return true
              }
              return false
            }
          }

          loadUrl("file:///android_asset/index.html")
        }
      },
      update = { webView ->
        webViewRef = webView
      }
    )
  }
}
