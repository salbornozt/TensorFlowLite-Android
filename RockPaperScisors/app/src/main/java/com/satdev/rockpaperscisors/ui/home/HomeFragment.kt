package com.satdev.rockpaperscisors.ui.home

import android.Manifest
import android.R.attr
import android.app.Activity.RESULT_OK
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
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
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.satdev.rockpaperscisors.ClasifyModel
import com.satdev.rockpaperscisors.R
import com.satdev.rockpaperscisors.databinding.FragmentHomeBinding
import com.satdev.rockpaperscisors.ml.Model
import com.satdev.rockpaperscisors.util.PlayStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.Buffer
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class HomeFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentHomeBinding? = null

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
        homeViewModel =
            ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        homeViewModel.playResult.observe(viewLifecycleOwner, Observer {
            when (it) {
                is PlayStatus.Player1Playing -> {
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
                is PlayStatus.Player2Playing -> {
                    changeButtonsState(
                        false, arrayListOf<View>(
                            binding.rockButton,
                            binding.paperButton,
                            binding.scissorButton,
                            binding.continueButton,
                            binding.detectButton
                        )
                    )
                    binding.selectedTxt.setText("Tu jugada fué: "+homeViewModel.getPlayerChoice(homeViewModel.JUGADOR_1))
                }
                is PlayStatus.GameTie -> {

                }
                is PlayStatus.GameWon -> {

                }
                is PlayStatus.GameFinished -> {

                }
                is PlayStatus.NoInput->{

                }
            }
        })

        binding.apply {
            rockButton.setOnClickListener(this@HomeFragment)
            paperButton.setOnClickListener(this@HomeFragment)
            scissorButton.setOnClickListener(this@HomeFragment)
            continueButton.setOnClickListener(this@HomeFragment)
        }








        return root
    }

    override fun onClick(p0: View?) {



        if (p0?.id == binding.continueButton.id) {

            homeViewModel.play(homeViewModel.JUGADOR_1, binding.selectedTxt.text.toString())
        }else{
            binding.selectedTxt.setText(p0?.contentDescription.toString())

        }
    }

    fun changeButtonsState(enable: Boolean, views: ArrayList<View>) {
        views.forEach {
            if (enable) {
                it.setBackgroundColor(resources.getColor(R.color.teal_200))
            } else {
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
            Log.d("sat_tag", "openCamera: permission")
            val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            Log.d("sat_tag", "openCamera: camera")

            // Create the File where the photo should go
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
            } catch (ex: IOException) {
                // Error occurred while creating the File
                Log.e("error", "openCamera: ", ex)
            }
            // Continue only if the File was successfully created
            Log.d("sat_tag", "openCamera: a "+photoFile)
            if (photoFile != null) {
                Log.d("sat_tag", "openCamera: ph")
                val photoURI: Uri = FileProvider.getUriForFile(
                    requireActivity(),
                    requireActivity().applicationContext.packageName + ".provider",
                    photoFile
                )
                //cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE)
            }
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST
            )
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File? {
        val outputDir = requireActivity()!!.externalCacheDir
        val image2 = File.createTempFile("temp", ".jpg", outputDir)
        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image2.name
        return image2
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK){
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
            binding.image2.setImageBitmap(rotated)

            binding.selectedTxt.setText(eux[0])
            binding.confidentTxt.setText(eux[1])
            //val b = getBitmap(a.buffer,imageSize,imageSize)
            //binding.image.setImageBitmap(b)
        }
        super.onActivityResult(requestCode, resultCode, data)
        /*
        val file = File(requireActivity().externalCacheDir, mCurrentPhotoPath)
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(
                requireActivity().contentResolver,
                Uri.fromFile(file)
            )
            var image = data?.extras?.get("data") as Bitmap
            val dimension = Math.min(image.width,image.height)
            image = ThumbnailUtils.extractThumbnail(image,dimension,dimension)
            binding.image2.setImageBitmap(image)
            image = Bitmap.createScaledBitmap(image,imageSize,imageSize,false)
            val mUri = Uri.fromFile(file)
            //val dimension: Int = getSquareCropDimensionForBitmap(bitmap)
            //val output = ThumbnailUtils.extractThumbnail(bitmap, dimension, dimension)
            //val matrix = Matrix()
            //matrix.postRotate(90F)
            //Glide.with(requireActivity()).load(mUri).into(binding.productRegisterImage)
            if (bitmap != null) {

                //Picasso.get().load(mUri).into(binding.image)
            }
            //viewModel.imageUri = mUri.toString()
            /*
            binding.productRegisterImage.setImageBitmap(
                Bitmap.createBitmap(
                    output,
                    0,
                    0,
                    output.width,
                    output.height,
                    matrix,
                    true
                )
            )
             */


            //binding.productRegisterImage.setImageBitmap(rotatedBitmap);
        } catch (e: IOException) {
            e.printStackTrace()
        }

         */
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun processImage(input:Bitmap){

         val rockModel: Model by lazy{
            Model.newInstance(requireActivity().applicationContext)
        }
        val matrix = Matrix()

        matrix.postRotate(90f)


        val bitmap = Bitmap.createScaledBitmap(input, imageSize, imageSize, false)

        val rotated = Bitmap.createBitmap(bitmap,0,0,imageSize,imageSize,matrix,true)
        val size = rotated.rowBytes * rotated.height
        Log.d("das_tag", "processImage: "+size)
        val input3 = ByteBuffer.allocate(size)

        val rotated2 = Bitmap.createBitmap(bitmap,0,0,imageSize,imageSize,matrix,true)

        ///
        val pixelValues = IntArray(300*300)
        //rotated2.getPixels(pixelValues,0,bitmap.width,0,0,bitmap.width,bitmap.height)
        ///
        Log.d("sat_tag", "processImage: byte  "+rotated.allocationByteCount)

        val input2 = ByteBuffer.allocateDirect(rotated.byteCount).order(ByteOrder.nativeOrder())

        //val input2 = ByteBuffer.allocateDirect(300*300*3*4).order(ByteOrder.nativeOrder())
        val inputFeature1 = TensorBuffer.createFixedSize(intArrayOf(1, 300, 300, 3), DataType.FLOAT32)



        /*var pixel = 0
        for (y in 0 until h) {
            for (x in 0 until h) {
                val px = bitmap.getPixel(x, y)
                val c =rotated.getColor(x,y).toArgb()
                val pixelValue = pixelValues[pixel++]

                // Get channel values from the pixel value.
                //val r = Color.red(px)
                //val g = Color.green(px)
                //val b = Color.blue(px)

                val r = Color.red(c)
                val g = Color.green(c)
                val b = Color.blue(c)
                // Normalize channel values to [-1.0, 1.0]. This requirement depends on the model.
                // For example, some models might require values to be normalized to the range
                // [0.0, 1.0] instead.
                val rf = (r - 127) / 255f
                val gf = (g - 127) / 255f
                val bf = (b - 127) / 255f

                //input2.putFloat((pixelValue shr 16 and 0xFF).toFloat())
                //input2.putFloat((pixelValue shr 8 and 0xFF).toFloat())
                //input2.putFloat((pixelValue and 0xFF).toFloat() )
                input2.putFloat(r.toFloat())
                input2.putFloat(g.toFloat())
                input2.putFloat(b.toFloat())
                inputFeature1.buffer.putFloat(r.toFloat())
                inputFeature1.buffer.putFloat(g.toFloat())
                inputFeature1.buffer.putFloat(b.toFloat())
            }
        }


         */




        //val cropSize = Math.min(bitmap.width,bitmap.height)
        val imageProcesor = ImageProcessor.Builder()

            .build()
        //var tensorImage2 = TensorImage(DataType.FLOAT32)

        //tensorImage2.load(rotated)

        //tensorImage = imageProcesor.process(tensorImage)



//        val imageByte = Bitmap.createBitmap(300,300,Bitmap.Config.RGB_565)
//        imageByte.copyPixelsFromBuffer(tensorImage.buffer)
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 224, 224, 3), DataType.FLOAT32)

        val buffer = ByteBuffer.allocateDirect(3*imageSize*imageSize*4).order(ByteOrder.nativeOrder())
        buffer.order(ByteOrder.nativeOrder())
        val intValue = intArrayOf(imageSize*imageSize)
        input.getPixels(intValue,0,input.width,0,0,input.width,input.height)
        var pixel = 0
        for (i in 0..imageSize){
            for (j in 0..imageSize){
                val aux = intValue[pixel++]
                buffer.putFloat((aux shr 16 and 0xFF) * (1f / 255f))
                buffer.putFloat((aux shr 8 and 0xFF) * (1f / 255f))
                buffer.putFloat((aux and 0xFF) * (1f / 255f))
            }
        }
        inputFeature0.loadBuffer(buffer)


        homeViewModel.viewModelScope.launch {
            val resp = withContext(Dispatchers.IO){
                //Log.d("sat_tag", "processImage: "+input.get(0))
                var aux = ""

                // Creates inputs for reference.


                //inputFeature0.loadBuffer(tensorImage.buffer)

// Runs model inference and gets result.

                val outputs = rockModel.process(inputFeature0)
                val outputFeature0 = outputs.outputFeature0AsTensorBuffer
                Log.d("sat_tag", "processImage:shpae1  "+outputFeature0.floatArray[0])
                Log.d("sat_tag", "processImage:shpae2  "+outputFeature0.floatArray[1])
                Log.d("sat_tag", "processImage:shpae 3 "+outputFeature0.floatArray[2])
                Log.d("sat_tag", "processImage:shpaes  "+outputFeature0.floatArray.size)
                for (i in 0..outputFeature0.floatArray.size-1){
                    Log.d("sat_tag", "processImage:shpaes  "+i)
                    aux +="${classes[i]} ${outputFeature0.floatArray[i]} \n"

                }




// Releases model resources if no longer used.
                rockModel.close()



                //interpreter.close()
                //return@withContext outputFeature0.floatArray
                return@withContext floatArrayOf()
                //modelOutput.rewind()
                //val probabilities = modelOutput.asFloatBuffer()
                //Log.d("sat_tag", "processImage: "+probabilityBuffer.floatArray)
            }

            binding.selectedTxt.setText(getHigestScore(resp))
        }



    }
    private fun getBitmap(buffer: Buffer, width: Int, height: Int): Bitmap? {
        buffer.rewind()
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.copyPixelsFromBuffer(buffer)
        return bitmap
    }
    fun loadModelFile(): MappedByteBuffer {
        val assetFileDescriptor  : AssetFileDescriptor = requireActivity().assets.openFd("model.tflite")
        var fileInputStream = FileInputStream(assetFileDescriptor.fileDescriptor)
        val fileChannel = fileInputStream.channel
        val startOffset= assetFileDescriptor.startOffset
        val len= assetFileDescriptor.length
        return fileChannel.map(FileChannel.MapMode.READ_ONLY,startOffset,len)
    }
    fun getImagePixels(image: Bitmap): ByteBuffer {
        // calculate how many bytes our image consists of
        val bytes = image.byteCount
        Log.d("sat_tag", "getImagePixels: "+bytes)
        //1080000
        val buffer = ByteBuffer.allocate(bytes) // Create a new buffer
        image.copyPixelsToBuffer(buffer) // Move the byte data to the buffer
        val temp = buffer.array() // Get the underlying array containing the data.
        val pixels = ByteArray(temp.size / 4 * 3) // Allocate for 3 byte BGR

        // Copy pixels into place
        for (i in 0 until temp.size / 4) {
            pixels[i * 3] = temp[i * 4 + 3] // B
            pixels[i * 3 + 1] = temp[i * 4 + 2] // G
            pixels[i * 3 + 2] = temp[i * 4 + 1] // R

            // Alpha is discarded
        }
        return buffer
    }

    fun getHigestScore(floatArray: FloatArray):String{
        var aux = 0f
        var ix = 0
        for (i in 0..floatArray.size-1){
            Log.d("sat_tag", "processImage:shpaes  "+i)
            if (floatArray[i] > aux){
                aux = floatArray[i]
                ix = i
            }

        }
        return classes[ix]
    }

}