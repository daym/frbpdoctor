package com.friendly_machines.frbpdoctor.ui.customization

import android.content.Context.MODE_PRIVATE
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.io.IOException
import com.friendly_machines.frbpdoctor.R

class WatchFaceEditorFragment : Fragment() {

    private val SHARED_IMAGE_FILENAME = "shared_watchface.png"
    private var imageUri: Uri? = null

    companion object {
        private const val ARG_IMAGE_URI = "imageUri"

        fun newInstance(imageUri: Uri): WatchFaceEditorFragment {
            val fragment = WatchFaceEditorFragment()
            val args = Bundle().apply {
                putParcelable(ARG_IMAGE_URI, imageUri)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            imageUri = it.getParcelable(ARG_IMAGE_URI)  // FIXME: Use newer function
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_watch_face_editor, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageUri?.let { uri ->
            saveImageToInternalStorage(uri)
        }
    }

    private fun saveImageToInternalStorage(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val outputStream = requireContext().openFileOutput(SHARED_IMAGE_FILENAME, MODE_PRIVATE)
            val buffer = ByteArray(1024)
            var length = inputStream?.read(buffer)
            while (length != null && length > 0) {
                outputStream.write(buffer, 0, length)
                length = inputStream?.read(buffer) // FIXME
            }
            inputStream?.close()
            outputStream.close()

            displayImageInEditor(SHARED_IMAGE_FILENAME)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayImageInEditor(imagePath: String) {
        val bitmap = BitmapFactory.decodeFile(requireContext().getFileStreamPath(imagePath).absolutePath)
        val editorView = view?.findViewById<WatchFacePixelEditorView>(R.id.pixelEditorView)
        editorView?.setImageBitmap(bitmap)
    }
}
