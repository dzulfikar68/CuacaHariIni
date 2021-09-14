package io.github.dzulfikar68.cuarahariini

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)

        supportActionBar?.title = "Credits"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val webView = findViewById<WebView>(R.id.webView)
        webView.loadFile("settings/index.html")

//        loadWebView("https://raw.githubusercontent.com/dzulfikar68/CuacaHariIni/master/app/src/main/assets/setting.html")
    }

    fun WebView.loadFile(filePath: String?) {
        loadUrl("file:///android_asset/$filePath");
    }

    @SuppressLint("JavascriptInterface", "SetJavaScriptEnabled")
    private fun loadWebView(url: String?) {
        val pgBlogView = findViewById<ProgressBar>(R.id.progressBar)
        pgBlogView.visibility = View.VISIBLE
        val handler = Handler()
        handler.postDelayed({ pgBlogView.visibility = View.GONE }, 3000)
        val webView = findViewById<WebView>(R.id.webView)
        webView.settings.loadWithOverviewMode = true
        webView.settings.useWideViewPort = true
        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView, url: String) {
                // do your stuff here
                pgBlogView.visibility = View.GONE
            }
        }
        webView.loadUrl(url ?: "https://dzulfikar68.github.io/")
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }
}