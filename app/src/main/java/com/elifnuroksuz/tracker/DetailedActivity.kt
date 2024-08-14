package com.elifnuroksuz.tracker

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.elifnuroksuz.tracker.databinding.ActivityDetailedBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetailedActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailedBinding
    private var transaction: Transaction? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailedBinding.inflate(layoutInflater)
        setContentView(binding.root)

        transaction = intent.getParcelableExtra("transaction_key")  // Key is consistent with TransactionAdapter

        if (transaction == null) {
            showError("Transaction could not be loaded.")
            finish()
            return
        }

        binding.apply {
            labelInput.setText(transaction!!.label)
            amountInput.setText(transaction!!.amount.toString())
            descriptionInput.setText(transaction!!.description)

            rootView.setOnClickListener {
                window.decorView.clearFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(it.windowToken, 0)
            }

            labelInput.addTextChangedListener {
                updateBtn.visibility = View.VISIBLE
                if (!it.isNullOrEmpty()) labelLayout.error = null
            }

            amountInput.addTextChangedListener {
                updateBtn.visibility = View.VISIBLE
                if (!it.isNullOrEmpty()) amountLayout.error = null
            }

            descriptionInput.addTextChangedListener {
                updateBtn.visibility = View.VISIBLE
            }

            updateBtn.setOnClickListener {
                val label = labelInput.text.toString()
                val description = descriptionInput.text.toString()
                val amount = amountInput.text.toString().toDoubleOrNull()

                when {
                    label.isEmpty() -> labelLayout.error = "Please enter a valid label"
                    amount == null -> amountLayout.error = "Please enter a valid amount"
                    else -> {
                        val updatedTransaction = transaction!!.copy(label = label, amount = amount, description = description)
                        updateTransaction(updatedTransaction)
                    }
                }
            }

            closeBtn.setOnClickListener {
                finish()
            }
        }
    }

    private fun updateTransaction(transaction: Transaction) {
        val db = Room.databaseBuilder(this, AppDatabase::class.java, "transactions").build()

        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    db.transactionDao().update(transaction)
                }
                setResult(RESULT_OK, Intent().apply {
                    putExtra("updated_transaction_key", transaction)
                })
                finish()
            } catch (e: Exception) {
                showError("Update failed: ${e.message}")
            }
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
