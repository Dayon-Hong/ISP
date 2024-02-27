package com.project.how.view.activity.calendar

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.material.tabs.TabLayout
import com.project.how.R
import com.project.how.adapter.DaysScheduleAdapter
import com.project.how.adapter.item_touch_helper.RecyclerViewItemTouchHelperCallback
import com.project.how.data_class.AiSchedule
import com.project.how.data_class.DaysSchedule
import com.project.how.data_class.Schedule
import com.project.how.databinding.ActivityCalendarEditBinding
import com.project.how.interface_af.interface_ada.ItemStartDragListener
import com.project.how.view.dialog.AiScheduleDialog
import com.project.how.view.dp.DpPxChanger
import com.project.how.view_model.ScheduleViewModel
import kotlinx.coroutines.launch
import java.io.Serializable
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


class CalendarEditActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var binding : ActivityCalendarEditBinding
    private val viewModel : ScheduleViewModel by viewModels()
    private lateinit var data : Schedule
    private lateinit var adapter : DaysScheduleAdapter
    private lateinit var supportMapFragment: SupportMapFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_calendar_edit)
        binding.edit = this
        binding.lifecycleOwner = this

        lifecycleScope.launch {
            supportMapFragment = SupportMapFragment.newInstance();

            supportFragmentManager.beginTransaction()
                .replace(R.id.map_card, supportMapFragment)
                .commit();
            supportMapFragment.getMapAsync(this@CalendarEditActivity)
        }

        lifecycleScope.launch {
            val type = intent.getIntExtra(resources.getString(R.string.type), FAILURE)
            Log.d("CalendarEditActivity", "type : $type")
            getData(type)
        }

        viewModel.scheduleLiveData.observe(this){
            Log.d("CalendarEditActivity", "scheduleLiveData.observe start\n data.title : ${it.title}")
            data = it

            binding.title.setText(data.title)
            binding.date.text = "${data.startDate} - ${data.endDate}"
            val formattedNumber = NumberFormat.getNumberInstance(Locale.getDefault()).format(data.cost)
            binding.budget.text = getString(R.string.calendar_budget, formattedNumber)
            adapter = DaysScheduleAdapter(data.dailySchedule[0], this@CalendarEditActivity)
            binding.daySchedules.adapter = adapter

            setDaysTab()
            setDaysTabItemMargin()

            val mCallback = RecyclerViewItemTouchHelperCallback(adapter)
            val mItemTouchHelper = ItemTouchHelper(mCallback)
            mItemTouchHelper.attachToRecyclerView(binding.daySchedules)

            adapter.itemDragListener(object : ItemStartDragListener {
                override fun onDropActivity(
                    initList: MutableList<DaysSchedule>,
                    changeList: MutableList<DaysSchedule>
                ) {
                    adapter.notifyDataSetChanged()
                }

            })
        }

        binding.daysTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val selectedTabPosition = binding.daysTab.selectedTabPosition
                Log.d("OnTabSelected", "selectedTabPosition : $selectedTabPosition")
                binding.daysTitle.text = getString(R.string.days_title, (selectedTabPosition + 1).toString(), getDaysTitle(selectedTabPosition))
                lifecycleScope.launch {
                    adapter.update(data.dailySchedule[selectedTabPosition])
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })

    }

    override fun onMapReady(p0: GoogleMap) {

    }

    private fun setDaysTab(){
        for(i in 1..data.dailySchedule.size){
            val tab = binding.daysTab.newTab().setText("${i}일차")
            binding.daysTab.addTab(tab)
        }
    }

    private fun setDaysTabItemMargin(){
        val tabs = binding.daysTab.getChildAt(0) as ViewGroup
        for(i in 0 until tabs.childCount){
            val tab = tabs.getChildAt(i)
            val lp = tab.layoutParams as LinearLayout.LayoutParams
            val dpPxChanger = DpPxChanger()
            lp.marginEnd = dpPxChanger.dpToPx(this, AiScheduleDialog.TAB_ITEM_MARGIN)
            lp.width = dpPxChanger.dpToPx(this, AiScheduleDialog.TAB_ITEM_WIDTH)
            lp.height = dpPxChanger.dpToPx(this, AiScheduleDialog.TAB_ITEM_HEIGHT)
            tab.layoutParams = lp
        }
        binding.daysTab.requestLayout()
    }

    private fun getDaysTitle(tabNum : Int) : String{
        val startDate = LocalDate.parse(data.startDate, DateTimeFormatter.ISO_DATE)
        val formatter = DateTimeFormatter.ofPattern("MM.dd")
        return startDate.plusDays(tabNum.toLong()).format(formatter)
    }

    private fun <T : Serializable?> getSerializable(activity: Activity, name: String, clazz: Class<T>): T
    {
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            activity.intent.getSerializableExtra(name, clazz)!!
        else
            activity.intent.getSerializableExtra(name) as T
    }

    private fun getData (type : Int){
        when(type){
            FAILURE ->{
                Toast.makeText(this, getString(R.string.get_data_failed), Toast.LENGTH_SHORT).show()
            }
            AI_SCHEDULE ->{
                viewModel.getSchedule(getSerializable(this, resources.getString(R.string.aischedule), AiSchedule::class.java))
            }
            BASIC_SCHEDULE ->{
                viewModel.getSchedule(getSerializable(this, getString(R.string.schedule), Schedule::class.java))
            }
        }
    }

    companion object{
        const val FAILURE = -1
        const val AI_SCHEDULE = 0
        const val BASIC_SCHEDULE = 1
    }

}