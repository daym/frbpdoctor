package com.friendly_machines.frbpdoctor.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.caverock.androidsvg.SVG
import com.caverock.androidsvg.SVGParseException
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.databinding.FragmentHomeBinding
import java.io.IOException
import java.io.InputStream


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var svgImageView: ImageView // SubsamplingScaleImageView

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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

//        val textView: TextView = binding.textHome
//        homeViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

        //val view = inflater.inflate(R.layout.fragment_home, container, false)
        //svgImageView = view.findViewById(R.id.clockView)
        //loadAndDisplaySVG(requireContext())

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}