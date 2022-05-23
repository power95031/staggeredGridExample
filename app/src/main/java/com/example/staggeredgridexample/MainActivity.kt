package com.example.staggeredgridexample

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MotionEvent
import android.view.ViewTreeObserver
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.SimpleItemAnimator
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.staggeredgridexample.Adapters.StaggeredAdapter
import com.example.staggeredgridexample.Objects.MergeObject
import com.example.staggeredgridexample.Objects.SearchObject
import com.example.staggeredgridexample.Retrofit.RetrofitForXML
import com.example.staggeredgridexample.databinding.ActivityMainBinding
import com.example.staggeredgridexample.dialogs.ResultPlayDialog
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.badge.BadgeUtils
import com.google.android.material.snackbar.Snackbar
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit


class MainActivity : AppCompatActivity() {

    val TAG : String = "yab"

    lateinit var binding : ActivityMainBinding
    lateinit var context : Context

    //뒤로가기 연속 클릭 대기 시간
    var mBackWait:Long = 0

    private lateinit var deviceUID : String

    private lateinit var progressDialog : ProgressDialog

    var searchArrayList : ArrayList<SearchObject.SearchItem>? = null

    lateinit var selectCounterBadge : BadgeDrawable

    // 아이템 선택 저장 변수
    private lateinit var selectedItem : LinkedHashMap<Int, String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        context = this

        initData()

