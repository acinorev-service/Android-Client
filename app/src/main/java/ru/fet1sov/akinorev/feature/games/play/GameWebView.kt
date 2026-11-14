package ru.fet1sov.akinorev.feature.games.play

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.FrameLayout
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import ru.fet1sov.akinorev.BuildConfig
import java.io.ByteArrayInputStream

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun GameWebView(
    url: String,
    modifier: Modifier = Modifier,
    onPageStarted: () -> Unit = {},
    onPageFinished: () -> Unit = {},
    onError: () -> Unit = {},
) {
    val context = LocalContext.current
    val callbacks = rememberUpdatedState(WebViewCallbacks(onPageStarted, onPageFinished, onError))
    val fullscreenHost = remember(context) { FullscreenHost(context) }
    val lastLoadedUrl = remember { arrayOf("") }

    val webView = remember(context) {
        buildGameWebView(
            context = context,
            callbacks = { callbacks.value },
            fullscreenHost = fullscreenHost,
        )
    }

    AndroidView(
        factory = { webView },
        modifier = modifier,
        update = { view ->
            if (lastLoadedUrl[0] != url) {
                lastLoadedUrl[0] = url
                view.loadUrl(url)
            }
        },
    )

    DisposableEffect(context) {
        onDispose {
            fullscreenHost.hide()
            webView.stopLoading()
            webView.loadUrl(AboutBlank)
            webView.destroy()
        }
    }
}

private data class WebViewCallbacks(
    val onPageStarted: () -> Unit,
    val onPageFinished: () -> Unit,
    val onError: () -> Unit,
)

private fun buildGameWebView(
    context: Context,
    callbacks: () -> WebViewCallbacks,
    fullscreenHost: FullscreenHost,
): WebView {
    val webView = WebView(context)
    val cookieManager = CookieManager.getInstance().apply { setAcceptCookie(true) }
    cookieManager.setAcceptThirdPartyCookies(webView, true)

    return webView.apply {
    setBackgroundColor(Color.BLACK)
    setLayerType(View.LAYER_TYPE_HARDWARE, null)
    isLongClickable = false
    setOnLongClickListener { true }
    setRendererPriorityPolicy(WebView.RENDERER_PRIORITY_IMPORTANT, false)

    settings.apply {
        javaScriptEnabled = true
        domStorageEnabled = true
        databaseEnabled = true
        mediaPlaybackRequiresUserGesture = false
        useWideViewPort = true
        loadWithOverviewMode = true
        mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        cacheMode = WebSettings.LOAD_DEFAULT
        setSupportZoom(false)
        builtInZoomControls = false
        displayZoomControls = false
        textZoom = 100
        allowFileAccess = false
        allowContentAccess = false
        javaScriptCanOpenWindowsAutomatically = false
        userAgentString = userAgentString.replace("; wv", "")
    }

    webViewClient = object : WebViewClient() {
        override fun shouldInterceptRequest(view: WebView, request: WebResourceRequest): WebResourceResponse? {
            val response = bootRedirectResponse(request.url)
            if (response != null) {
                Log.d(LogTag, "boot intercepted: ${request.url}")
            }
            return response ?: super.shouldInterceptRequest(view, request)
        }

        override fun shouldOverrideUrlLoading(view: WebView, request: WebResourceRequest): Boolean {
            val target = request.url
            when (target.scheme?.lowercase()) {
                "http", "https", "about", "blob", "data", "javascript" -> return false
            }
            openExternally(context, target)
            return true
        }

        override fun onPageStarted(view: WebView, url: String, favicon: android.graphics.Bitmap?) {
            if (url != AboutBlank) {
                Log.d(LogTag, "page started: $url")
                callbacks().onPageStarted()
            }
            super.onPageStarted(view, url, favicon)
        }

        override fun onPageFinished(view: WebView, url: String) {
            if (url != AboutBlank) {
                Log.d(LogTag, "page finished: $url")
                callbacks().onPageFinished()
            }
            super.onPageFinished(view, url)
        }

        override fun onReceivedError(
            view: WebView,
            request: WebResourceRequest,
            error: android.webkit.WebResourceError,
        ) {
            if (request.isForMainFrame) {
                Log.e(LogTag, "main frame error ${error.errorCode} ${error.description}: ${request.url}")
                callbacks().onError()
            }
            super.onReceivedError(view, request, error)
        }

        override fun onReceivedHttpError(
            view: WebView,
            request: WebResourceRequest,
            errorResponse: android.webkit.WebResourceResponse,
        ) {
            if (request.isForMainFrame && errorResponse.statusCode >= 400) {
                Log.e(LogTag, "main frame http ${errorResponse.statusCode}: ${request.url}")
                callbacks().onError()
            }
            super.onReceivedHttpError(view, request, errorResponse)
        }
    }

    webChromeClient = object : WebChromeClient() {
        override fun onShowCustomView(view: View?, callback: CustomViewCallback?) {
            if (view == null) return
            fullscreenHost.show(view, callback)
        }

        override fun onHideCustomView() {
            fullscreenHost.hide()
        }
    }
    }
}

