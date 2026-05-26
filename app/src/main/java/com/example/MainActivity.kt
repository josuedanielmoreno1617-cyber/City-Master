package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.data.CityDatabase
import com.example.data.CityRepository
import com.example.ui.CityScreen
import com.example.ui.CityViewModel
import com.example.ui.CityViewModelFactory
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    private lateinit var database: CityDatabase
    private lateinit var repository: CityRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        database = Room.databaseBuilder(
            applicationContext,
            CityDatabase::class.java,
            "city_builder_db"
        ).fallbackToDestructiveMigration(true).build()
        
        repository = CityRepository(database.cityDao())

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val viewModel: CityViewModel = viewModel(
                        factory = CityViewModelFactory(repository)
                    )
                    CityScreen(viewModel = viewModel)
                }
            }
        }
    }
}
