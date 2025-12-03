package com.example.sentio

import kotlinx.coroutines.CoroutineScope

expect open class BaseViewModel {

    val scope: CoroutineScope
}