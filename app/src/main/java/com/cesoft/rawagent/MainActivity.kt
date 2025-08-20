package com.cesoft.rawagent

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.cesoft.rawagent.ui.theme.RawAgentTheme

//https://api.deepseek.com
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        android.util.Log.e("AA", "-----------"+BuildConfig.API_KEY)
        enableEdgeToEdge()
        permissionContent()
    }

    private fun permissionContent() {
        setContent { MainScreenPermissionsCompo() }
    }
    @Composable
    private fun MainScreenPermissionsCompo() {
        RawAgentTheme {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxSize().safeDrawingPadding(),
            ) {
                val ret = checkAllPermissions()
                android.util.Log.e("MainAct", "checkAllPermissions---------$ret")
                if(!ret)askPermissions()
                MainScreen()
            }
        }
    }
    private val result = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result: Map<String, Boolean> ->
        val denied = result.filter { !it.value }.map { it.key }
        if(denied.isEmpty()) askPermissions()
        if(checkAllPermissions()) permissionContent()
    }

    fun Context.hasPermission(permission: String): Boolean {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }
    fun checkAllPermissions(): Boolean {
        return hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                && hasPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    }
    fun askPermissions(): Boolean {
        val pLocation = Manifest.permission.ACCESS_FINE_LOCATION
        val pRecordAudio = Manifest.permission.RECORD_AUDIO
        val permissions = arrayOf(pLocation, pRecordAudio)
        // ACCESS_FINE_LOCATION and ACCESS_BACKGROUND_LOCATION at the same time doesn't show perm dialog!!!!
        if( ! hasPermission(pLocation)) {
            val pL = shouldShowRequestPermissionRationale(pLocation)
            val pR = shouldShowRequestPermissionRationale(pRecordAudio)
            if(!pL || !pR) {
                result.launch(permissions)
            }
            if(pL || pR) {
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            }
            return false
        }
        return true
    }
}
