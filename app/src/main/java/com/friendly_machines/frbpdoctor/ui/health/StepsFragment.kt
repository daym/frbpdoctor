package com.friendly_machines.frbpdoctor.ui.health

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.WatchCommunicationClientShorthand
import com.friendly_machines.frbpdoctor.watchprotocol.notification.WatchResponse
import com.friendly_machines.frbpdoctor.watchprotocol.notification.big.StepsDataBlock

class StepsFragment : Fragment() {
    private var recyclerView: RecyclerView? = null

    companion object {
        fun newInstance() = StepsFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_steps, container, false)
        val setStepGoalButton = view.findViewById<Button>(R.id.setStepGoalButton)
        setStepGoalButton.setOnClickListener {
            val stepGoalEditText = EditText(requireContext())
            stepGoalEditText.inputType = InputType.TYPE_CLASS_NUMBER
            stepGoalEditText.hint = "Number of steps"

            AlertDialog.Builder(requireContext()).setTitle("Your target number of steps").setMessage("What number of steps do you target?") // TODO translate
                .setView(stepGoalEditText).setPositiveButton("Set Step Goal") { _, _ ->
                    val stepGoalText = stepGoalEditText.text.toString()
                    try {
                        val stepGoal = Integer.parseInt(stepGoalText)
                        WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponse.SetStepGoal(0)) { binder ->
                            binder.setStepGoal(stepGoal)
                        }
                    } catch (e: NumberFormatException) {
                        Toast.makeText(requireContext(), "Number of steps wasn't a positive number", Toast.LENGTH_LONG).show()
                    }
                }
                .setNegativeButton("Cancel") { _, _ -> }.show()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
//        val data = mutableListOf<SleepDataBlock>()
//        val adapter = HeatAdapter(data)
//        recyclerView.adapter = adapter
        //adapter.notifyDataSetChanged()
        this.recyclerView = recyclerView
    }

    fun setData(data: Array<StepsDataBlock>) {
        val adapter = StepsAdapter(data.sortedBy { it.dayTimestamp })
        recyclerView!!.adapter = adapter
        //adapter.notifyDataSetChanged()
    }
}