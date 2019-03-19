package com.model.chenille.reverseImpl;

import android.accounts.AccountManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;



public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

//    ABCManager impl = new ABCManagerImpl();


//    Log.d("MainActivity", impl.reverseMethod("调用方法"));
//    Log.d("MainActivity", impl.reversMethod(10));

    BaseAdapter adapter = new Adapter();

//    Log.d("MainActivity", adapter.reverseMethod("调用方法"));
//    Log.d("MainActivity", adapter.reversMethod(10, Adapter.TYPE_A));
  }
}