        initView()
    }

    override fun onBackPressed() {
        // 뒤로가기 버튼 클릭
        if(System.currentTimeMillis() - mBackWait >=2000 ) {
            mBackWait = System.currentTimeMillis()
            Snackbar.make(binding.root,"뒤로가기 버튼을 한번 더 누르면 종료됩니다.", Snackbar.LENGTH_LONG).show()
        } else {
            super.onBackPressed()
        }
    }

    @SuppressLint("HardwareIds")
    private fun initData() {
        deviceUID = Settings.Secure.getString(this.contentResolver, Settings.Secure.ANDROID_ID)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initView() {
        binding.InputKeyword.apply {
            setOnEditorActionListener { _, actionId, _ ->
                if(actionId == EditorInfo.IME_ACTION_SEARCH) {
                    if(text.toString().isNotBlank()) {
                        searchThread(text.toString())
                    } else {
                        Toast.makeText(context, "검색어를 입력해주세요", Toast.LENGTH_SHORT).show()
                    }
                    return@setOnEditorActionListener true
                }
                return@setOnEditorActionListener false
            }

            setOnTouchListener { _, event ->
                val DRAWABLE_LEFT:Int = 0
                val DRAWABLE_TOP:Int = 1
                val DRAWABLE_RIGHT:Int = 2
                val DRAWABLE_BOTTOM:Int = 3

                if(event.action == MotionEvent.ACTION_UP) {
                    if(event.rawX >= (right - compoundDrawables[DRAWABLE_RIGHT].bounds.width())) {
                        if(text.toString().isNotBlank()) {
                            searchThread(text.toString())
                        } else {
                            Toast.makeText(context, "검색어를 입력해주세요", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                false
            }
        }

        binding.SendSelected.setOnClickListener {
            if(selectedItem.isNotEmpty()) {
                var selectedId = ""
                selectedItem.forEach { (_, value) ->
                    selectedId += if(selectedId=="") {
                        value
                    } else {
                        ",$value"
                    }
                }

                selectedVideoMergeThread(selectedId)
            } else {
                Toast.makeText(context, "선택값이 비어있습니다", Toast.LENGTH_SHORT).show()
            }
        }

        progressDialog = ProgressDialog(context)

        selectCounterBadge = BadgeDrawable.create(context).apply {
            number = 0
            verticalOffset = 20
            horizontalOffset = 20
            backgroundColor = ContextCompat.getColor(context, R.color.badge_red)
            badgeTextColor = ContextCompat.getColor(context, R.color.white)
            badgeGravity = BadgeDrawable.TOP_END
        }
        binding.SendSelected.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            @SuppressLint("UnsafeOptInUsageError")
            override fun onGlobalLayout() {
                BadgeUtils.attachBadgeDrawable(selectCounterBadge, binding.SendSelected, null)

                binding.SendSelected.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

    }

    private fun searchThread(str:String) {
        setLoading(true)

        val headers : HashMap<String, String> = HashMap()
        headers[getString(R.string.apiAutoKey)] = getString(R.string.apiAutoValue)

        val call = RetrofitForXML(getString(R.string.apiUrl)).service.searchKeyword(headers, str)
        call.enqueue(object : Callback<SearchObject>{
            override fun onResponse(call: Call<SearchObject>, response: Response<SearchObject>) {
                Log.i(TAG, "searchThread http call onResponse \n$response")

                setLoading(false)

                if(response.isSuccessful) {
                    response.body()?.also {
                        searchArrayList = it.searchItems
                        searchResult()
                    } ?: run {
                        Log.d(TAG, "result object null")
                        searchFail()
                    }
                } else {
                    searchFail()
                }
            }
            override fun onFailure(call: Call<SearchObject>, t: Throwable) {
                Log.e(TAG, "searchThread http call onFailure ", t)
                setLoading(false)
            }
        })
    }
    private fun searchResult() {
        if(::selectedItem.isInitialized) {
            selectedItem.clear()
        } else {
            selectedItem = LinkedHashMap()
        }
        selectCounterBadge.number = selectedItem.size

        val resultAdapter = StaggeredAdapter(context, searchArrayList) { pos, status, dicId ->
            searchArrayList?.get(pos)?.isSelected = status
            if(status) {
                selectedItem[pos] = dicId
                selectCounterBadge.number = selectedItem.size
            } else {
                if(selectedItem.isNotEmpty()) selectedItem.remove(pos)
                selectCounterBadge.number = selectedItem.size
            }
        }

        with(binding.ResultView) {
            itemAnimator?.also {
                if(it is SimpleItemAnimator) {
                    it.supportsChangeAnimations = false
                }
            }
            layoutManager = StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL)
            adapter = resultAdapter
        }
    }
    private fun searchFail() {
        if(::selectedItem.isInitialized) {
            selectedItem.clear()
        }
        selectCounterBadge.number = 0
        with(binding.ResultView) {
            removeAllViews()
            adapter = null
        }
        Toast.makeText(context, "검색결과 없음", Toast.LENGTH_SHORT).show()
    }

    private fun selectedVideoMergeThread(str:String) {
        setLoading(true)

        val apiUrl = "https://search.dal-da.co.kr/api/merge"

        val headers : HashMap<String, String> = HashMap()
        headers["Authorization"] = "Basic ZGFsZGE6ZGFsZGExMjM="

        val postObject : JSONObject = JSONObject()
        postObject.put("selectedId", str)
        postObject.put("deviceUID", deviceUID)

        val body:RequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), postObject.toString())

        val call = RetrofitForXML(getString(R.string.apiUrl)).service.selectedVideoSend(apiUrl, headers, body)
        call.enqueue(object : Callback<MergeObject> {
            override fun onResponse(call: Call<MergeObject>, response: Response<MergeObject>) {
                Log.d(TAG, "selectedVideoMergeThread http call onResponse \n$response")

                setLoading(false)

                if(response.isSuccessful) {
                    response.body()?.also {
                        Log.d(TAG, "API Result: ${it.mergeVideoUri}")

                        ResultPlayDialog(context, it.mergeVideoUri).show()
                    }
                }
            }

            override fun onFailure(call: Call<MergeObject>, t: Throwable) {
                Log.e(TAG, "selectedVideoMergeThread http call onFailure ", t)
                setLoading(false)
            }
        })
    }

    private fun setLoading(b:Boolean) {
        if(b) {
            if(!progressDialog.isShowing)
                progressDialog.show()
        } else {
            progressDialog.dismiss()
        }
    }
}