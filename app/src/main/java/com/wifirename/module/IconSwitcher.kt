package com.wifirename.module

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Icon
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import kotlin.math.max
import kotlin.math.min

enum class PinResult { REQUESTED, UNSUPPORTED, FAILED }

object IconSwitcher {

    private const val CUSTOM_SHORTCUT_ID = "custom_icon"

    fun reconcile(context: Context, preferredId: String? = null) {
        val id = preferredId
            ?.takeIf { IconCatalog.isKnown(it) }
            ?: IconSettings.iconId(context)
        if (select(context, id)) {
            disableOthers(context, id)
        }
    }

    /**
     * Persists the choice and enables its alias. The caller must launch the
     * new alias and let [reconcile] disable the old one; disabling the alias
     * that hosts the current task would close the app before the preference
     * is written on some launchers.
     */
    fun select(context: Context, id: String): Boolean {
        if (!IconCatalog.isKnown(id)) return false
        return try {
            if (!IconSettings.setIconId(context, id)) return false
            context.packageManager.setComponentEnabledSetting(
                IconCatalog.component(context, id),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP,
            )
            true
        } catch (_: Exception) {
            false
        }
    }

    fun disableOthers(context: Context, id: String) {
        val pm = context.packageManager
        for (otherId in IconCatalog.allIds) {
            if (otherId == id) continue
            val other = IconCatalog.component(context, otherId)
            if (isEffectivelyEnabled(pm, other, otherId == IconCatalog.defaultId)) {
                try {
                    pm.setComponentEnabledSetting(
                        other,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP,
                    )
                } catch (_: Exception) {
                    // A stale component should not block the selected icon.
                }
            }
        }
    }

    private fun isEffectivelyEnabled(
        pm: PackageManager,
        component: android.content.ComponentName,
        manifestDefault: Boolean,
    ): Boolean = when (pm.getComponentEnabledSetting(component)) {
        PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER,
        PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED,
        -> false
        else -> manifestDefault
    }

    fun pinCustomShortcut(context: Context, bg: Color, fg: Color): PinResult {
        val manager = context.getSystemService(ShortcutManager::class.java) ?: return PinResult.FAILED
        if (!manager.isRequestPinShortcutSupported) return PinResult.UNSUPPORTED
        return try {
            val icon = Icon.createWithAdaptiveBitmap(renderCustomIcon(context, bg, fg, 432))
            val shortcut = ShortcutInfo.Builder(context, CUSTOM_SHORTCUT_ID)
                .setShortLabel(context.getString(R.string.icon_custom_shortcut_label))
                .setLongLabel(context.getString(R.string.icon_custom_shortcut_long))
                .setIcon(icon)
                .setIntent(
                    Intent(context, MainActivity::class.java)
                        .setAction(Intent.ACTION_MAIN)
                        .addCategory(Intent.CATEGORY_LAUNCHER),
                )
                .build()
            val callback = PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, PinShortcutReceiver::class.java)
                    .setAction(PinShortcutReceiver.ACTION_PINNED),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            if (manager.requestPinShortcut(shortcut, callback.intentSender)) {
                PinResult.REQUESTED
            } else {
                PinResult.FAILED
            }
        } catch (_: Exception) {
            PinResult.FAILED
        }
    }

    fun renderCustomIcon(context: Context, bg: Color, fg: Color, sizePx: Int): Bitmap {
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(bg.toArgb())
        context.getDrawable(R.drawable.ic_caret_wave_glyph)?.mutate()?.let { glyph ->
            glyph.setTint(fg.toArgb())
            glyph.setBounds(0, 0, sizePx, sizePx)
            glyph.draw(canvas)
        }
        return bitmap
    }

    fun contrastRatio(a: Color, b: Color): Float {
        val first = a.luminance()
        val second = b.luminance()
        return (max(first, second) + 0.05f) / (min(first, second) + 0.05f)
    }
}
