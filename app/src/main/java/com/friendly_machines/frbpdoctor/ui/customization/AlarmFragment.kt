package com.friendly_machines.frbpdoctor.ui.customization

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.friendly_machines.fr_yhe_api.commondata.WatchChangeAlarmAction
import com.friendly_machines.fr_yhe_api.watchprotocol.IWatchBinder
import com.friendly_machines.fr_yhe_api.watchprotocol.WatchResponseType
import com.friendly_machines.frbpdoctor.MedBigResponseBuffer
import com.friendly_machines.frbpdoctor.R
import com.friendly_machines.frbpdoctor.WatchCommunicationClientShorthand
import com.friendly_machines.frbpdoctor.service.WatchCommunicationService
import kotlinx.coroutines.launch

class AlarmFragment : Fragment() {
    private var recyclerView: RecyclerView? = null
    private lateinit var serviceConnection: ServiceConnection
    private var alarmController: AlarmController? = null
    private var disconnector: IWatchBinder? = null
    private val bigBuffers = MedBigResponseBuffer()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alarm, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val addAlarmTimeButton = view.findViewById<FloatingActionButton>(R.id.addAlarmButton)
        addAlarmTimeButton.setOnClickListener {
            val editAlarmDialog = EditAlarmDialog(WatchChangeAlarmAction.Add)
            editAlarmDialog.addListener(object : EditAlarmDialog.OnAlarmSetListener {
                override fun onAlarmSet(enabled: Boolean, title: com.friendly_machines.fr_yhe_api.commondata.AlarmTitleMed, hour: Byte, min: Byte, repeatOnDaysOfWeek: BooleanArray) {
                    WatchCommunicationClientShorthand.bindExecOneCommandUnbind(requireContext(), WatchResponseType.ChangeAlarm) { binder ->
                        val id = 1 // FIXME!
                        binder.addAlarm(id, enabled, hour, min, title, repeatOnDaysOfWeek)
                    }
                    // TODO refresh alarm list maybe
                }
            })
            editAlarmDialog.show(childFragmentManager, "edit_alarm_dialog")
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.list)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        //adapter.notifyDataSetChanged()
        this.recyclerView = recyclerView

        // Set up service connection and alarm controller
        serviceConnection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as IWatchBinder
                
                // Android calls onServiceConnected multiple times if service crashes/restarts - clean up previous controller
                alarmController?.let { oldController ->
                    disconnector?.removeListener(oldController)
                }
                
                alarmController = AlarmController(binder)
                bigBuffers.listener = alarmController
                disconnector = binder.addListener(alarmController!!)
                
                // Load alarms
                lifecycleScope.launch {
                    try {
                        val alarms = alarmController?.getAlarms()
                        alarms?.let { setData(it) }
                    } catch (e: Exception) {
                        Log.e("AlarmFragment", "Failed to load alarms", e)
                    }
                }
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                alarmController?.let { controller ->
                    disconnector?.removeListener(controller)
                }
                alarmController = null
                disconnector = null
            }
        }
        
        val serviceIntent = Intent(context, WatchCommunicationService::class.java)
        context?.bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        alarmController?.let { controller ->
            disconnector?.removeListener(controller)
        }
        context?.unbindService(serviceConnection)
        alarmController = null
        disconnector = null
    }

    fun setData(data: Array<com.friendly_machines.fr_yhe_api.commondata.AlarmDataBlock>) {
        val adapter = AlarmAdapter(data.sortedBy { it.id }) { alarmId ->
            // Handle delete click
            lifecycleScope.launch {
                try {
                    alarmController?.deleteAlarm(alarmId, 0) // Pass x=alarmId, y=0; FIXME
                    // Refresh alarm list after deletion
                    alarmController?.listAlarms()
                } catch (e: Exception) {
                    Log.e("AlarmFragment", "Failed to delete alarm", e)
                }
            }
        }
        recyclerView!!.adapter = adapter
        //adapter.notifyDataSetChanged()
    }
}