/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.jankstats

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.metrics.performance.PerformanceMetricsState
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.jankstats.databinding.ActivityJankLoggingBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asExecutor

/**
 * This activity shows how to use JankStatsAggregator, a class in this test directory layered
 * on top of JankStats which aggregates the per-frame data. Instead of receiving jank data
 * per frame (which would happen by using JankStats directly), the report listener only
 * receives data when a report is issued, either when the activity goes into the background
 * or if JankStatsAggregator issues the report itself.
 */
class JankAggregatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJankLoggingBinding
    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var jankStatsAggregator: JankStatsAggregator

    private val jankReportListener =
        JankStatsAggregator.OnJankReportListener { reason, totalFrames, jankFrameData ->
            Log.v(
                "JankStatsSample",
                "*** Jank Report ($reason), totalFrames = $totalFrames, " +
                        "jankFrames = ${jankFrameData.size}"
            )

            jankFrameData.forEach { frameData ->
                Log.v("JankStatsSample", frameData.toString())
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJankLoggingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupUi()

        val metricsStateHolder = PerformanceMetricsState.getForHierarchy(binding.root)

        jankStatsAggregator = JankStatsAggregator(
            window,
            Dispatchers.Default.asExecutor(),
            jankReportListener
        )

        metricsStateHolder.state?.addState("Activity", javaClass.simpleName)
    }

    override fun onResume() {
        super.onResume()
        jankStatsAggregator.jankStats.isTrackingEnabled = true
    }

    override fun onPause() {
        super.onPause()
        jankStatsAggregator.issueJankReport("Activity paused")
        jankStatsAggregator.jankStats.isTrackingEnabled = false
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    private fun setupUi() {
        setSupportActionBar(binding.toolbar)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.navigation_container) as NavHostFragment
        navController = navHostFragment.navController

        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }
}
