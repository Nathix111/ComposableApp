// ui/fragments/Fragment2.kt
package com.example.mycomposeapp.ui.fragments

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme

@Composable
fun Fragment2() {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = "Fragment 2 Content", style = MaterialTheme.typography.headlineMedium)
    }
}