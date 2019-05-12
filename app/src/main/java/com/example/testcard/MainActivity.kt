package com.example.testcard

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import ikidou.reflect.TypeBuilder
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.IOException


class MainActivity : AppCompatActivity() {

    private val okHttpClient = OkHttpClient()
    private val gson = Gson()
    private lateinit var recyclerViewAdapter:RecyclerViewAdapter
    private lateinit var layoutManager:LinearLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener { refreshFromServer() }
        layoutManager = LinearLayoutManager(this,RecyclerView.HORIZONTAL,false)
        recyclerViewAdapter = RecyclerViewAdapter()
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = recyclerViewAdapter

    }

    private fun refreshFromServer() {
        recyclerView.smoothScrollToPosition(0)
        button.apply {
            text= "请求中"
            isEnabled = false
        }
        val request = Request.Builder().apply {
            url("http://attect.studio:8888/jewel-s-free101j/getNewList.php")

        }.build()

        okHttpClient.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    response.body()?.let {

                        val list = gson.fromJson<ArrayList<CharacterBean>>(
                            it.string(),
                            TypeBuilder.newInstance(ArrayList::class.java).addTypeParam(CharacterBean::class.java).build()
                        )
                        button.post {
                            button.apply {
                                text= "再抽一次！"
                                isEnabled = true
                            }

                        }
                        recyclerViewAdapter.listContent = list
                    }

                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "onFailure")
                e.printStackTrace()
            }

        })
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private var imageView: ImageView = itemView.findViewById(R.id.imageView)
        private var noTextView: TextView = itemView.findViewById(R.id.no);
        private var nameTextView: TextView = itemView.findViewById(R.id.name)
        private var descriptionTextView: TextView = itemView.findViewById(R.id.description)

        fun setCharacter(position:Int,character: CharacterBean) {
            noTextView.text = "#$position"
            nameTextView.text = character.name
            descriptionTextView.text = character.comment
            GlideApp.with(imageView).load(character.imageUrl).into(imageView)
        }
    }

    inner class RecyclerViewAdapter : RecyclerView.Adapter<ViewHolder>() {
        var listContent = arrayListOf<CharacterBean>()
        set(value) {
            field.clear()
            field.addAll(value)
            runOnUiThread { notifyDataSetChanged() }

        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
            ViewHolder(layoutInflater.inflate(R.layout.viewholder_card, parent, false))

        override fun getItemCount(): Int = listContent.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.setCharacter(position+1,listContent[position])
        }

    }

    companion object {
        const val TAG = "TEST"
    }
}
