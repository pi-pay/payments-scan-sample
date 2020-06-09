package com.pipay.paymentscan

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.pipay.paymentscan.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_PiPay)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            invokeScheme()
        }

        binding.textViewInfo.text = getString(R.string.template_package_name, PI_PAY_PACKAGE_NAME)
    }

    /**
     * Start invocation
     */
    private fun invokeScheme() {
        val text = binding.editText.text?.toString() ?: return
        if (text.isBlank()) {
            Toast.makeText(
                this,
                "Invalid url text",
                Toast.LENGTH_SHORT
            ).show()
            binding.editText.requestFocus()
            return
        }

        if (hasAppInstalled()) {
            val uri = Uri.parse("pipay://$text")
            if (!openUriScheme(uri)) {
                Toast.makeText(
                    this,
                    "Cannot find the app for this URI.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            // Should navigate to Google Play Store to download the app
            navigateToPlayStore()
            Toast.makeText(
                this,
                "The app hasn't been installed on device yet",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    /**
     * Try invoking Uri scheme. We check this in case the current app is an older version
     * which does not support the scheme yet.
     * @return `true` if it found supported app.
     */
    private fun openUriScheme(uri: Uri): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage(PI_PAY_PACKAGE_NAME)
        val resolvedActivities = packageManager.queryIntentActivities(intent, 0)

        // Not found supported activities, so return `false`
        if (resolvedActivities.isEmpty()) {
            return false
        }

        startActivityForResult(intent, REQUEST_CODE_SCAN)
        return true
    }

    /**
     * Check whether device has the app or not
     * @return `false` if the app is not installed
     */
    private fun hasAppInstalled(): Boolean {
        return try {
            packageManager.getPackageInfo(PI_PAY_PACKAGE_NAME, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        /**
         * If it's a successful invocation, it will return [Activity.RESULT_OK]
         * with `isSuccessful=true` in the result bundle
         * else it will return [Activity.RESULT_CANCELED] with `isSuccessful=false`
         * in the result bundle.
         */

        if (requestCode == REQUEST_CODE_SCAN && resultCode == Activity.RESULT_CANCELED) {
            // The result from the app.
            if (data?.getBooleanExtra("isSuccessful", false) == false) {
                Toast.makeText(
                    this,
                    "Please update the app to the latest version.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * Open Google Play Store
     */
    private fun navigateToPlayStore() {
        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://play.google.com/store/apps/details?id=$PI_PAY_PACKAGE_NAME")
        )
        val resolvedActivities = packageManager.queryIntentActivities(intent, 0)
        if (resolvedActivities.isEmpty()) return
        startActivity(intent)
    }

    companion object {
        private const val PI_PAY_PACKAGE_NAME = "com.pipay"
        private const val REQUEST_CODE_SCAN = 1
    }
}