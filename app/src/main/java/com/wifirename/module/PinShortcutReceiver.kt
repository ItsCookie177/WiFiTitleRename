package com.wifirename.module

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class PinShortcutReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_PINNED = "com.wifirename.module.action.SHORTCUT_PINNED"
    }

    override fun onReceive(context: Context, intent: Intent) {
        Toast.makeText(
            context,
            context.getString(R.string.icon_color_shortcut_pinned),
            Toast.LENGTH_LONG,
        ).show()
    }
}
