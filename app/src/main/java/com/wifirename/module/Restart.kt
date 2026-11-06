package com.wifirename.module

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.util.concurrent.TimeUnit

enum class RestartTarget(
    val menuLabel: Int,
    val confirmTitle: Int,
    val confirmBody: Int,
    val successToast: Int,
) {
    SYSTEM_UI(
        R.string.restart_system_ui,
        R.string.restart_system_ui_title,
        R.string.restart_system_ui_confirm,
        R.string.restart_success_system_ui,
    ),
    SETTINGS(
        R.string.restart_settings,
        R.string.restart_settings_title,
        R.string.restart_settings_confirm,
        R.string.restart_success_settings,
    ),
    BOTH(
        R.string.restart_both,
        R.string.restart_both_title,
        R.string.restart_both_confirm,
        R.string.restart_success_both,
    ),
}

data class RestartFailure(
    val message: String,
    val commands: String,
)

object RestartRunner {
    private const val TIMEOUT_SECONDS = 10L

    private data class ShellResult(
        val command: String,
        val exitCode: Int?,
        val stderr: String,
        val timedOut: Boolean = false,
        val ioError: Boolean = false,
    ) {
        val denied: Boolean
            get() = stderr.contains("permission denied", ignoreCase = true) ||
                stderr.contains("not permitted", ignoreCase = true) ||
                stderr.contains("access denied", ignoreCase = true)

        val success: Boolean
            get() = !timedOut && !ioError && exitCode == 0 && !denied
    }

    fun run(context: Context, target: RestartTarget): RestartFailure? {
        val results = when (target) {
            RestartTarget.SYSTEM_UI -> listOf(restartSystemUi(context))
            RestartTarget.SETTINGS -> listOf(restartSettings(context))
            RestartTarget.BOTH -> listOf(restartSystemUi(context), restartSettings(context))
        }
        val failed = results.firstOrNull { !it.success } ?: return null
        val message = when {
            failed.ioError -> context.getString(R.string.restart_failed_su)
            failed.timedOut -> context.getString(R.string.restart_failed_su_timeout)
            failed.denied -> context.getString(R.string.restart_failed_denied)
            else -> {
                val code = failed.exitCode ?: -1
                if (failed.stderr.isBlank()) {
                    context.getString(R.string.restart_failed_command, failed.command, code)
                } else {
                    context.getString(
                        R.string.restart_failed_command_detail,
                        failed.command,
                        code,
                        failed.stderr,
                    )
                }
            }
        }
        return RestartFailure(message, manualCommands(context, target))
    }

    fun manualCommands(context: Context, target: RestartTarget): String {
        val lines = mutableListOf<String>()
        if (target != RestartTarget.SETTINGS) {
            lines += "su -c \"${context.getString(R.string.restart_command_system_ui)}\""
            lines += "su -c \"${context.getString(R.string.restart_command_system_ui_fallback)}\""
        }
        if (target != RestartTarget.SYSTEM_UI) {
            lines += "su -c \"${context.getString(R.string.restart_command_settings)}\""
        }
        return lines.joinToString("\n")
    }

    private fun restartSystemUi(context: Context): ShellResult {
        val primary = runSu(context.getString(R.string.restart_command_system_ui))
        if (primary.success) return primary
        val missingKillall = primary.exitCode == 127 ||
            primary.stderr.contains("not found", ignoreCase = true) ||
            primary.stderr.contains("no such file", ignoreCase = true) ||
            primary.stderr.contains("inaccessible", ignoreCase = true)
        return if (missingKillall) {
            runSu(context.getString(R.string.restart_command_system_ui_fallback))
        } else {
            primary
        }
    }

    private fun restartSettings(context: Context): ShellResult {
        return runSu(context.getString(R.string.restart_command_settings))
    }

    private fun runSu(command: String): ShellResult {
        return try {
            val process = ProcessBuilder("su", "-c", command).start()
            val finished = process.waitFor(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            if (!finished) {
                process.destroyForcibly()
                ShellResult(command = command, exitCode = null, stderr = "", timedOut = true)
            } else {
                process.inputStream.bufferedReader().use { it.readText() }
                val stderr = process.errorStream.bufferedReader().use { it.readText() }.trim()
                ShellResult(command = command, exitCode = process.exitValue(), stderr = stderr)
            }
        } catch (e: Exception) {
            ShellResult(
                command = command,
                exitCode = null,
                stderr = e.message ?: "",
                ioError = true,
            )
        }
    }
}

@Composable
fun RestartMenuDialog(
    show: Boolean,
    onDismiss: () -> Unit,
    onSelect: (RestartTarget) -> Unit,
) {
    OverlayDialog(
        show = show,
        title = stringResource(R.string.restart_title),
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            RestartTarget.entries.forEach { target ->
                TextButton(
                    text = stringResource(target.menuLabel),
                    onClick = { onSelect(target) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            TextButton(
                text = stringResource(R.string.cancel),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun RestartConfirmDialog(
    target: RestartTarget,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    OverlayDialog(
        show = true,
        title = stringResource(target.confirmTitle),
        summary = stringResource(target.confirmBody),
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TextButton(
                text = stringResource(R.string.restart_confirm_action),
                onClick = onConfirm,
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(
                text = stringResource(R.string.cancel),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
fun RestartFailureDialog(
    failure: RestartFailure,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    OverlayDialog(
        show = true,
        title = stringResource(R.string.restart_failed_title),
        summary = failure.message,
        onDismissRequest = onDismiss,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.restart_manual_hint),
                fontSize = 12.sp,
                color = MiuixTheme.colorScheme.onSurfaceContainerVariant,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = failure.commands,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = MiuixTheme.colorScheme.onSurface,
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(
                text = stringResource(R.string.restart_copy_command),
                onClick = {
                    val clipboard = context.getSystemService(ClipboardManager::class.java)
                    clipboard?.setPrimaryClip(
                        ClipData.newPlainText(
                            context.getString(R.string.restart_copy_command),
                            failure.commands,
                        ),
                    )
                    Toast.makeText(
                        context,
                        R.string.restart_copied,
                        Toast.LENGTH_SHORT,
                    ).show()
                },
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(
                text = stringResource(R.string.restart_close),
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
