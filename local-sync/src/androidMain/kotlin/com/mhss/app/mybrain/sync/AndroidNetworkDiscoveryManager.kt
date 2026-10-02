package com.mhss.app.mybrain.sync

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.os.Build
import com.mhss.app.mybrain.sync.domain.DiscoveredDevice
import com.mhss.app.mybrain.sync.domain.NetworkDiscoveryManager
import org.koin.core.annotation.Single
import java.net.Inet4Address

private const val SERVICE_TYPE = "_mybrain_client._tcp"
private const val SERVICE_NAME_PREFIX = "MyBrain-"
private const val DEVICE_ID_ATTRIBUTE = "deviceId"

@Single(binds = [NetworkDiscoveryManager::class])
class AndroidNetworkDiscoveryManager(
    private val context: Context
) : NetworkDiscoveryManager {

    private val nsdManager = context.getSystemService(Context.NSD_SERVICE) as NsdManager
    private var registrationListener: NsdManager.RegistrationListener? = null
    private var discoveryListener: NsdManager.DiscoveryListener? = null
    private var onDeviceDiscovered: ((DiscoveredDevice) -> Unit)? = null

    override fun registerService(deviceId: String, port: Int) {
        if (!hasLocalNetworkPermission()) return
        if (registrationListener != null) return

        val listener = object : NsdManager.RegistrationListener {
            override fun onServiceRegistered(serviceInfo: NsdServiceInfo) = Unit

            override fun onRegistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
                registrationListener = null
            }

            override fun onServiceUnregistered(serviceInfo: NsdServiceInfo) {
                registrationListener = null
            }

            override fun onUnregistrationFailed(serviceInfo: NsdServiceInfo, errorCode: Int) = Unit
        }
        registrationListener = listener

        val serviceInfo = NsdServiceInfo().apply {
            serviceName = SERVICE_NAME_PREFIX + deviceId
            serviceType = SERVICE_TYPE
            setPort(port)
            setAttribute(DEVICE_ID_ATTRIBUTE, deviceId)
        }

        try {
            nsdManager.registerService(serviceInfo, NsdManager.PROTOCOL_DNS_SD, listener)
        } catch (e: Exception) {
            registrationListener = null
            e.printStackTrace()
        }
    }

    override fun startDiscovery(onDeviceDiscovered: (DiscoveredDevice) -> Unit) {
        if (!hasLocalNetworkPermission()) return
        this.onDeviceDiscovered = onDeviceDiscovered
        if (discoveryListener != null) return

        val listener = object : NsdManager.DiscoveryListener {
            override fun onDiscoveryStarted(serviceType: String) = Unit

            override fun onServiceFound(serviceInfo: NsdServiceInfo) {
                resolveService(serviceInfo)
            }

            override fun onServiceLost(serviceInfo: NsdServiceInfo) = Unit

            override fun onDiscoveryStopped(serviceType: String) {
                discoveryListener = null
            }

            override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
                discoveryListener = null
            }

            override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) = Unit
        }
        discoveryListener = listener

        try {
            nsdManager.discoverServices(SERVICE_TYPE, NsdManager.PROTOCOL_DNS_SD, listener)
        } catch (e: Exception) {
            discoveryListener = null
            e.printStackTrace()
        }
    }

    private fun hasLocalNetworkPermission(): Boolean {
        return Build.VERSION.SDK_INT < 37 || context.checkSelfPermission(
            Manifest.permission.ACCESS_LOCAL_NETWORK
        ) == PackageManager.PERMISSION_GRANTED
    }

    @Suppress("DEPRECATION")
    private fun resolveService(serviceInfo: NsdServiceInfo) {
        try {
            nsdManager.resolveService(
                serviceInfo,
                object : NsdManager.ResolveListener {
                    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) = Unit

                    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
                        val deviceId = serviceInfo.attributes[DEVICE_ID_ATTRIBUTE]
                            ?.toString(Charsets.UTF_8)
                            ?.takeIf(String::isNotBlank)
                            ?: return
                        val ipAddress = getIpAddress(serviceInfo) ?: return
                        onDeviceDiscovered?.invoke(
                            DiscoveredDevice(
                                deviceId = deviceId,
                                ipAddress = ipAddress
                            )
                        )
                    }
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getIpAddress(serviceInfo: NsdServiceInfo): String? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            serviceInfo.hostAddresses
                .firstOrNull { it is Inet4Address }
                ?.hostAddress
        } else {
            @Suppress("DEPRECATION")
            serviceInfo.host
                ?.takeIf { it is Inet4Address }
                ?.hostAddress
        }
    }
}
