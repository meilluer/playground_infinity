package com.example.footballtarget
import android.app.AlarmManager
import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import android.widget.Toast
import com.kieronquinn.app.smartspacer.sdk.model.SmartspaceTarget
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Icon
import com.kieronquinn.app.smartspacer.sdk.model.uitemplatedata.Text
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider
import com.kieronquinn.app.smartspacer.sdk.provider.SmartspacerTargetProvider.Companion.notifyChange
import com.kieronquinn.app.smartspacer.sdk.utils.TargetTemplate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


var timesin = 0
var show = false
var display = false
var titlesp = ""
var subt = ""

var team1 = ""
var team2 = ""
var scorer = ""
var scorea = 0
var scoreb = 0
var tit = titlesp

class MyNotificationListenerService : NotificationListenerService() {
    override fun onNotificationPosted(sbn: StatusBarNotification) {
        val targetPackage = "eu.livesport.FlashScore_com"
        if (sbn.packageName != targetPackage) return

        val extras = sbn.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val subtitle = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        if (subtitle.isNotEmpty()) {
            Log.d("NotificationCatch", "Title: $title, Subtitle: $subtitle")

            if (subtitle.contains("Match start", ignoreCase = true)) {
                titlesp = title
                splitTeams()
                show = true
                startMatchAlarm(this)
            }

            handleMatchNotification(this, subtitle)

            if (subtitle.contains("Correction!", ignoreCase = true)) {
                handleCorrection(this,subtitle)
            }
            if(subtitle.contains("Finished.")){
               closer(this,subtitle);
            }
            else {
                handleScoreUpdate(subtitle)
            }
        }
    }
}

// Team splitter from notification title
fun splitTeams() {
    val teams = titlesp.split("-")
    if (teams.size == 2) {
        team1 = teams[0].trim()
        team2 = teams[1].trim()
    }
}

// Alarm starter for 10 min interval
fun startMatchAlarm(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, MatchAlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val triggerTime = System.currentTimeMillis() + 10 * 60 * 1000L
    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
}

// BroadcastReceiver triggered by AlarmManager
class MatchAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        timesin += 10
        subt = timesin.toString()
        Toast.makeText(context, "Alarm Triggered: $subt mins elapsed", Toast.LENGTH_SHORT).show()
        notifyChange(context, football::class.java, "football")
    }
}

// Handles pause/resume logic
fun handleMatchNotification(context: Context, notificationText: String) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, MatchAlarmReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(
        context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    when {
        notificationText.contains("half time.", ignoreCase = true) -> {
            alarmManager.cancel(pendingIntent)
            subt = "Half time"
            notifyChange(context, football::class.java, "football")
        }
        notificationText.contains("start of 2nd half.", ignoreCase = true) -> {
            val triggerTime = System.currentTimeMillis() + 10 * 60 * 1000L
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent)
            subt = timesin.toString()
            notifyChange(context, football::class.java, "football")
        }
    }
}
fun closer(context: Context, notificationText: String) {
    // Immediately update UI to show "Match ended"
    subt = "Match Ended"
    notifyChange(context, football::class.java, "football")

    // Delay hiding for 30 minutes (in background)
    CoroutineScope(Dispatchers.Main).launch {
        delay(30 * 60 * 1000L) // 30 minutes in milliseconds
        display = false
        notifyChange(context, football::class.java, "football")
    }
}

fun handleCorrection(context: Context,subtitle: String) {
    val regex = Regex("""Correction!\s*(\[?\d+\]?)\s*-\s*(\[?\d+\]?)""", RegexOption.IGNORE_CASE)
    val match = regex.find(subtitle)

    if (match != null) {
        val leftRaw = match.groupValues[1].trim()
        val rightRaw = match.groupValues[2].trim()

        // Parse score for team1 (left)
        if (leftRaw.contains("[")) {
            scorea = leftRaw.replace("[", "").replace("]", "").toIntOrNull() ?: scorea
        }

        // Parse score for team2 (right)
        if (rightRaw.contains("[")) {
            scoreb = rightRaw.replace("[", "").replace("]", "").toIntOrNull() ?: scoreb
        }

        tit = "$team1 $scorea vs $scoreb $team2"
        notifyChange(context, football::class.java, "football")
    }
}

// Score parsing logic
fun handleScoreUpdate(subtitle: String) {
    val parts = subtitle.split(" - ")
    if (parts.size >= 2) {
        scorer = parts[1].trim()
        if (parts[0].contains("[")) {
            scorea++
        } else if (parts[1].contains("[")) {
            scoreb++
        }
        subt = scorer
        tit = "$team1 $scorea vs $scoreb $team2"
    }
}

// Smartspacer Target Provider <--DO NOT TOUCH THIS-->
class football : SmartspacerTargetProvider() {

    fun disp() {
        if (show) {
            display = true
            notifyChange()
        }
    }

    override fun getSmartspaceTargets(smartspacerId: String): List<SmartspaceTarget> {
        val targets= mutableListOf<SmartspaceTarget>()
       targets.add(

                TargetTemplate.Basic(
                    id = "football",
                    componentName = ComponentName(context!!, football::class.java),
                    title = Text(tit),
                    subtitle = Text(subt),
                    icon = Icon(android.graphics.drawable.Icon.createWithResource(context, R.drawable.ic_launcher_foreground))
                ).create()
       )
if(display==true){
    return targets
        }
        else{
            return emptyList()
        }

    }

    override fun getConfig(smartspacerId: String?): Config {
        return Config(
            label = "Score using Flashscore",
            description = "Football score with 10min delay",
            icon = android.graphics.drawable.Icon.createWithResource(context, R.drawable.ic_launcher_foreground)
        )
    }

    override fun onDismiss(smartspacerId: String, targetId: String): Boolean {
        display = false
        return true
    }
}
//<--TILL HERE---->