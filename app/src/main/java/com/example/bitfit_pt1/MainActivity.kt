package com.example.bitfit_pt1

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.time.Instant

class MainActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fun closeKeyboard() {
            val view = this.currentFocus
            if(view != null) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        }

        var diaryTitleEmpty = true
        var diaryEntryEmpty = true
        val diaryTitleEntry: TextView = findViewById(R.id.DiaryTitleEntry)
        val diaryEntry: TextView = findViewById(R.id.DiaryEntry)
        val addButton: Button = findViewById(R.id.addButton)
        val diaryRV: RecyclerView = findViewById(R.id.DiaryRV)
        val diaryList: MutableList<DisplayDiary> = listOf<DisplayDiary>().toMutableList()
        val diaryAdapter = DiaryAdapter(diaryList)
        diaryRV.adapter = diaryAdapter
        diaryRV.layoutManager = LinearLayoutManager(this)

        lifecycleScope.launch {
            (application as ArticleApplication).db.diaryDao().getAllByDateDesc().collect { databaseList ->
                databaseList.map { entity ->
                    DisplayDiary(
                        entity.id,
                        entity.title,
                        entity.date,
                        entity.entry
                    )
                }.also { mappedList ->
                    diaryList.clear()
                    diaryList.addAll(mappedList)
                    diaryAdapter.notifyDataSetChanged()
                }
            }
        }


        diaryTitleEntry.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    diaryTitleEmpty = count <= 0
                addButton.isEnabled = (!diaryTitleEmpty && !diaryEntryEmpty)
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        diaryEntry.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                diaryEntryEmpty = count <= 0
                addButton.isEnabled = (!diaryTitleEmpty && !diaryEntryEmpty)
            }

            override fun afterTextChanged(s: Editable?) {
            }

        })

        addButton.setOnClickListener {
            lifecycleScope.launch(IO) {
                Log.v("database", diaryTitleEntry.text.toString())
                Log.v("database", diaryEntry.text.toString())
                (application as ArticleApplication).db.diaryDao().insert(DiaryEntity(
                    diaryTitleEntry.text.toString(),
                    Instant.now(),
                    diaryEntry.text.toString()
                ))
            }
            closeKeyboard()
            ///wait for insert to complete
            Thread.sleep(500)
            diaryTitleEntry.text = ""
            diaryEntry.text = ""
        }
    }
}