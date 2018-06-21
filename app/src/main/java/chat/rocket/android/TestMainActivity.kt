package chat.rocket.android

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import chat.rocket.android.activity.ChatMainActivity

class TestMainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_main2)

        findViewById<Button>(R.id.but).setOnClickListener {
            var intent = Intent(this,ChatMainActivity::class.java)
            startActivity(intent)
        }
    }
}
