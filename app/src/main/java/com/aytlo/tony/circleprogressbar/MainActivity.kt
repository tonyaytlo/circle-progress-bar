package com.aytlo.tony.circleprogressbar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.aytlo.tony.circleprogressbar.ext.getColorRes
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private var longTask: Thread? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        prepareProgressBar()

        btnLongTask.setOnClickListener { startFakeLongTask() }
    }

    private fun prepareProgressBar() {
        cpbProgress.run {
            setProgressTextColor(getColorRes(R.color.colorPrimaryDark))
            postSetGradient(
                GradientType.LINEAR_GRADIENT,
                getColorRes(R.color.colorPrimaryDark),
                getColorRes(R.color.colorPrimary)
            )
            setProgressBackgroundColor(getColorRes(R.color.colorAccent))
            textProgressDecorator = {
                "$it%"
            }
        }
    }

    private fun startFakeLongTask() {
        if (longTask?.isAlive == true) {
            return
        }
        longTask = Thread(Runnable {
            var progress = 0

            do {
                progress += Random(System.currentTimeMillis()).nextInt(1, 5)
                if (progress > FULL_PROGRESS) {
                    progress = FULL_PROGRESS
                }
                postProgress(progress)
                Thread.sleep(Random(System.currentTimeMillis()).nextInt(100, 137).toLong())
            } while (progress != FULL_PROGRESS)

        }).also { it.start() }
    }

    private fun postProgress(progress: Int) {
        cpbProgress.post {
            cpbProgress.setProgress(progress)
        }
    }

    companion object {
        private const val FULL_PROGRESS = 100
    }
}
