package com.friendly_machines.frbpdoctor.ui.home

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import com.friendly_machines.frbpdoctor.R
import java.io.IOException
import java.io.InputStream

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : Fragment() {
    private lateinit var svgImageView: ImageView // SubsamplingScaleImageView

    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private fun loadAndDisplaySVG(context: Context) {
        try {
            val inputStream: InputStream = requireContext().assets.open("clock.svg")
            val svg = SVG.getFromInputStream(inputStream)
//            val bitmap = Bitmap.createBitmap(svg.documentWidth.toInt(), svg.documentHeight.toInt(), Bitmap.Config.ARGB_8888)
//            val canvas = Canvas(bitmap)
//            svg.renderToCanvas(canvas)
//            svgImageView.setImageBitmap(bitmap)
            svgImageView.setImageDrawable(SvgDrawable(svg, svgImageView.width, svgImageView.height))

        } catch (e: SVGParseException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        svgImageView = view.findViewById(R.id.clockView)

        // Use OnPreDrawListener to wait until the ImageView is measured
        svgImageView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                // Remove the listener to avoid multiple calls
                svgImageView.viewTreeObserver.removeOnPreDrawListener(this)

                loadAndDisplaySVG(requireContext())
                return true
            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment MainFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            MainFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}