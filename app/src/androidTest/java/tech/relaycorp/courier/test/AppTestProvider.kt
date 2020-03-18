package tech.relaycorp.courier.test

import android.content.Context
import androidx.room.Room
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.coroutines.asExecutor
import kotlinx.coroutines.test.TestCoroutineDispatcher
import tech.relaycorp.courier.App
import tech.relaycorp.courier.data.database.AppDatabase

object AppTestProvider {
    private val context: Context
        get() = InstrumentationRegistry.getInstrumentation().targetContext

    val app
        get() = context.applicationContext as App

    val component
        get() = app.component

    val database
        get() =
            Room
                .inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .setTransactionExecutor(testDispatcher.asExecutor())
                .setQueryExecutor(testDispatcher.asExecutor())
                .build()

    private val testDispatcher by lazy { TestCoroutineDispatcher() }
}