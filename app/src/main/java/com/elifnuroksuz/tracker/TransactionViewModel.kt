package com.elifnuroksuz.tracker

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TransactionViewModel : ViewModel() {
    private val _transactions = MutableLiveData<List<Transaction>>()
    val transactions: LiveData<List<Transaction>> get() = _transactions

    private lateinit var db: AppDatabase

    fun initializeDatabase(context: Context) {
        if (!::db.isInitialized) {
            db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "transactions_db"
            ).build()
        }
    }

    fun fetchAllTransactions() {
        viewModelScope.launch(Dispatchers.IO) {
            val transactionsList = db.transactionDao().getAll()
            _transactions.postValue(transactionsList)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch(Dispatchers.IO) {
            db.transactionDao().update(transaction)
            fetchAllTransactions()
        }
    }
}
