package net.sample

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.activity_main.*
import net.sample.ui.ContextActivity
import net.sample.ui.GetActivity
import net.sample.ui.LifeCycleActivity
import net.sample.ui.TemplateActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolBar)
        listView.adapter=ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
                arrayOf("常规请求","模板请求","生命周期处理","提示处理"))
        listView.setOnItemClickListener { _, _, position, _ ->
            val item=listView.adapter.getItem(position) as String
            when(position){
                0->startActivity(Intent(this,GetActivity::class.java).apply { putExtra("title",item) })
                1->startActivity(Intent(this,TemplateActivity::class.java).apply { putExtra("title",item) })
                2->startActivity(Intent(this,ContextActivity::class.java).apply { putExtra("title",item) })
                3->startActivity(Intent(this,LifeCycleActivity::class.java).apply { putExtra("title",item) })
            }
        }
    }

}
