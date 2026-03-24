package test.android.downer.provider

import android.app.admin.DevicePolicyManager
import android.content.Context
import com.sdkapi.api.SdkApi
import com.sdkapi.common.ResultCallback
import com.sdkapi.common.ResultInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class FinalAdmins(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val default: CoroutineContext,
    loggers: Loggers,
) : Admins {
    private val logger = loggers.create("[Admins]")
    override val owners = object : StateFlow<Boolean> {
        override val value: Boolean
            get() {
                val dm = context.getSystemService(DevicePolicyManager::class.java)
                return dm.isDeviceOwnerApp(context.packageName)
            }
        override val replayCache = emptyList<Boolean>()

        override suspend fun collect(collector: FlowCollector<Boolean>): Nothing {
            val dm = context.getSystemService(DevicePolicyManager::class.java)
            val values = AtomicBoolean(dm.isDeviceOwnerApp(context.packageName))
            collector.emit(values.get())
            while (true) {
                val isDeviceOwner = dm.isDeviceOwnerApp(context.packageName)
                if (values.compareAndSet(!isDeviceOwner, isDeviceOwner)) {
                    collector.emit(isDeviceOwner)
                }
                delay(1.seconds)
            }
        }
    }

    private var mcuVersion: String? = null

    init {
        SdkApi.newInstance(context)
        coroutineScope.launch {
            withContext(default) {
                withTimeout(10.seconds) {
                    while (isActive) {
                        if (SdkApi.getInstance().serviceConnectionStatus) break
                        delay(250.milliseconds)
                    }
                }
                val callback = object : ResultCallback<ResultInfo<String>>() {
                    override fun onMsg(p0: ResultInfo<String>?) {
                        logger.debug("status: ${p0?.status}")
                        when (p0?.status) {
                            "success" -> {
                                mcuVersion = p0.`val`
                            }
                        }
                    }
                }
                SdkApi.getInstance().DeviceInfo().getMcuVersion(callback)
            }
        }
    }

    override fun update(isDeviceOwner: Boolean) {
        val dm = context.getSystemService(DevicePolicyManager::class.java)
        if (dm.isDeviceOwnerApp(context.packageName) == isDeviceOwner) return
        if (isDeviceOwner) error("Set an app the device owner is not supported!")
        dm.clearDeviceOwnerApp(context.packageName)
    }

    override fun getVersions(): Admins.Versions {
        val di = SdkApi.getInstance().DeviceInfo()
        if (mcuVersion == null) {
            Thread.sleep(1_000)
        }
        return Admins.Versions(
            api = di.apiVersion,
            sdk = di.apiSdkServiceVersion,
            firmware = di.firmwareVersion,
            mcu = mcuVersion,
        )
    }
}
