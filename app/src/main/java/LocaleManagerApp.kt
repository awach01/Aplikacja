import android.content.Context
import android.content.res.Configuration
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import java.util.Locale

class LocaleManagerApp(private val context: Context) {

    fun changeLocale(languageCode: String) {
        val newLocale = Locale(languageCode)
        Locale.setDefault(newLocale)
        val configuration = context.resources.configuration
        configuration.setLocale(newLocale)
        configuration.setLayoutDirection(newLocale)

        context.resources.updateConfiguration(configuration, context.resources.displayMetrics)

        if (context is AppCompatActivity) {
            context.recreate()
        }
    }

    fun getCurrentLocale(): String {
        val configuration = context.resources.configuration
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            configuration.locales.get(0).language
        } else {
            @Suppress("DEPRECATION")
            configuration.locale.language
        }
    }
}
