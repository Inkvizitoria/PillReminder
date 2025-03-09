import android.content.Context
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.health.pillreminder.R

object ToastUtils {

    fun showCustomToast(context: Context, message: String, type: ToastType) {
        val layoutInflater = LayoutInflater.from(context)
        val view: View = layoutInflater.inflate(R.layout.custom_toast, null)

        val toastText = view.findViewById<TextView>(R.id.toastText)
        val toastIcon = view.findViewById<ImageView>(R.id.toastIcon)

        toastText.text = message

        // Настройка иконки и цвета фона
        when (type) {
            ToastType.SUCCESS -> {
                toastIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_success))
                view.setBackgroundResource(R.drawable.toast_success)
            }
            ToastType.ERROR -> {
                toastIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_error))
                view.setBackgroundResource(R.drawable.toast_error)
            }
            ToastType.INFO -> {
                toastIcon.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_info))
                view.setBackgroundResource(R.drawable.toast_info)
            }
        }

        val toast = Toast(context)
        toast.duration = Toast.LENGTH_LONG
        toast.setGravity(Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL, 0, 150)
        toast.view = view
        toast.show()
    }
}

enum class ToastType {
    SUCCESS, ERROR, INFO
}
