package com.hackathon.fellas.homedepotapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.content.Intent

class SearchActivity : AppCompatActivity(), TextWatcher {

    private lateinit var searchResults: ListView
    private lateinit var searchBox: EditText

    private val toolResults: MutableList<String> = mutableListOf()
    private lateinit var toolAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)
        searchBox = findViewById(R.id.search_box)
        searchResults = findViewById(R.id.search_results)
        searchBox.addTextChangedListener(this)
        toolAdapter = ArrayAdapter(this, R.layout.tool_item, R.id.tool_name, toolResults)

        searchResults.adapter = toolAdapter

        searchResults.setOnItemClickListener{ _, _, position, _ ->
            val itemKey: String = searchResults.getItemAtPosition(position) as String
            val intent: Intent = Intent(this, NavigationActivity::class.java).apply {
                putExtra(SharedData.CHOSEN_NODE, SharedData.tools[itemKey])
            }
            startActivity(intent)
        }
    }

    override fun afterTextChanged(s: Editable?) {

    }
    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        if(s != null) {
            toolResults.clear()
            for((key, _) in SharedData.tools) {
                if(key.contains(s, true)) {
                    toolResults.add(key)
                }
            }
        }
        toolAdapter.notifyDataSetChanged()

    }
}
