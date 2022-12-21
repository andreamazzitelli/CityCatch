package com.example.citycatch.ui.composables

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.sharp.Lens
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.citycatch.viewmodel.SensorViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.util.concurrent.Executor
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import androidx.lifecycle.Observer
import com.example.citycatch.R
import com.example.citycatch.ui.theme.Green
import com.example.citycatch.ui.theme.LightOrange
import com.example.citycatch.ui.theme.Red
import com.example.citycatch.viewmodel.FirebaseViewModel


@SuppressLint("UnsafeOptInUsageError", "RestrictedApi")
@Composable
fun CameraView(
    executor: Executor,
    sensorsViewModel: SensorViewModel,
    firebaseViewModel: FirebaseViewModel,
    onError: (ImageCaptureException) -> Unit
){

    var check by remember {
        mutableStateOf(false)
    }

    val popUp =  remember {
        mutableStateOf(false)
    }


    val image = firebaseViewModel.imageBitmap.observeAsState()

    val lensFacing = CameraSelector.LENS_FACING_BACK

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configs = LocalConfiguration.current

    val width = configs.screenWidthDp * (configs.densityDpi/160)
    val height = configs.screenHeightDp * (configs.densityDpi/160)

    val preview = Preview.Builder().build()
    val previewView = remember { PreviewView(context) }

    val imageAnalyzer = ImageAnalysis.Builder()
        .setTargetResolution(android.util.Size(width, height))
        .build()
    imageAnalyzer.setAnalyzer(executor) {

        val frameImage = it.image
        if(frameImage == null){
            Log.i("ML-ERROR", "frameImage is NULL")
        }
        val image = InputImage.fromMediaImage(frameImage!!, it.imageInfo.rotationDegrees)

        //Log.i("TAG SIZE BEFORE", "${frameImage.width}, ${frameImage.height}")

        val options = ImageLabelerOptions.Builder().setConfidenceThreshold(0.7f).build()
        val labeler : ImageLabeler = ImageLabeling.getClient(options)

        labeler.process(image)
            .addOnSuccessListener { labels ->

                val labelsText = labels.map {it.text}

                if(labelsText.contains("Statue")) {
                    firebaseViewModel.setImageBitmap(it)
                    check = true
                }
                else{
                    check = false
                }
                //FirebaseRepository.addToStorage("giorgia", "coastal", it)

                it.close()
            }

    }

    val cameraSelector = CameraSelector.Builder()
        .requireLensFacing(lensFacing)
        .build()


    LaunchedEffect(lensFacing) {
        val cameraProvider = context.getCameraProvider()
        cameraProvider.unbindAll()
        cameraProvider.bindToLifecycle(
            lifecycleOwner,
            cameraSelector,
            preview,
            imageAnalyzer
        )

        preview.setSurfaceProvider(previewView.surfaceProvider)
    }

    Box(
        contentAlignment = Alignment.BottomCenter,
        modifier = Modifier.fillMaxSize()
    ) {

        AndroidView(
            { previewView },
            modifier = Modifier.fillMaxSize()
        )
        if(check) {
            OverlayGraphics(color = Color.Green)
            IconButton(
                modifier = Modifier.padding(bottom = 20.dp),
                onClick = {
                    Log.i("TAG IMAGE", "Set to True")
                    firebaseViewModel.updateImageChangeState()
                    popUp.value = true
                },
                content = {
                    Icon(
                        imageVector = Icons.Sharp.Lens,
                        contentDescription = "Take picture",
                        tint = Color.White,
                        modifier = Modifier
                            .size(100.dp)
                            .padding(1.dp)
                            .border(1.dp, Color.White, CircleShape)
                    )
                }
            )
        }
        else {
            OverlayGraphics(color = Color.Red)

            IconButton(
                modifier = Modifier.padding(bottom = 20.dp),
                enabled = false,
                onClick = {},
                content = {
                    Icon(
                        imageVector = Icons.Sharp.Lens,
                        contentDescription = "",
                        tint = Color.LightGray,
                        modifier = Modifier
                            .size(100.dp)
                            .padding(1.dp)
                            .border(1.dp, Color.White, CircleShape)
                    )
                }
            )
        }
    }

    Pointer(sensorsViewModel)

    if(popUp.value){
        Log.i("IMAGE", "PopU")
        PopUp(popUp, image.value!!, firebaseViewModel)
    }

}

@Composable
fun PopUp(state: MutableState<Boolean>, image: ImageBitmap, fm:FirebaseViewModel){

    Log.i("TAG IMAGE", "After Turn")

    AlertDialog(
        modifier = Modifier.clip(RoundedCornerShape(20.dp)),
        backgroundColor = LightOrange,
        onDismissRequest = { state.value = false },
        title = {
            Column (
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentWidth(),
            ){
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Do you like this Photo?",
                    textAlign = TextAlign.Center
                )
            }
        },

        text = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Image(
                    bitmap = image,
                    contentDescription = ""
                )
            }

        },
        buttons = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    colors = ButtonDefaults.buttonColors(Green),
                    onClick = { /*TODO*/ }
                ) {
                    Text(text = "Save it!")
                }

                Button(
                    colors = ButtonDefaults.buttonColors(Red),
                    onClick = {
                        state.value = false
                        fm.updateImageChangeState()
                    }
                ) {
                    Text(text = "Take it Again")
                }
            }
        }
    )
}


@Composable
fun Pointer(sensorsViewModel: SensorViewModel){

    var angle by remember {
        mutableStateOf(0.0f)
    }


    sensorsViewModel.direction.observe(LocalLifecycleOwner.current, Observer {
        Log.i("TAG OB", it.toString())
        when(it){
            sensorsViewModel.CENTER_TAG -> angle = 0.0f
            sensorsViewModel.LEFT_TAG -> angle = -90.0f
            sensorsViewModel.RIGHT_TAG -> angle = 90.0f
        }
    })

    Box(
        modifier = Modifier
            .size(100.dp, 100.dp)
            .offset(20.dp, 20.dp)
    ){
        if (angle != 0.0f) {
            Image(
                painter = painterResource(id = R.drawable.pointer),
                contentDescription = "Pointer Image",
                modifier = Modifier.rotate(angle)
            )
        }
    }
}

@Composable
fun OverlayGraphics(color: Color){

    Canvas(
        modifier = Modifier.fillMaxSize()
    ){
        val canvasWidth = size.width
        val canvasHeight = size.height

        drawRect( // TODO da posizionare megio
            color = color,
            topLeft = Offset(x = canvasWidth/6F, y = canvasHeight/5F),
            size = Size((canvasWidth*2f)/3f, (canvasHeight*3f)/5f),
            style = Stroke(8f)
        )

    }

}


private suspend fun Context.getCameraProvider(): ProcessCameraProvider = suspendCoroutine { continuation ->
    ProcessCameraProvider.getInstance(this).also { cameraProvider ->
        cameraProvider.addListener({
            continuation.resume(cameraProvider.get())
        }, ContextCompat.getMainExecutor(this))
    }
}