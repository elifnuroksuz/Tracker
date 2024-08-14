package com.elifnuroksuz.tracker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.EditorInfo
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import com.elifnuroksuz.tracker.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.absoluteValue

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var deletedTransaction: Transaction
    private lateinit var transactions: MutableList<Transaction>
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var db: AppDatabase

    private val resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            val updatedTransaction = result.data?.getParcelableExtra<Transaction>("updated_transaction_key")
            updatedTransaction?.let {
                updateTransactionInList(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transactions = mutableListOf()
        transactionAdapter = TransactionAdapter(transactions)
        linearLayoutManager = LinearLayoutManager(this)

        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "transactions"
        ).build()

        // Setup RecyclerView
        binding.recyclerview.apply {
            adapter = transactionAdapter
            layoutManager = linearLayoutManager
        }

        // Swipe to remove
        val itemTouchHelper = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                deleteTransaction(transactions[viewHolder.adapterPosition])
            }
        }

        val swipeHelper = ItemTouchHelper(itemTouchHelper)
        swipeHelper.attachToRecyclerView(binding.recyclerview)

        binding.addBtn.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }

        setupBalanceInput()
    }

    private fun saveTotalBalance(newBalance: Double) {
        val sharedPreferences = getSharedPreferences("tracker_prefs", MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putFloat("total_balance", newBalance.toFloat())
            apply()
        }
    }

    private fun loadTotalBalance(): Double {
        val sharedPreferences = getSharedPreferences("tracker_prefs", MODE_PRIVATE)
        return sharedPreferences.getFloat("total_balance", 0f).toDouble()
    }

    private fun setupBalanceInput() {
        val initialBalance = loadTotalBalance()
        binding.balance.setText("₺%.2f".format(initialBalance))

        binding.balance.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val inputText = binding.balance.text.toString()
                val newBalance = inputText.replace("₺", "").replace(",", "").toDoubleOrNull() ?: 0.0
                saveTotalBalance(newBalance)
                updateDashboard()
                true
            } else {
                false
            }
        }
    }

    private fun fetchAll() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                transactions = db.transactionDao().getAll().toMutableList()
                withContext(Dispatchers.Main) {
                    Log.d("MainActivity", "Fetched transactions successfully: ${transactions.size} items")
                    updateDashboard()
                    transactionAdapter.setData(transactions)
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error fetching transactions: ${e.message}", e)
            }
        }
    }

    private fun updateDashboard() {
        val totalAmount = transactions.sumOf { it.amount }
        val budgetAmount = transactions.filter { it.amount > 0 }.sumOf { it.amount }
        val expenseAmount = transactions.filter { it.amount < 0 }.sumOf { it.amount }.absoluteValue

        val totalBalance = budgetAmount - expenseAmount
        val formattedBalance = "₺%.2f".format(totalBalance)

        binding.balance.setText(formattedBalance)
        binding.budget.text = "₺%.2f".format(budgetAmount)
        binding.expense.text = "₺%.2f".format(expenseAmount)

        Log.d("MainActivity", "Dashboard updated: Balance = $formattedBalance, Budget = ${binding.budget.text}, Expense = ${binding.expense.text}")
    }

    private fun updateTransactionInList(updatedTransaction: Transaction) {
        val index = transactions.indexOfFirst { it.id == updatedTransaction.id }
        if (index != -1) {
            transactions[index] = updatedTransaction
            transactionAdapter.notifyItemChanged(index)
            updateDashboard()
        }
    }

    private fun undoDelete() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.transactionDao().insertAll(deletedTransaction)
                transactions = db.transactionDao().getAll().toMutableList()
                withContext(Dispatchers.Main) {
                    Log.d("MainActivity", "Undo delete successful: ${transactions.size} items restored")
                    transactionAdapter.setData(transactions)
                    updateDashboard()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error undoing delete: ${e.message}", e)
            }
        }
    }

    private fun deleteTransaction(transaction: Transaction) {
        deletedTransaction = transaction
        CoroutineScope(Dispatchers.IO).launch {
            try {
                db.transactionDao().delete(transaction)
                transactions = db.transactionDao().getAll().toMutableList()
                withContext(Dispatchers.Main) {
                    Log.d("MainActivity", "Transaction deleted successfully")
                    transactionAdapter.setData(transactions)
                    updateDashboard()
                    Snackbar.make(binding.root, "Transaction deleted", Snackbar.LENGTH_LONG)
                        .setAction("Undo") {
                            undoDelete()
                        }
                        .setActionTextColor(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary))
                        .show()
                }
            } catch (e: Exception) {
                Log.e("MainActivity", "Error deleting transaction: ${e.message}", e)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchAll()
    }
}
