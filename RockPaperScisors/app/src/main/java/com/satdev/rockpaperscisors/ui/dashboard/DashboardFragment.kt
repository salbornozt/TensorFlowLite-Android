package com.satdev.rockpaperscisors.ui.dashboard

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ThumbnailUtils
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.satdev.rockpaperscisors.ClasifyModel
import com.satdev.rockpaperscisors.R
import com.satdev.rockpaperscisors.databinding.FragmentDashboardBinding
import com.satdev.rockpaperscisors.ui.home.HomeViewModel
import com.satdev.rockpaperscisors.util.PlayStatus
import java.io.File
import java.io.IOException

class DashboardFragment : Fragment(),View.OnClickListener {

    private var _binding: FragmentDashboardBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var homeViewModel: HomeViewModel
    private var selectedButton = ""
    val REQUEST_IMAGE_CAPTURE = 2
    val MY_PERMISSIONS_REQUEST = 1
    private lateinit var mCurrentPhotoPath: String

    private val classes = listOf<String>("paper","rock","scissors")
    private val imageSize = 300
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        homeViewModel =
            ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
        homeViewModel.playResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is PlayStatus.Player1Playing -> {
                    changeButtonsState(
                        false, arrayListOf<View>(
                            binding.rockButton,
                            binding.paperButton,
                            binding.scissorButton,
                            binding.continueButton,
                            binding.detectButton
                        )
                    )
                    binding.selectedTxt.setText("Tu jugada fué: "+homeViewModel.getPlayerChoice(homeViewModel.JUGADOR_2))
                }
                is PlayStatus.Player2Playing -> {
                    changeButtonsState(
                        true, arrayListOf<View>(
                            binding.rockButton,
                            binding.paperButton,
                            binding.scissorButton,
                            binding.continueButton,
                            binding.detectButton
                        )
                    )

                }
                is PlayStatus.GameTie -> {

                }
                is PlayStatus.GameWon -> {

                }
                is PlayStatus.GameFinished -> {

                }
            }
        })

        binding.apply {
            rockButton.setOnClickListener(this@DashboardFragment)
            paperButton.setOnClickListener(this@DashboardFragment)
            scissorButton.setOnClickListener(this@DashboardFragment)
            continueButton.setOnClickListener(this@DashboardFragment)
        }
    }

    override fun onClick(p0: View?) {
        if (p0?.id == binding.continueButton.id) {
            homeViewModel.play(homeViewModel.JUGADOR_2, binding.selectedTxt.text.toString())
        }else{
            binding.selectedTxt.setText(p0?.contentDescription.toString())

        }
    }

    fun changeButtonsState(enable:Boolean, views:ArrayList<View>){
        views.forEach {
            if (enable) {
                it.setBackgroundColor(resources.getColor(R.color.teal_200))
            }else{
                it.setBackgroundColor(resources.getColor(R.color.purple_200))
            }
            it.isEnabled = enable
        }
        binding.detectButton.setOnClickListener {
            openCamera()
        }

    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun openCamera() {
        Log.d("sat_tag", "openCamera: ")
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {

            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)

        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST
            )
        }
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK){
            var image = data?.getExtras()?.get("data") as Bitmap
            val dimension = Math.min(image.width, image.height)
            image = ThumbnailUtils.extractThumbnail(image, dimension, dimension)
            val matrix = Matrix()

            matrix.postRotate(90f)
            image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false)
            val rotated = Bitmap.createBitmap(image,0,0,imageSize,imageSize,matrix,true)

            //classifyImage(image)
            val a = ClasifyModel()
            val resp = a.classifyImage(rotated,requireActivity().applicationContext,imageSize)
            val eux = resp.split(";")
            binding.selectedTxt.setText(eux[0])
            binding.confidentTxt.setText(eux[1])
            binding.image2.setImageBitmap(rotated)

            //val b = getBitmap(a.buffer,imageSize,imageSize)
            //binding.image.setImageBitmap(b)
        }
        super.onActivityResult(requestCode, resultCode, data)

    }
}