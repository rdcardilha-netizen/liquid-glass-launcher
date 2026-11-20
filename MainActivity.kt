package com.izffx.liquidglass

import android.app.Activity
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import android.widget.ScrollView
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.FrameLayout

class MainActivity : Activity() {
    private val bg = Color.rgb(22, 27, 43)
    private val white = Color.WHITE
    private lateinit var root: LinearLayout
    private lateinit var grid: GridLayout
    private var showingAll = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = bg
        window.navigationBarColor = Color.rgb(18, 21, 34)
        buildHome()
    }

    private fun glass(color: Int = 0x44FFFFFF): GradientDrawable =
        GradientDrawable().apply {
            setColor(color)
            cornerRadius = dp(24).toFloat()
            setStroke(dp(1), 0x55FFFFFF)
        }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun label(text: String, size: Float, bold: Boolean = false): TextView =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(white)
            gravity = Gravity.CENTER
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

    private fun buildHome() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(22), dp(18), dp(14))
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(Color.rgb(35, 54, 83), Color.rgb(37, 42, 70), Color.rgb(19, 24, 39))
            )
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val brand = label("Liquid Glass", 23f, true)
        brand.gravity = Gravity.CENTER_VERTICAL
        top.addView(brand, LinearLayout.LayoutParams(0, dp(48), 1f))
        val clock = label("◷  ${java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date())}", 14f)
        clock.background = glass(0x33FFFFFF)
        clock.setPadding(dp(14), dp(8), dp(14), dp(8))
        top.addView(clock)
        root.addView(top)

        val date = label("Sua tela inicial", 14f)
        date.alpha = 0.75f
        date.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
        date.setPadding(dp(2), dp(8), 0, dp(16))
        root.addView(date)

        val scroll = ScrollView(this).apply { fillViewport = true; clipToPadding = false }
        val content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val section = label(if (showingAll) "Todos os aplicativos" else "Favoritos e aplicativos", 17f, true)
        section.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
        section.setPadding(dp(2), dp(8), 0, dp(12))
        content.addView(section)

        grid = GridLayout(this).apply { columnCount = 4; useDefaultMargins = false }
        val apps = loadApps()
        val chosen = if (showingAll) apps else apps.take(16)
        chosen.forEach { info -> addAppTile(info) }
        content.addView(grid)

        val foldersTitle = label("Pastas", 17f, true)
        foldersTitle.gravity = Gravity.LEFT or Gravity.CENTER_VERTICAL
        foldersTitle.setPadding(dp(2), dp(18), 0, dp(10))
        content.addView(foldersTitle)

        val folders = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        folders.addView(folderTile("Social", "Mensagens • Fotos"))
        folders.addView(folderTile("Utilitários", "Relógio • Ajustes"))
        folders.addView(folderTile("Entretenimento", "Música • Vídeos"))
        content.addView(folders)

        scroll.addView(content)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))

        val dock = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            background = glass(0x44FFFFFF)
            setPadding(dp(8), dp(10), dp(8), dp(10))
        }
        val dockApps = apps.take(4)
        if (dockApps.isEmpty()) {
            dock.addView(label("Adicione seus aplicativos favoritos", 12f))
        } else {
            dockApps.forEach { info ->
                val item = ImageView(this).apply {
                    setImageDrawable(info.loadIcon(packageManager))
                    scaleType = ImageView.ScaleType.FIT_CENTER
                    setPadding(dp(8), dp(4), dp(8), dp(4))
                    setOnClickListener { launch(info) }
                }
                dock.addView(item, LinearLayout.LayoutParams(0, dp(54), 1f))
            }
        }
        root.addView(dock, LinearLayout.LayoutParams(-1, dp(72)))
        val all = TextView(this).apply {
            text = if (showingAll) "Voltar à página inicial" else "Biblioteca de apps  ⌄"
            textSize = 13f
            setTextColor(white)
            gravity = Gravity.CENTER
            setPadding(0, dp(10), 0, dp(2))
            setOnClickListener { showingAll = !showingAll; buildHome() }
        }
        root.addView(all)
        setContentView(root)
    }

    private fun loadApps(): List<ResolveInfo> {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return packageManager.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != packageName }
            .sortedBy { it.loadLabel(packageManager).toString().lowercase() }
    }

    private fun addAppTile(info: ResolveInfo) {
        val tile = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(3), dp(7), dp(3), dp(10))
            setOnClickListener { launch(info) }
        }
        val icon = ImageView(this).apply {
            setImageDrawable(info.loadIcon(packageManager))
            scaleType = ImageView.ScaleType.FIT_CENTER
        }
        tile.addView(icon, LinearLayout.LayoutParams(dp(52), dp(52)))
        val name = TextView(this).apply {
            text = info.loadLabel(packageManager)
            textSize = 10.5f
            setTextColor(white)
            gravity = Gravity.CENTER
            maxLines = 1
            ellipsize = android.text.TextUtils.TruncateAt.END
            setPadding(0, dp(4), 0, 0)
        }
        tile.addView(name, LinearLayout.LayoutParams(-1, dp(24)))
        val lp = GridLayout.LayoutParams().apply {
            width = 0
            height = dp(92)
            columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
        }
        grid.addView(tile, lp)
    }

    private fun folderTile(title: String, subtitle: String): View {
        val box = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = glass(0x33FFFFFF)
            setPadding(dp(10), dp(12), dp(10), dp(12))
            val t = label("▦  $title", 12f, true)
            t.gravity = Gravity.LEFT
            addView(t)
            val s = label(subtitle, 9f)
            s.alpha = 0.7f
            s.gravity = Gravity.LEFT
            s.setPadding(0, dp(6), 0, 0)
            addView(s)
        }
        val lp = LinearLayout.LayoutParams(0, dp(76), 1f)
        lp.setMargins(dp(3), 0, dp(3), dp(8))
        return box.apply {
            setOnClickListener {
                Toast.makeText(this@MainActivity, "Pastas demonstrativas: organização visual em teste", Toast.LENGTH_SHORT).show()
            }
            layoutParams = lp
        }
    }

    private fun launch(info: ResolveInfo) {
        val intent = packageManager.getLaunchIntentForPackage(info.activityInfo.packageName)
        if (intent != null) startActivity(intent)
        else Toast.makeText(this, "Não foi possível abrir este aplicativo", Toast.LENGTH_SHORT).show()
    }
}
