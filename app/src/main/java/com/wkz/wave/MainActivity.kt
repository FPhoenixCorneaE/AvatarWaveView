package com.wkz.wave

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.fphoenixcorneae.wave.AvatarWaveHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private var avatarWaveHelper:AvatarWaveHelper?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        avatarWaveHelper= AvatarWaveHelper(
            wvHeader,
            ivAvatar,
            Color.parseColor("#80FC7A8C"),
            Color.parseColor("#40FB3D53")
        )
    }

    override fun onPause() {
        super.onPause()
        avatarWaveHelper?.cancel()
    }

    override fun onResume() {
        super.onResume()
        avatarWaveHelper?.start()
    }
}