// CDN отдаёт игру через boot-страницу облачных сохранений, которая ждёт ответа
// родительского окна до 5 секунд. В WebView родителя нет: сразу уводим браузер
// на index.html игры, сохраняя параметры комнаты и облачной сессии.
private fun bootRedirectResponse(url: Uri): WebResourceResponse? {
    val path = url.path ?: return null
    if (!path.endsWith(BootSuffix)) return null

    val gameId = url.getQueryParameter("game").orEmpty()
    if (gameId.isBlank()) return null

    val redirect = StringBuilder()
        .append(resolveGameOrigin(url))
        .append(path.removeSuffix(BootSuffix))
        .append("/index.html?mt_game=")
        .append(Uri.encode(gameId))
    url.getQueryParameter("room")?.takeIf { it.isNotBlank() }?.let {
        redirect.append("&room=").append(Uri.encode(it))
    }
    if (url.getQueryParameter("online") == "1") {
        redirect.append("&online=1")
    }

    val html = """
        <!doctype html>
        <html><head><meta charset="utf-8"></head>
        <body><script>location.replace(${quoteJs(redirect.toString())});</script></body></html>
    """.trimIndent()

    return WebResourceResponse(
        "text/html",
        "UTF-8",
        ByteArrayInputStream(html.toByteArray(Charsets.UTF_8)),
    )
}

private fun resolveGameOrigin(url: Uri): String {
    val host = url.host.orEmpty()
    if (host.isNotBlank() && !isLoopbackHost(host)) {
        return buildOrigin(url.scheme, host, url.port)
    }

    val cdn = runCatching { Uri.parse(BuildConfig.CDN_BASE_URL) }.getOrNull()
    val cdnHost = cdn?.host
    val fallbackHost = when {
        !cdnHost.isNullOrBlank() && !isLoopbackHost(cdnHost) -> cdnHost
        else -> runCatching { Uri.parse(BuildConfig.API_BASE_URL).host }.getOrNull() ?: host
    }
    val fallbackPort = cdn?.port?.takeIf { it > 0 }
        ?: url.port.takeIf { it > 0 }
        ?: DefaultCdnPort
    val scheme = cdn?.scheme?.takeIf { it.isNotBlank() } ?: url.scheme ?: "http"

    return buildOrigin(scheme, fallbackHost, fallbackPort)
}

private fun buildOrigin(scheme: String?, host: String, port: Int): String {
    val resolvedScheme = scheme?.takeIf { it.isNotBlank() } ?: "http"
    val portPart = if (port > 0) ":$port" else ""
    return "$resolvedScheme://$host$portPart"
}

private fun isLoopbackHost(host: String): Boolean {
    val normalized = host.lowercase().removePrefix("[").removeSuffix("]")
    return normalized == "localhost" ||
        normalized == "127.0.0.1" ||
        normalized == "0.0.0.0" ||
        normalized == "::1"
}

private fun quoteJs(value: String): String = buildString {
    append('"')
    value.forEach { char ->
        when (char) {
            '"', '\\' -> append('\\').append(char)
            '\n' -> append("\\n")
            '\r' -> append("\\r")
            else -> append(char)
        }
    }
    append('"')
}

private fun openExternally(context: Context, target: Uri) {
    when (target.scheme?.lowercase()) {
        "http", "https", "about", "blob", "data", "javascript", null -> return
    }
    runCatching {
        context.startActivity(Intent(Intent.ACTION_VIEW, target).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }
}

private class FullscreenHost(private val context: Context) {
    private var dialog: Dialog? = null
    private var callback: WebChromeClient.CustomViewCallback? = null

    fun show(view: View, customViewCallback: WebChromeClient.CustomViewCallback?) {
        hide()

        val container = FrameLayout(context).apply {
            setBackgroundColor(Color.BLACK)
            addView(
                view,
                FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT,
                ),
            )
        }

        dialog = Dialog(context, android.R.style.Theme_Black_NoTitleBar_Fullscreen).apply {
            setContentView(container)
            setOnDismissListener {
                (view.parent as? ViewGroup)?.removeView(view)
                callback?.onCustomViewHidden()
                callback = null
            }
            show()
        }
        callback = customViewCallback
    }

    fun hide() {
        dialog?.dismiss()
        dialog = null
    }
}

private const val AboutBlank = "about:blank"
private const val BootSuffix = "/__acinorev/boot.html"
private const val LogTag = "GameWebView"
private const val DefaultCdnPort = 8090
