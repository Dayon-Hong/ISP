package com.project.how.view.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.databinding.DataBindingUtil
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.project.how.R
import com.project.how.databinding.ActivityAddAicalendarBinding
import com.project.how.interface_af.OnDateTimeListener
import com.project.how.interface_af.OnDesListener
import com.project.how.interface_af.OnPurposeListener
import com.project.how.interface_af.OnTimeListener
import com.project.how.view.dialog.bottom_sheet_dialog.CalendarBottomSheetDialog
import com.project.how.view.dialog.bottom_sheet_dialog.DesBottomSheetDialog
import com.project.how.view.dialog.bottom_sheet_dialog.PurposeBottomSheetDialog

class AddAICalendarActivity :
    AppCompatActivity(), OnDateTimeListener, OnDesListener, OnPurposeListener {
    private lateinit var binding : ActivityAddAicalendarBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_add_aicalendar)
        binding.ai = this
        binding.lifecycleOwner = this

        MobileAds.initialize(this)
        val adRequest = AdRequest.Builder().build()
        binding.adView.loadAd(adRequest)
    }

    fun showDepartureInput(){
        val calendar = CalendarBottomSheetDialog(CalendarBottomSheetDialog.DEPARTURE, this)
        calendar.show(supportFragmentManager, "CalendarBottomSheetDialog")
    }

    fun showEntranceInput(){
        val calendar = CalendarBottomSheetDialog(CalendarBottomSheetDialog.ENTRANCE, this)
        calendar.show(supportFragmentManager, "CalendarBottomSheetDialog")
    }

    fun showDesInput(){
        val des = DesBottomSheetDialog(this)
        des.show(supportFragmentManager, "DesBottomSheetDialog")
    }

    fun showPurposeInput(){
        val purpose = PurposeBottomSheetDialog(this)
        purpose.show(supportFragmentManager, "PurposeBottomSheetDialog")
    }

    fun search(){
        moveAiScheduleList()
    }

    private fun moveAiScheduleList(){
        val intent = Intent(this, AiScheduleListActivity::class.java)
        startActivity(intent)
    }

    override fun onSaveDate(date: String, type: Int) {
        when(type){
            CalendarBottomSheetDialog.BASIC ->{

            }
            CalendarBottomSheetDialog.DEPARTURE->{
                binding.departureOutput.text = date
                binding.departureOutput.visibility = View.VISIBLE
            }
            CalendarBottomSheetDialog.ENTRANCE->{
                binding.entranceOutput.text = date
                binding.entranceOutput.visibility = View.VISIBLE
            }
        }
    }

    override fun onSaveDateTime(dateTime: String, type: Int) {
        if(type == CalendarBottomSheetDialog.ENTRANCE){
            binding.entranceOutput.text = dateTime
            binding.entranceOutput.visibility = View.VISIBLE
        }else if(type == CalendarBottomSheetDialog.DEPARTURE){
            binding.departureOutput.text = dateTime
            binding.departureOutput.visibility = View.VISIBLE
        }
    }

    override fun onDesListener(des: String) {
        binding.desOutput.text = des
        binding.desOutput.visibility = View.VISIBLE
    }

    override fun onWhoListener(who: String) {
        binding.purposeOutput.visibility = View.VISIBLE
        binding.purposeOutput.text = binding.purposeOutput.text.toString() + who +", "
    }

    override fun onActivityLevelListener(activityLevel: String) {
        binding.purposeOutput.visibility = View.VISIBLE
        binding.purposeOutput.text = binding.purposeOutput.text.toString() + activityLevel + ", "
    }

    override fun onThemeListener(theme: List<String>) {
        binding.purposeOutput.visibility = View.VISIBLE
        theme.forEachIndexed { index, s ->
            if (index == theme.lastIndex){
                binding.purposeOutput.text = binding.purposeOutput.text.toString() + s
            }else{
                binding.purposeOutput.text = binding.purposeOutput.text.toString() + s + ", "
            }
        }
    }
}