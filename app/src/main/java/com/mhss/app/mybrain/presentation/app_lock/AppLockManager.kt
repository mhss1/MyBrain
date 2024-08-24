package com.mhss.app.mybrain.presentation.app_lock

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import androidx.biometric.BiometricPrompt
import com.mhss.app.ui.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class AppLockManager(
    private val activity: AppCompatActivity
) {

    private val biometricManager = BiometricManager.from(activity)

    private val resultChannel = Channel<AuthResult>()
    val resultFlow = resultChannel.receiveAsFlow()

    private val authenticators = if (Build.VERSION.SDK_INT >= 30) {
        BIOMETRIC_WEAK or DEVICE_CREDENTIAL
    } else BIOMETRIC_WEAK

    fun showAuthPrompt() {
        val info = BiometricPrompt.PromptInfo.Builder()
            .setTitle(activity.getString(R.string.auth_title))
            .setConfirmationRequired(false)
            .setAllowedAuthenticators(authenticators)

        if (Build.VERSION.SDK_INT < 30) info.setNegativeButtonText(activity.getString(R.string.cancel))

        when (biometricManager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                resultChannel.trySend(AuthResult.NoHardware)
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                resultChannel.trySend(AuthResult.HardwareUnavailable)
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                resultChannel.trySend(AuthResult.NoneEnrolled)
            }
            else -> Unit
        }
        val prompt = BiometricPrompt(
            activity,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED) {
                        resultChannel.trySend(AuthResult.Error(errString.toString()))
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    resultChannel.trySend(AuthResult.Success)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    resultChannel.trySend(AuthResult.Failed)
                }
            }
        )
        prompt.authenticate(info.build())
    }

    fun canUseFeature(): Boolean {
        return biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS
    }

    sealed interface AuthResult {
        data object NoneEnrolled: AuthResult
        data object HardwareUnavailable: AuthResult
        data object NoHardware: AuthResult
        data class Error(val message: String): AuthResult
        data object Success: AuthResult
        data object Failed: AuthResult
    }
}