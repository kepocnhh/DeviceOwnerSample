package test.android.downer

import android.app.Application
import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import test.android.downer.provider.Admins
import test.android.downer.provider.Contexts
import test.android.downer.provider.FinalAdmins
import test.android.downer.provider.FinalLoggers
import test.android.downer.provider.Loggers
import test.android.downer.provider.Providers

internal class App : Application() {
    override fun onCreate() {
        super.onCreate()
        val contexts = Contexts(
            main = Dispatchers.Main,
            default = Dispatchers.Default,
        )
        val loggers: Loggers = FinalLoggers
        val job = SupervisorJob()
        val coroutineScope = CoroutineScope(contexts.main + job)
        val context: Context = this
        val admins: Admins = FinalAdmins(
            context = context,
            coroutineScope = coroutineScope,
            default = contexts.default,
            loggers = loggers,
        )
        _providers = Providers(
            contexts = contexts,
            loggers = loggers,
            admins = admins,
        )
    }

    companion object {
        private var _providers: Providers? = null
        val providers: Providers get() = checkNotNull(_providers) { "No providers!" }
    }
}
