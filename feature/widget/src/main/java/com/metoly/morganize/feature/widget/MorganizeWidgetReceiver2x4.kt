package com.metoly.morganize.feature.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.launch

class MorganizeWidgetReceiver2x4 : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MorganizeWidget()

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == "com.metoly.morganize.ACTION_UPDATE_WIDGET") {
            val result = goAsync()
            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
                try {
                    MorganizeWidget().updateAll(context)
                } finally {
                    result.finish()
                }
            }
        }
    }
}
