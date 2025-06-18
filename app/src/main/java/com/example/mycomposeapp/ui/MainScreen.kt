package com.example.mycomposeapp.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.mycomposeapp.MainActivity
import com.example.mycomposeapp.ui.fragments.Fragment1
import com.example.mycomposeapp.ui.fragments.Fragment2
import com.example.mycomposeapp.ui.fragments.Fragment3

@Composable
fun MainScreen(acceleration: State<Triple<Float, Float, Float>>, mainActivity: MainActivity) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Text("Force G") }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Text("Sonomètre") }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Text("Pixel Cam") }
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> Fragment1(acceleration)
                1 -> Fragment2(mainActivity)
                2 -> Fragment3()
            }
        }
    }
}