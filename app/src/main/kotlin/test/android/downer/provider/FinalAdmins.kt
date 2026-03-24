package test.android.downer.provider

import android.app.admin.DevicePolicyManager
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

internal class FinalAdmins(
    private val context: Context,
    private val coroutineScope: CoroutineScope,
    private val default: CoroutineContext,
) : Admins {
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

    override fun update(isDeviceOwner: Boolean) {
        val dm = context.getSystemService(DevicePolicyManager::class.java)
        if (dm.isDeviceOwnerApp(context.packageName) == isDeviceOwner) return
        if (isDeviceOwner) error("Set an app the device owner is not supported!")
        dm.clearDeviceOwnerApp(context.packageName)
    }
}
