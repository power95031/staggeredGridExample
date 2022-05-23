package com.example.staggeredgridexample.dialogs

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import com.example.staggeredgridexample.databinding.DialogResultPlayBinding
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem

class ResultPlayDialog(var con:Context, var uri:String) : Dialog(con) {

    lateinit var binding : DialogResultPlayBinding

    lateinit var exoPlayer : ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogResultPlayBinding.inflate(layoutInflater)
        setContentView(binding.root)

        exoPlayer = ExoPlayer.Builder(con).build()
        exoPlayer.setMediaItem(MediaItem.fromUri(uri))
        binding.PlayerView.player = exoPlayer
        exoPlayer.play()
    }

    override fun onStop() {
        if(::exoPlayer.isInitialized) {
            if(exoPlayer.isPlaying)
                exoPlayer.stop()

            exoPlayer.release()
        }
        super.onStop()
    }
}