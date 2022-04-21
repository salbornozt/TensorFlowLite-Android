package com.satdev.rockpaperscisors

import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions
import com.satdev.rockpaperscisors.databinding.ActivityMainBinding
import com.satdev.rockpaperscisors.ui.home.HomeViewModel
import com.satdev.rockpaperscisors.util.PlayStatus
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel



class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        /*val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard
            )
        )
        setupActionBarWithNavController(navController, appBarConfiguration)*/
        navView.setupWithNavController(navController)

        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        homeViewModel.playResult.observe(this, Observer {
            when(it){
                is PlayStatus.GameTie->{
                    Snackbar.make(binding.root,"Empate",Snackbar.LENGTH_LONG).show()
                }
                is PlayStatus.GameWon->{
                    Snackbar.make(binding.root,it.results.toString(),Snackbar.LENGTH_LONG).show()
                }
                is PlayStatus.GameFinished->{
                    Snackbar.make(binding.root,it.results.toString(),Snackbar.LENGTH_LONG).show()
                }
                is PlayStatus.NoInput->{
                    Snackbar.make(binding.root,"Debe selecionar una opcion",Snackbar.LENGTH_LONG).show()
                }
            }
        })
        val interpreter = Interpreter(loadModelFile(),null)
        val im = ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(300,300))
            //.add(Toar)
            .build()




    }

    fun initModel(){
        val conditions = CustomModelDownloadConditions.Builder()
            .requireWifi()  // Also possible: .requireCharging() and .requireDeviceIdle()
            .build()


    }

    fun loadModelFile():MappedByteBuffer{
        val assetFileDescriptor  : AssetFileDescriptor = this.assets.openFd("model.tflite")
        var fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset= assetFileDescriptor.startOffset
        val len= assetFileDescriptor.length
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,len)
    }
}