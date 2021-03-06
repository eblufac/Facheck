package com.fzu.facheck.module.home;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.fzu.facheck.R;
import com.fzu.facheck.adapter.section.HomeClassInfoSection;
import com.fzu.facheck.base.RxBaseActivity;
import com.fzu.facheck.entity.RollCall.ClassInfo;
import com.fzu.facheck.network.RetrofitHelper;
import com.fzu.facheck.widget.CustomEmptyView;
import com.fzu.facheck.widget.sectioned.SectionedRecyclerViewAdapter;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import jp.wasabeef.glide.transformations.RoundedCornersTransformation;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ClassPageActivity extends RxBaseActivity {

    public String classname;
    public String classid;
    public boolean master;
    @BindView(R.id.toolbar)
    Toolbar mtoolbar;
    @BindView(R.id.toolbar_title)
    TextView title_text;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.empty_layout)
    CustomEmptyView mCustomEmptyView;
    @BindView(R.id.recycle)
    RecyclerView mRecyclerView;
    @BindView(R.id.the_class_code)
    TextView codeText;
    @BindView(R.id.the_class_time)
    TextView timeText;
    @BindView(R.id.class_icon)
    ImageView classView;
    @BindView(R.id.the_teacher)
    TextView teacherName;
    private ClassInfo result=new ClassInfo();
    private boolean mIsRefreshing = false;
    private SectionedRecyclerViewAdapter mSectionedAdapter;
    @Override
    public int getLayoutId() {
        return R.layout.fragment_class_pager;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        Intent intent=getIntent();
        classid=intent.getStringExtra("classid");
        classname=intent.getStringExtra("classname");
        master=intent.getBooleanExtra("master",false);
        Glide.with(this).load(R.mipmap.class_icon).bitmapTransform(new RoundedCornersTransformation(this
                ,28,1,RoundedCornersTransformation.CornerType.ALL)).into(classView);
        initToolBar();
        initRefreshLayout();
        initRecyclerView();
    }

    @Override
    public void initToolBar() {
        mtoolbar.setNavigationIcon(R.drawable.backicon);
        title_text.setText(classname);
         mtoolbar.setNavigationOnClickListener(v->finish());
    }
    @Override
    public void initNavigationView() {
    }

    @Override
    public void initRecyclerView() {
        mSectionedAdapter=new SectionedRecyclerViewAdapter();
        GridLayoutManager gridLayoutManager=new GridLayoutManager(this,1);
        mRecyclerView.setLayoutManager(gridLayoutManager);
        mRecyclerView.setAdapter(mSectionedAdapter);
        setRecycleNoScroll();
    }
    @Override
    public void initRefreshLayout() {
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.post(() -> {
            mSwipeRefreshLayout.setRefreshing(true);
            mIsRefreshing = true;
            loadData();
        });
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            clearData();
            loadData();
        });
    }
    @Override
    public void loadData() {
        JSONObject userobject=new JSONObject();
        try {
            userobject.put("classId",classid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=utf-8"), userobject.toString());
        RetrofitHelper.getClassInfo().getclassInfo("get_class_info",requestBody)
                .compose(bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ClassInfo>() {
                    @Override
                    public void onCompleted() { }

                    @Override
                    public void onError(Throwable e) {
                        initEmptyView();
                    }
                    @Override
                    public void onNext(ClassInfo classInfo) {
                        if(classInfo.code.equals("1100")){
                            result=classInfo;
                            teacherName.setText("任课教师:"+classInfo.teacherName);
                            codeText.setText("邀请码:"+classInfo.classCode);
                            timeText.setText("课程时间："+classInfo.startTime+"——"+classInfo.endTime);
                            finishTask();
                        }
                    }
                });
    }
    @Override
    public void finishTask() {
        mSwipeRefreshLayout.setRefreshing(false);
        mIsRefreshing=false;
        hideEmptyView();
        mSectionedAdapter.addSection(new HomeClassInfoSection(result,"student",this));
        mSectionedAdapter.addSection(new HomeClassInfoSection(result,"records",this));
        mSectionedAdapter.notifyDataSetChanged();
    }
    public void initEmptyView() {
        mSwipeRefreshLayout.setRefreshing(false);
        mCustomEmptyView.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.GONE);
        mCustomEmptyView.setEmptyImage(R.drawable.img_tips_error);
        mCustomEmptyView.setEmptyText("加载失败!请重试或检查网络连接");
    }
    public void hideEmptyView() {
        mCustomEmptyView.setVisibility(View.GONE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }
    private void clearData() {
        if(result!=null){
            if(result.getStudents()!=null)
                result.getStudents().clear();
            if(result.getRecords()!=null)
                result.getRecords().clear();
        }
        mIsRefreshing = true;
        mSectionedAdapter.removeAllSections();
    }
    private void setRecycleNoScroll() {
        mRecyclerView.setOnTouchListener((v, event) -> mIsRefreshing);
    }
}
