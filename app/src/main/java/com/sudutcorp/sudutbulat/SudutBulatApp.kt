package com.sudutcorp.sudutbulat

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.sudutcorp.sudutbulat.core.ui.theme.SudutBulatTheme
import com.sudutcorp.sudutbulat.navigation.AppNavigation

@Composable
fun SudutBulatApp() {
    SudutBulatTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            AppNavigation()
        }
    }
}
