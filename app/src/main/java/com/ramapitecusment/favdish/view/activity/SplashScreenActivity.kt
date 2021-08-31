package com.ramapitecusment.favdish.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.TextView
import com.ramapitecusment.favdish.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SplashScreenActivity : AppCompatActivity() {
    private val applicationScope = CoroutineScope(Dispatchers.Default)

    private fun launchCoroutine() {
        applicationScope.launch {
            navigateToMainActivity()
        }
    }

    private suspend fun navigateToMainActivity() {
        delay(300)
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        setUpAnimation()
    }

    private fun setUpAnimation() {
        val splashAnimation = AnimationUtils.loadAnimation(this, R.anim.anim_splash)
        findViewById<TextView>(R.id.splashScreenTitle).animation = splashAnimation

        splashAnimation.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                launchCoroutine()
            }

            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) {}
        })
    }
}