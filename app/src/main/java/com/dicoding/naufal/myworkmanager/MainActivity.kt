package com.dicoding.naufal.myworkmanager

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.dicoding.naufal.myworkmanager.databinding.ActivityMainBinding
import androidx.work.Data.Builder
import androidx.lifecycle.Observer
import androidx.work.*
import com.dicoding.naufal.myworkmanager.worker.MyWorker
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var periodicWorkRequest: PeriodicWorkRequest

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnOneTimeTask.setOnClickListener(this)
        binding.btnPeriodicTask.setOnClickListener(this)
        binding.btnCancelTask.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when(v.id){
                R.id.btnOneTimeTask -> startOneTimeTask()
                R.id.btnPeriodicTask -> startPeriodicTask()
                R.id.btnCancelTask -> cancelPeriodicTask()
            }
        }
    }

    private fun cancelPeriodicTask() {
        WorkManager.getInstance().cancelWorkById(periodicWorkRequest.id)
    }

    private fun startPeriodicTask() {
        binding.textStatus.text = getString(R.string.status)
        val data = Builder()
            .putString(MyWorker.EXTRA_CITY, binding.editCity.text.toString())
            .build()
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        periodicWorkRequest = PeriodicWorkRequest.Builder(MyWorker::class.java, 15, TimeUnit.MINUTES)
            .setInputData(data)
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance().enqueue(periodicWorkRequest)
        WorkManager.getInstance()
            .getWorkInfoByIdLiveData(periodicWorkRequest.id)
            .observe(this@MainActivity, object: Observer<WorkInfo>{
                override fun onChanged(workInfo: WorkInfo) {
                    val status = workInfo.state.name
                    binding.textStatus.append("\n" + status)
                    binding.btnCancelTask.isEnabled = false
                    if (workInfo.state == WorkInfo.State.ENQUEUED){
                        binding.btnCancelTask.isEnabled = true
                    }
                }
            })
    }

    private fun startOneTimeTask() {
        binding.textStatus.text = getString(R.string.status)
        val data = Builder()
            .putString(MyWorker.EXTRA_CITY, binding.editCity.text.toString())
            .build()
        val constraint = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val oneTimeWorkRequest = OneTimeWorkRequest.Builder(MyWorker::class.java)
            .setInputData(data)
            .setConstraints(constraint)
            .build()
        WorkManager.getInstance().enqueue(oneTimeWorkRequest)
        WorkManager.getInstance()
            .getWorkInfoByIdLiveData(oneTimeWorkRequest.id)
            .observe(this@MainActivity, object : Observer<WorkInfo>{
                override fun onChanged(t: WorkInfo) {
                    val status = t.state.name
                    binding.textStatus.append("\n" + status)
                }
            })
    }
}